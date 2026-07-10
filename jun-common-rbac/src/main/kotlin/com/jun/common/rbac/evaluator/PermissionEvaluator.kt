package com.jun.common.rbac.evaluator

import com.jun.common.rbac.model.Resource
import com.jun.common.rbac.model.User

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/7/10
 **/
interface PermissionEvaluator {
    fun hasPermission(user: User, permission: String): Boolean
    fun hasPermission(user: User, resource: Resource): Boolean
}
