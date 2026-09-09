package com.jun.common.rbac.auth

import com.jun.common.rbac.service.AuthService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 解析 token 并写入 [LoginContext]，请求结束清理。
 * 未携带 token 或解析失败时直接放行，由业务鉴权决定是否需要登录。
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class RbacContextFilter(private val authService: AuthService) : OncePerRequestFilter() {

    companion object {
        const val AUTHORIZATION = "Authorization"
        const val BEARER = "bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            tokenOf(request)?.let { token ->
                runCatching { authService.parse(token) }
                    .getOrNull()
                    ?.let { LoginContext.set(it) }
            }
            filterChain.doFilter(request, response)
        } finally {
            LoginContext.clear()
        }
    }

    private fun tokenOf(request: HttpServletRequest): String? {
        val header = request.getHeader(AUTHORIZATION)
        if (!header.isNullOrBlank()) {
            return if (header.startsWith(BEARER, true)) header.substring(BEARER.length) else header
        }
        return request.getParameter("token")
    }
}
