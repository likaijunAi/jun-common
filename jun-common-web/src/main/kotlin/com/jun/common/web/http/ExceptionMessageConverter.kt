package com.jun.common.web.http

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at  2024/6/1 22:44
 **/

interface ExceptionMessageConverter {
    fun toMessage(e: Throwable): Any?
}