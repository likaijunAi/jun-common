package com.jun.common.web.exception

import com.jun.common.core.web.Resp

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/17 21:37
 **/
open class JunErrorException(private var code: Int, private var msg: String? = null) : RuntimeException(msg) {

    fun toResp(): Resp<String?> {
        return Resp.fail(msg, code)
    }
}