package com.jun.common.rbac.service

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Permission
import com.jun.common.rbac.model.TenantPermission
import com.jun.common.rbac.model.TreeNode
import com.jun.common.rbac.model.req.PermissionCreateReq
import com.jun.common.rbac.model.req.PermissionMoveReq
import com.jun.common.rbac.model.req.PermissionQuery
import com.jun.common.rbac.model.req.PermissionUpdateReq

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface PermissionService {

    fun create(req: PermissionCreateReq): Permission

    fun update(req: PermissionUpdateReq): Permission

    /** 校验：无子权限；同时清理角色/商户/用户关联 */
    fun delete(id: String)

    fun move(id: String, newParentId: String): Permission

    fun move(req: PermissionMoveReq): Permission

    fun findById(id: String): Permission?

    fun findByCode(tenantId: String, code: String): Permission?

    /** 平台级 + 指定商户的权限树 */
    fun tree(tenantId: String): List<TreeNode<Permission>>

    /** 商户可用权限白名单 */
    fun tenantPermissions(tenantId: String): List<TenantPermission>

    /** admin 向商户下发可用权限，校验不超出父商户范围 */
    fun grantToTenant(tenantId: String, permissionCodes: List<String>): List<TenantPermission>

    fun revokeFromTenant(tenantId: String, permissionCode: String)

    fun page(query: PermissionQuery): Resp<Permission>
}
