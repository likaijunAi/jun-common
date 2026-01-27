package com.jun.common.upload.provider

import com.jun.common.upload.AbstractUploaderFactory
import com.jun.common.upload.Uploader
import com.jun.common.upload.provider.config.FileUploadConfig
import com.jun.common.upload.provider.config.FileUploadProperties
import org.springframework.stereotype.Service

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 15:29
 **/
@Service
class FileUploaderFactory(val config: FileUploadConfig) : AbstractUploaderFactory(FileUploader.NAME) {
    companion object {
        fun build(config: FileUploadConfig): FileUploaderFactory {
            return FileUploaderFactory(config)
        }

        fun createUploader(properties: FileUploadProperties): FileUploader {
            properties.bucket ?: throw IllegalArgumentException("bucket is empty")
            return FileUploader(properties.bucket!!, properties)
        }
    }

    override fun build(bucket: String): Uploader? {
        val properties = config.of(bucket) ?: return null
        return FileUploader(bucket, properties)
    }
}