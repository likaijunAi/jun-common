package com.jun.common.web.http

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.util.StreamUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/30 23:22
 **/
class JunCachingRequestWrapper(request: HttpServletRequest): HttpServletRequestWrapper(request) {
    private val requestBody: ByteArray = StreamUtils.copyToByteArray(request.inputStream)

    @Throws(IOException::class)
    override fun getInputStream(): ServletInputStream {
        val byteArrayInputStream = ByteArrayInputStream(requestBody)

        return object : ServletInputStream() {
            override fun isFinished(): Boolean {
                return request.inputStream.isFinished
            }

            override fun isReady(): Boolean {
                return request.inputStream.isReady
            }

            override fun setReadListener(readListener: ReadListener) {
                request.inputStream.setReadListener(readListener)
            }

            @Throws(IOException::class)
            override fun read(): Int {
                return byteArrayInputStream.read()
            }
        }
    }

    @Throws(IOException::class)
    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(inputStream))
    }

    fun getContentAsByteArray(): ByteArray {
        return requestBody
    }

    fun getContentAsString(): String {
        val charset = try {
            Charset.forName(request.characterEncoding ?: "UTF-8")
        } catch (e: Exception) {
            Charset.forName("UTF-8") // 默认回退到 UTF-8
        }
        return this.requestBody.toString(charset)
    }
}