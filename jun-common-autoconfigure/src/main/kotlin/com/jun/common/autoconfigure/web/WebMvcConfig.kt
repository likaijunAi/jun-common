package com.jun.common.autoconfigure.web

import com.jun.common.web.http.DefaultGsonHttpMessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/5 23:00
 **/
@Configuration
class WebMvcConfig(@Qualifier("JunDefaultGsonHttpMessageConverter") val defaultGsonHttpMessageConverter: DefaultGsonHttpMessageConverter) : WebMvcConfigurationSupport() {

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.removeIf { it.javaClass == DefaultGsonHttpMessageConverter::class.java }
        converters.add(0, defaultGsonHttpMessageConverter)
        super.extendMessageConverters(converters)
    }
}