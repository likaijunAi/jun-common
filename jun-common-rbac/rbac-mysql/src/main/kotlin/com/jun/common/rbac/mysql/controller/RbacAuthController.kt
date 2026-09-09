package com.jun.common.rbac.mysql.controller

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.LoginUser
import com.jun.common.rbac.model.req.LoginReq
import com.jun.common.rbac.model.req.LoginResp
import com.jun.common.rbac.model.req.fail
import com.jun.common.rbac.model.req.ok
import com.jun.common.rbac.service.AuthService
import com.jun.common.rbac.service.RbacService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 认证入口
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/9
 **/
@RestController
@RequestMapping("/api/rbac/auth")
class RbacAuthController(
    private val authService: AuthService,
    private val rbacService: RbacService
) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginReq): Resp<LoginResp> = ok(authService.login(req))

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): Resp<Boolean> {
        tokenOf(request)?.let { authService.logout(it) }
        return ok(true)
    }

    @PostMapping("/refresh")
    fun refresh(request: HttpServletRequest): Resp<LoginResp> {
        val token = tokenOf(request) ?: return fail("缺少 token")
        return ok(authService.refresh(token))
    }

    @GetMapping("/me")
    fun me(): Resp<LoginUser> = ok(authService.requireUser())

    @GetMapping("/permissions")
    fun permissions(): Resp<Set<String>> {
        val userId = authService.requireUser().userid ?: return ok(emptySet())
        return ok(rbacService.getUserPermissions(userId))
    }

    private fun tokenOf(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
        if (!header.isNullOrBlank()) {
            return if (header.startsWith("bearer ", true)) header.substring(7) else header
        }
        return request.getParameter("token")
    }
}
