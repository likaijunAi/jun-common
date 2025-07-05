package com.jun.common.web.feign

import cn.hutool.core.util.RandomUtil
import com.jun.common.core.util.Signer
import feign.RequestInterceptor
import feign.RequestTemplate
import org.slf4j.LoggerFactory

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/5 22:39
 **/
abstract class RequestInterceptor : RequestInterceptor {
    private val logger = LoggerFactory.getLogger(RequestInterceptor::class.java)

    companion object {
        const val REQ_ID = "org.springframework.web.server.ServerWebExchange.LOG_ID"
        val reqId = ThreadLocal<String>()
    }

    override fun apply(requestTemplate: RequestTemplate) {
        val path = requestTemplate.path()
        val method = requestTemplate.method()
        val id = RandomUtil.randomString(6)
        requestTemplate.request().header(REQ_ID, id)
        reqId.set(id)

        val type = signType(path, method)

        if (type?.isNotEmpty() == true) {
            var headerName: String? = null
            var signature: String? = null
            val body: String?  = if ("post".equals(method, ignoreCase = true)) {
               String(requestTemplate.body())
            } else
                null


            when (type) {
                Signer.BASIC -> {
                    headerName = "Authorization"
                    signature = signer()?.basicSignature()
                }

                Signer.JWT -> {
                    headerName = "Authorization"
                    signature = signer()?.jwtSignature()
                }

                Signer.MD5 -> {
                    if(body!=null){
                        headerName = "signature"
                        signature = signer()?.md5Signature(body)
                    }
                }

                Signer.RSA -> {
                    if(body!=null){
                        headerName = "signature"
                        signature = signer()?.rsaSignature(body)
                    }
                }
            }

            signature?.let {
                requestTemplate.header(headerName, it)
            }

            logger.info("Req {} - {}({}) - signature:{}\n{}", id, method, path, signature, body)
        }
    }

    abstract fun signType(path: String, method: String): String?

    abstract fun signer(): Signer?
}