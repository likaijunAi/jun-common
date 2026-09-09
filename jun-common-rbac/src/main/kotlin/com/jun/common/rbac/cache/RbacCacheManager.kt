package com.jun.common.rbac.cache

/**
 * RBAC 缓存。默认实现为空实现（不缓存），由 rbac-mysql 提供 Redis 实现。
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface RbacCacheManager {

    fun getRolePermissions(roleId: String): Set<String>?

    fun putRolePermissions(roleId: String, permissions: Set<String>)

    fun getUserPermissions(userId: String): Set<String>?

    fun putUserPermissions(userId: String, permissions: Set<String>)

    fun getTenantChildren(tenantId: String): List<String>?

    fun putTenantChildren(tenantId: String, children: List<String>)

    /** 失效角色及其所有子角色的权限缓存 */
    fun invalidateRole(roleId: String)

    /** 失效指定角色集合（含子树）的权限缓存 */
    fun invalidateRoleTree(roleIds: Collection<String>)

    fun invalidateUser(userId: String)

    fun invalidateUserTree(userIds: Collection<String>)

    fun invalidateTenant(tenantId: String)

    /** token 加入黑名单（登出） */
    fun blacklist(tokenId: String, ttlSeconds: Long)

    fun isBlacklisted(tokenId: String): Boolean
}

/** 无缓存实现 */
open class NoOpRbacCacheManager : RbacCacheManager {
    override fun getRolePermissions(roleId: String): Set<String>? = null
    override fun putRolePermissions(roleId: String, permissions: Set<String>) = Unit
    override fun getUserPermissions(userId: String): Set<String>? = null
    override fun putUserPermissions(userId: String, permissions: Set<String>) = Unit
    override fun getTenantChildren(tenantId: String): List<String>? = null
    override fun putTenantChildren(tenantId: String, children: List<String>) = Unit
    override fun invalidateRole(roleId: String) = Unit
    override fun invalidateRoleTree(roleIds: Collection<String>) = Unit
    override fun invalidateUser(userId: String) = Unit
    override fun invalidateUserTree(userIds: Collection<String>) = Unit
    override fun invalidateTenant(tenantId: String) = Unit
    override fun blacklist(tokenId: String, ttlSeconds: Long) = Unit
    override fun isBlacklisted(tokenId: String): Boolean = false
}
