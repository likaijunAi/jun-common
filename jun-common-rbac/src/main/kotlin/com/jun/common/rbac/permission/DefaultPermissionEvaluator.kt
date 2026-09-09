package com.jun.common.rbac.permission

import com.jun.common.rbac.auth.LoginContext
import com.jun.common.rbac.service.RbacService

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class DefaultPermissionEvaluator(private val rbacService: RbacService) : PermissionEvaluator {

    override fun permissionsOf(userId: String): Set<String> = rbacService.getUserPermissions(userId)

    override fun hasPermission(userId: String, permission: String): Boolean =
        permissionsOf(userId).contains(permission)

    override fun hasAnyPermission(userId: String, permissions: Collection<String>): Boolean {
        if (permissions.isEmpty()) return true
        val owned = permissionsOf(userId)
        return permissions.any { owned.contains(it) }
    }

    override fun hasAllPermissions(userId: String, permissions: Collection<String>): Boolean {
        if (permissions.isEmpty()) return true
        val owned = permissionsOf(userId)
        return permissions.all { owned.contains(it) }
    }

    override fun hasPermission(permission: String): Boolean {
        val user = LoginContext.get() ?: return false
        return hasPermission(user.userid ?: "", permission)
    }
}
