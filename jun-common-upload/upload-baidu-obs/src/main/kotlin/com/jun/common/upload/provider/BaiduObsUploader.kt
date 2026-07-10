package com.jun.common.upload.provider

import cn.hutool.crypto.digest.DigestUtil
import com.baidubce.auth.DefaultBceCredentials
import com.baidubce.services.bos.BosClient
import com.baidubce.services.bos.BosClientConfiguration
import com.baidubce.services.bos.model.ObjectMetadata
import com.jun.common.core.util.TempFileUtil
import com.jun.common.core.web.Resp
import com.jun.common.upload.AbstractUploader
import com.jun.common.upload.model.Media
import com.jun.common.upload.provider.config.BaiduObsUploadProperties
import java.io.*
import java.util.*


/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 17:10
 *
 * 百度智能云 BOS 对象存储上传实现，基于官方 Java SDK (com.baidubce:bce-java-sdk)。
 * 文档: https://cloud.baidu.com/doc/BOS/s/4jwvyrq6p
 **/
class BaiduObsUploader(private val properties: BaiduObsUploadProperties) :
    AbstractUploader(properties) {

    companion object {
        const val NAME = "baidu-obs"
        // 百度 BOS region 短码：bj(北京) gz(广州) su(苏州) cd(成都) hkg(香港) 等
        const val DEFAULT_REGION = "gz"
    }

    private fun getClient(): BosClient {
        val accessKey = properties.secretId
            ?: throw IllegalStateException("Missing Baidu BOS accessKey (secretId)")
        val secretKey = properties.secretKey
            ?: throw IllegalStateException("Missing Baidu BOS secretKey")
        val region = properties.region?.takeIf { it.isNotEmpty() } ?: DEFAULT_REGION

        val config = BosClientConfiguration()
        config.credentials = DefaultBceCredentials(accessKey, secretKey)
        config.endpoint = "$region.bcebos.com"

        return BosClient(config)
    }

    override fun upload(
        inputStream: InputStream,
        name: String,
        type: String,
        size: Long,
        createBy: String,
        contentType: String?
    ): Resp<Media?> {
        val bucket = properties.bucket ?: return Resp.fail("Invalid bos bucket")

        logger.info("bosBucket:$bucket")

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
                    val putObjectResponse = client.putObject(bucket, objectKey, input, objectMetadata)
                    logger.info("eTag:${putObjectResponse.eTag}")
                } finally {
                    client.shutdown()
                }
            }

            calculatedMd5 = DigestUtil.md5Hex(tempFile)

        } catch (e: Exception) {
            logger.error("BOS file upload failed: ${e.message}", e)
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
            val bosBucket = properties.bucket ?: return Resp.fail("Invalid bos bucket")

            val objectKey = if (properties.prefix?.isNotEmpty() == true && path.startsWith(properties.prefix!!)) {
                path.replaceFirst("/${properties.prefix}", "")
            } else
                path

            val client = getClient()
            val bosObject = client.getObject(bosBucket, objectKey)

            return Resp.success(bosObject.objectContent)

        } catch (e: Exception) {
            logger.error("BOS file download failed: ${e.message}", e)
            return Resp.fail("Download file failed:  ${e.message}")
        }
    }
}
