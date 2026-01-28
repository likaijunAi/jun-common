package com.jun.common.web.filter

import com.jun.common.web.config.JunBasicProperties
import com.jun.common.web.config.JunWebSecurityProperties
import com.jun.common.web.exception.ResolveException
import com.jun.common.web.exception.UnauthorizedException
import com.jun.common.web.resolver.BasicResolver
import com.jun.common.web.resolver.ResolverManager
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.*

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/6/14 17:51
 **/
class JunBasicSecurityFilter(
    private val resolverManager: ResolverManager,
    private val properties: JunWebSecurityProperties,
    private val basicConfig: JunBasicProperties
) : SecurityFilter(basicConfig) {

    companion object {
        const val AUTHORIZATION = "Authorization"
        const val BASIC = "basic "
        const val BASIC_LENGTH = BASIC.length
    }

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
            val token = (if (tokenStr.startsWith(BASIC, true)) {
                tokenStr.substring(BASIC_LENGTH)
            } else
                tokenStr).let {
                String(Base64.getDecoder().decode(it))
            }

            val (user, passwd) = if (token.contains(":")) {
                token.split(":").let {
                    Pair(it[0], it[1])
                }
            } else {
                Pair(null, null)
            }

            if (user == null || passwd == null) {
                val exception = UnauthorizedException(-1, "User or password is empty")
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

            val resolver = this@JunBasicSecurityFilter.resolver ?: (resolverManager.createResolver(basicConfig).also {
                this@JunBasicSecurityFilter.resolver = it as? BasicResolver
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
            if (resolver is BasicResolver) {
                if (resolver.verify(user, passwd)) {
                    request.setAttribute(JunWebRequestTraceFilter.VERIFY_USER_ID, user)
                    request.setAttribute(JunWebRequestTraceFilter.VERIFY_DEF_ID, user)
                } else {
                    val exception = UnauthorizedException(-1, "User or password is error")
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