package com.jun.common.upload.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
@ConfigurationProperties("jun.upload")
class JunUploadProperties {
    var enable: Boolean = true
}