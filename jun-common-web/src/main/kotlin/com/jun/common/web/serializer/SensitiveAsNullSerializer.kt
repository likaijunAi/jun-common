package com.jun.common.web.serializer

import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonPrimitive
import java.lang.reflect.Type

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/5/10 10:59
 **/

class SensitiveAsNullSerializer: JsonSerializer<String> {
    override fun serialize(src: String?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonPrimitive? {
        return null
    }
}