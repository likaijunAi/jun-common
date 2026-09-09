package com.jun.common.rbac.model.req

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class PermissionCreateReq {
    var tenantId: String = "0"
    var parentId: String = "0"
    var code: String = ""
    var name: String = ""
    var type: String? = null
    var sort: Int? = null
    var remark: String? = null
}

open class PermissionUpdateReq {
    var id: String = ""
    var name: String? = null
    var type: String? = null
    var sort: Int? = null
    var remark: String? = null
}

open class PermissionQuery : PageQuery() {
    var tenantId: String? = null
    var parentId: String? = null
    var code: String? = null
    var name: String? = null
    var type: String? = null
}

open class PermissionMoveReq {
    var id: String = ""
    var newParentId: String = "0"
}

/** admin 向商户下发可用权限 */
open class TenantGrantPermissionReq {
    var tenantId: String = ""
    var permissionCodes: List<String> = emptyList()
}
