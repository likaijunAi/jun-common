package com.jun.common.upload.provider

import cn.hutool.crypto.digest.DigestUtil
import com.jun.common.core.util.TempFileUtil
import com.jun.common.core.web.Resp
import com.jun.common.upload.AbstractUploader
import com.jun.common.upload.model.Media
import com.jun.common.upload.provider.config.HuaweiObsUploadProperties
import com.obs.services.ObsClient
import com.obs.services.model.GetObjectRequest
import com.obs.services.model.ObjectMetadata
import com.obs.services.model.PutObjectRequest
import java.io.*
import java.util.*


/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 17:10
 *
 * 华为云 OBS 对象存储上传实现，基于官方 Java SDK (com.huaweicloud:obs-java)。
 * 文档: https://support.huaweicloud.com/sdk-java-devg-obs/obs_21_0001.html
 **/
class HuaweiObsUploader(private val properties: HuaweiObsUploadProperties) :
    AbstractUploader(properties) {

    companion object {
        const val NAME = "huawei-obs"
        // 华为云 OBS region，如 cn-north-4(北京四) cn-south-1(广州) cn-east-3(上海一) 等
        const val DEFAULT_REGION = "cn-north-4"
    }

    private fun getClient(): ObsClient {
        val accessKey = properties.secretId
            ?: throw IllegalStateException("Missing Huawei OBS accessKey (secretId)")
        val secretKey = properties.secretKey
            ?: throw IllegalStateException("Missing Huawei OBS secretKey")
        val region = properties.region?.takeIf { it.isNotEmpty() } ?: DEFAULT_REGION
        val endpoint = "https://obs.$region.myhuaweicloud.com"

        return ObsClient(accessKey, secretKey, endpoint)
    }

    override fun upload(
        inputStream: InputStream,
        name: String,
        type: String,
        size: Long,
        createBy: String,
        contentType: String?
    ): Resp<Media?> {
        val bucket = properties.bucket ?: return Resp.fail("Invalid obs bucket")

        logger.info("obsBucket:$bucket")

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
                    logger.info("eTag:${putObjectResult.etag}")
                } finally {
                    client.shutdown()
                }
            }

            calculatedMd5 = DigestUtil.md5Hex(tempFile)

        } catch (e: Exception) {
            logger.error("OBS file upload failed: ${e.message}", e)
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
            val obsBucket = properties.bucket ?: return Resp.fail("Invalid obs bucket")

            val objectKey = if (properties.prefix?.isNotEmpty() == true && path.startsWith(properties.prefix!!)) {
                path.replaceFirst("/${properties.prefix}", "")
            } else
                path

            val client = getClient()
            val getObjectRequest = GetObjectRequest(obsBucket, objectKey)
            val obsObject = client.getObject(getObjectRequest)

            return Resp.success(obsObject.objectContent)

        } catch (e: Exception) {
            logger.error("OBS file download failed: ${e.message}", e)
            return Resp.fail("Download file failed:  ${e.message}")
        }
    }
}
