package com.jun.common.upload.utils

import com.jun.common.core.web.Resp
import com.jun.common.upload.UploadManager
import com.jun.common.upload.model.Media
import com.jun.common.upload.model.UploadObject
import org.springframework.http.MediaTypeFactory
import org.springframework.util.MimeTypeUtils
import java.io.File
import java.net.URLConnection
import java.nio.file.Files

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/2/3 13:44
 **/
object UploadUtils {

    fun File.upload(
        manager: UploadManager,
        dataName: String,
        dataType: String? = null,
        createdBy: String? = null
    ): Resp<Media?> {
        val uploadObject = this.toUploadObject(dataName, dataType, createdBy)
        return manager.upload(uploadObject)
    }

    private fun File.toUploadObject(dataName: String, dataType: String?, createdBy: String? = null): UploadObject {
        require(exists()) { "File does not exist: $absolutePath" }
        require(isFile) { "Path is not a file: $absolutePath" }
        require(canRead()) { "File cannot be read: $absolutePath" }

        val contentType = determineContentType()

        return UploadObject(
            dataName = dataName,
            dataType = dataType,
            inputStream = inputStream(),
            createdBy = createdBy,
            contentType = contentType,
            name = name,
            size = length(),
            type = extension.lowercase()
        )
    }

    private fun File.determineContentType(): String {
        MediaTypeFactory.getMediaType(name)
            .map { it.toString() }
            .orElse(null)
            ?.let { return it }

        runCatching { Files.probeContentType(toPath()) }
            .getOrNull()
            ?.let { return it }

        URLConnection.guessContentTypeFromName(name)
            ?.let { return it }

        return MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE
    }
}