package com.jun.common.upload.provider.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 16:51
 **/
@Component
@ConfigurationProperties("jun.upload.provider")
class AliOssUploadConfig {
    var aliOss: List<AliOssUploadProperties>? = null

    fun of(name: String): AliOssUploadProperties? {
        return aliOss?.firstOrNull { it.name == name }
    }
}
