package com.jun.common.web.filter

import com.auth0.jwt.exceptions.TokenExpiredException
import com.jun.common.web.config.JunWebJwtProperties
import com.jun.common.web.config.JunWebSecurityProperties
import com.jun.common.web.exception.UnauthorizedException
import com.jun.common.web.http.DefaultJwtResolver
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.server.PathContainer
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/6/14 17:51
 **/
class JunWebSecurityFilter(
    private var handlerExceptionResolver: HandlerExceptionResolver,
    private val properties: JunWebSecurityProperties,
    private val jwtConfig: JunWebJwtProperties
) : OncePerRequestFilter() {

    companion object {
        const val AUTHORIZATION = "Authorization"
        const val BEARER = "bearer "
        const val BEARER_LENGTH = BEARER.length
    }

    private val resolver by lazy {
        DefaultJwtResolver(jwtConfig)
    }

    private val ignoringPatterns by lazy {
        pathPatterns(properties.ignoring)
    }


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

        if (!properties.enable || isIgnore(requestUri)) {
            filterChain.doFilter(request, response)
            return
        }

        val tokenHeader = request.headerNames?.toList()?.firstOrNull { it.equals(AUTHORIZATION, true) }

        val tokenStr = request.getHeader(tokenHeader) ?: request.parameterMap?.get("token")?.first() ?: run {
            val error = properties.errorNoToken
            handlerExceptionResolver.resolveException(
                request,
                response,
                null,
                UnauthorizedException(error.code, error.msg)
            )
            return
        }
        logger.info("AUTHORIZATION $tokenStr{}")

        tokenStr.apply {
            val token = if (tokenStr.startsWith(BEARER, true)) {
                tokenStr.substring(BEARER_LENGTH)
            } else
                tokenStr

            val verifyId: String?
            try {
                val payload = resolver.parseToken(token)
                verifyId = payload.payloadId
            } catch (e: Exception) {
                if (e is TokenExpiredException) {
                    val error = properties.errorTokenExpired
                    handlerExceptionResolver.resolveException(
                        request,
                        response,
                        null,
                        UnauthorizedException(error.code, error.msg)
                    )
                    return
                } else {
                    logger.error("", e)
                    val error = properties.errorTokenInvalid
                    handlerExceptionResolver.resolveException(
                        request,
                        response,
                        null,
                        UnauthorizedException(error.code, error.msg)
                    )
                    return
                }
            }
            request.setAttribute(JunWebRequestTraceFilter.CACHED_REQUEST_IP_ATTR, ip)
            request.setAttribute(JunWebRequestTraceFilter.VERIFY_DEF_ID, verifyId)

            filterChain.doFilter(request, response)
        }
    }

    private fun isIgnore(requestUri: String): Boolean {
        return ignoringPatterns.firstOrNull { pattern: PathPattern ->
            pattern.matches(
                PathContainer.parsePath(requestUri)
            )
        }?.let { true } ?: false
    }

    private fun pathPatterns(mapping: List<String>? = null): List<PathPattern> {
        val pathPatterns = mutableListOf<PathPattern>()
        if (!mapping.isNullOrEmpty()) {
            for (url in mapping) {
                val pathPattern = PathPatternParser.defaultInstance.parse(url)
                pathPatterns.add(pathPattern)
            }
        }
        return pathPatterns
    }

    fun getClientIp(request: HttpServletRequest): String? {
        var ipAddress: String?
        ipAddress = request.getHeader("x-forwarded-for")
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            ipAddress = request.getHeader("Proxy-Client-IP")
        }
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            ipAddress = request.getHeader("X-Real-IP")
        }
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            ipAddress = request.remoteAddr
        }
        if (ipAddress != null && ipAddress.length > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","))
            }
        }
        return ipAddress
    }
}