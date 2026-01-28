package com.jun.common.autoconfigure.web

import com.google.gson.Gson
import com.jun.common.web.filter.JunWebRequestTraceFilter
import com.jun.common.web.http.DefaultGsonHttpMessageConverter
import com.jun.common.web.config.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.Ordered

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/6/18 22:41
 **/
@Configuration
@EnableConfigurationProperties(
    JunWebTraceProperties::class,
    JunWebSecurityProperties::class,
    JunWebRespProperties::class
)
class JunWebConfig {

    @Primary
    @Bean("JunDefaultHttpMessageConverters")
    @ConditionalOnBean(name = ["JunDefaultGsonHttpMessageConverter"])
    fun httpMessageConverters(@Qualifier("JunDefaultGsonHttpMessageConverter") defaultGsonHttpMessageConverter: DefaultGsonHttpMessageConverter): HttpMessageConverters {
        return HttpMessageConverters(defaultGsonHttpMessageConverter)
    }

    @Bean("JunDefaultGsonHttpMessageConverter")
    @ConditionalOnMissingBean(DefaultGsonHttpMessageConverter::class)
    fun defaultGsonHttpMessageConverter(
        @Qualifier("JunDefaultGson") gson: Gson?
    ): DefaultGsonHttpMessageConverter {
        return if (gson != null) {
            DefaultGsonHttpMessageConverter(gson)
        } else {
            DefaultGsonHttpMessageConverter()
        }
    }

    @Bean
    @ConditionalOnProperty("jun.web.trace.enable")
    fun requestTraceFilter(properties: JunWebTraceProperties): FilterRegistrationBean<JunWebRequestTraceFilter> {
        val registration = FilterRegistrationBean<JunWebRequestTraceFilter>()
        registration.filter = JunWebRequestTraceFilter(properties)
        registration.addUrlPatterns("/*")
        registration.order = Ordered.HIGHEST_PRECEDENCE
        return registration
    }
}