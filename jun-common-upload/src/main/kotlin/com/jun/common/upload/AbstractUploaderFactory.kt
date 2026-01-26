package com.jun.common.upload

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 16:55
 **/
abstract class AbstractUploaderFactory(private val name: String) : UploaderFactory {
    private val cache = mutableMapOf<String, Uploader>()

    override fun createUploader(bucket: String): Uploader? {
        getUploader(bucket)?.apply {
            return this
        }
        val uploader = build(bucket)
        return uploader
    }

    override fun name(): String {
        return name
    }

    fun getUploader(bucket: String): Uploader? {
        return if (cache.containsKey(bucket)) cache[bucket] else null
    }

    fun setUploader(bucket: String, uploader: Uploader) {
        cache[bucket] = uploader
    }

    abstract fun build(bucket: String): Uploader?
}