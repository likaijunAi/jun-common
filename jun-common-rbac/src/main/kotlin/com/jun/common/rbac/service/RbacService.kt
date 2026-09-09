package com.jun.common.rbac.service

import com.jun.common.rbac.model.DataScope

/**
 * 权限计算门面
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface RbacService {

    /** 角色沿父链展开后的有效权限 */
    fun getRolePermissions(roleId: String): Set<String>

    /** 用户最终权限 = 角色有效权限 + 用户附加权限(未过期) - 用户禁用 */
    fun getUserPermissions(userId: String): Set<String>

    fun hasPermission(userId: String, permissionCode: String): Boolean

    fun hasAnyPermission(userId: String, permissionCodes: Collection<String>): Boolean

    fun hasAllPermissions(userId: String, permissionCodes: Collection<String>): Boolean

    fun getDataScope(userId: String): DataScope

    /** 用户可见商户 id；null 表示不限制 */
    fun resolveTenantIds(userId: String): Set<String>?

    /** 角色权限变更：失效该角色子树及其下用户缓存 */
    fun refreshCache(roleId: String)

    fun refreshUserCache(userId: String)
}
