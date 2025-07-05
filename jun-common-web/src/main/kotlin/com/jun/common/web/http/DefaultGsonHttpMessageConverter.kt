package com.jun.common.web.http

import cn.hutool.core.io.IoUtil
import com.google.gson.Gson
import com.jun.common.core.web.GsonFactory
import com.jun.common.web.exception.ResolveException
import org.springframework.http.converter.json.GsonHttpMessageConverter
import java.io.InputStream
import java.io.Reader
import java.io.Writer
import java.lang.reflect.Type

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at  2024/6/1 22:44
 **/

class DefaultGsonHttpMessageConverter
@JvmOverloads
constructor(gson: Gson = GsonFactory.defaultGson) : GsonHttpMessageConverter(gson) {

    private val defaultSupportedMediaTypes: List<org.springframework.http.MediaType> by lazy {
        return@lazy listOf(
            org.springframework.http.MediaType.APPLICATION_JSON,
            org.springframework.http.MediaType.TEXT_PLAIN,
            org.springframework.http.MediaType.TEXT_XML,
            org.springframework.http.MediaType.APPLICATION_XML,
            org.springframework.http.MediaType.TEXT_HTML,
        )
    }

    init {
        supportedMediaTypes = defaultSupportedMediaTypes
    }

    override fun canRead(
        clazz: Class<*>,
        @org.springframework.lang.Nullable mediaType: org.springframework.http.MediaType?
    ): Boolean {
        return this.supports(clazz) && canRead(mediaType)
    }

    override fun canWrite(
        clazz: Class<*>,
        @org.springframework.lang.Nullable mediaType: org.springframework.http.MediaType?
    ): Boolean {
        return clazz != InputStream::class.java
    }

    @Throws(ResolveException::class)
    override fun readInternal(resolvedType: Type, reader: Reader): Any {
        try {
            val string = IoUtil.read(reader)
            return if (resolvedType === String::class.java) string else gson.fromJson<Any>(string, resolvedType)
        } catch (e: Exception) {
            throw ResolveException(e)
        }
    }

    @Throws(ResolveException::class)
    override fun writeInternal(
        `object`: Any,
        @org.springframework.lang.Nullable type: Type?,
        writer: Writer
    ) {
        try {
            if (`object` is String) {
                writer.write(`object`)
                writer.flush()
                return
            }
            gson.toJson(`object`, writer)
            writer.flush()
        } catch (e: Exception) {
            throw ResolveException(e)
        }

    }
}