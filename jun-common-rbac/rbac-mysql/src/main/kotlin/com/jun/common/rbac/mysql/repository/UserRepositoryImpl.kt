package com.jun.common.rbac.mysql.repository

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.User
import com.jun.common.rbac.model.req.UserQuery
import com.jun.common.rbac.model.req.pageResp
import com.jun.common.rbac.mysql.entity.TenantPO
import com.jun.common.rbac.mysql.entity.UserPO
import com.jun.common.rbac.mysql.mapper.TenantMapper
import com.jun.common.rbac.mysql.mapper.UserMapper
import com.jun.common.rbac.mysql.support.SqlSupport
import com.jun.common.rbac.repository.UserRepository
import com.jun.common.rbac.tree.PathTree
import org.springframework.stereotype.Repository

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Repository
class UserRepositoryImpl(
    private val mapper: UserMapper,
    private val tenantMapper: TenantMapper
) : UserRepository {

    private fun w() = SqlSupport.notDeleted<UserPO>()

    override fun findById(id: String): User? = mapper.selectOne(w().eq("id", id))

    override fun findByUsername(username: String): User? =
        mapper.selectOne(w().eq("username", username).orderByAsc("id").last("LIMIT 1"))

    override fun findByIds(ids: Collection<String>): List<User> =
        if (ids.isEmpty()) emptyList() else mapper.selectList(w().`in`("id", ids))

    override fun findByRoleIds(roleIds: Collection<String>): List<User> =
        if (roleIds.isEmpty()) emptyList() else mapper.selectList(w().`in`("role_id", roleIds))

    override fun findByTenantIds(tenantIds: Collection<String>): List<User> =
        if (tenantIds.isEmpty()) emptyList() else mapper.selectList(w().`in`("tenant_id", tenantIds))

    override fun countByRoleId(roleId: String): Long = mapper.selectCount(w().eq("role_id", roleId))

    override fun countByTenantId(tenantId: String): Long = mapper.selectCount(w().eq("tenant_id", tenantId))

    override fun existsByUsername(username: String, excludeId: String?): Boolean {
        val w = w().eq("username", username)
        excludeId?.takeIf { it.isNotBlank() }?.let { w.ne("id", it) }
        return mapper.selectCount(w) > 0
    }

    override fun insert(user: User): User {
        val now = System.currentTimeMillis()
        if (user.id.isEmpty()) user.id = SqlSupport.nextId()
        if (user.createAt <= 0) user.createAt = now
        user.updateAt = now
        user.delAt = 0
        mapper.insert(user as UserPO)
        return user
    }

    override fun updateById(user: User): Int {
        user.updateAt = System.currentTimeMillis()
        return mapper.updateById(user as UserPO)
    }

    override fun logicDelete(id: String): Int = mapper.logicDelete(id, System.currentTimeMillis())

    override fun page(query: UserQuery): Resp<User> {
        val w = w()
        query.tenantPath?.takeIf { it.isNotBlank() }?.let { path ->
            val ids = tenantMapper.selectList(
                SqlSupport.notDeleted<TenantPO>().likeRight("path", PathTree.descendantPrefix(path))
            ).map { it.id }
            if (ids.isEmpty()) return pageResp(emptyList(), 0)
            w.`in`("tenant_id", ids)
        } ?: query.tenantId?.takeIf { it.isNotBlank() }?.let { w.eq("tenant_id", it) }

        query.roleId?.takeIf { it.isNotBlank() }?.let { w.eq("role_id", it) }
        query.username?.takeIf { it.isNotBlank() }?.let { w.like("username", it) }
        query.nickname?.takeIf { it.isNotBlank() }?.let { w.like("nickname", it) }
        query.phone?.takeIf { it.isNotBlank() }?.let { w.like("phone", it) }
        query.status?.let { w.eq("status", it) }
        SqlSupport.order(query, w, "id")
        val p = mapper.selectPage(SqlSupport.page<UserPO>(query, "id"), w)
        return pageResp(p.records, p.total)
    }
}
