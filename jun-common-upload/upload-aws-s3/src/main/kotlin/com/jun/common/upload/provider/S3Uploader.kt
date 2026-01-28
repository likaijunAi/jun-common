package com.jun.common.upload.provider

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.jun.common.core.web.Resp
import com.jun.common.upload.AbstractUploader
import com.jun.common.upload.model.Media
import com.jun.common.upload.provider.config.S3UploadProperties
import java.io.File
import java.io.InputStream
import java.util.Date

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 17:10
 **/
class S3Uploader(private val bucket: String, private val properties: S3UploadProperties) :
    AbstractUploader(properties) {

    companion object {
        const val NAME = "s3"
    }

    private fun getClient(): AmazonS3 {
        val awsCredentials = BasicAWSCredentials(properties.accessKeyId, properties.secretAccessKey)
        val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(properties.endpoint, properties.region)


        return AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(endpointConfiguration)
            .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
            .withPathStyleAccessEnabled(properties.pathStyleAccessEnabled)
            .build()
    }

    override fun upload(
        inputStream: InputStream,
        name: String,
        type: String,
        size: Long,
        createBy: String,
        contentType: String?
    ): Resp<Media?> {

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
        val objectKey = objectKey(bucket, mediaId, mediaName)
        logger.info("objectKey:$objectKey")
        var calculatedMd5: String?
        try {
            val objectMetadata = ObjectMetadata().apply {
                this.contentLength = size
                if (contentType?.isNotEmpty() == true) {
                    this.contentType = contentType
                }
            }
            val client = getClient()
            inputStream.use { input ->

                val putObjectRequest = PutObjectRequest(bucket, objectKey, input, objectMetadata)

                val putObjectResult = client.putObject(putObjectRequest)
                calculatedMd5 = putObjectResult.metadata.eTag?.replace("\"", "")
            }
        } catch (e: AmazonServiceException) {
            logger.error("R3 file upload failed: ${e.message}", e)
            return Resp.fail("Upload file failed: ${e.message}")
        } catch (e: SdkClientException) {
            logger.error("R3 file upload failed: ${e.message}", e)
            return Resp.fail("Upload file failed: ${e.message}")
        }

        val media = Media(NAME)
        media.mediaId = mediaId
        media.name = mediaName
        media.bucket = bucket
        media.contentType = contentType
        media.dataType = NAME
        media.size = size
        media.type = type
        media.createdBy = createBy
        media.createdAt = Date()
        media.md5 = calculatedMd5
        media.path = objectPath(objectKey)

        return Resp.success(media)
    }

    override fun getInputStream(path: String): Resp<InputStream?> {
        try {
            val r3Bucket = properties.bucket ?: return Resp.fail("Invalid r3 bucket")

            val objectKey = if (properties.prefix?.isNotEmpty() == true && path.startsWith(properties.prefix!!)) {
                path.replaceFirst("/${properties.prefix}", "")
            } else
                path

            val getObjectRequest = GetObjectRequest(r3Bucket, objectKey)
            val client = getClient()
            val s3Object = client.getObject(getObjectRequest)

            return Resp.success(s3Object.objectContent)

        } catch (e: AmazonServiceException) {
            logger.error("R3 file download failed: ${e.message}", e)
            return Resp.fail("Download file failed: ${e.message}")
        } catch (e: SdkClientException) {
            logger.error("R3 file download failed: ${e.message}", e)
            return Resp.fail("Download file failed: ${e.message}")
        }
    }

    override fun objectKey(bucket: String, mediaId: String, mediaName: String): String {
        if (properties.uploadPath?.isNotEmpty() == true) {
            return "${properties.uploadPath}/$mediaName".replace("//", "/")
        }

        return (if (properties.splitBucket == 1) {
            "/${splitBucket()}/$mediaId/$mediaName"
        } else
            "/$mediaId/$mediaName").replace("//", "/")
    }

    override fun objectPath(objectKey: String): String {
        val path = (if (properties.prefix?.isNotEmpty() == true) {
            "/${properties.prefix}$objectKey".replace(File.separator, "/")
        } else
            objectKey.replace(File.separator, "/")).replace("//", "/")
        return "/${properties.bucket}$path"
    }
}