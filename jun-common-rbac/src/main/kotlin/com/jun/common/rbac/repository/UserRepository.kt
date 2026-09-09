package com.jun.common.rbac.repository

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.User
import com.jun.common.rbac.model.req.UserQuery

/**
 * 说明：username 全平台唯一，登录不需要租户信息
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface UserRepository {

    fun findById(id: String): User?

    /** 全平台唯一 */
    fun findByUsername(username: String): User?

    fun findByIds(ids: Collection<String>): List<User>

    fun findByRoleIds(roleIds: Collection<String>): List<User>

    fun findByTenantIds(tenantIds: Collection<String>): List<User>

    fun countByRoleId(roleId: String): Long

    fun countByTenantId(tenantId: String): Long

    fun existsByUsername(username: String, excludeId: String?): Boolean

    fun insert(user: User): User

    fun updateById(user: User): Int

    fun logicDelete(id: String): Int

    fun page(query: UserQuery): Resp<User>
}
