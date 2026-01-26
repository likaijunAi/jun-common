package com.jun.common.upload

import cn.hutool.core.date.DateUtil
import com.jun.common.core.web.Resp
import com.jun.common.upload.config.UploadProviderProperties
import cn.hutool.core.lang.UUID
import com.jun.common.upload.model.Media
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 15:36
 **/
abstract class AbstractUploader(private val properties: UploadProviderProperties) : Uploader {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val dateFormat: String = "yyyy-MM"

    override fun verify(type: String, size: Long, contentType: String?): Resp<String?> {
        if (properties.maxSize != null && properties.maxSize!! > 0 && properties.maxSize!! < size) {
            return Resp.fail("Upload Limit (size)")
        }
        if (properties.type?.isNotEmpty() == true && properties.type?.contains(type.lowercase()) != true) {
            return Resp.fail("Upload Limit (type)")
        }
        return Resp.success()
    }

    fun splitBucket(): String {
        val bucket: String =
            DateUtil.format(Date(System.currentTimeMillis()), dateFormat)
        return bucket
    }

    fun mediaId(): String {
        return UUID.fastUUID().toString()
    }

    fun mediaName(): String {
        return UUID.fastUUID().toString()
    }

    open fun objectKey(bucket: String, mediaId: String, mediaName: String): String {
        return if (properties.splitBucket == 1) {
            "/$bucket/${splitBucket()}/$mediaId/$mediaName".replace("//", "/")
        } else
            "/$bucket/$mediaId/$mediaName".replace("//", "/")
    }

    open fun objectPath(objectKey: String): String {
        return if (properties.prefix?.isNotEmpty() == true) {
            "/${properties.prefix}$objectKey".replace(File.separator, "/")
        } else
            objectKey.replace(File.separator, "/")
    }

    fun isValidPathSegment(segment: String): Boolean {
        return segment.isNotEmpty() &&
                !segment.contains("..") &&
                !segment.startsWith("/") &&
                !segment.startsWith("../") &&
                !segment.startsWith("..\\") &&
                !segment.contains("\\..") &&
                !segment.contains("/../") &&
                !segment.contains("\\..") &&
                segment.none { it in listOf('<', '>', ':', '"', '|', '?', '*') }
    }

    fun isValidFileName(fileName: String): Boolean {
        if (fileName.isEmpty() || fileName.contains("/")) {
            return false
        }
        val normalizedFileName = fileName.replace("\\", "/")
        val parts = normalizedFileName.split("/")
        return parts.all { part ->
            part.isNotEmpty() &&
                    part != ".." &&
                    part != "." &&
                    !part.contains("<") && !part.contains(">") &&
                    !part.contains(":") && !part.contains('"') &&
                    !part.contains("|") && !part.contains('?') &&
                    !part.contains('*')
        }
    }
}