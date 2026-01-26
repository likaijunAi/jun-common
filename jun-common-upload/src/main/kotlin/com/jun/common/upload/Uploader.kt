package com.jun.common.upload

import com.jun.common.core.web.Resp
import com.jun.common.upload.model.Media
import java.io.InputStream

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 11:55
 **/
interface Uploader {
    fun verify(type: String, size: Long, contentType: String? = "application/octet-stream"): Resp<String?>

    fun upload(
        inputStream: InputStream,
        name: String,
        type: String,
        size: Long,
        createBy: String,
        contentType: String? = "application/octet-stream"
    ): Resp<Media?>

    fun getInputStream(path: String): Resp<InputStream?>
}