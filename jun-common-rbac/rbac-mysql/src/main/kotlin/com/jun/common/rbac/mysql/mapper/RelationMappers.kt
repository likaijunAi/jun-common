package com.jun.common.rbac.mysql.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.jun.common.rbac.mysql.entity.RolePermissionPO
import com.jun.common.rbac.mysql.entity.TenantPermissionPO
import com.jun.common.rbac.mysql.entity.UserPermissionPO
import org.apache.ibatis.annotations.Mapper

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/

@Mapper
interface RolePermissionMapper : BaseMapper<RolePermissionPO>

@Mapper
interface UserPermissionMapper : BaseMapper<UserPermissionPO>

@Mapper
interface TenantPermissionMapper : BaseMapper<TenantPermissionPO>
