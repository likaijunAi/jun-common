package com.jun.common.web.annotation

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/23 23:03
 **/
@Retention(AnnotationRetention.RUNTIME)
@Target(allowedTargets = [AnnotationTarget.FUNCTION,AnnotationTarget.CLASS, ])
annotation class IgnoreLog
