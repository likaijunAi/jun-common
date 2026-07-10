package com.jun.common.rbac.service

import com.jun.common.rbac.model.Resource
import com.jun.common.rbac.model.ResourceType
import com.jun.common.rbac.model.Role

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/7/10
 **/
interface RbacService {
    fun getUserRoles(userId: String): List<Role>
    fun getUserPermissions(userId: String): Set<String>
    fun getResources(): List<Resource>
    fun getResourcesByType(type: ResourceType): List<Resource>
}
