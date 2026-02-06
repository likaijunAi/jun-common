package com.jun.common.web.resolver

import com.jun.common.core.util.Signer
import com.jun.common.web.http.JunCachingRequestWrapper
import jakarta.servlet.http.HttpServletRequest

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/2/6 14:22
 **/
abstract class AbstractSignatureResolver : SignatureResolver {
    companion object {
        const val RAS = "RSA"
        const val MD5 = "MD5"
    }

    override fun verify(
        request: HttpServletRequest,
        signType: String,
        signature: String,
        requestBody: String?
    ): Boolean {
        val body = requestBody ?: JunCachingRequestWrapper(request).getContentAsString()
        val signer = try {
            singer(body)
        } catch (e: Exception) {
            throw e
        }
        if (signType == RAS) {
            if (signer.rsaSignature(body) != signature) {
                return false
            }
        } else if (signType == MD5) {
            if (signer.md5Signature(body) != signature) {
                return false
            }
        }
        return false
    }

    abstract fun singer(body: String): Signer
}