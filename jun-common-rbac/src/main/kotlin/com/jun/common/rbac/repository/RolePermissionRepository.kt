package com.jun.common.rbac.repository

import com.jun.common.rbac.model.RolePermission

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface RolePermissionRepository {

    fun findByRoleId(roleId: String): List<RolePermission>

    fun findByRoleIds(roleIds: Collection<String>): List<RolePermission>

    fun insert(item: RolePermission): RolePermission

    fun insertBatch(items: Collection<RolePermission>): Int

    /** 先删除该角色全部记录再写入 */
    fun replaceAll(roleId: String, items: Collection<RolePermission>): Int

    fun deleteByRoleId(roleId: String): Int

    fun deleteByRoleIdAndCode(roleId: String, permissionCode: String): Int

    fun deleteByPermissionCode(permissionCode: String): Int
}
