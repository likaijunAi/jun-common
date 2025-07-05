package com.jun.common.web.feign

import com.jun.common.core.util.Signer

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/24 23:39
 **/
open class DefaultRequestInterceptor: RequestInterceptor() {
    override fun signType(path: String, method: String): String? {
        return null
    }

    override fun signer(): Signer? {
        return null
    }
}