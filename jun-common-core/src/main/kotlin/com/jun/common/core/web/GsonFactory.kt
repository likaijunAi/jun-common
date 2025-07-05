package com.jun.common.core.web

import cn.hutool.core.codec.Base64Decoder
import cn.hutool.core.codec.Base64Encoder
import cn.hutool.core.date.DatePattern
import cn.hutool.core.date.DateUtil
import com.google.gson.*
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
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
        builder.registerTypeAdapter(
            ByteArray::class.java,
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                Base64Decoder.decode(json.asString.toByteArray(StandardCharsets.UTF_8))
            })
        builder.registerTypeAdapter(
            ByteArray::class.java,
            JsonSerializer { arg0: ByteArray, _: Type?, _: JsonSerializationContext? ->
                JsonPrimitive(
                    Base64Encoder.encode(arg0)
                )
            })
        builder.registerTypeAdapter(
            Timestamp::class.java,
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                Timestamp(
                    json.asJsonPrimitive.asLong
                )
            } as JsonDeserializer<Timestamp>)
        builder.registerTypeAdapter(
            Date::class.java,
            JsonSerializer { arg0: Date?, _: Type?, _: JsonSerializationContext? ->
                if (arg0 != null) JsonPrimitive(
                    arg0.time
                ) else null
            })
        builder.registerTypeAdapter(
            Timestamp::class.java,
            JsonSerializer { arg0: Timestamp?, _: Type?, _: JsonSerializationContext? ->
                if (arg0 != null) JsonPrimitive(
                    arg0.time
                ) else null
            })
        return builder
    }
}