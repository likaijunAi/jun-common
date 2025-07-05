package com.jun.common.web.annotation

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/5/11 23:59
 **/
@Target(allowedTargets = [AnnotationTarget.CLASS])
@Retention(AnnotationRetention.RUNTIME)
annotation class SensitiveData
