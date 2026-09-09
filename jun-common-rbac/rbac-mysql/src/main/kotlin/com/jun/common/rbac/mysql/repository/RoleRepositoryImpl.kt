package com.jun.common.rbac.mysql.repository

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Role
import com.jun.common.rbac.model.req.RoleQuery
import com.jun.common.rbac.model.req.pageResp
import com.jun.common.rbac.mysql.entity.RolePO
import com.jun.common.rbac.mysql.mapper.RoleMapper
import com.jun.common.rbac.mysql.mapper.UserMapper
import com.jun.common.rbac.mysql.support.SqlSupport
import com.jun.common.rbac.repository.RoleRepository
import com.jun.common.rbac.tree.PathTree
import org.springframework.stereotype.Repository

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Repository
class RoleRepositoryImpl(
    private val mapper: RoleMapper,
    private val userMapper: UserMapper
) : RoleRepository {

    private fun w() = SqlSupport.notDeleted<RolePO>()

    override fun findById(id: String): Role? = mapper.selectOne(w().eq("id", id))

    override fun findByCode(tenantId: String, code: String): Role? =
        mapper.selectOne(w().eq("tenant_id", tenantId).eq("code", code))

    override fun findByIds(ids: Collection<String>): List<Role> =
        if (ids.isEmpty()) emptyList() else mapper.selectList(w().`in`("id", ids))

    override fun findByTenantId(tenantId: String): List<Role> =
        mapper.selectList(w().eq("tenant_id", tenantId).orderByAsc("path"))

    override fun findSharedIn(tenantIds: Collection<String>): List<Role> =
        if (tenantIds.isEmpty()) emptyList()
        else mapper.selectList(
            w().eq("share_to_children", 1).`in`("tenant_id", tenantIds).orderByAsc("path")
        )

    override fun findByParentId(parentId: String): List<Role> =
        mapper.selectList(w().eq("parent_id", parentId).orderByAsc("id"))

    override fun findDescendants(pathPrefix: String): List<Role> =
        mapper.selectList(w().likeRight("path", pathPrefix).orderByAsc("path"))

    override fun countChildren(parentId: String): Long = mapper.selectCount(w().eq("parent_id", parentId))

    override fun countUsers(roleId: String): Long =
        userMapper.selectCount(SqlSupport.notDeleted<com.jun.common.rbac.mysql.entity.UserPO>().eq("role_id", roleId))

    override fun insert(role: Role): Role {
        val now = System.currentTimeMillis()
        if (role.id.isEmpty()) role.id = SqlSupport.nextId()
        if (role.path.isBlank()) role.path = PathTree.ROOT_PATH
        if (role.createAt <= 0) role.createAt = now
        role.updateAt = now
        role.delAt = 0
        mapper.insert(role as RolePO)
        return role
    }

    override fun updateById(role: Role): Int {
        role.updateAt = System.currentTimeMillis()
        return mapper.updateById(role as RolePO)
    }

    override fun logicDelete(id: String): Int = mapper.logicDelete(id, System.currentTimeMillis())

    override fun updatePath(oldPrefix: String, newPrefix: String, deltaLevel: Int): Int =
        mapper.updatePath(oldPrefix, newPrefix, deltaLevel)

    override fun page(query: RoleQuery): Resp<Role> {
        val w = w()
        query.tenantId?.takeIf { it.isNotBlank() }?.let { w.eq("tenant_id", it) }
        query.parentId?.takeIf { it.isNotBlank() }?.let { w.eq("parent_id", it) }
        query.code?.takeIf { it.isNotBlank() }?.let { w.like("code", it) }
        query.name?.takeIf { it.isNotBlank() }?.let { w.like("name", it) }
        query.status?.let { w.eq("status", it) }
        SqlSupport.order(query, w, "id")
        val p = mapper.selectPage(SqlSupport.page<RolePO>(query, "id"), w)
        return pageResp(p.records, p.total)
    }
}
