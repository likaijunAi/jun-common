package com.jun.common.upload.provider

import com.jun.common.upload.AbstractUploaderFactory
import com.jun.common.upload.Uploader
import com.jun.common.upload.provider.config.BaiduObsUploadConfig
import com.jun.common.upload.provider.config.BaiduObsUploadProperties
import org.springframework.stereotype.Service

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 15:29
 **/
@Service
class BaiduObsUploaderFactory(val config: BaiduObsUploadConfig) : AbstractUploaderFactory(BaiduObsUploader.NAME) {
    companion object {
        fun build(config: BaiduObsUploadConfig): BaiduObsUploaderFactory {
            return BaiduObsUploaderFactory(config)
        }

        fun createUploader(properties: BaiduObsUploadProperties): BaiduObsUploader {
            return BaiduObsUploader(properties)
        }
    }

    override fun build(name: String): Uploader? {
        val properties = config.of(name) ?: return null
        return createUploader(properties)
    }
}