package com.jun.common.upload.provider

import com.jun.common.upload.AbstractUploaderFactory
import com.jun.common.upload.Uploader
import com.jun.common.upload.provider.config.CosUploadConfig
import org.springframework.stereotype.Service

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 15:29
 **/
@Service
class CosUploaderFactory(val config: CosUploadConfig) : AbstractUploaderFactory(CosUploader.NAME) {

    override fun build(bucket: String): Uploader? {
        val properties = config.of(bucket) ?: return null
        return CosUploader(bucket, properties)
    }
}