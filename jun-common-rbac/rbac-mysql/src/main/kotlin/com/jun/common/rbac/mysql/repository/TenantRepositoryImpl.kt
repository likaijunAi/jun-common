package com.jun.common.rbac.mysql.repository

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Tenant
import com.jun.common.rbac.model.req.TenantQuery
import com.jun.common.rbac.model.req.pageResp
import com.jun.common.rbac.mysql.entity.TenantPO
import com.jun.common.rbac.mysql.mapper.TenantMapper
import com.jun.common.rbac.mysql.support.SqlSupport
import com.jun.common.rbac.repository.TenantRepository
import com.jun.common.rbac.tree.PathTree
import org.springframework.stereotype.Repository

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Repository
class TenantRepositoryImpl(private val mapper: TenantMapper) : TenantRepository {

    private fun w() = SqlSupport.notDeleted<TenantPO>()

    override fun findById(id: String): Tenant? =
        mapper.selectOne(w().eq("id", id))

    override fun findByCode(code: String): Tenant? =
        mapper.selectOne(w().eq("code", code))

    override fun findByIds(ids: Collection<String>): List<Tenant> =
        if (ids.isEmpty()) emptyList() else mapper.selectList(w().`in`("id", ids))

    override fun findByParentId(parentId: String): List<Tenant> =
        mapper.selectList(w().eq("parent_id", parentId).orderByAsc("id"))

    override fun findDescendants(pathPrefix: String): List<Tenant> =
        mapper.selectList(w().likeRight("path", pathPrefix).orderByAsc("path"))

    override fun findAll(): List<Tenant> =
        mapper.selectList(w().orderByAsc("path"))

    override fun countChildren(parentId: String): Long =
        mapper.selectCount(w().eq("parent_id", parentId))

    override fun insert(tenant: Tenant): Tenant {
        val now = System.currentTimeMillis()
        if (tenant.id.isEmpty()) tenant.id = SqlSupport.nextId()
        if (tenant.path.isBlank()) tenant.path = PathTree.ROOT_PATH
        if (tenant.createAt <= 0) tenant.createAt = now
        tenant.updateAt = now
        tenant.delAt = 0
        mapper.insert(tenant as TenantPO)
        return tenant
    }

    override fun updateById(tenant: Tenant): Int {
        tenant.updateAt = System.currentTimeMillis()
        return mapper.updateById(tenant as TenantPO)
    }

    override fun logicDelete(id: String): Int = mapper.logicDelete(id, System.currentTimeMillis())

    override fun updatePath(oldPrefix: String, newPrefix: String, deltaLevel: Int): Int =
        mapper.updatePath(oldPrefix, newPrefix, deltaLevel)

    override fun page(query: TenantQuery): Resp<Tenant> {
        val w = w()
        query.path?.takeIf { it.isNotBlank() }?.let { w.likeRight("path", PathTree.descendantPrefix(it)) }
            ?: query.parentId?.takeIf { it.isNotBlank() }?.let { w.eq("parent_id", it) }
        query.name?.takeIf { it.isNotBlank() }?.let { w.like("name", it) }
        query.code?.takeIf { it.isNotBlank() }?.let { w.like("code", it) }
        query.status?.let { w.eq("status", it) }
        SqlSupport.order(query, w, "id")
        val p = mapper.selectPage(SqlSupport.page<TenantPO>(query, "id"), w)
        return pageResp(p.records, p.total)
    }
}
