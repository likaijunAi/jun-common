package com.jun.common.upload.provider.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 16:51
 **/
@ConfigurationProperties("jun.upload.provider")
class CosUploadConfig {
    var cos: List<CosUploadProperties>? = null

    fun of(bucket: String): CosUploadProperties? {
        return cos?.firstOrNull { it.bucket == bucket }
    }
}