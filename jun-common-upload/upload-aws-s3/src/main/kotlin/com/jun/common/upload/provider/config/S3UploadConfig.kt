package com.jun.common.upload.provider.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/28 16:22
 **/
@Component
@ConfigurationProperties("jun.upload.provider")
class S3UploadConfig {
    var r3: List<S3UploadProperties>? = null

    fun of(name: String): S3UploadProperties? {
        return r3?.firstOrNull { it.name == name }
    }
}