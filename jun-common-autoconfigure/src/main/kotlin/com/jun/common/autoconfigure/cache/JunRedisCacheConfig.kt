package com.jun.common.autoconfigure.cache

import com.jun.common.core.cache.JunCache
import com.jun.common.core.cache.JunJsonRedisTemplate
import com.jun.common.core.cache.JunRedisCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/27 21:22
 **/
@Configuration
@ConditionalOnBean(RedisConnectionFactory::class)
class JunRedisCacheConfig {

    @Bean("junJsonRedisTemplate")
    fun junJsonRedisTemplate(@Autowired connectionFactory: RedisConnectionFactory) : JunJsonRedisTemplate {
        return JunJsonRedisTemplate(connectionFactory)
    }

    @Bean
    fun junCache(@Qualifier("junJsonRedisTemplate") junJsonRedisTemplate: JunJsonRedisTemplate):JunCache {
        return JunRedisCache(junJsonRedisTemplate)
    }
}