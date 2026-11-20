package com.jun.common.web.resolver.permission

import com.jun.common.core.util.JwtPayload

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/11/20 15:24
 **/
interface JwtPermissionResolver : PermissionResolver {
    fun hasPermission(jwtPayload: JwtPayload, path: String): Boolean
}