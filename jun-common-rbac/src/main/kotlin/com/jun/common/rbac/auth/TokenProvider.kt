package com.jun.common.rbac.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.jun.common.rbac.model.LoginUser
import java.util.Date

/**
 * 令牌签发/解析。token 中只保留身份 + 权限版本，权限本身走缓存，
 * 保证父角色权限变更后子角色用户无需重新登录即可生效。
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface TokenProvider {
    fun generate(user: LoginUser, expiresInSeconds: Long): String
    fun parse(token: String): LoginUser
}

open class JwtTokenProvider(
    private val secret: String = "JunRbacSecret",
    private val issuer: String = "JunRbac"
) : TokenProvider {

    private val algorithm: Algorithm by lazy { Algorithm.HMAC256(secret) }

    private val verifier by lazy { JWT.require(algorithm).withIssuer(issuer).build() }

    override fun generate(user: LoginUser, expiresInSeconds: Long): String {
        val now = Date()
        val expiresAt = Date(now.time + expiresInSeconds * 1000)
        user.createdAt = now
        user.expiresIn = expiresAt
        user.payloadId = java.util.UUID.randomUUID().toString().replace("-", "")
        return JWT.create()
            .withIssuer(issuer)
            .withJWTId(user.payloadId)
            .withSubject(user.userid)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .withClaim(C_USERNAME, user.username)
            .withClaim(C_NICKNAME, user.nickname)
            .withClaim(C_TENANT_ID, user.tenantId)
            .withClaim(C_TENANT_PATH, user.tenantPath)
            .withClaim(C_PARENT_TENANT_ID, user.parentTenantId)
            .withClaim(C_ROLE_ID, user.roleId)
            .withClaim(C_ROLE_PATH, user.rolePath)
            .withClaim(C_DATA_SCOPE, user.dataScope)
            .withClaim(C_PERM_VERSION, user.permVersion)
            .sign(algorithm)
    }

    override fun parse(token: String): LoginUser {
        val decoded = verifier.verify(token)
        val user = LoginUser()
        user.payloadId = decoded.id ?: ""
        user.userid = decoded.subject
        user.createdAt = decoded.issuedAt
        user.expiresIn = decoded.expiresAt
        user.username = decoded.getClaim(C_USERNAME).asString()
        user.nickname = decoded.getClaim(C_NICKNAME).asString()
        user.tenantId = decoded.getClaim(C_TENANT_ID).asString() ?: "0"
        user.tenantPath = decoded.getClaim(C_TENANT_PATH).asString() ?: "/0/"
        user.parentTenantId = decoded.getClaim(C_PARENT_TENANT_ID).asString() ?: "0"
        user.roleId = decoded.getClaim(C_ROLE_ID).asString() ?: "0"
        user.rolePath = decoded.getClaim(C_ROLE_PATH).asString() ?: "/0/"
        user.dataScope = decoded.getClaim(C_DATA_SCOPE).asString() ?: "TENANT"
        user.permVersion = decoded.getClaim(C_PERM_VERSION).asLong() ?: 0L
        return user
    }

    companion object {
        const val C_USERNAME = "username"
        const val C_NICKNAME = "nickname"
        const val C_TENANT_ID = "tenantId"
        const val C_TENANT_PATH = "tenantPath"
        const val C_PARENT_TENANT_ID = "parentTenantId"
        const val C_ROLE_ID = "roleId"
        const val C_ROLE_PATH = "rolePath"
        const val C_DATA_SCOPE = "dataScope"
        const val C_PERM_VERSION = "permVersion"
    }
}
