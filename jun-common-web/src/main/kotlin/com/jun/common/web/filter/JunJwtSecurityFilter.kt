package com.jun.common.web.filter

import com.auth0.jwt.exceptions.TokenExpiredException
import com.jun.common.web.config.JunJwtProperties
import com.jun.common.web.config.JunWebSecurityProperties
import com.jun.common.web.exception.ResolveException
import com.jun.common.web.exception.UnauthorizedException
import com.jun.common.web.resolver.JwtResolver
import com.jun.common.web.resolver.ResolverManager
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/6/14 17:51
 **/
class JunJwtSecurityFilter(
    private val resolverManager: ResolverManager,
    private val properties: JunWebSecurityProperties,
    private val jwtConfig: JunJwtProperties
) : SecurityFilter(jwtConfig) {

    companion object {
        const val AUTHORIZATION = "Authorization"
        const val BEARER = "bearer "
        const val BEARER_LENGTH = BEARER.length
    }

    private var resolver: JwtResolver? = null

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
            filterChain.doFilter(request, response)
            return
        }

        val tokenHeader = request.headerNames?.toList()?.firstOrNull { it.equals(AUTHORIZATION, true) }

        val tokenStr = request.getHeader(tokenHeader) ?: request.parameterMap?.get("token")?.first()
        if (tokenStr.isNullOrEmpty()) {
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

        logger.info("AUTHORIZATION $tokenStr")

        tokenStr.apply {
            val token = if (tokenStr.startsWith(BEARER, true)) {
                tokenStr.substring(BEARER_LENGTH)
            } else
                tokenStr

            val resolver = this@JunJwtSecurityFilter.resolver ?: (resolverManager.createResolver(jwtConfig).also {
                this@JunJwtSecurityFilter.resolver = it as? JwtResolver
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
            if (resolver is JwtResolver) {
                val verifyId: String?
                try {
                    val payload = resolver.parseToken(token)
                    verifyId = payload.payloadId
                    payload.userid?.apply {
                        request.setAttribute(JunWebRequestTraceFilter.VERIFY_USER_ID, this)
                    }
                } catch (e: Exception) {
                    if (e is TokenExpiredException) {
                        val error = properties.errorTokenExpired
                        val exception = UnauthorizedException(error.code, error.msg)
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
                    } else {
                        logger.error("", e)
                        val error = properties.errorTokenInvalid
                        val exception = UnauthorizedException(error.code, error.msg)
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
                }
                request.setAttribute(JunWebRequestTraceFilter.CACHED_REQUEST_IP_ATTR, ip)
                request.setAttribute(JunWebRequestTraceFilter.VERIFY_DEF_ID, verifyId)
            } else {
                val exception = ResolveException(UnauthorizedException(-1, "Resolver is not JwtResolver"))
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

            filterChain.doFilter(request, response)
        }
    }
}