package com.jun.common.core.util

import cn.hutool.core.codec.Base64
import cn.hutool.core.util.StrUtil
import cn.hutool.crypto.SecureUtil
import cn.hutool.crypto.asymmetric.Sign
import cn.hutool.crypto.asymmetric.SignAlgorithm
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/5 22:01
 **/
class Signer(
    private val secret:String?=null,
    privateKey:String?=null,
    publicKey:String?=null,
    private val username:String?=null,
    private val passwd:String?=null,
    private val  token:String?=null
) {
    private  var sign: Sign? = null

    init {
        if (privateKey!=null || publicKey!=null){
             val public:PublicKey?   = publicKey?.let { rsaPublicKey(it) }
             val private:PrivateKey? = privateKey?.let { rsaPrivateKey(it)}
            sign = sign(private,public)
        }
    }

    companion object {
        const val RSA   = "RSA"
        const val MD5   = "MD5"
        const val BASIC = "BASIC"
        const val JWT   = "JTW"

        private val logger: Logger = LoggerFactory.getLogger(Signer::class.java)

        fun basicSigner(username:String,passwd:String):Signer {
            return Signer(username = username, passwd = passwd)
        }

        fun jwtSigner(token:String):Signer {
            return Signer(token = token)
        }

        fun md5Signer(secret:String):Signer {
            return Signer(secret = secret)
        }

        fun rsaSigner(privateKey:String, publicKey:String):Signer {
            return Signer(privateKey = privateKey, publicKey = publicKey)
        }

        private fun readFile(file: File):String {
            return Files.readAllLines(file.toPath(), Charset.defaultCharset())
                .filter { !it.contains("----") }
                .joinToString { it }
                .replace("\r\n", "")
                .replace("\n", "")
                .replace(" ","")
        }

        fun rsaPublicKey(file: File): PublicKey {
            val publicKeyPEM = readFile(file)

            val encoded    = Base64.decode(publicKeyPEM)
            val keyFactory = KeyFactory.getInstance(RSA)
            val keySpec = X509EncodedKeySpec(encoded)
            return keyFactory.generatePublic(keySpec)
        }

        fun rsaPrivateKey(file: File): PrivateKey {
            val privateKeyPEM  = readFile(file)

            val encoded    = Base64.decode(privateKeyPEM)
            val keyFactory = KeyFactory.getInstance(RSA)
            val keySpec = PKCS8EncodedKeySpec(encoded)
            return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
        }

        fun rsaPublicKey(key: String): PublicKey {
            val encoded    = Base64.decode(key)
            val keyFactory = KeyFactory.getInstance(RSA)
            val keySpec = X509EncodedKeySpec(encoded)
            return keyFactory.generatePublic(keySpec)
        }

        fun rsaPrivateKey(key: String): PrivateKey {
            val encoded    = Base64.decode(key)
            val keyFactory = KeyFactory.getInstance(RSA)
            val keySpec = PKCS8EncodedKeySpec(encoded)
            return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
        }

        fun sign(privateKey: PrivateKey?=null, publicKey: PublicKey?=null): Sign {
            return SecureUtil.sign(SignAlgorithm.SHA256withRSA, privateKey?.encoded, publicKey?.encoded)
        }

        fun md5Signature(body: String, secret:String): String {
            return SecureUtil.md5(StrUtil.format("{}{}", body, secret))
        }

        fun rsaSignature(body: String, sign: Sign): String? {
            try {
                return Base64.encode(sign.sign(body))
            } catch (e: IOException) {
                logger.error("", e)
            } catch (e: NoSuchAlgorithmException) {
                logger.error("", e)
            } catch (e: InvalidKeySpecException) {
                logger.error("", e)
            }
            return null
        }

        fun basicSignature(username:String,passwd:String):String {
            return Base64.encode("$username:$passwd")
        }
    }

    fun md5Signature(body: String): String {
        if(secret == null){
            throw NullPointerException("secret is null")
        }
        return md5Signature(body, secret)
    }

    fun rsaSignature(body: String): String? {
        if(sign == null){
            throw NullPointerException("sign is null")
        }

        try {
            return Base64.encode(sign!!.sign(body))
        } catch (e: IOException) {
            logger.error("", e)
        } catch (e: NoSuchAlgorithmException) {
            logger.error("", e)
        } catch (e: InvalidKeySpecException) {
            logger.error("", e)
        }
        return null
    }

    fun basicSignature():String? {
        if(username.isNullOrEmpty() || passwd.isNullOrEmpty())
            return null
        return "basic ${Companion.basicSignature(username, passwd)}"
    }

    fun jwtSignature():String? {
        if(token.isNullOrEmpty())
            return null
        return "bearer $token"
    }
}