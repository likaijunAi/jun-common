package com.jun.common.upload.provider

import cn.hutool.crypto.digest.DigestUtil
import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.model.ObjectMetadata
import com.aliyun.oss.model.PutObjectRequest
import com.jun.common.core.util.TempFileUtil
import com.jun.common.core.web.Resp
import com.jun.common.upload.AbstractUploader
import com.jun.common.upload.model.Media
import com.jun.common.upload.provider.config.AliOssUploadProperties
import java.io.*
import java.util.*


/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 17:10
 *
 * 阿里云 OSS 对象存储上传实现，基于官方 Java SDK (com.aliyun.oss:aliyun-sdk-oss)。
 * 文档: https://help.aliyun.com/zh/oss/developer-reference/oss-sdk-for-java
 **/
class AliOssUploader(private val properties: AliOssUploadProperties) :
    AbstractUploader(properties) {

    companion object {
        const val NAME = "ali-oss"
        // 阿里云 OSS region，如 cn-hangzhou(华东1) cn-beijing(华北2) 等
        const val DEFAULT_REGION = "cn-hangzhou"
    }

    private fun getClient(): OSS {
        val accessKey = properties.secretId
            ?: throw IllegalStateException("Missing Aliyun OSS accessKey (secretId)")
        val secretKey = properties.secretKey
            ?: throw IllegalStateException("Missing Aliyun OSS secretKey")
        val region = properties.region?.takeIf { it.isNotEmpty() } ?: DEFAULT_REGION
        val endpoint = "https://oss-$region.aliyuncs.com"

        return OSSClientBuilder().build(endpoint, accessKey, secretKey)
    }

    override fun upload(
        inputStream: InputStream,
        name: String,
        type: String,
        size: Long,
        createBy: String,
        contentType: String?
    ): Resp<Media?> {
        val bucket = properties.bucket ?: return Resp.fail("Invalid oss bucket")

        logger.info("ossBucket:$bucket")

        val mediaName = name.takeIf { it.trim().isNotEmpty() } ?: mediaName()
        if (!isValidFileName(mediaName)) {
            return Resp.fail("Invalid file name")
        }

        if (!isValidPathSegment(bucket)) {
            return Resp.fail("Invalid bucket name")
        }

        val mediaId = mediaId()
        if (!isValidPathSegment(mediaId)) {
            return Resp.fail("Invalid media ID")
        }
        val objectKey = objectKey(mediaId, mediaName)
        val actualSize: Long
        val calculatedMd5: String
        val tempFile = TempFileUtil.getTempFile()
        try {
            inputStream.use {
                FileOutputStream(tempFile).use { out ->
                    actualSize = it.copyTo(out)
                }
            }

            val objectMetadata = ObjectMetadata()

            if (contentType?.isNotEmpty() == true)
                objectMetadata.contentType = contentType
            objectMetadata.contentLength = actualSize

            FileInputStream(tempFile).use { input ->
                val client = getClient()
                try {
                    val putObjectRequest = PutObjectRequest(bucket, objectKey, input, objectMetadata)
                    val putObjectResult = client.putObject(putObjectRequest)
                    logger.info("eTag:${putObjectResult.eTag}")
                } finally {
                    client.shutdown()
                }
            }

            calculatedMd5 = DigestUtil.md5Hex(tempFile)

        } catch (e: Exception) {
            logger.error("OSS file upload failed: ${e.message}", e)
            return Resp.fail("Upload file failed: ${e.message}")
        } finally {
            try {
                tempFile.exists() && tempFile.delete()
            } catch (e: Exception) {
                logger.warn("Failed to delete temp file: ${tempFile.absolutePath}", e)
            }
        }

        val media = Media(NAME)
        media.mediaId = mediaId
        media.name = mediaName
        media.bucket = properties.name
        media.contentType = contentType
        media.dataType = NAME
        media.size = actualSize
        media.type = type
        media.createdBy = createBy
        media.createdAt = Date()
        media.md5 = calculatedMd5
        media.path = objectPath(objectKey)

        return Resp.success(media)

    }

    override fun getInputStream(path: String): Resp<InputStream?> {
        try {
            val ossBucket = properties.bucket ?: return Resp.fail("Invalid oss bucket")

            val objectKey = if (properties.prefix?.isNotEmpty() == true && path.startsWith(properties.prefix!!)) {
                path.replaceFirst("/${properties.prefix}", "")
            } else
                path

            val client = getClient()
            val ossObject = client.getObject(ossBucket, objectKey)

            return Resp.success(ossObject.objectContent)

        } catch (e: Exception) {
            logger.error("OSS file download failed: ${e.message}", e)
            return Resp.fail("Download file failed:  ${e.message}")
        }
    }
}
