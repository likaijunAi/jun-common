package com.jun.common.rbac.service

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.User
import com.jun.common.rbac.model.req.UserCreateReq
import com.jun.common.rbac.model.req.UserQuery
import com.jun.common.rbac.model.req.UserUpdateReq

/**
 * 说明：username 全平台唯一
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface UserService {

    fun create(req: UserCreateReq): User

    fun update(req: UserUpdateReq): User

    fun delete(id: String)

    fun findById(id: String): User?

    fun findByUsername(username: String): User?

    /** 分配角色，校验角色与用户同商户 */
    fun assignRole(userId: String, roleId: String)

    fun resetPassword(userId: String, rawPassword: String)

    fun changeStatus(userId: String, status: Int)

    fun unlock(userId: String)

    fun permissions(userId: String): Set<String>

    fun page(query: UserQuery): Resp<User>
}
