package com.jun.common.rbac.mysql.cache

import com.google.gson.reflect.TypeToken
import com.jun.common.core.cache.JunCache
import com.jun.common.rbac.cache.RbacCacheKeys
import com.jun.common.rbac.cache.RbacCacheManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * 基于 [JunCache] 的 Redis 缓存实现
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Component
@ConditionalOnBean(JunCache::class)
class RedisRbacCacheManager(private val cache: JunCache) : RbacCacheManager {

    private val setType = object : TypeToken<Set<String>>() {}.type
    private val listType = object : TypeToken<List<String>>() {}.type

    override fun getRolePermissions(roleId: String): Set<String>? =
        cache.getObject(RbacCacheKeys.rolePerm(roleId), setType)

    override fun putRolePermissions(roleId: String, permissions: Set<String>) =
        cache.setObject(RbacCacheKeys.rolePerm(roleId), permissions, RbacCacheKeys.EXPIRE_SECONDS, TimeUnit.SECONDS)

    override fun getUserPermissions(userId: String): Set<String>? =
        cache.getObject(RbacCacheKeys.userPerm(userId), setType)

    override fun putUserPermissions(userId: String, permissions: Set<String>) =
        cache.setObject(RbacCacheKeys.userPerm(userId), permissions, RbacCacheKeys.EXPIRE_SECONDS, TimeUnit.SECONDS)

    override fun getTenantChildren(tenantId: String): List<String>? =
        cache.getObject(RbacCacheKeys.tenantChildren(tenantId), listType)

    override fun putTenantChildren(tenantId: String, children: List<String>) =
        cache.setObject(RbacCacheKeys.tenantChildren(tenantId), children, RbacCacheKeys.EXPIRE_SECONDS, TimeUnit.SECONDS)

    override fun invalidateRole(roleId: String) = cache.del(RbacCacheKeys.rolePerm(roleId))

    override fun invalidateRoleTree(roleIds: Collection<String>) =
        cache.del(*roleIds.map { RbacCacheKeys.rolePerm(it) }.toTypedArray())

    override fun invalidateUser(userId: String) = cache.del(RbacCacheKeys.userPerm(userId))

    override fun invalidateUserTree(userIds: Collection<String>) =
        cache.del(*userIds.map { RbacCacheKeys.userPerm(it) }.toTypedArray())

    override fun invalidateTenant(tenantId: String) = cache.del(RbacCacheKeys.tenantChildren(tenantId))

    override fun blacklist(tokenId: String, ttlSeconds: Long) =
        cache.setObject(RbacCacheKeys.TOKEN_BLACKLIST + tokenId, "1", ttlSeconds, TimeUnit.SECONDS)

    override fun isBlacklisted(tokenId: String): Boolean = cache.hasKey(RbacCacheKeys.TOKEN_BLACKLIST + tokenId)
}
