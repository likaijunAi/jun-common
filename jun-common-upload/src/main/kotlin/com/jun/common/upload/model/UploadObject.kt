package com.jun.common.upload.model

import java.io.InputStream

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/2/3 13:44
 **/
data class UploadObject(
    var dataType: String? = null,

    var dataName: String,

    var inputStream: InputStream,

    var name: String? = null,

    var type: String? = null,

    var contentType: String? = null,

    var size: Long = 0,

    var createdBy: String? = null
)