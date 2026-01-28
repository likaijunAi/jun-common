package com.jun.common.upload

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.Volatile


/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/28 13:52
 **/
class CloseAwareInputStream(inputStream: InputStream) : FilterInputStream(inputStream) {
    @Volatile
    private var closed = false

    @Throws(IOException::class)
    override fun close() {
        super.close()
        closed = true
    }

    fun isClosed(): Boolean {
        return closed
    }
}