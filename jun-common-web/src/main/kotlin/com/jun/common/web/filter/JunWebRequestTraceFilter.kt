package com.jun.common.web.filter

import cn.hutool.core.util.RandomUtil
import com.jun.common.web.config.JunWebTraceProperties
import com.jun.common.web.http.JunCachingRequestWrapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.concurrent.atomic.AtomicLong

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/30 16:11
 **/
class JunWebRequestTraceFilter(private val properties: JunWebTraceProperties) : SecurityFilter(properties) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val atomicLong = AtomicLong()
    private var prefix: String = RandomUtil.randomString(8)

    companion object {
        const val LOG_ID = "org.springframework.web.server.ServerWebExchange.LOG_ID"
        const val VERIFY_USER_ID = "JunServerWebExchangeUtils.verifierUserId"
        const val VERIFY_DEF_ID = "JunServerWebExchangeUtils.verifierDefId"
        const val VERIFY_DEF_TYPE = "JunServerWebExchangeUtils.verifierDefType"
        const val VERIFY_JWT_PAYLOAD = "JunServerWebExchangeUtils.jwt.payload"
        const val CACHED_REQUEST_BODY_ATTR = "cachedRequestBody"
        const val CACHED_REQUEST_IP_ATTR = "cachedRequestIp"
        const val AUTHORIZATION = "Authorization"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val requestUri = request.requestURI
        val logId = request.getHeader(LOG_ID) ?: "$prefix-${atomicLong.incrementAndGet()}"
        val method = request.method

        val ip = getClientIp(request)

        if (properties.header) {
            request.headerNames.asIterator().forEach { name ->
                logger.info("header-> $name: ${request.getHeader(name)}")
            }
        }

        if (!shouldFilter(requestUri)) {
            request.setAttribute(LOG_ID, logId)
            request.setAttribute(CACHED_REQUEST_IP_ATTR, ip)
            filterChain.doFilter(request, response)
            return
        }
        val cachingRequest = JunCachingRequestWrapper(request)
        val cachingResponse = ContentCachingResponseWrapper(response)

        // Retrieve request body content
        val requestBody = cachingRequest.getContentAsString()
        cachingRequest.setAttribute(LOG_ID, logId)
        cachingRequest.setAttribute(CACHED_REQUEST_BODY_ATTR, requestBody)
        cachingRequest.setAttribute(CACHED_REQUEST_IP_ATTR, ip)

        log.info("Trace[{}] IN-{}({})-{} {}", logId, method, requestUri, ip, requestBody)

        // Proceed with the filter chain with wrapped request and response
        filterChain.doFilter(cachingRequest, cachingResponse)

        // Retrieve response status and body content
        val status = cachingResponse.status
        val responseBody = String(cachingResponse.contentAsByteArray)
        cachingResponse.copyBodyToResponse() // Write the cached response body back to the original response

        log.info("Trace[{}] OUT-({}) {}", logId, status, responseBody)
    }
}