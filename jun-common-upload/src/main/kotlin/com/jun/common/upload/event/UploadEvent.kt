package com.jun.common.upload.event

import com.jun.common.upload.model.Media
import java.io.Serializable

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 14:42
 **/
class UploadEvent(success: Boolean, error: String? = null, media: Media? = null) : Serializable