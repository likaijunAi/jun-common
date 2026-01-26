package com.jun.common.upload

import com.jun.common.core.web.Resp
import com.jun.common.upload.event.UploadEvent
import com.jun.common.upload.model.Media
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 11:56
 **/
class UploadManager(
    private val applicationContext: ApplicationContext,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val defaultTempDir = File(System.getProperty("user.dir"), "temp")

    companion object {
        const val TEMP_ROOT = "jun.uploader.temp.dir"
    }

    private val uploaderFactories by lazy {
        val list = mutableListOf<UploaderFactory>()
        applicationContext.getBeanNamesForType(UploaderFactory::class.java).apply {
            if (this.isNotEmpty()) {
                for (name in this) {
                    applicationContext.getBean(name, UploaderFactory::class.java).apply {
                        list.add(this)
                    }
                }
            }
        }
        list
    }

    private fun findUploader(name: String, bucket: String): Uploader? {
        return uploaderFactories.firstOrNull { it.name() == name }?.createUploader(bucket)
    }

    private fun tempRoot(): File {
        val path = System.getProperty(TEMP_ROOT) ?: defaultTempDir.absolutePath
        return File(path).apply {
            if (!this.exists()) {
                this.mkdirs()
            }
        }
    }

    fun getTempFile(isDirectory: Boolean = false): File {
        val name = "temp_${System.currentTimeMillis()}"
        val temp = File(tempRoot(), name)
        if (isDirectory) {
            temp.mkdir()
        } else {
            temp.createNewFile()
        }
        temp.deleteOnExit()
        return temp
    }

    fun verify(
        name: String,
        bucket: String,
        type: String,
        size: Long,
        contentType: String? = "application/octet-stream"
    ): Resp<String?> {
        logger.info("$name;$bucket;$type;$size;$contentType")
        val factory = findUploader(name, bucket) ?: return Resp.fail("Can't uploader by ({name:$name;bucket:$bucket})")
        return factory.verify(type, size, contentType)
    }

    fun upload(
        name: String,
        bucket: String,
        inputStream: InputStream,
        type: String,
        size: Long,
        contentType: String? = "application/octet-stream",
        createBy: String
    ): Resp<Media?> {
        logger.info("$name;$bucket;$type;$size;$contentType;$createBy")
        val factory = findUploader(name, bucket) ?: return Resp.fail("Can't uploader by ({name:$name;bucket:$bucket})")

        val resp = factory.upload(
            inputStream, name, type, size, if (contentType?.isNotEmpty() == true)
                contentType
            else
                "application/octet-stream", createBy
        )
        val media = resp.result
        val event = UploadEvent(media != null, resp.error, media)
        applicationEventPublisher.publishEvent(event)
        return resp
    }

    fun getInputStream(name: String, bucket: String, path: String): Resp<InputStream?> {
        logger.info("$name;$bucket;$path")
        val factory = findUploader(name, bucket) ?: return Resp.fail("Can't uploader by ({name:$name;bucket:$bucket})")
        return factory.getInputStream(path)
    }

    fun upload(
        name: String,
        bucket: String,
        createBy: String,
        file: MultipartFile
    ): Resp<Media?> {
        logger.info("$name;$bucket;$createBy")
        logger.info("upload (${file.originalFilename})")
        if (file.isEmpty) {
            return Resp.fail("File is is empty")
        }
        val contentType = file.contentType ?: "unknown"
        val fileName = file.originalFilename
        val size = file.size
        val type = fileName?.substringAfterLast('.', "") ?: ""

        val resp = verify(name, bucket, type, size, contentType)
        if (!resp.isSuccess())
            return Resp.fail(resp)

        return upload(name, bucket, file.inputStream, type, size, contentType, createBy)
    }
}