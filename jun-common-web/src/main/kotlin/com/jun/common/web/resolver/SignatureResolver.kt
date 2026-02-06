package com.jun.common.web.resolver

import jakarta.servlet.http.HttpServletRequest

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/27 9:58
 **/
interface SignatureResolver : Resolver {
    fun verify(request: HttpServletRequest, signType: String, signature: String, requestBody: String? = null): Boolean
}