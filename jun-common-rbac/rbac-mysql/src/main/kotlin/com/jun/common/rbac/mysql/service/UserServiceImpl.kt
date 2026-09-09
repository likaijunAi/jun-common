package com.jun.common.rbac.mysql.service

import com.jun.common.core.web.Resp
import com.jun.common.rbac.auth.PasswordEncoder
import com.jun.common.rbac.cache.RbacCacheManager
import com.jun.common.rbac.exception.RbacErrorCode
import com.jun.common.rbac.exception.RbacException
import com.jun.common.rbac.model.User
import com.jun.common.rbac.model.UserStatus
import com.jun.common.rbac.model.req.UserCreateReq
import com.jun.common.rbac.model.req.UserQuery
import com.jun.common.rbac.model.req.UserUpdateReq
import com.jun.common.rbac.mysql.entity.UserPO
import com.jun.common.rbac.repository.RoleRepository
import com.jun.common.rbac.repository.UserRepository
import com.jun.common.rbac.service.RbacService
import com.jun.common.rbac.service.RoleService
import com.jun.common.rbac.service.UserService
import com.jun.common.rbac.tree.PathTree
import org.springframework.stereotype.Service

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder,
    private val rbacService: RbacService,
    private val cacheManager: RbacCacheManager
) : UserService {

    override fun create(req: UserCreateReq): User {
        if (req.username.isBlank()) throw RbacException(RbacErrorCode.BAD_REQUEST, "username 不能为空")
        if (req.password.isBlank()) throw RbacException(RbacErrorCode.BAD_REQUEST, "password 不能为空")
        if (userRepository.existsByUsername(req.username, null))
            throw RbacException(RbacErrorCode.USER_NAME_DUPLICATE)

        val role = if (req.roleId.isBlank() || req.roleId == PathTree.ROOT_ID) null
        else roleRepository.findById(req.roleId) ?: throw RbacException(RbacErrorCode.USER_ROLE_INVALID)

        if (role != null && !roleService.isAssignableTo(role.id, req.tenantId))
            throw RbacException(RbacErrorCode.USER_ROLE_INVALID)

        val salt = passwordEncoder.generateSalt()
        val user = UserPO().apply {
            this.tenantId = req.tenantId
            this.username = req.username
            this.salt = salt
            this.password = passwordEncoder.encode(req.password, salt)
            this.nickname = req.nickname
            this.phone = req.phone
            this.email = req.email
            this.avatar = req.avatar
            this.roleId = role?.id ?: PathTree.ROOT_ID
            this.status = req.status ?: UserStatus.ENABLED.value
        }
        userRepository.insert(user)
        return user
    }

    override fun update(req: UserUpdateReq): User {
        val user = userRepository.findById(req.id) ?: throw RbacException(RbacErrorCode.USER_NOT_FOUND)
        req.nickname?.let { user.nickname = it }
        req.phone?.let { user.phone = it }
        req.email?.let { user.email = it }
        req.avatar?.let { user.avatar = it }
        req.status?.let { user.status = it }
        req.roleId?.takeIf { it.isNotBlank() }?.let { assignRole(user, it) }
        userRepository.updateById(user)
        cacheManager.invalidateUser(user.id)
        return user
    }

    override fun delete(id: String) {
        val user = userRepository.findById(id) ?: throw RbacException(RbacErrorCode.USER_NOT_FOUND)
        userRepository.logicDelete(user.id)
        cacheManager.invalidateUser(user.id)
    }

    override fun findById(id: String): User? = userRepository.findById(id)

    override fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    override fun assignRole(userId: String, roleId: String) {
        val user = userRepository.findById(userId) ?: throw RbacException(RbacErrorCode.USER_NOT_FOUND)
        assignRole(user, roleId)
        userRepository.updateById(user)
        cacheManager.invalidateUser(user.id)
    }

    override fun resetPassword(userId: String, rawPassword: String) {
        val user = userRepository.findById(userId) ?: throw RbacException(RbacErrorCode.USER_NOT_FOUND)
        val salt = passwordEncoder.generateSalt()
        user.salt = salt
        user.password = passwordEncoder.encode(rawPassword, salt)
        userRepository.updateById(user)
    }

    override fun changeStatus(userId: String, status: Int) {
        val user = userRepository.findById(userId) ?: throw RbacException(RbacErrorCode.USER_NOT_FOUND)
        user.status = status
        userRepository.updateById(user)
        cacheManager.invalidateUser(user.id)
    }

    override fun unlock(userId: String) {
        val user = userRepository.findById(userId) ?: throw RbacException(RbacErrorCode.USER_NOT_FOUND)
        user.lockUntil = 0
        user.loginFailCount = 0
        userRepository.updateById(user)
    }

    override fun permissions(userId: String): Set<String> = rbacService.getUserPermissions(userId)

    override fun page(query: UserQuery): Resp<User> = userRepository.page(query)

    private fun assignRole(user: User, roleId: String) {
        // 允许绑定「平台/祖先商户共享」的角色
        if (!roleService.isAssignableTo(roleId, user.tenantId))
            throw RbacException(RbacErrorCode.USER_ROLE_INVALID)
        user.roleId = roleId
    }
}
