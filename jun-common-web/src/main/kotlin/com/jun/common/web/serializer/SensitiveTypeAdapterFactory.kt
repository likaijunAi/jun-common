package com.jun.common.web.serializer

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import com.jun.common.web.annotation.Sensitive
import com.jun.common.web.annotation.SensitiveType
import com.jun.common.core.util.DesensitizationUtil
import com.jun.common.core.web.GsonFactory
import com.jun.common.web.annotation.SensitiveData

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/5/11 22:57
 **/

class SensitiveTypeAdapterFactory : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType.getAnnotation(SensitiveData::class.java) == null) return null

        return object : TypeAdapter<T>() {
            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: T) {
                val jsonObject = GsonFactory.defaultGson.toJsonTree(value).asJsonObject
                val classType = type.rawType
                for (field in cn.hutool.core.util.ReflectUtil.getFields(classType) ) {
                    field.isAccessible = true
                    val annotation = field.getAnnotation(Sensitive::class.java)
                    if (annotation != null) {
                        val fieldName = field.name
                        if (jsonObject.has(fieldName)) {
                            val fieldValue = jsonObject.get(fieldName).asString
                            val maskedValue = when (annotation.type) {
                                SensitiveType.MOBILE -> DesensitizationUtil.maskMobile(fieldValue)
                                SensitiveType.ID_CARD -> DesensitizationUtil.maskIdCard(fieldValue)
                                SensitiveType.EMAIL -> DesensitizationUtil.maskEmail(fieldValue)
                                SensitiveType.CUSTOM -> "******"
                                SensitiveType.PASSWORD -> null
                            }
                            if (maskedValue == null) {
                                jsonObject.remove(fieldName)
                            } else {
                                jsonObject.addProperty(fieldName, maskedValue)
                            }
                        }
                    }
                }
                gson.toJson(jsonObject, out)
            }

            @Throws(IOException::class)
            override fun read(`in`: JsonReader): T {
                return GsonFactory.defaultGson.fromJson(`in`, type.type)
            }
        }
    }
}