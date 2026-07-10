package com.jun.common.rbac.repository

import com.jun.common.rbac.model.Resource
import com.jun.common.rbac.model.ResourceType

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/7/10
 **/
interface ResourceRepository {
    fun findAll(): List<Resource>
    fun findByType(type: ResourceType): List<Resource>
}
