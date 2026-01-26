package com.jun.common.upload.config


/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
open class UploadProviderProperties {
    var bucket: String? = null
    var maxSize: Long? = null
    var type: List<String>? = null
    var prefix: String? = null
    var splitBucket: Int  = 1
}