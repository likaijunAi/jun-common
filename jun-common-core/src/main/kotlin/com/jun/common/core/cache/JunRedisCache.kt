package com.jun.common.core.cache

import com.jun.common.core.web.GsonFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/27 20:57
 **/
@Suppress("UNCHECKED_CAST")
class JunRedisCache(private var redisTemplate: JunJsonRedisTemplate) : JunCache {

    override fun <T> getObject(key: String, type: Type): T? {
        if (!redisTemplate.hasKey(key))return null
        val bytes = redisTemplate.opsForValue()[key] as ByteArray? ?: return null
        return if (type === String::class.java)
            String(bytes) as T
        else if (type === Int::class.java)
            JunJsonRedisTemplate.byteArrayToInt(bytes) as T
        else
            GsonFactory.defaultGson.fromJson(String(bytes), type)
    }

    override fun setObject(key: String, value: Any, time: Long?, timeUnit: TimeUnit) {
        time?.apply {
            redisTemplate.opsForValue().set(key,value,time,timeUnit)
        }?:redisTemplate.opsForValue().set(key,value)
    }

    override fun expire(key: String, timeUnit: TimeUnit): Long {
        return redisTemplate.getExpire(key, timeUnit)
    }

    override fun hasKey(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }

    override fun keys(pattern: String): Collection<String>? {
        return redisTemplate.keys(pattern)
    }

    override fun delByPrefix(prefix: String) {
        val keys: Set<String> = redisTemplate.keys("${prefix}*")
        if (keys.isNotEmpty()) redisTemplate.delete(keys)
    }

    override fun del(vararg keys: String) {
        if (keys.isNotEmpty()){
            keys.toList().apply {
                redisTemplate.delete(this)
            }
        }
    }

    override fun getLock(key: String, releaseTime: Long): Boolean {
        val boo = redisTemplate.opsForValue().setIfAbsent(key, "", releaseTime, TimeUnit.SECONDS)
        return boo != null && boo
    }

    override fun releaseLock(key: String) {
        redisTemplate.delete(key)
    }

}