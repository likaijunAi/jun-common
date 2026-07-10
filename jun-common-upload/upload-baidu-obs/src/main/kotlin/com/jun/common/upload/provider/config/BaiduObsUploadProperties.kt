package com.jun.common.upload.provider.config

import com.jun.common.upload.config.UploadProviderProperties

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
class BaiduObsUploadProperties : UploadProviderProperties() {
    var secretId: String? = null
    var secretKey: String? = null

    var region: String? = null
}