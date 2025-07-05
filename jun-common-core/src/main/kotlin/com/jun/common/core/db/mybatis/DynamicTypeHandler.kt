package com.jun.common.core.db.mybatis


import com.google.gson.*
import com.jun.common.core.model.DynamicType
import com.jun.common.core.web.GsonFactory
import java.lang.reflect.Field
import java.lang.reflect.Type

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/2/17 17:56
 **/
abstract class DynamicTypeHandler : MyGsonTypeHandler<DynamicType> {
    private val gson: Gson = getGson({ typeName() }, { type: JsonPrimitive -> type(type) })

    constructor(clazz: Class<*>) : super(clazz)
    constructor(clazz: Class<*>, field: Field) : super(clazz, field)

    companion object {
        fun getGson(typeName: () -> String, type: (JsonPrimitive) -> Class<out DynamicType>?): Gson {
            return GsonFactory.defaultBuilder()
                .registerTypeAdapter(
                    DynamicType::class.java,
                    object : JsonDeserializer<DynamicType> {
                        override fun deserialize(
                            json: JsonElement?,
                            typeOfT: Type?,
                            context: JsonDeserializationContext?
                        ): DynamicType? {
                            json?.asJsonObject?.apply {
                                val dataClazz = getAsJsonPrimitive(typeName())?.let {
                                    type(it)
                                }
                                if (dataClazz != null) {
                                    return GsonFactory.defaultGson.fromJson(this, dataClazz)
                                }
                            }
                            return null
                        }
                    }
                ).disableHtmlEscaping().create()
        }
    }

    abstract fun type(type: JsonPrimitive): Class<out DynamicType>?

    open fun typeName(): String {
        return "type"
    }

    override fun parse(json: String?): DynamicType? {
        if (json.isNullOrEmpty()) return null
        return gson.fromJson(json, DynamicType::class.java)
    }

    override fun toJson(obj: DynamicType?): String? {
        return obj?.let {  gson.toJson(it) }
    }
}