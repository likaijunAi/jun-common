package com.jun.common.upload.model

import com.jun.common.core.model.DataType
import java.io.Serializable
import java.util.*

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 13:34
 **/
class Media(override var dataType: String) : DataType, Serializable {
    var id: Int? = null

    var mediaId: String? = null

    var name: String? = null

    var bucket: String? = null

    var type: String? = null

    var md5: String? = null

    var contentType: String? = null

    var size: Long? = null

    var createdBy: String? = null

    var createdAt: Date? = null

    var path: String? = null
}