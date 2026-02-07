package com.jun.common.web.filter

import com.jun.common.web.config.JunCustomProperties
import com.jun.common.web.exception.ResolveException
import com.jun.common.web.exception.UnauthorizedException
import com.jun.common.web.resolver.CustomResolver
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
class JunCustomSecurityFilter(
    private val resolverManager: ResolverManager,
    private val customProperties: JunCustomProperties
) : SecurityFilter(customProperties) {

    private var resolver: CustomResolver? = null

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

        val resolver = this@JunCustomSecurityFilter.resolver ?: (resolverManager.createResolver(customProperties).also {
            this@JunCustomSecurityFilter.resolver = it as? CustomResolver
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
        if (resolver is CustomResolver) {
            try {
                resolver.verify(request)
            } catch (e: Exception) {
                throw e
            }
        } else {
            val exception = ResolveException(UnauthorizedException(-1, "Resolver is not CustomResolver"))
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