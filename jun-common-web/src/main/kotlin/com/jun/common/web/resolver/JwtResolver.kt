package com.jun.common.web.resolver

import com.jun.common.core.util.JwtPayload
import java.io.UnsupportedEncodingException

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/15 22:28
 **/
interface JwtResolver : Resolver {
    @Throws(UnsupportedEncodingException::class)
    fun generateToken(jwtPayload: JwtPayload): String?

    @Throws(UnsupportedEncodingException::class)
    fun parseToken(token: String?): JwtPayload
}