package com.jun.common.web.feign

import cn.hutool.core.io.IoUtil
import com.jun.common.web.feign.RequestInterceptor.Companion.reqId
import feign.FeignException
import feign.Response
import feign.codec.Decoder
import feign.optionals.OptionalDecoder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.support.SpringDecoder
import java.io.IOException
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/5 22:48
 **/
open class ResponseDecoder : Decoder {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private val messageConvertersObjectFactory: ObjectFactory<HttpMessageConverters>? = null
    private val decoder: Decoder by lazy {
        OptionalDecoder(
            org.springframework.cloud.openfeign.support.ResponseEntityDecoder(
                SpringDecoder(messageConvertersObjectFactory)
            )
        )
    }

    @Throws(IOException::class, FeignException::class)
    override fun decode(response: Response, type: Type?): Any? {
        val id = reqId.get()?.let {
            reqId.remove()
            it
        }?:""
        val resp: String = IoUtil.read(response.body().asInputStream(), StandardCharsets.UTF_8)
        logger.info("Resp {} - {}", id, resp)
        return decoder.decode(response.toBuilder().body(resp, StandardCharsets.UTF_8).build(), type)
    }
}