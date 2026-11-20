package com.jun.common.web.resolver

import com.jun.common.web.config.JunBasicProperties
import com.jun.common.web.config.JunJwtProperties
import com.jun.common.web.config.JunSecurityProperties
import org.slf4j.LoggerFactory

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/11/20 14:54
 **/
open class DefaultResolverFactory : ResolverFactory {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun createResolver(props: JunSecurityProperties): Resolver? = when (props) {
        is JunBasicProperties -> DefaultBasicResolver(props)
        is JunJwtProperties -> DefaultJwtResolver(props)
        else -> {
            logger.warn("Unknown security properties type: ${props.javaClass.name}")
            null
        }
    }
}