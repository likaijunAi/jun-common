package com.jun.common.autoconfigure.rbac

import com.jun.common.rbac.evaluator.DefaultPermissionEvaluator
import com.jun.common.rbac.evaluator.PermissionEvaluator
import com.jun.common.rbac.service.RbacService
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
}
