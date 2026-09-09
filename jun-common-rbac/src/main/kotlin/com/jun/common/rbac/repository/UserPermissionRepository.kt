package com.jun.common.rbac.repository

import com.jun.common.rbac.model.UserPermission

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface UserPermissionRepository {

    fun findByUserId(userId: String): List<UserPermission>

    fun insert(item: UserPermission): UserPermission

    fun insertBatch(items: Collection<UserPermission>): Int

    fun deleteByUserId(userId: String): Int

    fun deleteByUserIdAndCode(userId: String, permissionCode: String): Int

    fun deleteByPermissionCode(permissionCode: String): Int
}
