package com.jun.common.rbac.model.req

import com.jun.common.rbac.model.DataScope
import com.jun.common.rbac.model.LoginUser

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class LoginReq {

    /** 全平台唯一 */
    var username: String = ""

    var password: String = ""
}

open class LoginResp {
    var token: String = ""
    var expiresIn: Long = 0
    var userId: String = ""
    var username: String? = null
    var nickname: String? = null
    var tenantId: String = "0"
    var tenantPath: String = "/0/"
    var roleId: String = "0"
    var dataScope: String = DataScope.TENANT.value
    var permissions: Set<String> = emptySet()

    companion object {
        fun of(token: String, expiresIn: Long, user: LoginUser, permissions: Set<String>): LoginResp {
            val r = LoginResp()
            r.token = token
            r.expiresIn = expiresIn
            r.userId = user.userid ?: ""
            r.username = user.username
            r.nickname = user.nickname
            r.tenantId = user.tenantId
            r.tenantPath = user.tenantPath
            r.roleId = user.roleId
            r.dataScope = user.dataScope
            r.permissions = permissions
            return r
        }
    }
}
