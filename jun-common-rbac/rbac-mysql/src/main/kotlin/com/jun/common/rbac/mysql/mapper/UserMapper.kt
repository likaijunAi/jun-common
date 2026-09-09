package com.jun.common.rbac.mysql.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.jun.common.rbac.mysql.entity.UserPO
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
interface UserMapper : BaseMapper<UserPO> {

    @Update("UPDATE sys_user SET del_at = #{now} WHERE id = #{id} AND del_at = 0")
    fun logicDelete(@Param("id") id: String, @Param("now") now: Long): Int
}
