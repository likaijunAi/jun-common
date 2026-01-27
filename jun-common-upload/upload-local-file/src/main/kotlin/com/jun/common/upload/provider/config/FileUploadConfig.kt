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
class FileUploadConfig {
    var file: List<FileUploadProperties>? = null

    fun of(bucket: String): FileUploadProperties? {
        return file?.firstOrNull { it.bucket == bucket }
    }
}