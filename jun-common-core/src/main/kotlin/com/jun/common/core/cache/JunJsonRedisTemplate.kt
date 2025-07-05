package com.jun.common.core.cache

import com.jun.common.core.web.GsonFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.SerializationException
import java.nio.charset.StandardCharsets

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/26 22:39
 **/
class JunJsonRedisTemplate(connectionFactory: RedisConnectionFactory) :
    RedisTemplate<String, Any>() {
    init {
        setConnectionFactory(connectionFactory)
        keySerializer = RedisSerializer.string()
        valueSerializer = JunJsonRedisSerializer()
        hashKeySerializer = RedisSerializer.string()
        hashValueSerializer = RedisSerializer.string()
    }

    companion object {
        val EMPTY_ARRAY = ByteArray(0)
        fun isEmpty(data: ByteArray?): Boolean {
            return data?.isEmpty() ?: true
        }

        fun intToByteArray(a: Int): ByteArray {
            return byteArrayOf(
                (a shr 24 and 0xFF).toByte(),
                (a shr 16 and 0xFF).toByte(),
                (a shr 8 and 0xFF).toByte(),
                (a and 0xFF).toByte()
            )
        }

        fun byteArrayToInt(b: ByteArray): Int {
            return  b[3].toInt() and 0xFF or
                    (b[2].toInt() and 0xFF shl 8) or
                    (b[1].toInt() and 0xFF shl 16) or
                    (b[0].toInt() and 0xFF shl 24)
        }
    }

    class JunJsonRedisSerializer : RedisSerializer<Any> {

        @Throws(SerializationException::class)
        override fun serialize(o: Any?): ByteArray {
            if (o == null) return EMPTY_ARRAY
            return when (o) {
                is String -> {
                    o.toByteArray(StandardCharsets.UTF_8)
                }

                is Int -> {
                    intToByteArray(o)
                }

                else -> {
                    GsonFactory.defaultGson.toJson(o).toByteArray()
                }
            }
        }

        @Throws(SerializationException::class)
        override fun deserialize(bytes: ByteArray?): Any? {
            return if (isEmpty(bytes)) {
                null
            } else {
                bytes
            }
        }

        override fun canSerialize(type: Class<*>): Boolean {
            return true
        }

    }

}