package com.jun.common.rbac.model

/**
 * 权限（按商户隔离；tenantId=0 为平台级权限）
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class Permission {
    var id: String = ""

    var tenantId: String = "0"

    var parentId: String = "0"

    var path: String = "/0/"

    var level: Int = 0

    /** 权限标识，如 sys:user:add */
    var code: String = ""

    var name: String = ""

    var type: String = PermissionType.API.value

    var sort: Int = 0

    var remark: String? = null

    var createAt: Long = 0

    var updateAt: Long = 0

    var delAt: Long = 0

    fun typeEnum(): PermissionType = PermissionType.of(type)
}
