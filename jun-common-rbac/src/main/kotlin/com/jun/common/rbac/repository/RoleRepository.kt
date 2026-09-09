package com.jun.common.rbac.repository

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Role
import com.jun.common.rbac.model.req.RoleQuery

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface RoleRepository {

    fun findById(id: String): Role?

    fun findByCode(tenantId: String, code: String): Role?

    fun findByIds(ids: Collection<String>): List<Role>

    fun findByTenantId(tenantId: String): List<Role>

    /** 指定商户集合中 shareToChildren=true 的角色（tenantIds 传 '0' 表示平台级） */
    fun findSharedIn(tenantIds: Collection<String>): List<Role>

    fun findByParentId(parentId: String): List<Role>

    fun findDescendants(pathPrefix: String): List<Role>

    fun countChildren(parentId: String): Long

    fun countUsers(roleId: String): Long

    fun insert(role: Role): Role

    fun updateById(role: Role): Int

    fun logicDelete(id: String): Int

    fun updatePath(oldPrefix: String, newPrefix: String, deltaLevel: Int): Int

    fun page(query: RoleQuery): Resp<Role>
}
