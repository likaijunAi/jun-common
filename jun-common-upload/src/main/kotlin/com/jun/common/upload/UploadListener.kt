package com.jun.common.upload

import com.jun.common.upload.event.UploadEvent

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/27 13:32
 **/
interface UploadListener {
    fun onUpload(event: UploadEvent)
}