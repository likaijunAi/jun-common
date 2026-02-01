package com.jun.common.upload.provider

import cn.hutool.crypto.digest.DigestUtil
import com.jun.common.core.web.Resp
import com.jun.common.upload.AbstractUploader
import com.jun.common.upload.UploadManager
import com.jun.common.upload.model.Media
import com.jun.common.upload.provider.config.CosUploadProperties
import com.qcloud.cos.COSClient
import com.qcloud.cos.ClientConfig
import com.qcloud.cos.auth.BasicCOSCredentials
import com.qcloud.cos.http.HttpProtocol
import com.qcloud.cos.model.GetObjectRequest
import com.qcloud.cos.model.ObjectMetadata
import com.qcloud.cos.model.PutObjectRequest
import com.qcloud.cos.region.Region
import com.qcloud.cos.transfer.TransferManager
import java.io.*
import java.util.*
import java.util.concurrent.Executors


/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 17:10
 **/
class CosUploader(private val properties: CosUploadProperties) :
    AbstractUploader(properties) {

    companion object {
        const val NAME = "cos"
    }

    private val region = "ap-shanghai"
    private val threadPool = Executors.newFixedThreadPool(32)

    private fun getClient(): COSClient {
        val cred = BasicCOSCredentials(properties.secretId, properties.secretKey)
        val region = Region(properties.region?.takeIf { it.isNotEmpty() } ?: region)
        val clientConfig = ClientConfig(region)
        clientConfig.httpProtocol = HttpProtocol.http

        return COSClient(cred, clientConfig)
    }

    private fun <T> withTransferManager(block: (TransferManager) -> T): T {
        val tm = TransferManager(getClient(), threadPool)
        try {
            return block(tm)
        } finally {
            tm.shutdownNow(true)
        }
    }

    override fun upload(
        inputStream: InputStream,
        name: String,
        type: String,
        size: Long,
        createBy: String,
        contentType: String?
    ): Resp<Media?> {
        val bucket = properties.bucket ?: return Resp.fail("Invalid cos bucket")

        logger.info("cosBucket:$bucket")

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
        val tempFile = UploadManager.getTempFile()
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
                val putObjectRequest = PutObjectRequest(bucket, objectKey, input, objectMetadata)

                withTransferManager {
                    val upload = it.upload(putObjectRequest)
                    val uploadResult = upload.waitForUploadResult()
                    logger.info("eTag:${uploadResult.eTag}")
                }
            }

            calculatedMd5 = DigestUtil.md5Hex(tempFile)

        } catch (e: Exception) {
            logger.error("COS file upload failed: ${e.message}", e)
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
            val cosBucket = properties.bucket ?: return Resp.fail("Invalid cos bucket")

            val objectKey = if (properties.prefix?.isNotEmpty() == true && path.startsWith(properties.prefix!!)) {
                path.replaceFirst("/${properties.prefix}", "")
            } else
                path

            val getObjectRequest = GetObjectRequest(cosBucket, objectKey)
            val client = getClient()
            val cosObject = client.getObject(getObjectRequest)

            return Resp.success(cosObject.objectContent)

        } catch (e: Exception) {
            logger.error("COS file download failed: ${e.message}", e)
            return Resp.fail("Download file failed:  ${e.message}")
        }
    }
}