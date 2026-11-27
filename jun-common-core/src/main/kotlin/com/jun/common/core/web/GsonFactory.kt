package com.jun.common.core.web

import cn.hutool.core.codec.Base64Decoder
import cn.hutool.core.codec.Base64Encoder
import cn.hutool.core.date.DatePattern
import cn.hutool.core.date.DateUtil
import com.google.gson.*
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2024/6/1 22:44
 **/

object GsonFactory {

    val defaultGson: Gson by lazy {
        defaultBuilder().disableHtmlEscaping().create()
    }

    fun defaultBuilder(): GsonBuilder {
        val builder = GsonBuilder()

        // Date 反序列化
        builder.registerTypeAdapter(
            Date::class.java,
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                val jsonPrimitive = json.asJsonPrimitive ?: return@JsonDeserializer null
                if (jsonPrimitive.asString.isEmpty()) {
                    return@JsonDeserializer null
                }
                if (jsonPrimitive.isNumber)
                    return@JsonDeserializer Date(jsonPrimitive.asLong)
                else if (jsonPrimitive.isString) {
                    val ss = jsonPrimitive.asString
                    if (ss.isEmpty() || ss.startsWith("0000-00-00") || "null" == ss)
                        return@JsonDeserializer null
                    if (ss.indexOf(":") != -1) {
                        return@JsonDeserializer DateUtil.parse(
                            jsonPrimitive.asString,
                            DatePattern.NORM_DATETIME_PATTERN,
                            DatePattern.NORM_DATETIME_MINUTE_PATTERN
                        )
                    }
                    return@JsonDeserializer DateUtil.parse(
                        jsonPrimitive.asString,
                        DatePattern.NORM_DATE_PATTERN
                    )
                }
                null
            })

        // Int 反序列化
        builder.registerTypeAdapter(
            Int::class.java,
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                if (json.asJsonPrimitive == null) {
                    return@JsonDeserializer null
                }
                if (json.asJsonPrimitive.asString.isEmpty()) {
                    return@JsonDeserializer null
                }
                json.asJsonPrimitive.asInt
            })

        // Integer 反序列化
        builder.registerTypeAdapter(
            java.lang.Integer::class.java,
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                if (json.asJsonPrimitive == null) {
                    return@JsonDeserializer null
                }
                if (json.asJsonPrimitive.asString.isEmpty()) {
                    return@JsonDeserializer null
                }
                json.asJsonPrimitive.asInt
            })

        // ByteArray 反序列化
        builder.registerTypeAdapter(
            ByteArray::class.java,
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                Base64Decoder.decode(json.asString.toByteArray(StandardCharsets.UTF_8))
            })

        // ByteArray 序列化
        builder.registerTypeAdapter(
            ByteArray::class.java,
            JsonSerializer { arg0: ByteArray, _: Type?, _: JsonSerializationContext? ->
                JsonPrimitive(
                    Base64Encoder.encode(arg0)
                )
            })

        // Timestamp 反序列化
        builder.registerTypeAdapter(
            Timestamp::class.java,
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                Timestamp(
                    json.asJsonPrimitive.asLong
                )
            } as JsonDeserializer<Timestamp>)

        // Date 序列化
        builder.registerTypeAdapter(
            Date::class.java,
            JsonSerializer { arg0: Date?, _: Type?, _: JsonSerializationContext? ->
                if (arg0 != null) JsonPrimitive(
                    arg0.time
                ) else null
            })

        // Timestamp 序列化
        builder.registerTypeAdapter(
            Timestamp::class.java,
            JsonSerializer { arg0: Timestamp?, _: Type?, _: JsonSerializationContext? ->
                if (arg0 != null) JsonPrimitive(
                    arg0.time
                ) else null
            })

        // LocalDateTime 反序列化
        builder.registerTypeAdapter(
            LocalDateTime::class.java,
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                val jsonPrimitive = json.asJsonPrimitive ?: return@JsonDeserializer null
                if (jsonPrimitive.asString.isEmpty()) {
                    return@JsonDeserializer null
                }

                val str = jsonPrimitive.asString
                if (str.isEmpty() || str == "null") {
                    return@JsonDeserializer null
                }

                // 如果是数字（时间戳）
                if (jsonPrimitive.isNumber) {
                    try {
                        val timestamp = jsonPrimitive.asLong
                        return@JsonDeserializer LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(timestamp),
                            java.time.ZoneId.systemDefault()
                        )
                    } catch (e: Exception) {
                        return@JsonDeserializer null
                    }
                }

                // 如果是字符串，尝试多种格式解析
                try {
                    // 尝试解析标准日期时间格式
                    return@JsonDeserializer LocalDateTime.parse(
                        str,
                        DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)
                    )
                } catch (e: Exception) {
                    try {
                        // 尝试解析 ISO 格式
                        return@JsonDeserializer LocalDateTime.parse(str)
                    } catch (e2: Exception) {
                        try {
                            // 尝试解析带毫秒的格式
                            return@JsonDeserializer LocalDateTime.parse(
                                str,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                            )
                        } catch (e3: Exception) {
                            return@JsonDeserializer null
                        }
                    }
                }
            })

        // LocalDateTime 序列化
        builder.registerTypeAdapter(
            LocalDateTime::class.java,
            JsonSerializer { localDateTime: LocalDateTime?, _: Type?, _: JsonSerializationContext? ->
                if (localDateTime != null) {
                    JsonPrimitive(localDateTime.format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)))
                } else {
                    null
                }
            })

        return builder
    }
}