package com.jun.common.rbac.repository

import com.jun.common.rbac.model.TenantPermission

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface TenantPermissionRepository {

    fun findByTenantId(tenantId: String): List<TenantPermission>

    fun findByTenantIds(tenantIds: Collection<String>): List<TenantPermission>

    fun exists(tenantId: String, permissionCode: String): Boolean

    fun insertBatch(items: Collection<TenantPermission>): Int

    fun deleteByTenantId(tenantId: String): Int

    fun deleteByTenantIdAndCode(tenantId: String, permissionCode: String): Int

    fun deleteByPermissionCode(permissionCode: String): Int
}
