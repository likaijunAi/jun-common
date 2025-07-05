package com.jun.common.core.cache

import java.util.concurrent.TimeUnit

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/27 20:52
 **/
data class JunCacheKey(
    val prefix: String? = null,
    val expire: Long? = null,
    val timeUnit: TimeUnit = TimeUnit.SECONDS
) {
    fun key(key: String): String {
        return if (prefix?.isNotEmpty() == true)
            "$prefix:$key"
        else
            key
    }
}
