package com.jun.common.web.http

import com.jun.common.core.web.Resp
import com.jun.common.web.exception.JunErrorException
import com.jun.common.web.exception.ResolveException
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/9 21:54
 **/
open class DefaultExceptionMessageConverter : ExceptionMessageConverter {

    override fun toMessage(e: Throwable): Any {
        var code = -1
        var msg = e.message

        when(e){
            is ResolveException -> {
                code = -1
                msg = "Json 转换失败(${e.message})"
            }
            is JunErrorException -> {
                e.toResp().let {
                    code = it.code
                    msg  = it.error
                }
            }
            is ResponseStatusException -> {
                code = e.statusCode.value()
                msg  = e.reason ?: HttpStatus.resolve(e.statusCode.value())?.reasonPhrase
            }
        }

        return Resp.fail<String>(msg, code)
    }
}