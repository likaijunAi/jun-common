package com.jun.common.rbac.model.req

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class RoleCreateReq {
    var tenantId: String = "0"
    var parentId: String = "0"
    var code: String = ""
    var name: String = ""
    var description: String? = null
    var inherit: Boolean? = null
    var dataScope: String? = null

    /** 是否可被子商户引用为父角色 */
    var shareToChildren: Boolean? = null

    /** 初始授予的权限码 */
    var permissionCodes: List<String>? = null
}

open class RoleUpdateReq {
    var id: String = ""
    var name: String? = null
    var description: String? = null
    var inherit: Boolean? = null
    var dataScope: String? = null
    var shareToChildren: Boolean? = null
    var status: Int? = null
}

open class RoleQuery : PageQuery() {
    var tenantId: String? = null
    var parentId: String? = null
    var code: String? = null
    var name: String? = null
    var status: Int? = null
}

open class RoleMoveReq {
    var id: String = ""
    var newParentId: String = "0"
}

/** 全量覆盖式绑定：grants=授予，denies=显式拒绝 */
open class RoleBindPermissionReq {
    var roleId: String = ""
    var grants: List<String> = emptyList()
    var denies: List<String> = emptyList()
}

open class RoleChangePermissionReq {
    var roleId: String = ""
    var permissionCode: String = ""
    var granted: Boolean = true
}
