package com.jun.common.autoconfigure.web

import com.google.gson.Gson
import com.jun.common.web.filter.JunWebRequestTraceFilter
import com.jun.common.web.http.DefaultGsonHttpMessageConverter
import com.jun.common.web.config.*
import org.springframework.beans.factory.annotation.Qualifier
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
    fun httpMessageConverters(@Qualifier("JunDefaultGsonHttpMessageConverter") defaultGsonHttpMessageConverter: DefaultGsonHttpMessageConverter): HttpMessageConverters {
        return HttpMessageConverters(defaultGsonHttpMessageConverter)
    }

    @Bean("JunDefaultGsonHttpMessageConverter")
    fun defaultGsonHttpMessageConverter(gson: Gson? = null): DefaultGsonHttpMessageConverter {
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
        registration.order = Ordered.HIGHEST_PRECEDENCE
        return registration
    }

//    @Bean
//    @ConditionalOnProperty(
//        prefix = "jun.web.security",
//        name = ["jwt-enable"],
//        havingValue = "true",
//        matchIfMissing = false
//    )
//    fun webJwtSecurityFilter(
//        handlerExceptionResolver: HandlerExceptionResolver,
//        webProperties: JunWebSecurityProperties
//    ): List<FilterRegistrationBean<JunJwtSecurityFilter>> {
//        val list = mutableListOf<FilterRegistrationBean<JunJwtSecurityFilter>>()
//        webProperties.jwt?.forEach { properties ->
//            val registration = FilterRegistrationBean<JunJwtSecurityFilter>()
//            registration.filter = JunJwtSecurityFilter(handlerExceptionResolver, webProperties, properties)
//            registration.addUrlPatterns("/*")
//            registration.order = properties.order ?: Ordered.HIGHEST_PRECEDENCE
//
//            if (properties.name?.isNotEmpty() == true)
//                registration.setName(properties.name)
//            list.add(registration)
//        }
//        return list
//    }
//
//    @Bean
//    @ConditionalOnProperty(
//        prefix = "jun.web.security",
//        name = ["basic-enable"],
//        havingValue = "true",
//        matchIfMissing = false
//    )
//    fun webBasicSecurityFilter(
//        handlerExceptionResolver: HandlerExceptionResolver,
//        webProperties: JunWebSecurityProperties
//    ): List<FilterRegistrationBean<JunBasicSecurityFilter>> {
//        val list = mutableListOf<FilterRegistrationBean<JunBasicSecurityFilter>>()
//        webProperties.basic?.forEach { properties ->
//            val registration = FilterRegistrationBean<JunBasicSecurityFilter>()
//            registration.filter = JunBasicSecurityFilter(handlerExceptionResolver, webProperties, properties)
//            registration.addUrlPatterns("/*")
//            registration.order = properties.order ?: Ordered.HIGHEST_PRECEDENCE
//
//            if (properties.name?.isNotEmpty() == true)
//                registration.setName(properties.name)
//
//            list.add(registration)
//        }
//        return list
//    }


}