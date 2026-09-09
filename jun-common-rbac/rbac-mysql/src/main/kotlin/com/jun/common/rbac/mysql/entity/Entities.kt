package com.jun.common.rbac.mysql.entity

import com.baomidou.mybatisplus.annotation.TableName
import com.jun.common.rbac.model.Permission
import com.jun.common.rbac.model.Role
import com.jun.common.rbac.model.RolePermission
import com.jun.common.rbac.model.Tenant
import com.jun.common.rbac.model.TenantPermission
import com.jun.common.rbac.model.User
import com.jun.common.rbac.model.UserPermission

/**
 * MyBatis-Plus 实体（继承抽象层模型，字段全部复用）
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/

@TableName("sys_tenant")
open class TenantPO : Tenant()

@TableName("sys_user")
open class UserPO : User()

@TableName("sys_role")
open class RolePO : Role()

@TableName("sys_permission")
open class PermissionPO : Permission()

@TableName("sys_role_permission")
open class RolePermissionPO : RolePermission()

@TableName("sys_user_permission")
open class UserPermissionPO : UserPermission()

@TableName("sys_tenant_permission")
open class TenantPermissionPO : TenantPermission()
