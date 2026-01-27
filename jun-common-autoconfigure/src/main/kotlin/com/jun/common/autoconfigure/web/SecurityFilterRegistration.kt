package com.jun.common.autoconfigure.web

import com.jun.common.web.config.JunWebSecurityProperties
import com.jun.common.web.filter.JunBasicSecurityFilter
import com.jun.common.web.filter.JunJwtSecurityFilter
import com.jun.common.web.resolver.ResolverManager
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.env.Environment

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/26 16:08
 **/
@Configuration
class SecurityFilterRegistration :
    BeanFactoryPostProcessor, EnvironmentAware {

    private lateinit var env: Environment

    override fun setEnvironment(environment: Environment) {
        this.env = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val webProperties = Binder.get(env).bind("jun.web.security", JunWebSecurityProperties::class.java).orElse(null)

        val resolverManager = ResolverManager()
        if (webProperties?.basicEnable == true) {
            webProperties.basic?.forEachIndexed { index, prop ->
                val registration = FilterRegistrationBean<JunBasicSecurityFilter>()
                registration.filter = JunBasicSecurityFilter(resolverManager, webProperties, prop)
                registration.addUrlPatterns("/*")
                registration.order = prop.order ?: (Ordered.HIGHEST_PRECEDENCE + 1)

                val beanName = prop.name ?: "basicFilterReg$index"

                beanFactory.registerSingleton(beanName, registration)
            }
        }
        if (webProperties?.jwtEnable == true) {
            webProperties.jwt?.forEachIndexed { index, prop ->
                val registration = FilterRegistrationBean<JunJwtSecurityFilter>()
                registration.filter = JunJwtSecurityFilter(resolverManager, webProperties, prop)
                registration.addUrlPatterns("/*")
                registration.order = prop.order ?: (Ordered.HIGHEST_PRECEDENCE + 1)

                val beanName = prop.name ?: "jwtFilterReg$index"

                beanFactory.registerSingleton(beanName, registration)
            }
        }
    }
}