package com.jun.common.web.config

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
class JunJwtProperties : JunSecurityProperties() {

    var secret: String = "JunSecret"

    var issuer: String = "Jun"

    var claimName: String = "JunPayload"

    var expiresIn: Long = 3600 * 2L
}