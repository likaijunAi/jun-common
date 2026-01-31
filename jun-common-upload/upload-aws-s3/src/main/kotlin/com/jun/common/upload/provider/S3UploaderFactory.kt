package com.jun.common.upload.provider

import com.jun.common.upload.AbstractUploaderFactory
import com.jun.common.upload.Uploader
import com.jun.common.upload.provider.config.S3UploadConfig
import com.jun.common.upload.provider.config.S3UploadProperties
import org.springframework.stereotype.Service

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 15:29
 **/
@Service
class S3UploaderFactory(val config: S3UploadConfig) : AbstractUploaderFactory(S3Uploader.NAME) {
    companion object {
        fun build(config: S3UploadConfig): S3UploaderFactory {
            return S3UploaderFactory(config)
        }

        fun createUploader(properties: S3UploadProperties): S3Uploader {
            return S3Uploader(properties)
        }
    }

    override fun build(name: String): Uploader? {
        val properties = config.of(name) ?: return null
        return createUploader(properties)
    }
}