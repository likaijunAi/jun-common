package com.jun.common.upload.provider.config

import com.jun.common.upload.config.UploadProviderProperties

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
class S3UploadProperties : UploadProviderProperties() {
    var accessKeyId: String? = null
    var secretAccessKey: String? = null
    var pathStyleAccessEnabled: Boolean = false

    var endpoint: String? = null
    var region: String? = null
}