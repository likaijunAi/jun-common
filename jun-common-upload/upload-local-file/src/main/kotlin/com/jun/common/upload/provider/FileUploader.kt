package com.jun.common.upload.provider

import com.jun.common.core.web.Resp
import com.jun.common.upload.AbstractUploader
import com.jun.common.upload.model.Media
import com.jun.common.upload.provider.config.FileUploadProperties
import cn.hutool.crypto.digest.DigestUtil
import org.apache.tomcat.util.http.fileupload.IOUtils
import java.io.*
import java.util.Date

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 15:28
 **/
class FileUploader(private val bucket: String, private val properties: FileUploadProperties) :
    AbstractUploader(properties) {

    companion object {
        const val NAME = "file"
    }

    private var storeFile: File? = null


    private fun storeDir(): File? {
        if (storeFile != null)
            return storeFile

        storeFile = properties.storeDir?.let { File(it) }

        storeFile?.apply {
            if (!this.exists()) {
                this.mkdirs()
            }
        }

        return storeFile
    }

    override fun upload(
        inputStream: InputStream,
        name: String,
        type: String,
        size: Long,
        createBy: String,
        contentType: String?
    ): Resp<Media?> {
        val root = storeDir() ?: return Resp.fail("The store directory has not been set yet.")

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
        val saveResp = getOutputStream(root, objectKey)
        if (!saveResp.isSuccess()) {
            return Resp.fail(saveResp)
        }
        val file = saveResp.result!!

        try {
            FileOutputStream(file).use { outputStream ->
                inputStream.use { input ->
                    IOUtils.copy(input, outputStream)
                }
                outputStream.flush()
            }
        } catch (e: Exception) {
            logger.error("File upload failed: ${e.message}", e)
            file.delete()
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
        media.md5 = DigestUtil.md5Hex(file)
        media.path = objectPath(objectKey)

        return Resp.success(media)
    }

    private fun getOutputStream(
        root: File,
        objectKey: String
    ): Resp<File?> {
        val filePath = objectKey.replace("/", File.separator)
        val file = File(root, filePath)
        val dir = file.parentFile.absoluteFile
        val canonicalRoot = root.canonicalPath
        val canonicalDir = dir.canonicalPath

        if (!canonicalDir.startsWith(canonicalRoot)) {
            return Resp.fail("Invalid directory path")
        }

        if (!dir.exists() || dir.isFile) {
            val re = dir.mkdirs()
            if (!re) return Resp.fail("Create Temp File Directory Fail")
        }

        val canonicalFile = file.canonicalPath
        if (!canonicalFile.startsWith(canonicalRoot)) {
            return Resp.fail("Invalid file path")
        }

        if (!file.exists()) {
            val re = file.createNewFile()
            if (!re) return Resp.fail("Create Temp File Fail")
        }

        return Resp.success(file)
    }

    override fun getInputStream(path: String): Resp<InputStream?> {
        val root = storeDir() ?: return Resp.fail("The store directory has not been set yet.")
        val objectKey = if (properties.prefix?.isNotEmpty() == true && path.startsWith(properties.prefix!!)) {
            path.replaceFirst("/${properties.prefix}", "")
        } else
            path

        val filePath = objectKey.replace("/", File.separator)
        val file = File(root, filePath)
        if (file.isFile && file.exists()) {
            return Resp.success(FileInputStream(file))
        }
        return Resp.fail("Media is not found($path)")
    }
}