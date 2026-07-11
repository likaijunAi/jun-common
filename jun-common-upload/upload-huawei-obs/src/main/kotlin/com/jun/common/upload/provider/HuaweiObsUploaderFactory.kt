package com.jun.common.upload.provider

import com.jun.common.upload.AbstractUploaderFactory
import com.jun.common.upload.Uploader
import com.jun.common.upload.provider.config.HuaweiObsUploadConfig
import com.jun.common.upload.provider.config.HuaweiObsUploadProperties
import org.springframework.stereotype.Service

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 15:29
 **/
@Service
class HuaweiObsUploaderFactory(val config: HuaweiObsUploadConfig) : AbstractUploaderFactory(HuaweiObsUploader.NAME) {
    companion object {
        fun build(config: HuaweiObsUploadConfig): HuaweiObsUploaderFactory {
            return HuaweiObsUploaderFactory(config)
        }

        fun createUploader(properties: HuaweiObsUploadProperties): HuaweiObsUploader {
            return HuaweiObsUploader(properties)
        }
    }

    override fun build(name: String): Uploader? {
        val properties = config.of(name) ?: return null
        return createUploader(properties)
    }
}
