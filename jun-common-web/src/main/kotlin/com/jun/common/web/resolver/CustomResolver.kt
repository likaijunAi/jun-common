package com.jun.common.web.resolver

import jakarta.servlet.http.HttpServletRequest

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/2/7 14:36
 **/
interface CustomResolver : Resolver {
    fun verify(request: HttpServletRequest)
}