package com.jun.common.rbac.service

import com.jun.common.rbac.model.LoginUser
import com.jun.common.rbac.model.req.LoginReq
import com.jun.common.rbac.model.req.LoginResp

/**
 * 认证：登录 / 登出 / 续期 / 上下文
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface AuthService {

    fun login(req: LoginReq): LoginResp

    fun logout(token: String)

    fun refresh(token: String): LoginResp

    fun parse(token: String): LoginUser

    fun currentUser(): LoginUser?

    fun requireUser(): LoginUser
}
