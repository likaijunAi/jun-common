package com.jun.common.web.http

import cn.hutool.core.codec.Base64
import cn.hutool.crypto.SecureUtil
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.jun.common.core.util.JwtPayload
import com.jun.common.core.web.GsonFactory
import com.jun.common.web.config.JunWebJwtProperties
import java.io.UnsupportedEncodingException
import java.util.Date


/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/15 22:31
 **/
class DefaultJwtResolver(private val config: JunWebJwtProperties) : JwtResolver {

    private val jwtVerifier by lazy {
        val algorithm = Algorithm.HMAC256(config.secret)
        val builder = JWT.require(algorithm)
        if (config.issuer.isNotEmpty())
            builder.withIssuer(config.issuer)
        builder.build()
    }

    @Throws(UnsupportedEncodingException::class)
    override fun generateToken(jwtPayload: JwtPayload): String? {
        if (jwtPayload.expiresIn == null){
            jwtPayload.expiresIn = Date(System.currentTimeMillis() + config.expiresIn.times(1000))
        }
        if (jwtPayload.createdAt == null){
            jwtPayload.createdAt = Date(System.currentTimeMillis())
        }

        val algorithm: Algorithm = Algorithm.HMAC256(config.secret)
        val dataBytes: ByteArray =
            SecureUtil.des(config.secret.toByteArray(Charsets.UTF_8)).encrypt(GsonFactory.defaultGson.toJson(jwtPayload))
        val payload = Base64.encode(dataBytes)
        val builder: JWTCreator.Builder =
            JWT.create().withIssuer(config.issuer).withClaim(config.claimName, payload).withJWTId(jwtPayload.payloadId)
        builder.withExpiresAt(jwtPayload.expiresIn)
        return builder.sign(algorithm)
    }

    @Throws(UnsupportedEncodingException::class)
    override fun parseToken(token: String?): JwtPayload {
        val decodedJWT: DecodedJWT = jwtVerifier.verify(token)
        val payload: String = decodedJWT.getClaim(config.claimName).asString()
        val dataBytes = Base64.decode(payload)
        val decryptBytes = SecureUtil.des(config.secret.toByteArray(Charsets.UTF_8)).decrypt(dataBytes)
        return GsonFactory.defaultGson.fromJson(String(decryptBytes,Charsets.UTF_8), JwtPayload::class.java)
    }
}