package com.jun.common.autoconfigure.upload

import com.jun.common.upload.UploadManager
import com.jun.common.upload.config.JunUploadProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
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
@ComponentScan(basePackages = ["com.jun.common.upload.provider.config","com.jun.common.upload.provider"])
class JunUploadConfig(
    val applicationContext: ApplicationContext
) {

    @Bean
    @ConditionalOnProperty(
        prefix = "jun.upload",
        name = ["enable"],
        havingValue = "true",
        matchIfMissing = false
    )
    fun uploadManager(): UploadManager {
        return UploadManager(applicationContext)
    }
}