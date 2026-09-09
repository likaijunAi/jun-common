package com.jun.common.rbac.auth

import com.jun.common.rbac.exception.RbacErrorCode
import com.jun.common.rbac.exception.RbacException
import com.jun.common.rbac.model.LoginUser

/**
 * 当前登录用户上下文（ThreadLocal），由 [RbacContextFilter] 写入与清理
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
object LoginContext {

    private val holder = ThreadLocal<LoginUser>()

    fun set(user: LoginUser) = holder.set(user)

    fun get(): LoginUser? = holder.get()

    fun require(): LoginUser = holder.get() ?: throw RbacException(RbacErrorCode.UNAUTHORIZED)

    fun userId(): String? = holder.get()?.userid

    fun tenantId(): String? = holder.get()?.tenantId

    fun clear() = holder.remove()
}
