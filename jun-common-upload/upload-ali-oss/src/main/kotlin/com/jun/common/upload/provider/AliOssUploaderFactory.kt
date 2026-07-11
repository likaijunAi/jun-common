package com.jun.common.upload.provider

import com.jun.common.upload.AbstractUploaderFactory
import com.jun.common.upload.Uploader
import com.jun.common.upload.provider.config.AliOssUploadConfig
import com.jun.common.upload.provider.config.AliOssUploadProperties
import org.springframework.stereotype.Service

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 15:29
 **/
@Service
class AliOssUploaderFactory(val config: AliOssUploadConfig) : AbstractUploaderFactory(AliOssUploader.NAME) {
    companion object {
        fun build(config: AliOssUploadConfig): AliOssUploaderFactory {
            return AliOssUploaderFactory(config)
        }

        fun createUploader(properties: AliOssUploadProperties): AliOssUploader {
            return AliOssUploader(properties)
        }
    }

    override fun build(name: String): Uploader? {
        val properties = config.of(name) ?: return null
        return createUploader(properties)
    }
}
