package com.jun.common.web.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
@ConfigurationProperties("jun.web.security")
class JunWebSecurityProperties {

    var enable: Boolean = false

    var ignoring: List<String>? = null

    var errorNoToken: ErrorCode = ErrorCode(4011, "请登录")
    var errorTokenExpired: ErrorCode = ErrorCode(4012, "登录已过期")
    var errorTokenInvalid: ErrorCode = ErrorCode(4013, "登录已失效")

    class ErrorCode {
        var code: Int = 0
        var msg: String? = null

        constructor(code: Int, msg: String?) {
            this.code = code
            this.msg = msg
        }

        constructor() {
        }
    }
}