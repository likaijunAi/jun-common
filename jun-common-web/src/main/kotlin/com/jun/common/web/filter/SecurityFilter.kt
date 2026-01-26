package com.jun.common.web.filter

import jakarta.servlet.http.HttpServletRequest
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
abstract class SecurityFilter(private var ignoring: List<String>? = null) : OncePerRequestFilter() {

    private val ignoringPatterns by lazy {
        pathPatterns(ignoring)
    }

    fun isIgnore(requestUri: String): Boolean {
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