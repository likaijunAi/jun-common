package com.jun.common.autoconfigure.web

import com.google.gson.Gson
import com.jun.common.web.filter.JunWebRequestTraceFilter
import com.jun.common.web.filter.JunWebSecurityFilter
import com.jun.common.web.http.DefaultGsonHttpMessageConverter
import com.jun.common.web.config.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.servlet.HandlerExceptionResolver

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/6/18 22:41
 **/
@Configuration
@EnableConfigurationProperties(
    JunWebTraceProperties::class,
    JunWebJwtProperties::class,
    JunWebSecurityProperties::class,
    JunWebRespProperties::class
)
class JunWebConfig {

    @Bean("JunDefaultHttpMessageConverters")
    fun httpMessageConverters(@Qualifier("JunDefaultGsonHttpMessageConverter")  defaultGsonHttpMessageConverter: DefaultGsonHttpMessageConverter): HttpMessageConverters {
        return HttpMessageConverters(defaultGsonHttpMessageConverter)
    }

    @Bean("JunDefaultGsonHttpMessageConverter")
    fun defaultGsonHttpMessageConverter(gson: Gson?=null): DefaultGsonHttpMessageConverter {
        if (gson == null) {
            return DefaultGsonHttpMessageConverter()
        }
        return DefaultGsonHttpMessageConverter(gson)
    }

    @Bean
    @ConditionalOnProperty("jun.web.trace.enable")
    fun requestTraceFilter(properties: JunWebTraceProperties): FilterRegistrationBean<JunWebRequestTraceFilter> {
        val registration = FilterRegistrationBean<JunWebRequestTraceFilter>()
        registration.filter = JunWebRequestTraceFilter(properties)
        registration.addUrlPatterns("/*")
        registration.order = Ordered.HIGHEST_PRECEDENCE // 设置优先级
        return registration
    }

    @Bean
    @ConditionalOnProperty("jun.web.security.enable")
    fun webSecurityFilter(
        handlerExceptionResolver: HandlerExceptionResolver,
        properties: JunWebSecurityProperties,
        jwtConfig: JunWebJwtProperties
    ): FilterRegistrationBean<JunWebSecurityFilter> {
        val registration = FilterRegistrationBean<JunWebSecurityFilter>()
        registration.filter = JunWebSecurityFilter(handlerExceptionResolver, properties, jwtConfig)
        registration.addUrlPatterns("/*")
        registration.order = Ordered.HIGHEST_PRECEDENCE + 1 // 设置优先级
        return registration
    }
}