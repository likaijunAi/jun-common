package com.jun.common.rbac.mysql.service

import com.jun.common.rbac.cache.RbacCacheManager
import com.jun.common.rbac.model.DataScope
import com.jun.common.rbac.model.Role
import com.jun.common.rbac.permission.PermissionMerger
import com.jun.common.rbac.permission.RoleGrant
import com.jun.common.rbac.repository.RolePermissionRepository
import com.jun.common.rbac.repository.RoleRepository
import com.jun.common.rbac.repository.TenantRepository
import com.jun.common.rbac.repository.UserPermissionRepository
import com.jun.common.rbac.repository.UserRepository
import com.jun.common.rbac.service.RbacService
import com.jun.common.rbac.tree.PathTree
import org.springframework.stereotype.Service

/**
 * 权限计算：读时沿父链展开，deny 优先
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Service
class RbacServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val userPermissionRepository: UserPermissionRepository,
    private val tenantRepository: TenantRepository,
    private val cacheManager: RbacCacheManager
) : RbacService {

    override fun getRolePermissions(roleId: String): Set<String> {
        cacheManager.getRolePermissions(roleId)?.let { return it }

        val role = roleRepository.findById(roleId) ?: return emptySet()
        val chain = roleChain(role)
        val permissions = rolePermissionRepository.findByRoleIds(chain.map { it.id })

        val grants = HashMap<String, MutableSet<String>>()
        val denies = HashMap<String, MutableSet<String>>()
        permissions.forEach { p ->
            val bucket = if (p.granted) grants else denies
            bucket.getOrPut(p.roleId) { LinkedHashSet() }.add(p.permissionCode)
        }

        val result = PermissionMerger.merge(
            chain.map { r ->
                RoleGrant(
                    roleId = r.id,
                    inherit = r.inherit,
                    grants = grants[r.id] ?: emptySet(),
                    denies = denies[r.id] ?: emptySet()
                )
            }
        )
        cacheManager.putRolePermissions(roleId, result)
        return result
    }

    override fun getUserPermissions(userId: String): Set<String> {
        cacheManager.getUserPermissions(userId)?.let { return it }

        val user = userRepository.findById(userId) ?: return emptySet()
        val now = System.currentTimeMillis()
        val result = LinkedHashSet(getRolePermissions(user.roleId))

        userPermissionRepository.findByUserId(userId).forEach { p ->
            if (p.expired(now)) return@forEach
            if (p.granted) result.add(p.permissionCode) else result.remove(p.permissionCode)
        }

        cacheManager.putUserPermissions(userId, result)
        return result
    }

    override fun hasPermission(userId: String, permissionCode: String): Boolean =
        getUserPermissions(userId).contains(permissionCode)

    override fun hasAnyPermission(userId: String, permissionCodes: Collection<String>): Boolean {
        if (permissionCodes.isEmpty()) return true
        val owned = getUserPermissions(userId)
        return permissionCodes.any { owned.contains(it) }
    }

    override fun hasAllPermissions(userId: String, permissionCodes: Collection<String>): Boolean {
        if (permissionCodes.isEmpty()) return true
        val owned = getUserPermissions(userId)
        return permissionCodes.all { owned.contains(it) }
    }

    override fun getDataScope(userId: String): DataScope {
        val user = userRepository.findById(userId) ?: return DataScope.SELF
        val role = roleRepository.findById(user.roleId) ?: return DataScope.SELF
        return role.scope()
    }

    override fun resolveTenantIds(userId: String): Set<String>? {
        val user = userRepository.findById(userId) ?: return null
        val role = roleRepository.findById(user.roleId) ?: return setOf(user.tenantId)
        return when (role.scope()) {
            DataScope.ALL -> null
            DataScope.TENANT_AND_CHILDREN -> {
                val cached = cacheManager.getTenantChildren(user.tenantId)
                val children = cached ?: tenantRepository
                    .findDescendants(PathTree.descendantPrefix(tenantPathOf(user.tenantId)))
                    .map { it.id }
                    .also { cacheManager.putTenantChildren(user.tenantId, it) }
                LinkedHashSet<String>(children).apply { add(user.tenantId) }
            }

            DataScope.TENANT -> setOf(user.tenantId)
            DataScope.SELF -> setOf(user.tenantId)
        }
    }

    override fun refreshCache(roleId: String) {
        val role = roleRepository.findById(roleId)
        if (role == null) {
            cacheManager.invalidateRole(roleId)
            return
        }
        val prefix = PathTree.descendantPrefix(role.path)
        val roleIds = roleRepository.findDescendants(prefix).map { it.id } + roleId
        cacheManager.invalidateRoleTree(roleIds)

        val userIds = userRepository.findByRoleIds(roleIds).map { it.id }
        cacheManager.invalidateUserTree(userIds)
    }

    override fun refreshUserCache(userId: String) = cacheManager.invalidateUser(userId)

    private fun tenantPathOf(tenantId: String): String {
        if (tenantId == PathTree.ROOT_ID) return PathTree.ROOT_PATH
        return tenantRepository.findById(tenantId)?.path ?: PathTree.ROOT_PATH
    }

    /** 自顶向下的角色链（root -> 目标角色） */
    private fun roleChain(role: Role): List<Role> {
        val ids = LinkedHashSet(PathTree.ids(role.path))
        ids.add(role.id)
        val found = roleRepository.findByIds(ids).toMutableList()
        if (found.none { it.id == role.id }) found.add(role)
        return found.sortedBy { it.level }
    }
}
