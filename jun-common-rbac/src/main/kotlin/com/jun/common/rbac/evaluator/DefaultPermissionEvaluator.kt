package com.jun.common.rbac.evaluator

import com.jun.common.rbac.model.Resource
import com.jun.common.rbac.model.User
import com.jun.common.rbac.service.RbacService

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/7/10
 **/
open class DefaultPermissionEvaluator(private val rbacService: RbacService) : PermissionEvaluator {

    override fun hasPermission(user: User, permission: String): Boolean {
        return rbacService.getUserPermissions(user.id).contains(permission)
    }

    override fun hasPermission(user: User, resource: Resource): Boolean {
        return hasPermission(user, resource.requiredPermission)
    }
}
