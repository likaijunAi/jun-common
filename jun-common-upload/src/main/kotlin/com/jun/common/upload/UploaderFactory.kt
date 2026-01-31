package com.jun.common.upload

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 11:56
 **/
interface UploaderFactory {
    fun createUploader(name: String): Uploader?
    fun dataType(): String
}