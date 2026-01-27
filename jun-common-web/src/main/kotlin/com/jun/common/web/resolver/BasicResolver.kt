package com.jun.common.web.resolver

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/27 9:58
 **/
interface BasicResolver : Resolver {
    fun verify(username: String, password: String): Boolean
}