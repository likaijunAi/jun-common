package com.jun.common.core.db.mybatis

import com.baomidou.mybatisplus.core.toolkit.StringUtils
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler
import java.lang.reflect.Field
import com.google.gson.*
import com.jun.common.core.util.AesUtil
import org.apache.ibatis.type.JdbcType
import java.lang.reflect.Type
import java.sql.*
import java.util.*
import java.util.Date

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/12/6 22:49
 **/
abstract class MyGsonTypeHandler<T> : AbstractJsonTypeHandler<T> {
    private var encryptKey: String? = null

    constructor(clazz: Class<*>) : super(clazz)
    constructor(clazz: Class<*>, field: Field) : super(clazz, field)
    fun setEncryptKey(encryptKey: String) {
        this.encryptKey = encryptKey
    }
    override fun parse(json: String?): T? {
        if (json.isNullOrEmpty()) return null

        return gson.fromJson<T>(json, super.type)
    }

    override fun toJson(obj: T?): String? {
        return obj?.let { gson.toJson(it) }
    }

    companion object {
        private val gson = gson()

        private fun gson(): Gson {
            val builder = GsonBuilder()
            builder.registerTypeAdapter(Date::class.java,
                JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                    if (json.asJsonPrimitive == null) {
                        return@JsonDeserializer null
                    }
                    if (json.asJsonPrimitive.asString.isEmpty()) {
                        return@JsonDeserializer null
                    }
                    Date(json.asJsonPrimitive.asLong)
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
                Timestamp::class.java,
                JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                    Timestamp(
                        json.asJsonPrimitive.asLong
                    )
                })
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
            return builder.disableHtmlEscaping().create()
        }
    }

    @Throws(SQLException::class)
    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: T?, jdbcType: JdbcType?) {
        if (parameter == null) {
            ps.setString(i, null)
            return
        }
        val result = this.toJson(parameter)?.let {
            if (encryptKey?.isNotEmpty() == true) {
                AesUtil.encrypt(it, encryptKey!!)
            } else it
        }
        ps.setString(i, result)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnName: String?): T? {
        val json = rs.getString(columnName)?.let {
            if (encryptKey?.isNotEmpty() == true) {
                AesUtil.decrypt(it, encryptKey!!)
            } else it
        }
        return if (StringUtils.isBlank(json)) null else this.parse(json)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): T? {
        val json = rs.getString(columnIndex)?.let {
            if (encryptKey?.isNotEmpty() == true) {
                AesUtil.decrypt(it, encryptKey!!)
            } else it
        }
        return if (StringUtils.isBlank(json)) null else this.parse(json)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): T? {
        val json = cs.getString(columnIndex)?.let {
            if (encryptKey?.isNotEmpty() == true) {
                AesUtil.decrypt(it, encryptKey!!)
            } else it
        }
        return if (StringUtils.isBlank(json)) null else this.parse(json)
    }
}