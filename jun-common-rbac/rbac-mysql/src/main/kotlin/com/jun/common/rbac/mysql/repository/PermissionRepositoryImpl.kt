package com.jun.common.rbac.mysql.repository

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Permission
import com.jun.common.rbac.model.req.PermissionQuery
import com.jun.common.rbac.model.req.pageResp
import com.jun.common.rbac.mysql.entity.PermissionPO
import com.jun.common.rbac.mysql.mapper.PermissionMapper
import com.jun.common.rbac.mysql.support.SqlSupport
import com.jun.common.rbac.repository.PermissionRepository
import com.jun.common.rbac.tree.PathTree
import org.springframework.stereotype.Repository

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Repository
class PermissionRepositoryImpl(private val mapper: PermissionMapper) : PermissionRepository {

    private fun w() = SqlSupport.notDeleted<PermissionPO>()

    override fun findById(id: String): Permission? = mapper.selectOne(w().eq("id", id))

    override fun findByCode(tenantId: String, code: String): Permission? =
        mapper.selectOne(w().eq("tenant_id", tenantId).eq("code", code))

    override fun findByIds(ids: Collection<String>): List<Permission> =
        if (ids.isEmpty()) emptyList() else mapper.selectList(w().`in`("id", ids))

    override fun findByCodes(codes: Collection<String>): List<Permission> =
        if (codes.isEmpty()) emptyList() else mapper.selectList(w().`in`("code", codes))

    override fun findByTenantIdOrPlatform(tenantId: String): List<Permission> =
        mapper.selectList(
            w().apply("(tenant_id = '${PathTree.ROOT_ID}' OR tenant_id = {0})", tenantId)
                .orderByAsc("sort", "id")
        )

    override fun findByTenantId(tenantId: String): List<Permission> =
        mapper.selectList(w().eq("tenant_id", tenantId).orderByAsc("sort", "id"))

    override fun findByParentId(parentId: String): List<Permission> =
        mapper.selectList(w().eq("parent_id", parentId).orderByAsc("sort", "id"))

    override fun findDescendants(pathPrefix: String): List<Permission> =
        mapper.selectList(w().likeRight("path", pathPrefix).orderByAsc("path"))

    override fun countChildren(parentId: String): Long = mapper.selectCount(w().eq("parent_id", parentId))

    override fun insert(permission: Permission): Permission {
        val now = System.currentTimeMillis()
        if (permission.id.isEmpty()) permission.id = SqlSupport.nextId()
        if (permission.path.isBlank()) permission.path = PathTree.ROOT_PATH
        if (permission.createAt <= 0) permission.createAt = now
        permission.updateAt = now
        permission.delAt = 0
        mapper.insert(permission as PermissionPO)
        return permission
    }

    override fun updateById(permission: Permission): Int {
        permission.updateAt = System.currentTimeMillis()
        return mapper.updateById(permission as PermissionPO)
    }

    override fun logicDelete(id: String): Int = mapper.logicDelete(id, System.currentTimeMillis())

    override fun updatePath(oldPrefix: String, newPrefix: String, deltaLevel: Int): Int =
        mapper.updatePath(oldPrefix, newPrefix, deltaLevel)

    override fun page(query: PermissionQuery): Resp<Permission> {
        val w = w()
        query.tenantId?.takeIf { it.isNotBlank() }?.let { w.eq("tenant_id", it) }
        query.parentId?.takeIf { it.isNotBlank() }?.let { w.eq("parent_id", it) }
        query.code?.takeIf { it.isNotBlank() }?.let { w.like("code", it) }
        query.name?.takeIf { it.isNotBlank() }?.let { w.like("name", it) }
        query.type?.takeIf { it.isNotBlank() }?.let { w.eq("type", it) }
        SqlSupport.order(query, w, "sort")
        val p = mapper.selectPage(SqlSupport.page<PermissionPO>(query, "sort"), w)
        return pageResp(p.records, p.total)
    }
}
