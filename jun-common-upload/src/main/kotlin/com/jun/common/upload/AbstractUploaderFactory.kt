package com.jun.common.upload

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 16:55
 **/
abstract class AbstractUploaderFactory(private val dataType: String) : UploaderFactory {
    private val cache = mutableMapOf<String, Uploader>()

    override fun createUploader(name: String): Uploader? {
        getUploader(name)?.apply {
            return this
        }
        val uploader = build(name)
        return uploader
    }

    override fun dataType(): String {
        return dataType
    }

    private fun getUploader(name: String): Uploader? {
        return if (cache.containsKey(name)) cache[name] else null
    }

    fun setUploader(bucket: String, uploader: Uploader) {
        cache[bucket] = uploader
    }

    abstract fun build(name: String): Uploader?
}