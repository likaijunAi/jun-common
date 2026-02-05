package com.jun.common.core.util

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.CharsetUtil
import cn.hutool.core.util.ZipUtil
import java.io.File

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/2/1 14:21
 **/
object TempFileUtil {
    private const val TEMP_ROOT = "jun.uploader.temp.dir"
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

    fun zipTempFile(vararg files: File): File? {
        if (files.isEmpty()) return null

        val dir = getTempFile(true)
        val zipFile = FileUtil.file(dir, "temp_${System.currentTimeMillis()}.zip")
        ZipUtil.zip(zipFile, true, *files)
        zipFile.deleteOnExit()
        return zipFile
    }
}