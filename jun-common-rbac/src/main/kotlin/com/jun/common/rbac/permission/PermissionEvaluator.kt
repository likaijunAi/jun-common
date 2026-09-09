package com.jun.common.rbac.permission

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface PermissionEvaluator {

    fun permissionsOf(userId: String): Set<String>

    fun hasPermission(userId: String, permission: String): Boolean

    fun hasAnyPermission(userId: String, permissions: Collection<String>): Boolean

    fun hasAllPermissions(userId: String, permissions: Collection<String>): Boolean

    /** 当前登录用户是否拥有该权限 */
    fun hasPermission(permission: String): Boolean
}
