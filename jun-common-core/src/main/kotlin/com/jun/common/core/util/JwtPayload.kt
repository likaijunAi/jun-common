package com.jun.common.core.util

import java.util.*

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/5 22:01
 **/
open class JwtPayload {
    lateinit var payloadId: String
    var createdAt: Date? = null
    var expiresIn: Date? = null
    var userid: String? = null
}
