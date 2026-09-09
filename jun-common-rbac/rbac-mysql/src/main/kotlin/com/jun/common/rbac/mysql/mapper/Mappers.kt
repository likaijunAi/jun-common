package com.jun.common.rbac.mysql.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.jun.common.rbac.mysql.entity.PermissionPO
import com.jun.common.rbac.mysql.entity.RolePO
import com.jun.common.rbac.mysql.entity.TenantPO
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Update

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/

@Mapper
interface TenantMapper : BaseMapper<TenantPO> {

    @Update("UPDATE sys_tenant SET del_at = #{now} WHERE id = #{id} AND del_at = 0")
    fun logicDelete(@Param("id") id: String, @Param("now") now: Long): Int

    @Update(
        "UPDATE sys_tenant SET path = CONCAT(#{newPrefix}, SUBSTRING(path, LENGTH(#{oldPrefix}) + 1)), " +
            "level = level + #{delta} WHERE path LIKE CONCAT(#{oldPrefix}, '%') AND del_at = 0"
    )
    fun updatePath(
        @Param("oldPrefix") oldPrefix: String,
        @Param("newPrefix") newPrefix: String,
        @Param("delta") delta: Int
    ): Int
}

@Mapper
interface RoleMapper : BaseMapper<RolePO> {

    @Update("UPDATE sys_role SET del_at = #{now} WHERE id = #{id} AND del_at = 0")
    fun logicDelete(@Param("id") id: String, @Param("now") now: Long): Int

    @Update(
        "UPDATE sys_role SET path = CONCAT(#{newPrefix}, SUBSTRING(path, LENGTH(#{oldPrefix}) + 1)), " +
            "level = level + #{delta} WHERE path LIKE CONCAT(#{oldPrefix}, '%') AND del_at = 0"
    )
    fun updatePath(
        @Param("oldPrefix") oldPrefix: String,
        @Param("newPrefix") newPrefix: String,
        @Param("delta") delta: Int
    ): Int
}

@Mapper
interface PermissionMapper : BaseMapper<PermissionPO> {

    @Update("UPDATE sys_permission SET del_at = #{now} WHERE id = #{id} AND del_at = 0")
    fun logicDelete(@Param("id") id: String, @Param("now") now: Long): Int

    @Update(
        "UPDATE sys_permission SET path = CONCAT(#{newPrefix}, SUBSTRING(path, LENGTH(#{oldPrefix}) + 1)), " +
            "level = level + #{delta} WHERE path LIKE CONCAT(#{oldPrefix}, '%') AND del_at = 0"
    )
    fun updatePath(
        @Param("oldPrefix") oldPrefix: String,
        @Param("newPrefix") newPrefix: String,
        @Param("delta") delta: Int
    ): Int
}
