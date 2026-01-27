package com.jun.common.web.config

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
class JunBasicProperties : JunSecurityProperties() {
    var account: List<Account>? = null

    class Account(val username: String, val password: String)
}