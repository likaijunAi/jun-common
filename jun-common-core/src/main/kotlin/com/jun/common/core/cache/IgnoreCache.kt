package com.jun.common.core.cache

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/6/17 15:16
 **/
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget

/**
 * Annotation to indicate that the annotated element should be ignored by cache mechanisms.
 * Typically used on methods or classes to bypass caching logic.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreCache
