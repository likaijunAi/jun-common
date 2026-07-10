package com.jun.common.rbac.repository

import com.jun.common.rbac.model.Permission

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/7/10
 **/
interface PermissionRepository {
    fun findByCodes(codes: List<String>): List<Permission>
}
