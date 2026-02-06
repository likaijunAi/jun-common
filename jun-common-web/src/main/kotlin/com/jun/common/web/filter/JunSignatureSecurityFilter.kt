package com.jun.common.web.filter

import com.jun.common.web.config.JunSignatureProperties
import com.jun.common.web.config.JunWebSecurityProperties
import com.jun.common.web.exception.ResolveException
import com.jun.common.web.exception.UnauthorizedException
import com.jun.common.web.filter.JunWebRequestTraceFilter.Companion.CACHED_REQUEST_BODY_ATTR
import com.jun.common.web.resolver.BasicResolver
import com.jun.common.web.resolver.ResolverManager
import com.jun.common.web.resolver.SignatureResolver
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/6/14 17:51
 **/
class JunSignatureSecurityFilter(
    private val resolverManager: ResolverManager,
    private val properties: JunWebSecurityProperties,
    private val signatureConfig: JunSignatureProperties
) : SecurityFilter(signatureConfig) {

    private var resolver: BasicResolver? = null

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val requestUri = request.requestURI
        val ip = getClientIp(request)
        logger.info("requestUri[${request.method}]($ip): $requestUri")

        if ("OPTIONS".equals(request.method, ignoreCase = true)) {
            response.status = HttpServletResponse.SC_OK
            filterChain.doFilter(request, response)
            return
        }

        if (!shouldFilter(requestUri)) {
            logger.info("doFilter")
            filterChain.doFilter(request, response)
            return
        }

        val tokenHeader = request.headerNames?.toList()?.firstOrNull { it.equals(signatureConfig.signHeader, true) }

        val signature = request.getHeader(tokenHeader)
        if (signature.isNullOrEmpty()) {
            val error = properties.errorNoToken
            if (resolveException(
                    request,
                    response,
                    UnauthorizedException(error.code, error.msg)
                )
            ) {
                return
            } else {
                throw UnauthorizedException(error.code, error.msg)
            }
        }
        logger.info("signature $signature")

        signature.apply {
            val requestBody = request.getAttribute(CACHED_REQUEST_BODY_ATTR) as? String
            val resolver =
                this@JunSignatureSecurityFilter.resolver ?: (resolverManager.createResolver(signatureConfig).also {
                    this@JunSignatureSecurityFilter.resolver = it as? BasicResolver
                })
            if (resolver == null) {
                val exception = ResolveException(UnauthorizedException(-1, "Resolver is not found"))
                if (resolveException(
                        request,
                        response,
                        exception
                    )
                ) {
                    return
                } else {
                    throw exception
                }
            }
            if (resolver is SignatureResolver) {
                try {
                    if (!resolver.verify(request, signatureConfig.signType, signature, requestBody = requestBody)) {
                        val exception = UnauthorizedException(-1, "Signature error")
                        if (resolveException(
                                request,
                                response,
                                exception
                            )
                        ) {
                            return
                        } else {
                            throw exception
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            } else {
                val exception = ResolveException(UnauthorizedException(-1, "Resolver is not BasicResolver"))
                if (resolveException(
                        request,
                        response,
                        exception
                    )
                ) {
                    return
                } else {
                    throw exception
                }
            }
            request.setAttribute(JunWebRequestTraceFilter.CACHED_REQUEST_IP_ATTR, ip)
            filterChain.doFilter(request, response)
        }
    }
}