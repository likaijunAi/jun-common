package com.jun.common.rbac.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface PasswordEncoder {
    fun generateSalt(): String

    fun encode(rawPassword: String, salt: String): String

    fun matches(rawPassword: String, salt: String, encoded: String): Boolean
}

/**
 * 默认实现：PBKDF2WithHmacSHA256，JDK 内置，无三方依赖
 */
open class Pbkdf2PasswordEncoder(
    private val iterations: Int = 120_000,
    private val keyLength: Int = 256
) : PasswordEncoder {

    private val random = SecureRandom()

    override fun generateSalt(): String {
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    override fun encode(rawPassword: String, salt: String): String {
        val spec = PBEKeySpec(rawPassword.toCharArray(), salt.toByteArray(Charsets.UTF_8), iterations, keyLength)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return Base64.getEncoder().encodeToString(factory.generateSecret(spec).encoded)
    }

    override fun matches(rawPassword: String, salt: String, encoded: String): Boolean {
        if (encoded.isBlank()) return false
        val actual = encode(rawPassword, salt).toByteArray(Charsets.UTF_8)
        val expected = encoded.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(actual, expected)
    }

    companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
    }
}
