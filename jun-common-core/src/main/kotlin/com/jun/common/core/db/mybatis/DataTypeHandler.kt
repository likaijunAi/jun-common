package com.jun.common.core.db.mybatis

import com.google.gson.JsonParser
import com.jun.common.core.model.DataType
import org.slf4j.LoggerFactory

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 10:58
 **/
abstract class DataTypeHandler(clazz: Class<out DataType>, encryptKey: String? = null) :
    MyGsonTypeHandler<DataType>(clazz) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dataType = "dataType"

    init {
        encryptKey?.apply {
            setEncryptKey(this)
        }
    }

    override fun parse(json: String?): DataType? {
        if (json.isNullOrEmpty()) return null

        val jsonElement = try {
            JsonParser.parseString(json)
        } catch (e: Exception) {
            logger.error("", e)
            null
        }

        jsonElement ?: return null

        val name = jsonElement.asJsonObject.get(dataType)?.takeIf { it.isJsonPrimitive }?.asString
        if (name.isNullOrBlank()) {
            logger.warn("dataType is missing or invalid")
            return null
        }

        return parseToData(name, jsonStr = json)
    }

    abstract fun parseToData(name: String, jsonStr: String): DataType?
}