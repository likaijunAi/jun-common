package com.jun.common.autoconfigure.upload

import com.jun.common.upload.UploadManager
import com.jun.common.upload.config.JunUploadProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/6/18 22:41
 **/
@Configuration
@EnableConfigurationProperties(
    JunUploadProperties::class
)
@ComponentScan(basePackages = ["com.jun.common.upload.provider","com.jun.common.upload.provider.config"])
class JunUploadConfig(
    val applicationContext: ApplicationContext,
    val applicationEventPublisher: ApplicationEventPublisher
) {

    @Bean
    @ConditionalOnProperty("jun.upload.enable")
    fun requestTraceFilter(properties: JunUploadProperties): UploadManager {
        return UploadManager(applicationContext, applicationEventPublisher)
    }
}