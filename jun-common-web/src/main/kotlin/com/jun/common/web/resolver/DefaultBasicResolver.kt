package com.jun.common.web.resolver

import com.jun.common.web.config.JunBasicProperties

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/27 10:01
 **/
open class DefaultBasicResolver(private val basicConfig: JunBasicProperties) : BasicResolver {
    override fun verify(username: String, password: String): Boolean {
        val account = basicConfig.account?.firstOrNull { it.username == username }
        return account?.password == password
    }
}