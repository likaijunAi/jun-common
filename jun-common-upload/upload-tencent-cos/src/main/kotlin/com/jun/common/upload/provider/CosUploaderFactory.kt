package com.jun.common.upload.provider

import com.jun.common.upload.AbstractUploaderFactory
import com.jun.common.upload.Uploader
import com.jun.common.upload.provider.config.CosUploadConfig
import com.jun.common.upload.provider.config.CosUploadProperties
import org.springframework.stereotype.Service

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 15:29
 **/
@Service
class CosUploaderFactory(val config: CosUploadConfig) : AbstractUploaderFactory(CosUploader.NAME) {
    companion object {
        fun build(config: CosUploadConfig): CosUploaderFactory {
            return CosUploaderFactory(config)
        }

        fun createUploader(properties: CosUploadProperties): CosUploader {
            return CosUploader(properties)
        }
    }

    override fun build(name: String): Uploader? {
        val properties = config.of(name) ?: return null
        return createUploader(properties)
    }
}