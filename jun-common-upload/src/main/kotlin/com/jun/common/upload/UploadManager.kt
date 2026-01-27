package com.jun.common.upload

import com.jun.common.core.web.Resp
import com.jun.common.upload.event.UploadEvent
import com.jun.common.upload.model.Media
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
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
    private val uploaderFactories: List<UploaderFactory>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val defaultTempDir = File(System.getProperty("user.dir"), "temp")

    companion object {
        const val TEMP_ROOT = "jun.uploader.temp.dir"
    }

    private val listeners = mutableListOf<UploadListener>()

    fun addListener(listener: UploadListener) {
        logger.info("$listener")
        listeners.add(listener)
    }

    fun removeListener(listener: UploadListener) {
        logger.info("$listener")
        listeners.remove(listener)
    }

    private fun findUploader(name: String? = null, bucket: String): Uploader? {
        if (name.isNullOrEmpty())
            return findUploaderByBucket(bucket)
        return uploaderFactories.firstOrNull { it.name() == name }?.createUploader(bucket)
    }

    private fun findUploaderByBucket(bucket: String): Uploader? {
        return uploaderFactories.firstNotNullOfOrNull { it.createUploader(bucket) }
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

    @JvmOverloads
    fun verify(
        name: String? = null,
        bucket: String,
        type: String,
        size: Long,
        contentType: String? = "application/octet-stream"
    ): Resp<String?> {
        logger.info("$name;$bucket;$type;$size;$contentType")
        val factory =
            findUploader(name, bucket) ?: return Resp.fail("Can't find uploader by ({name:$name;bucket:$bucket})")
        return factory.verify(type, size, contentType)
    }

    @JvmOverloads
    fun upload(
        name: String? = null,
        bucket: String,
        inputStream: InputStream,
        fileName: String,
        type: String,
        size: Long,
        contentType: String? = "application/octet-stream",
        createBy: String
    ): Resp<Media?> {
        logger.info("$name;$bucket;$type;$size;$contentType;$createBy")
        val factory =
            findUploader(name, bucket) ?: return Resp.fail("Can't find uploader by ({name:$name;bucket:$bucket})")

        val resp = factory.upload(
            inputStream, fileName, type, size, createBy, (if (contentType?.isNotEmpty() == true)
                contentType
            else
                "application/octet-stream")
        )
        val media = resp.result
        val event = UploadEvent(media != null, resp.error, media)
        listeners.forEach {
            it.onUpload(event)
        }
        return resp
    }

    @JvmOverloads
    fun getInputStream(name: String? = null, bucket: String, path: String): Resp<InputStream?> {
        logger.info("$name;$bucket;$path")
        val factory =
            findUploader(name, bucket) ?: return Resp.fail("Can't find uploader by ({name:$name;bucket:$bucket})")
        return factory.getInputStream(path)
    }

    @JvmOverloads
    fun upload(
        name: String? = null,
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

        return upload(name, bucket, file.inputStream, fileName ?: "", type, size, contentType, createBy)
    }
}