package com.jun.common.rbac.mysql.service

import com.jun.common.core.web.Resp
import com.jun.common.rbac.auth.PasswordEncoder
import com.jun.common.rbac.cache.RbacCacheManager
import com.jun.common.rbac.exception.RbacErrorCode
import com.jun.common.rbac.exception.RbacException
import com.jun.common.rbac.model.DataScope
import com.jun.common.rbac.model.Tenant
import com.jun.common.rbac.model.TreeNode
import com.jun.common.rbac.model.req.TenantCreateReq
import com.jun.common.rbac.model.req.TenantMoveReq
import com.jun.common.rbac.model.req.TenantQuery
import com.jun.common.rbac.model.req.TenantUpdateReq
import com.jun.common.rbac.mysql.entity.RolePO
import com.jun.common.rbac.mysql.entity.RolePermissionPO
import com.jun.common.rbac.mysql.entity.TenantPO
import com.jun.common.rbac.mysql.entity.TenantPermissionPO
import com.jun.common.rbac.mysql.entity.UserPO
import com.jun.common.rbac.repository.PermissionRepository
import com.jun.common.rbac.repository.RolePermissionRepository
import com.jun.common.rbac.repository.RoleRepository
import com.jun.common.rbac.repository.TenantPermissionRepository
import com.jun.common.rbac.repository.TenantRepository
import com.jun.common.rbac.repository.UserRepository
import com.jun.common.rbac.service.RoleService
import com.jun.common.rbac.service.TenantService
import com.jun.common.rbac.tree.PathTree
import com.jun.common.rbac.model.TreeNodes
import org.springframework.stereotype.Service

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Service
class TenantServiceImpl(
    private val tenantRepository: TenantRepository,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val tenantPermissionRepository: TenantPermissionRepository,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder,
    private val cacheManager: RbacCacheManager
) : TenantService {

    override fun create(req: TenantCreateReq): Tenant {
        if (req.name.isBlank()) throw RbacException(RbacErrorCode.BAD_REQUEST, "name 不能为空")
        if (req.code.isBlank()) throw RbacException(RbacErrorCode.BAD_REQUEST, "code 不能为空")
        if (tenantRepository.findByCode(req.code) != null) throw RbacException(RbacErrorCode.TENANT_CODE_DUPLICATE)

        val parentId = req.parentId.takeIf { it.isNotBlank() } ?: PathTree.ROOT_ID
        val parent = if (parentId == PathTree.ROOT_ID) null
        else tenantRepository.findById(parentId) ?: throw RbacException(RbacErrorCode.TENANT_NOT_FOUND)

        val tenant = TenantPO().apply {
            this.parentId = parentId
            this.name = req.name
            this.code = req.code
            this.contact = req.contact
            this.phone = req.phone
            this.remark = req.remark
            this.level = (parent?.level ?: -1) + 1
        }
        tenantRepository.insert(tenant)
        tenant.path = PathTree.build(parent?.path ?: PathTree.ROOT_PATH, tenant.id)
        tenantRepository.updateById(tenant)

        createAdmin(tenant, parent?.id, req)
        return tenant
    }

    override fun update(req: TenantUpdateReq): Tenant {
        val tenant = tenantRepository.findById(req.id) ?: throw RbacException(RbacErrorCode.TENANT_NOT_FOUND)
        req.name?.let { tenant.name = it }
        req.contact?.let { tenant.contact = it }
        req.phone?.let { tenant.phone = it }
        req.remark?.let { tenant.remark = it }
        req.status?.let { tenant.status = it }
        tenantRepository.updateById(tenant)
        cacheManager.invalidateTenant(tenant.id)
        return tenant
    }

    override fun delete(id: String) {
        val tenant = tenantRepository.findById(id) ?: throw RbacException(RbacErrorCode.TENANT_NOT_FOUND)
        if (tenantRepository.countChildren(tenant.id) > 0) throw RbacException(RbacErrorCode.TENANT_HAS_CHILDREN)
        if (userRepository.countByTenantId(tenant.id) > 0) throw RbacException(RbacErrorCode.TENANT_HAS_USER)
        if (roleRepository.findByTenantId(tenant.id).isNotEmpty()) throw RbacException(RbacErrorCode.TENANT_HAS_ROLE)
        tenantRepository.logicDelete(tenant.id)
        cacheManager.invalidateTenant(tenant.id)
    }

    override fun move(id: String, newParentId: String): Tenant {
        val tenant = tenantRepository.findById(id) ?: throw RbacException(RbacErrorCode.TENANT_NOT_FOUND)
        val target = newParentId.takeIf { it.isNotBlank() && it != PathTree.ROOT_ID }
            ?.let { tenantRepository.findById(it) ?: throw RbacException(RbacErrorCode.TENANT_NOT_FOUND) }

        val newParentPath = target?.path ?: PathTree.ROOT_PATH
        val newParentIdValue = target?.id ?: PathTree.ROOT_ID
        if (PathTree.isAncestor(tenant.path, newParentPath)) throw RbacException(RbacErrorCode.TENANT_MOVE_TO_CHILD)

        val oldPrefix = PathTree.descendantPrefix(tenant.path)
        val newPrefix = PathTree.build(newParentPath, tenant.id)
        val delta = ((target?.level ?: -1) + 1) - tenant.level
        tenantRepository.updatePath(oldPrefix, newPrefix, delta)

        val moved = tenantRepository.findById(id) ?: tenant
        moved.parentId = newParentIdValue
        moved.path = newPrefix
        moved.level = (target?.level ?: -1) + 1
        tenantRepository.updateById(moved)
        cacheManager.invalidateTenant(id)
        return moved
    }

    override fun move(req: TenantMoveReq): Tenant = move(req.id, req.newParentId)

    override fun findById(id: String): Tenant? = tenantRepository.findById(id)

    override fun selfAndDescendantIds(id: String): List<String> {
        val tenant = tenantRepository.findById(id) ?: return emptyList()
        return listOf(id) + tenantRepository
            .findDescendants(PathTree.descendantPrefix(tenant.path))
            .map { it.id }
    }

    override fun descendantIds(id: String): List<String> {
        val tenant = tenantRepository.findById(id) ?: return emptyList()
        return tenantRepository.findDescendants(PathTree.descendantPrefix(tenant.path)).map { it.id }
    }

    override fun ancestorIds(id: String): List<String> {
        val tenant = tenantRepository.findById(id) ?: return emptyList()
        return PathTree.ancestorIds(tenant.path)
    }

    override fun tree(rootId: String?): List<TreeNode<Tenant>> {
        val list = if (rootId.isNullOrBlank() || rootId == PathTree.ROOT_ID) {
            tenantRepository.findAll()
        } else {
            val root = tenantRepository.findById(rootId) ?: return emptyList()
            listOf(root) + tenantRepository.findDescendants(PathTree.descendantPrefix(root.path))
        }
        return TreeNodes.build(list, PathTree.ROOT_ID, { it.id }, { it.parentId })
    }

    override fun page(query: TenantQuery): Resp<Tenant> = tenantRepository.page(query)

    /**
     * 创建子商户的内置「管理员」角色、登录账号，并分配权限
     */
    private fun createAdmin(tenant: Tenant, parentTenantId: String?, req: TenantCreateReq) {
        val username = req.adminUsername?.takeIf { it.isNotBlank() } ?: return
        val password = req.adminPassword?.takeIf { it.isNotBlank() }
            ?: throw RbacException(RbacErrorCode.BAD_REQUEST, "adminPassword 不能为空")
        if (userRepository.existsByUsername(username, null))
            throw RbacException(RbacErrorCode.USER_NAME_DUPLICATE)

        // 指定了共享角色则作为父角色挂载（改父角色权限 → 本商户立即生效），否则独立创建
        val parentRole = req.roleId?.takeIf { it.isNotBlank() && it != PathTree.ROOT_ID }
            ?.let {
                roleRepository.findById(it) ?: throw RbacException(RbacErrorCode.ROLE_NOT_FOUND)
            }
        if (parentRole != null && !roleService.isAssignableTo(parentRole.id, parentTenantId ?: PathTree.ROOT_ID))
            throw RbacException(RbacErrorCode.ROLE_CROSS_TENANT)

        val role = RolePO().apply {
            this.tenantId = tenant.id
            this.parentId = parentRole?.id ?: PathTree.ROOT_ID
            this.path = PathTree.ROOT_PATH
            this.level = (parentRole?.level ?: -1) + 1
            this.code = "ADMIN"
            this.name = "管理员"
            this.inherit = true
            this.dataScope = parentRole?.dataScope ?: DataScope.TENANT_AND_CHILDREN.value
            this.builtin = true
        }
        roleRepository.insert(role)
        role.path = PathTree.build(parentRole?.path ?: PathTree.ROOT_PATH, role.id)
        roleRepository.updateById(role)

        // 父商户可用权限（父为根则取全部平台权限）
        val parentAllowed = if (parentTenantId == null || parentTenantId == PathTree.ROOT_ID)
            permissionRepository.findByTenantId(PathTree.ROOT_ID).map { it.code }.toSet()
        else
            tenantPermissionRepository.findByTenantId(parentTenantId).map { it.permissionCode }.toSet()

        // 未指定则默认继承父商户全部可用权限
        val codes = req.permissionCodes?.takeIf { it.isNotEmpty() }?.let { requested ->
            val illegal = requested.filter { !parentAllowed.contains(it) }
            if (illegal.isNotEmpty())
                throw RbacException(RbacErrorCode.PERMISSION_NOT_IN_TENANT, illegal.joinToString(","))
            requested.distinct()
        } ?: parentAllowed.toList()

        if (codes.isNotEmpty()) {
            // 商户可用权限白名单（始终写入）
            tenantPermissionRepository.insertBatch(
                codes.map {
                    TenantPermissionPO().apply {
                        this.tenantId = tenant.id
                        this.permissionCode = it
                        this.sourceTenantId = parentTenantId ?: PathTree.ROOT_ID
                    }
                }
            )
            // 未挂载共享父角色时才写本地权限；有父角色则靠继承，改父角色即可对本商户生效
            if (parentRole == null) {
                rolePermissionRepository.replaceAll(
                    role.id,
                    codes.map {
                        RolePermissionPO().apply {
                            this.roleId = role.id
                            this.permissionCode = it
                            this.granted = true
                        }
                    }
                )
            }
        }

        val salt = passwordEncoder.generateSalt()
        userRepository.insert(
            UserPO().apply {
                this.tenantId = tenant.id
                this.username = username
                this.salt = salt
                this.password = passwordEncoder.encode(password, salt)
                this.nickname = req.adminNickname ?: "管理员"
                this.phone = req.adminPhone
                this.email = req.adminEmail
                this.roleId = role.id
            }
        )
    }
}
