package com.jun.common.rbac.repository

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Permission
import com.jun.common.rbac.model.req.PermissionQuery

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface PermissionRepository {

    fun findById(id: String): Permission?

    fun findByCode(tenantId: String, code: String): Permission?

    fun findByIds(ids: Collection<String>): List<Permission>

    fun findByCodes(codes: Collection<String>): List<Permission>

    /** 平台级(0) + 指定商户 的全部权限 */
    fun findByTenantIdOrPlatform(tenantId: String): List<Permission>

    fun findByTenantId(tenantId: String): List<Permission>

    fun findByParentId(parentId: String): List<Permission>

    fun findDescendants(pathPrefix: String): List<Permission>

    fun countChildren(parentId: String): Long

    fun insert(permission: Permission): Permission

    fun updateById(permission: Permission): Int

    fun logicDelete(id: String): Int

    fun updatePath(oldPrefix: String, newPrefix: String, deltaLevel: Int): Int

    fun page(query: PermissionQuery): Resp<Permission>
}
