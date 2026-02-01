package com.jun.common.upload

import com.jun.common.core.web.Resp
import com.jun.common.upload.event.UploadEvent
import com.jun.common.upload.model.Media
import org.slf4j.LoggerFactory
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

    companion object {
        const val TEMP_ROOT = "jun.uploader.temp.dir"
        private val defaultTempDir = File(System.getProperty("user.dir"), "temp")

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

    private fun findUploader(dataType: String? = null, dataName: String): Uploader? {
        if (dataType.isNullOrEmpty())
            return findUploaderByBucket(dataName)
        return uploaderFactories.firstOrNull { it.dataType() == dataType }?.createUploader(dataName)
    }

    private fun findUploaderByBucket(bucket: String): Uploader? {
        return uploaderFactories.firstNotNullOfOrNull { it.createUploader(bucket) }
    }

    @JvmOverloads
    fun verify(
        dataType: String? = null,
        dataName: String,
        type: String,
        size: Long,
        contentType: String? = "application/octet-stream"
    ): Resp<String?> {
        logger.info("$dataType;$dataName;$type;$size;$contentType")
        val factory =
            findUploader(dataType, dataName)
                ?: return Resp.fail("Can't find uploader by ({dataType:$dataType;bucket:$dataName})")
        return factory.verify(type, size, contentType)
    }

    @JvmOverloads
    fun upload(
        dataType: String? = null,
        dataName: String,
        inputStream: InputStream,
        fileName: String,
        type: String,
        size: Long,
        contentType: String? = "application/octet-stream",
        createBy: String
    ): Resp<Media?> {
        logger.info("$dataType;$dataName;$type;$size;$contentType;$createBy")
        val cloneStream = CloseAwareInputStream(inputStream = inputStream)
        return fun(): Resp<Media?> {
            val factory =
                findUploader(dataType, dataName)
                    ?: return Resp.fail("Can't find uploader by ({name:$dataType;dataName:$dataName})")

            val resp = factory.upload(
                cloneStream, fileName, type, size, createBy, (if (contentType?.isNotEmpty() == true)
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
        }().apply {
            try {
                if (!cloneStream.isClosed()) {
                    cloneStream.close()
                }
            } catch (_: Exception) {
            }
        }
    }

    @JvmOverloads
    fun getInputStream(dataType: String? = null, dataName: String, path: String): Resp<InputStream?> {
        logger.info("$dataType;$dataName;$path")
        val factory =
            findUploader(dataType, dataName)
                ?: return Resp.fail("Can't find uploader by ({dataType:$dataType;dataName:$dataName})")
        return factory.getInputStream(path)
    }

    @JvmOverloads
    fun upload(
        dataType: String? = null,
        bucket: String,
        createBy: String,
        file: MultipartFile
    ): Resp<Media?> {
        logger.info("$dataType;$bucket;$createBy")
        logger.info("upload (${file.originalFilename})")
        if (file.isEmpty) {
            return Resp.fail("File is is empty")
        }
        val contentType = file.contentType ?: "unknown"
        val fileName = file.originalFilename
        val size = file.size
        val type = fileName?.substringAfterLast('.', "") ?: ""

        val resp = verify(dataType, bucket, type, size, contentType)
        if (!resp.isSuccess())
            return Resp.fail(resp)

        val inputStream = CloseAwareInputStream(inputStream = file.inputStream)

        return upload(dataType, bucket, inputStream, fileName ?: "", type, size, contentType, createBy).apply {
            try {
                if (!inputStream.isClosed()) {
                    inputStream.close()
                }
            } catch (_: Exception) {
            }
        }
    }
}