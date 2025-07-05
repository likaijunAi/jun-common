package com.jun.common.web.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
@ConfigurationProperties("jun.web.jwt")
class JunWebJwtProperties {

    var secret: String = "JunSecret"

    var issuer: String = "Jun"

    var claimName: String = "JunPayload"

    var expiresIn: Long = 3600 * 2L
}