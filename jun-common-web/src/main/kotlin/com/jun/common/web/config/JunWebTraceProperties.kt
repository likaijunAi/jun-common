package com.jun.common.web.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
@ConfigurationProperties("jun.web.trace")
class JunWebTraceProperties {

    var enable: Boolean = true

    var header: Boolean = false

    var ignoring: List<String>? = null
}