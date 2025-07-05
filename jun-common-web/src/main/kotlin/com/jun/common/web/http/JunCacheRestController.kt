package com.jun.common.web.http

import com.jun.common.core.cache.JunCache

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/17 20:00
 **/
abstract class JunCacheRestController<T>(protected val junCache: JunCache) : JunCommonRestController<T>() {

    fun getUserInfo(clazz: Class<T>): T? {
        return runCatching {
            val key = getVerifyDefId() ?: return null
            return junCache.getObject(key, clazz)
        }.onFailure {
            logger.error("getUserInfo", it)
        }.getOrNull()
    }

    abstract fun getUserInfo(): T?

    override fun getReq(): T? {
        return null
    }
}