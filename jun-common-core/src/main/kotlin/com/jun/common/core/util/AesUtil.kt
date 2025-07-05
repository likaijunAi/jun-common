package com.jun.common.core.util

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


/**
 * @author leolee
 * 广州海音信息科技有限公司
 * l@xsocket.cn
 * create 2025/3/21 16:47
 **/
class AesUtil {

    companion object {
        private const val ALGORITHM: String = "AES"
        private const val TRANSFORMATION: String = "AES"

        @Throws(Exception::class)
        fun encrypt(data: String, key: String): String {
            val secretKeySpec = SecretKeySpec(key.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            return Base64.getEncoder().encodeToString(encryptedBytes)
        }

        @Throws(Exception::class)
        fun decrypt(encryptedData: String?, key: String): String {
            val secretKeySpec = SecretKeySpec(key.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            val decodedBytes: ByteArray = Base64.getDecoder().decode(encryptedData)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            return String(decryptedBytes)
        }
    }
}