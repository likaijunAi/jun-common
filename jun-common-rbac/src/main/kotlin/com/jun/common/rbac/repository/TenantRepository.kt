package com.jun.common.rbac.repository

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Tenant
import com.jun.common.rbac.model.req.TenantQuery

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface TenantRepository {

    fun findById(id: String): Tenant?

    fun findByCode(code: String): Tenant?

    fun findByIds(ids: Collection<String>): List<Tenant>

    fun findByParentId(parentId: String): List<Tenant>

    /** path 前缀匹配的全部子孙（不含自身） */
    fun findDescendants(pathPrefix: String): List<Tenant>

    fun findAll(): List<Tenant>

    fun countChildren(parentId: String): Long

    fun insert(tenant: Tenant): Tenant

    fun updateById(tenant: Tenant): Int

    /** 逻辑删除：del_at = now */
    fun logicDelete(id: String): Int

    /** 移动子树：path 换前缀 + level 加 delta */
    fun updatePath(oldPrefix: String, newPrefix: String, deltaLevel: Int): Int

    fun page(query: TenantQuery): Resp<Tenant>
}
