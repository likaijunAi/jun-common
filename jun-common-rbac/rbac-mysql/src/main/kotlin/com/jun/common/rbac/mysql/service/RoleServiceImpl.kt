package com.jun.common.rbac.mysql.service

import com.jun.common.core.web.Resp
import com.jun.common.rbac.exception.RbacErrorCode
import com.jun.common.rbac.exception.RbacException
import com.jun.common.rbac.model.Role
import com.jun.common.rbac.model.RolePermission
import com.jun.common.rbac.model.TreeNode
import com.jun.common.rbac.model.TreeNodes
import com.jun.common.rbac.model.req.RoleBindPermissionReq
import com.jun.common.rbac.model.req.RoleCreateReq
import com.jun.common.rbac.model.req.RoleMoveReq
import com.jun.common.rbac.model.req.RoleQuery
import com.jun.common.rbac.model.req.RoleUpdateReq
import com.jun.common.rbac.mysql.entity.RolePO
import com.jun.common.rbac.mysql.entity.RolePermissionPO
import com.jun.common.rbac.repository.PermissionRepository
import com.jun.common.rbac.repository.RolePermissionRepository
import com.jun.common.rbac.repository.RoleRepository
import com.jun.common.rbac.repository.TenantPermissionRepository
import com.jun.common.rbac.repository.TenantRepository
import com.jun.common.rbac.service.RbacService
import com.jun.common.rbac.service.RoleService
import com.jun.common.rbac.tree.PathTree
import org.springframework.stereotype.Service

