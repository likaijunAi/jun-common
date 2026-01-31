package com.jun.common.upload.config


/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
open class UploadProviderProperties {
    //配置名字标记
    lateinit var name: String
    //指定存储位置
    lateinit var uploadPath: String

    var maxSize: Long? = null
    var type: List<String>? = null
    var prefix: String? = null

    //默认生成子目录（yyyy-MM/mediaId）
    //设置0时候，覆盖原文件
    var splitBucket: Int = 1

    var bucket: String? = null
}