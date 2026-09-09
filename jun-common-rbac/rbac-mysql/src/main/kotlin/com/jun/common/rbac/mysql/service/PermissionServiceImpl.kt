package com.jun.common.rbac.mysql.service

import com.jun.common.core.web.Resp
import com.jun.common.rbac.exception.RbacErrorCode
import com.jun.common.rbac.exception.RbacException
import com.jun.common.rbac.model.Permission
import com.jun.common.rbac.model.PermissionType
import com.jun.common.rbac.model.TenantPermission
import com.jun.common.rbac.model.TreeNode
import com.jun.common.rbac.model.TreeNodes
import com.jun.common.rbac.model.req.PermissionCreateReq
import com.jun.common.rbac.model.req.PermissionMoveReq
import com.jun.common.rbac.model.req.PermissionQuery
import com.jun.common.rbac.model.req.PermissionUpdateReq
import com.jun.common.rbac.mysql.entity.PermissionPO
import com.jun.common.rbac.mysql.entity.TenantPermissionPO
import com.jun.common.rbac.repository.PermissionRepository
import com.jun.common.rbac.repository.RolePermissionRepository
import com.jun.common.rbac.repository.TenantPermissionRepository
import com.jun.common.rbac.repository.TenantRepository
import com.jun.common.rbac.repository.UserPermissionRepository
import com.jun.common.rbac.service.PermissionService
import com.jun.common.rbac.tree.PathTree
import org.springframework.stereotype.Service

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Service
class PermissionServiceImpl(
    private val permissionRepository: PermissionRepository,
    private val tenantPermissionRepository: TenantPermissionRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val userPermissionRepository: UserPermissionRepository,
    private val tenantRepository: TenantRepository
) : PermissionService {

    override fun create(req: PermissionCreateReq): Permission {
        if (req.code.isBlank()) throw RbacException(RbacErrorCode.BAD_REQUEST, "code 不能为空")
        if (req.name.isBlank()) throw RbacException(RbacErrorCode.BAD_REQUEST, "name 不能为空")
        if (permissionRepository.findByCode(req.tenantId, req.code) != null)
            throw RbacException(RbacErrorCode.PERMISSION_CODE_DUPLICATE)

        val parentId = req.parentId.takeIf { it.isNotBlank() } ?: PathTree.ROOT_ID
        val parent = if (parentId == PathTree.ROOT_ID) null
        else permissionRepository.findById(parentId) ?: throw RbacException(RbacErrorCode.PERMISSION_NOT_FOUND)

        val permission = PermissionPO().apply {
            this.tenantId = req.tenantId
            this.parentId = parentId
            this.path = PathTree.ROOT_PATH
            this.level = (parent?.level ?: -1) + 1
            this.code = req.code
            this.name = req.name
            this.type = req.type ?: PermissionType.API.value
            this.sort = req.sort ?: 0
            this.remark = req.remark
        }
        permissionRepository.insert(permission)
        permission.path = PathTree.build(parent?.path ?: PathTree.ROOT_PATH, permission.id)
        permissionRepository.updateById(permission)

        // 商户自建权限自动进入该商户白名单
        if (permission.tenantId != PathTree.ROOT_ID) {
            tenantPermissionRepository.insertBatch(
                listOf(
                    TenantPermissionPO().apply {
                        this.tenantId = permission.tenantId
                        this.permissionCode = permission.code
                        this.sourceTenantId = permission.tenantId
                    }
                )
            )
        }
        return permission
    }

    override fun update(req: PermissionUpdateReq): Permission {
        val permission = permissionRepository.findById(req.id)
            ?: throw RbacException(RbacErrorCode.PERMISSION_NOT_FOUND)
        req.name?.let { permission.name = it }
        req.type?.let { permission.type = it }
        req.sort?.let { permission.sort = it }
        req.remark?.let { permission.remark = it }
        permissionRepository.updateById(permission)
        return permission
    }

    override fun delete(id: String) {
        val permission = permissionRepository.findById(id)
            ?: throw RbacException(RbacErrorCode.PERMISSION_NOT_FOUND)
        if (permissionRepository.countChildren(id) > 0) throw RbacException(RbacErrorCode.PERMISSION_HAS_CHILDREN)

        permissionRepository.logicDelete(id)
        rolePermissionRepository.deleteByPermissionCode(permission.code)
        userPermissionRepository.deleteByPermissionCode(permission.code)
        tenantPermissionRepository.deleteByPermissionCode(permission.code)
    }

    override fun move(id: String, newParentId: String): Permission {
        val permission = permissionRepository.findById(id)
            ?: throw RbacException(RbacErrorCode.PERMISSION_NOT_FOUND)
        val target = newParentId.takeIf { it.isNotBlank() && it != PathTree.ROOT_ID }
            ?.let { permissionRepository.findById(it) ?: throw RbacException(RbacErrorCode.PERMISSION_NOT_FOUND) }

        val newParentPath = target?.path ?: PathTree.ROOT_PATH
        if (PathTree.isAncestor(permission.path, newParentPath))
            throw RbacException(RbacErrorCode.PERMISSION_MOVE_TO_CHILD)

        val oldPrefix = PathTree.descendantPrefix(permission.path)
        val newPrefix = PathTree.build(newParentPath, permission.id)
        val delta = ((target?.level ?: -1) + 1) - permission.level
        permissionRepository.updatePath(oldPrefix, newPrefix, delta)

        val moved = permissionRepository.findById(id) ?: permission
        moved.parentId = target?.id ?: PathTree.ROOT_ID
        moved.path = newPrefix
        moved.level = (target?.level ?: -1) + 1
        permissionRepository.updateById(moved)
        return moved
    }

    override fun move(req: PermissionMoveReq): Permission = move(req.id, req.newParentId)

    override fun findById(id: String): Permission? = permissionRepository.findById(id)

    override fun findByCode(tenantId: String, code: String): Permission? =
        permissionRepository.findByCode(tenantId, code)

    override fun tree(tenantId: String): List<TreeNode<Permission>> {
        val list = permissionRepository.findByTenantIdOrPlatform(tenantId)
        return TreeNodes.build(list, PathTree.ROOT_ID, { it.id }, { it.parentId })
    }

    override fun tenantPermissions(tenantId: String): List<TenantPermission> =
        tenantPermissionRepository.findByTenantId(tenantId)

    override fun grantToTenant(tenantId: String, permissionCodes: List<String>): List<TenantPermission> {
        if (permissionCodes.isEmpty()) return emptyList()
        val tenant = tenantRepository.findById(tenantId) ?: throw RbacException(RbacErrorCode.TENANT_NOT_FOUND)

        // 下发范围不能超出父商户白名单（根商户可下发全部平台权限）
        val allowed = if (tenant.parentId == PathTree.ROOT_ID)
            permissionRepository.findByTenantId(PathTree.ROOT_ID).map { it.code }.toSet()
        else
            tenantPermissionRepository.findByTenantId(tenant.parentId).map { it.permissionCode }.toSet()

        val granted = permissionCodes.distinct().filter { allowed.contains(it) }
        val items = granted.map {
            TenantPermissionPO().apply {
                this.tenantId = tenantId
                this.permissionCode = it
                this.sourceTenantId = tenant.parentId
            }
        }
        items.forEach { tenantPermissionRepository.deleteByTenantIdAndCode(tenantId, it.permissionCode) }
        tenantPermissionRepository.insertBatch(items)
        return items
    }

    override fun revokeFromTenant(tenantId: String, permissionCode: String) {
        tenantPermissionRepository.deleteByTenantIdAndCode(tenantId, permissionCode)
    }

    override fun page(query: PermissionQuery): Resp<Permission> = permissionRepository.page(query)
}