/**
 * 角色：多级继承；权限变更时失效整棵子树缓存
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Service
class RoleServiceImpl(
    private val roleRepository: RoleRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val tenantPermissionRepository: TenantPermissionRepository,
    private val permissionRepository: PermissionRepository,
    private val tenantRepository: TenantRepository,
    private val rbacService: RbacService
) : RoleService {

    override fun create(req: RoleCreateReq): Role {
        if (req.code.isBlank()) throw RbacException(RbacErrorCode.BAD_REQUEST, "code 不能为空")
        if (req.name.isBlank()) throw RbacException(RbacErrorCode.BAD_REQUEST, "name 不能为空")
        if (roleRepository.findByCode(req.tenantId, req.code) != null)
            throw RbacException(RbacErrorCode.ROLE_CODE_DUPLICATE)

        val parentId = req.parentId.takeIf { it.isNotBlank() } ?: PathTree.ROOT_ID
        val parent = if (parentId == PathTree.ROOT_ID) null
        else roleRepository.findById(parentId) ?: throw RbacException(RbacErrorCode.ROLE_NOT_FOUND)

        // 允许跨商户挂载：父角色为该商户可见的共享角色（平台/祖先商户 shareToChildren）
        if (parent != null && parent.tenantId != req.tenantId && !isSharedVisible(parent, req.tenantId))
            throw RbacException(RbacErrorCode.ROLE_CROSS_TENANT)

        val role = RolePO().apply {
            this.tenantId = req.tenantId
            this.parentId = parentId
            this.path = PathTree.ROOT_PATH
            this.level = (parent?.level ?: -1) + 1
            this.code = req.code
            this.name = req.name
            this.description = req.description
            this.inherit = req.inherit ?: true
            this.dataScope = req.dataScope ?: com.jun.common.rbac.model.DataScope.TENANT.value
            this.shareToChildren = req.shareToChildren ?: false
        }
        roleRepository.insert(role)
        role.path = PathTree.build(parent?.path ?: PathTree.ROOT_PATH, role.id)
        roleRepository.updateById(role)

        val codes = req.permissionCodes?.takeIf { it.isNotEmpty() }
        if (codes != null) bindPermissions(role.id, checkCodes(role.tenantId, codes), emptyList())
        return role
    }

    override fun update(req: RoleUpdateReq): Role {
        val role = roleRepository.findById(req.id) ?: throw RbacException(RbacErrorCode.ROLE_NOT_FOUND)
        req.name?.let { role.name = it }
        req.description?.let { role.description = it }
        req.inherit?.let { role.inherit = it }
        req.dataScope?.let { role.dataScope = it }
        req.shareToChildren?.let { role.shareToChildren = it }
        req.status?.let { role.status = it }
        roleRepository.updateById(role)
        rbacService.refreshCache(role.id)
        return role
    }

    override fun delete(id: String) {
        val role = roleRepository.findById(id) ?: throw RbacException(RbacErrorCode.ROLE_NOT_FOUND)
        if (role.builtin) throw RbacException(RbacErrorCode.ROLE_BUILTIN)
        if (roleRepository.countChildren(id) > 0) throw RbacException(RbacErrorCode.ROLE_HAS_CHILDREN)
        if (roleRepository.countUsers(id) > 0) throw RbacException(RbacErrorCode.ROLE_HAS_USER)
        roleRepository.logicDelete(id)
        rolePermissionRepository.deleteByRoleId(id)
        rbacService.refreshCache(id)
    }

    override fun move(id: String, newParentId: String): Role {
        val role = roleRepository.findById(id) ?: throw RbacException(RbacErrorCode.ROLE_NOT_FOUND)
        val target = newParentId.takeIf { it.isNotBlank() && it != PathTree.ROOT_ID }
            ?.let { roleRepository.findById(it) ?: throw RbacException(RbacErrorCode.ROLE_NOT_FOUND) }

        if (target != null && target.tenantId != role.tenantId && !isSharedVisible(target, role.tenantId))
            throw RbacException(RbacErrorCode.ROLE_CROSS_TENANT)

        val newParentPath = target?.path ?: PathTree.ROOT_PATH
        if (PathTree.isAncestor(role.path, newParentPath)) throw RbacException(RbacErrorCode.ROLE_MOVE_TO_CHILD)

        val oldPrefix = PathTree.descendantPrefix(role.path)
        val newPrefix = PathTree.build(newParentPath, role.id)
        val delta = ((target?.level ?: -1) + 1) - role.level
        roleRepository.updatePath(oldPrefix, newPrefix, delta)

        val moved = roleRepository.findById(id) ?: role
        moved.parentId = target?.id ?: PathTree.ROOT_ID
        moved.path = newPrefix
        moved.level = (target?.level ?: -1) + 1
        roleRepository.updateById(moved)
        rbacService.refreshCache(id)
        return moved
    }

    override fun move(req: RoleMoveReq): Role = move(req.id, req.newParentId)

    override fun findById(id: String): Role? = roleRepository.findById(id)

    override fun bindPermissions(roleId: String, grants: List<String>, denies: List<String>): List<RolePermission> {
        val role = roleRepository.findById(roleId) ?: throw RbacException(RbacErrorCode.ROLE_NOT_FOUND)
        val items = mutableListOf<RolePermission>()
        checkCodes(role.tenantId, grants).forEach {
            items.add(RolePermissionPO().apply { this.roleId = roleId; this.permissionCode = it; this.granted = true })
        }
        checkCodes(role.tenantId, denies).forEach {
            items.add(RolePermissionPO().apply { this.roleId = roleId; this.permissionCode = it; this.granted = false })
        }
        rolePermissionRepository.replaceAll(roleId, items)
        rbacService.refreshCache(roleId)
        return items
    }

    override fun bindPermissions(req: RoleBindPermissionReq): List<RolePermission> =
        bindPermissions(req.roleId, req.grants, req.denies)

    override fun addPermission(roleId: String, permissionCode: String, granted: Boolean): RolePermission {
        val role = roleRepository.findById(roleId) ?: throw RbacException(RbacErrorCode.ROLE_NOT_FOUND)
        checkCodes(role.tenantId, listOf(permissionCode))
        val item = RolePermissionPO().apply {
            this.roleId = roleId
            this.permissionCode = permissionCode
            this.granted = granted
        }
        rolePermissionRepository.deleteByRoleIdAndCode(roleId, permissionCode)
        rolePermissionRepository.insert(item)
        rbacService.refreshCache(roleId)
        return item
    }

    override fun removePermission(roleId: String, permissionCode: String) {
        rolePermissionRepository.deleteByRoleIdAndCode(roleId, permissionCode)
        rbacService.refreshCache(roleId)
    }

    override fun directPermissions(roleId: String): List<RolePermission> =
        rolePermissionRepository.findByRoleId(roleId)

    override fun effectivePermissions(roleId: String): Set<String> = rbacService.getRolePermissions(roleId)

    override fun selfAndDescendantIds(id: String): List<String> {
        val role = roleRepository.findById(id) ?: return emptyList()
        return listOf(id) + roleRepository
            .findDescendants(PathTree.descendantPrefix(role.path))
            .map { it.id }
    }

    override fun availableParentRoles(tenantId: String): List<Role> {
        val own = roleRepository.findByTenantId(tenantId)
        val shared = roleRepository.findSharedIn(sharedTenantIdsOf(tenantId))
        return (own + shared).distinctBy { it.id }
    }

    override fun isAssignableTo(roleId: String, tenantId: String): Boolean {
        val role = roleRepository.findById(roleId) ?: return false
        return role.tenantId == tenantId || isSharedVisible(role, tenantId)
    }

    override fun tree(tenantId: String): List<TreeNode<Role>> {
        val list = availableParentRoles(tenantId)
        return TreeNodes.build(list, PathTree.ROOT_ID, { it.id }, { it.parentId })
    }

    override fun page(query: RoleQuery): Resp<Role> = roleRepository.page(query)

    /** 平台(0) + 该商户的祖先商户 id */
    private fun sharedTenantIdsOf(tenantId: String): Set<String> {
        val ids = LinkedHashSet<String>()
        ids.add(PathTree.ROOT_ID)
        if (tenantId != PathTree.ROOT_ID) {
            tenantRepository.findById(tenantId)?.let { ids.addAll(PathTree.ids(it.path)) }
        }
        return ids
    }

    /** 角色是否为「平台或目标商户祖先」共享出来的角色 */
    private fun isSharedVisible(role: Role, tenantId: String): Boolean {
        if (role.tenantId == tenantId) return true
        if (!role.shareToChildren) return false
        return sharedTenantIdsOf(tenantId).contains(role.tenantId)
    }

    /** 校验权限码在商户可用白名单内；平台级权限(tenantId=0)始终可用 */
    private fun checkCodes(tenantId: String, codes: Collection<String>): List<String> {
        if (codes.isEmpty()) return emptyList()
        if (tenantId == PathTree.ROOT_ID) return codes.distinct()

        val whitelist = tenantPermissionRepository.findByTenantId(tenantId).map { it.permissionCode }.toSet()
        val platform = permissionRepository.findByTenantId(PathTree.ROOT_ID).map { it.code }.toSet()
        return codes.distinct().filter {
            whitelist.contains(it) || platform.contains(it)
        }
    }
}
