package com.jun.common.rbac.mysql.repository

import com.jun.common.rbac.model.RolePermission
import com.jun.common.rbac.model.TenantPermission
import com.jun.common.rbac.model.UserPermission
import com.jun.common.rbac.mysql.entity.RolePermissionPO
import com.jun.common.rbac.mysql.entity.TenantPermissionPO
import com.jun.common.rbac.mysql.entity.UserPermissionPO
import com.jun.common.rbac.mysql.mapper.RolePermissionMapper
import com.jun.common.rbac.mysql.mapper.TenantPermissionMapper
import com.jun.common.rbac.mysql.mapper.UserPermissionMapper
import com.jun.common.rbac.repository.RolePermissionRepository
import com.jun.common.rbac.repository.TenantPermissionRepository
import com.jun.common.rbac.repository.UserPermissionRepository
import com.jun.common.rbac.support.Ids
import org.springframework.stereotype.Repository

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
@Repository
class RolePermissionRepositoryImpl(private val mapper: RolePermissionMapper) : RolePermissionRepository {

    override fun findByRoleId(roleId: String): List<RolePermission> =
        mapper.selectList(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RolePermissionPO>().eq("role_id", roleId)
        )

    override fun findByRoleIds(roleIds: Collection<String>): List<RolePermission> =
        if (roleIds.isEmpty()) emptyList()
        else mapper.selectList(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RolePermissionPO>().`in`("role_id", roleIds)
        )

    override fun insert(item: RolePermission): RolePermission {
        if (item.id.isEmpty()) item.id = Ids.next()
        if (item.createAt <= 0) item.createAt = System.currentTimeMillis()
        mapper.insert(item as RolePermissionPO)
        return item
    }

    override fun insertBatch(items: Collection<RolePermission>): Int {
        var n = 0
        items.forEach { n += mapper.insert(it as RolePermissionPO) }
        return n
    }

    override fun replaceAll(roleId: String, items: Collection<RolePermission>): Int {
        deleteByRoleId(roleId)
        return insertBatch(items)
    }

    override fun deleteByRoleId(roleId: String): Int =
        mapper.delete(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RolePermissionPO>().eq("role_id", roleId)
        )

    override fun deleteByRoleIdAndCode(roleId: String, permissionCode: String): Int =
        mapper.delete(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RolePermissionPO>()
                .eq("role_id", roleId).eq("permission_code", permissionCode)
        )

    override fun deleteByPermissionCode(permissionCode: String): Int =
        mapper.delete(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RolePermissionPO>()
                .eq("permission_code", permissionCode)
        )
}

@Repository
class UserPermissionRepositoryImpl(private val mapper: UserPermissionMapper) : UserPermissionRepository {

    override fun findByUserId(userId: String): List<UserPermission> =
        mapper.selectList(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserPermissionPO>().eq("user_id", userId)
        )

    override fun insert(item: UserPermission): UserPermission {
        if (item.id.isEmpty()) item.id = Ids.next()
        if (item.createAt <= 0) item.createAt = System.currentTimeMillis()
        mapper.insert(item as UserPermissionPO)
        return item
    }

    override fun insertBatch(items: Collection<UserPermission>): Int {
        var n = 0
        items.forEach { n += mapper.insert(it as UserPermissionPO) }
        return n
    }

    override fun deleteByUserId(userId: String): Int =
        mapper.delete(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserPermissionPO>().eq("user_id", userId)
        )

    override fun deleteByUserIdAndCode(userId: String, permissionCode: String): Int =
        mapper.delete(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserPermissionPO>()
                .eq("user_id", userId).eq("permission_code", permissionCode)
        )

    override fun deleteByPermissionCode(permissionCode: String): Int =
        mapper.delete(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserPermissionPO>()
                .eq("permission_code", permissionCode)
        )
}

@Repository
class TenantPermissionRepositoryImpl(private val mapper: TenantPermissionMapper) : TenantPermissionRepository {

    private fun q() = com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TenantPermissionPO>()

    override fun findByTenantId(tenantId: String): List<TenantPermission> =
        mapper.selectList(q().eq("tenant_id", tenantId))

    override fun findByTenantIds(tenantIds: Collection<String>): List<TenantPermission> =
        if (tenantIds.isEmpty()) emptyList() else mapper.selectList(q().`in`("tenant_id", tenantIds))

    override fun exists(tenantId: String, permissionCode: String): Boolean =
        mapper.selectCount(q().eq("tenant_id", tenantId).eq("permission_code", permissionCode)) > 0

    override fun insertBatch(items: Collection<TenantPermission>): Int {
        var n = 0
        items.forEach { item ->
            if (item.id.isEmpty()) item.id = Ids.next()
            if (item.createAt <= 0) item.createAt = System.currentTimeMillis()
            n += mapper.insert(item as TenantPermissionPO)
        }
        return n
    }

    override fun deleteByTenantId(tenantId: String): Int =
        mapper.delete(q().eq("tenant_id", tenantId))

    override fun deleteByTenantIdAndCode(tenantId: String, permissionCode: String): Int =
        mapper.delete(q().eq("tenant_id", tenantId).eq("permission_code", permissionCode))

    override fun deleteByPermissionCode(permissionCode: String): Int =
        mapper.delete(q().eq("permission_code", permissionCode))
}
