package com.jun.common.core.cache

import cn.hutool.core.util.ReflectUtil

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/6/17 15:17
 **/
interface CacheReq {
    fun cacheKey(): String? {
        val fields = ReflectUtil.getFields(javaClass).filter {
            !it.isAnnotationPresent(IgnoreCache::class.java) &&
                    !it.isSynthetic &&
                    !java.lang.reflect.Modifier.isStatic(it.modifiers)
        }
        if (fields.isEmpty()) {
            return null
        }
        val sb = StringBuilder()
        for (field in fields) {
            val value = ReflectUtil.getFieldValue(this, field)
            if (value != null) {
                sb.append(field.name).append("=").append(value).append("&")
            }
        }
        if (sb.isNotEmpty()) {
            sb.setLength(sb.length - 1) // Remove last '&'
        }
        return if (sb.isNotEmpty()) sb.toString() else null
    }
}