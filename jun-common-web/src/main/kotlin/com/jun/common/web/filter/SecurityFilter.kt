package com.jun.common.web.filter

import com.jun.common.web.config.JunSecurityProperties
import com.jun.common.web.spring.ApplicationHolder
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.server.PathContainer
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/8/4 15:49
 **/
abstract class SecurityFilter(private var properties: JunSecurityProperties) : OncePerRequestFilter() {

    private val excludePatterns by lazy {
        pathPatterns(properties.excludePaths)
    }

    private val includePatterns by lazy {
        pathPatterns(properties.urlPatterns)
    }

    fun resolveException(request: HttpServletRequest, response: HttpServletResponse, ex: Exception): Boolean {
        return ApplicationHolder.getHandlerExceptionResolver()?.let {
            it.resolveException(request, response, null, ex)
            true
        } ?: false
    }

    fun isExcluded(requestUri: String): Boolean {
        return excludePatterns.firstOrNull { pattern: PathPattern ->
            pattern.matches(
                PathContainer.parsePath(requestUri)
            )
        }?.let { true } ?: false
    }

    fun isUriMatched(requestUri: String): Boolean {
        if (properties.urlPatterns?.isEmpty() == true) {
            return false
        }
        return includePatterns.firstOrNull { pattern: PathPattern ->
            pattern.matches(
                PathContainer.parsePath(requestUri)
            )
        }?.let { true } ?: false
    }

    fun shouldFilter(uri: String): Boolean {
        val path = PathContainer.parsePath(uri)

        if (excludePatterns.any { it.matches(path) }) return false

        if (includePatterns.isEmpty()) return true

        return includePatterns.any { it.matches(path) }
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