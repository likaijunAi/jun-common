package com.jun.common.web.resolver

import com.jun.common.web.config.JunSecurityProperties

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/27 9:48
 **/
interface ResolverFactory {
    fun createResolver(props: JunSecurityProperties): Resolver?
}