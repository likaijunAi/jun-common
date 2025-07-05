package com.jun.common.web.http

import com.jun.common.core.web.GsonFactory
import com.jun.common.web.filter.JunWebRequestTraceFilter
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/30 23:22
 **/
abstract class JunCommonRestController<T> {
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    protected lateinit var request: HttpServletRequest

    fun getClientIp():String? {
        return request.getAttribute(JunWebRequestTraceFilter.CACHED_REQUEST_IP_ATTR) as? String
    }

    fun getReq(clazz: Class<T>): T? {
        return runCatching {
            (request.getAttribute(JunWebRequestTraceFilter.CACHED_REQUEST_BODY_ATTR) as? String)?.let {
                GsonFactory.defaultGson.fromJson(it, clazz)
            }
        }.onFailure {
            logger.error("getReq",it)
        }.getOrNull()
    }

    fun getAuthorization(): String? {
        return runCatching {
            request.getHeader(JunWebRequestTraceFilter.AUTHORIZATION)
        }.onFailure {
            logger.error("getAuthorization",it)
        }.getOrNull()
    }

    fun getVerifyDefId(): String? {
        return runCatching {
            request.getHeader(JunWebRequestTraceFilter.VERIFY_DEF_ID)
        }.onFailure {
            logger.error("getVerifyDefId",it)
        }.getOrNull()
    }

    fun getVerifyDefType(): String? {
        return runCatching {
            request.getHeader(JunWebRequestTraceFilter.VERIFY_DEF_TYPE)
        }.onFailure {
            logger.error("getVerifyDefType",it)
        }.getOrNull()
    }

    abstract fun getReq():T?
}