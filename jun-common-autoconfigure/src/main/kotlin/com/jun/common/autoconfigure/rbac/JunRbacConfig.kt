package com.jun.common.autoconfigure.rbac

import com.jun.common.rbac.auth.PasswordEncoder
import com.jun.common.rbac.auth.Pbkdf2PasswordEncoder
import com.jun.common.rbac.auth.RbacContextFilter
import com.jun.common.rbac.auth.TokenProvider
import com.jun.common.rbac.auth.JwtTokenProvider
import com.jun.common.rbac.cache.NoOpRbacCacheManager
import com.jun.common.rbac.cache.RbacCacheManager
import com.jun.common.rbac.permission.DefaultPermissionEvaluator
import com.jun.common.rbac.permission.PermissionEvaluator
import com.jun.common.rbac.service.AuthService
import com.jun.common.rbac.service.RbacService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/7/10
 **/
@Configuration
@ConditionalOnBean(RbacService::class)
class JunRbacConfig {

    @Bean
    @ConditionalOnMissingBean(PermissionEvaluator::class)
    fun permissionEvaluator(rbacService: RbacService): PermissionEvaluator {
        return DefaultPermissionEvaluator(rbacService)
    }

    @Bean
    @ConditionalOnMissingBean(RbacCacheManager::class)
    fun rbacCacheManager(): RbacCacheManager = NoOpRbacCacheManager()

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder::class)
    fun passwordEncoder(): PasswordEncoder = Pbkdf2PasswordEncoder()

    @Bean
    @ConditionalOnMissingBean(TokenProvider::class)
    fun tokenProvider(
        @Value("\${jun.rbac.jwt.secret:JunRbacSecret}") secret: String,
        @Value("\${jun.rbac.jwt.issuer:JunRbac}") issuer: String
    ): TokenProvider = JwtTokenProvider(secret, issuer)

    @Bean
    @ConditionalOnBean(AuthService::class)
    fun rbacContextFilter(authService: AuthService): FilterRegistrationBean<RbacContextFilter> {
        val registration = FilterRegistrationBean<RbacContextFilter>()
        registration.filter = RbacContextFilter(authService)
        registration.addUrlPatterns("/*")
        registration.order = Ordered.LOWEST_PRECEDENCE - 100
        return registration
    }
}
