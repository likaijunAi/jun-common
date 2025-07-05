package com.jun.common.web.annotation


/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/5/10 11:40
 **/

@Target(allowedTargets = [AnnotationTarget.FIELD])
@Retention(AnnotationRetention.RUNTIME)
annotation class Sensitive(val type: SensitiveType)