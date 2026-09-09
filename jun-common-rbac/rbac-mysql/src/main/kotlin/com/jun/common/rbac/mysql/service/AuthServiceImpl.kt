package com.jun.common.rbac.mysql.service

import com.jun.common.rbac.auth.LoginContext
import com.jun.common.rbac.auth.PasswordEncoder
import com.jun.common.rbac.auth.TokenProvider
import com.jun.common.rbac.cache.RbacCacheManager
import com.jun.common.rbac.exception.RbacErrorCode
import com.jun.common.rbac.exception.RbacException
import com.jun.common.rbac.model.LoginUser
import com.jun.common.rbac.model.UserStatus
import com.jun.common.rbac.model.req.LoginReq
import com.jun.common.rbac.model.req.LoginResp
import com.jun.common.rbac.repository.RoleRepository
import com.jun.common.rbac.repository.TenantRepository
import com.jun.common.rbac.repository.UserRepository
import com.jun.common.rbac.service.AuthService
import com.jun.common.rbac.service.RbacService
import com.jun.common.rbac.tree.PathTree
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val tenantRepository: TenantRepository,
    private val rbacService: RbacService,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider,
    private val cacheManager: RbacCacheManager,
    @Value("\${jun.rbac.token-expires-in:7200}") private val expiresInSeconds: Long = 7200,
    @Value("\${jun.rbac.max-fail-count:5}") private val maxFailCount: Int = 5,
    @Value("\${jun.rbac.lock-seconds:600}") private val lockSeconds: Long = 600
) : AuthService {

    override fun login(req: LoginReq): LoginResp {
        val user = resolveUser(req) ?: throw RbacException(RbacErrorCode.USER_PASSWORD_ERROR)

        if (user.status == UserStatus.DISABLED.value) throw RbacException(RbacErrorCode.USER_DISABLED)

        val now = System.currentTimeMillis()
        if (user.locked(now)) throw RbacException(RbacErrorCode.USER_LOCKED)

        if (!passwordEncoder.matches(req.password, user.salt, user.password)) {
            user.loginFailCount += 1
            if (user.loginFailCount >= maxFailCount) {
                user.lockUntil = now + lockSeconds * 1000
                user.loginFailCount = 0
            }
            userRepository.updateById(user)
            throw RbacException(RbacErrorCode.USER_PASSWORD_ERROR)
        }

        user.loginFailCount = 0
        user.lockUntil = 0
        user.lastLoginAt = now
        userRepository.updateById(user)

        val loginUser = toLoginUser(user)
        val token = tokenProvider.generate(loginUser, expiresInSeconds)
        return LoginResp.of(token, expiresInSeconds, loginUser, rbacService.getUserPermissions(user.id))
    }

    override fun logout(token: String) {
        runCatching { tokenProvider.parse(token) }.getOrNull()?.let {
            cacheManager.blacklist(it.payloadId, expiresInSeconds)
            it.userid?.let { id -> cacheManager.invalidateUser(id) }
        }
    }

    override fun refresh(token: String): LoginResp {
        val old = tokenProvider.parse(token)
        val userId = old.userid ?: throw RbacException(RbacErrorCode.UNAUTHORIZED)
        val user = userRepository.findById(userId) ?: throw RbacException(RbacErrorCode.USER_NOT_FOUND)
        if (!user.enabled()) throw RbacException(RbacErrorCode.USER_DISABLED)

        cacheManager.blacklist(old.payloadId, expiresInSeconds)
        val loginUser = toLoginUser(user)
        val newToken = tokenProvider.generate(loginUser, expiresInSeconds)
        return LoginResp.of(newToken, expiresInSeconds, loginUser, rbacService.getUserPermissions(user.id))
    }

    override fun parse(token: String): LoginUser {
        val user = tokenProvider.parse(token)
        if (user.payloadId.isNotEmpty() && cacheManager.isBlacklisted(user.payloadId))
            throw RbacException(RbacErrorCode.UNAUTHORIZED)
        return user
    }

    override fun currentUser(): LoginUser? = LoginContext.get()

    override fun requireUser(): LoginUser = LoginContext.require()

    /** username 全平台唯一，无需租户信息 */
    private fun resolveUser(req: LoginReq) = userRepository.findByUsername(req.username)

    private fun toLoginUser(user: com.jun.common.rbac.model.User): LoginUser {
        val tenant = tenantRepository.findById(user.tenantId)
        val role = roleRepository.findById(user.roleId)
        val loginUser = LoginUser()
        loginUser.payloadId = ""
        loginUser.userid = user.id
        loginUser.username = user.username
        loginUser.nickname = user.nickname
        loginUser.tenantId = user.tenantId
        loginUser.tenantPath = tenant?.path ?: PathTree.ROOT_PATH
        loginUser.parentTenantId = tenant?.parentId ?: PathTree.ROOT_ID
        loginUser.roleId = user.roleId
        loginUser.rolePath = role?.path ?: PathTree.ROOT_PATH
        loginUser.dataScope = role?.dataScope ?: com.jun.common.rbac.model.DataScope.SELF.value
        loginUser.permVersion = System.currentTimeMillis()
        return loginUser
    }
}
