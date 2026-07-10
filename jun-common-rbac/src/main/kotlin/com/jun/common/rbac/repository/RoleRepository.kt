package com.jun.common.rbac.repository

import com.jun.common.rbac.model.Role

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/7/10
 **/
interface RoleRepository {
    fun findByIds(ids: List<String>): List<Role>
}
