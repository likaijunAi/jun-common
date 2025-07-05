package com.jun.common.core.cache

import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/27 20:52
 **/
interface JunCache {
    fun <T> getObject(key: String, type: Type): T?
    fun setObject(key: String, value: Any, time: Long? = null, timeUnit: TimeUnit = TimeUnit.SECONDS)
    fun expire(key: String, timeUnit: TimeUnit): Long
    fun hasKey(key: String): Boolean
    fun keys(pattern: String): Collection<String>?
    fun delByPrefix(prefix: String)
    fun del(vararg keys: String)
    fun getLock(key: String, releaseTime: Long): Boolean
    fun releaseLock(key: String)

    fun <T> getObject(cacheKey: JunCacheKey, key: String, type: Type): T? {
        return getObject(cacheKey.key(key), type)
    }

    fun setObject(cacheKey: JunCacheKey, key: String, value: Any) {
        setObject(cacheKey.key(key), value, cacheKey.expire, cacheKey.timeUnit)
    }

    fun hasKey(cacheKey: JunCacheKey, key: String): Boolean {
        return hasKey(cacheKey.key(key))
    }

    fun delByPrefix(cacheKey: JunCacheKey) {
        cacheKey.prefix?.let {
            delByPrefix(it)
        }
    }

    fun getLock(cacheKey: JunCacheKey, key: String): Boolean {
        return cacheKey.expire?.let { getLock(cacheKey.key(key), it) } ?: false
    }

    fun releaseLock(cacheKey: JunCacheKey, key: String) {
        releaseLock(cacheKey.key(key))
    }

}