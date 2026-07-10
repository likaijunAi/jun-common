package com.jun.common.rbac.repository

import com.jun.common.rbac.model.User

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/7/10
 **/
interface UserRepository {
    fun findById(id: String): User?
    fun findByUsername(username: String): User?
}
