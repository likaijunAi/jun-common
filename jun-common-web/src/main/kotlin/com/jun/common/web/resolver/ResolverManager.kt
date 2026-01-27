package com.jun.common.web.resolver

import com.jun.common.web.config.JunBasicProperties
import com.jun.common.web.config.JunJwtProperties
import com.jun.common.web.config.JunSecurityProperties
import com.jun.common.web.spring.ApplicationHolder
import org.slf4j.LoggerFactory

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/27 9:53
 **/
class ResolverManager {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val resolverFactories: List<ResolverFactory> by lazy {
        val applicationContext = ApplicationHolder.applicationContext()
        applicationContext?.getBeanNamesForType(ResolverFactory::class.java)
            ?.map { name -> applicationContext.getBean(name, ResolverFactory::class.java) }
            ?: emptyList()
    }

    fun createResolver(props: JunSecurityProperties): Resolver? {
        logger.info("${props.javaClass.name} -> ${props.name}")

        resolverFactories.firstNotNullOfOrNull { factory ->
            factory.createResolver(props)
        }?.let { return it }

        return createDefaultResolver(props)
    }

    private fun createDefaultResolver(props: JunSecurityProperties): Resolver? = when (props) {
        is JunBasicProperties -> DefaultBasicResolver(props)
        is JunJwtProperties -> DefaultJwtResolver(props)
        else -> {
            logger.warn("Unknown security properties type: ${props.javaClass.name}")
            null
        }
    }
}