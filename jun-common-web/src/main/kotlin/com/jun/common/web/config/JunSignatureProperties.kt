package com.jun.common.web.config

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
open class JunSignatureProperties : JunSecurityProperties() {
    lateinit var signType: String
    var signHeader: String = "signature"
}