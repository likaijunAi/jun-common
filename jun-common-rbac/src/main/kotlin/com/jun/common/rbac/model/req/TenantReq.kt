package com.jun.common.rbac.model.req

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class TenantCreateReq {
    var parentId: String = "0"
    var name: String = ""
    var code: String = ""
    var contact: String? = null
    var phone: String? = null
    var remark: String? = null

    /**
     * 可选：指定一个「共享角色」作为子商户内置管理员角色的父角色。
     * 该角色需 shareToChildren=true 且属于平台(0)或本商户的祖先商户；
     * 留空则创建一个完全独立的管理员角色。
     */
    var roleId: String? = null

    /** 同时创建该子商户的登录账号（username 全平台唯一） */
    var adminUsername: String? = null
    var adminPassword: String? = null
    var adminNickname: String? = null
    var adminPhone: String? = null
    var adminEmail: String? = null

    /**
     * 分配给该子商户内置「管理员」角色的权限码。
     * 为空时默认继承父商户的全部可用权限（父为根则取全部平台权限）。
     * 只能从父商户的可用权限范围内挑选。
     */
    var permissionCodes: List<String>? = null
}

open class TenantUpdateReq {
    var id: String = ""
    var name: String? = null
    var contact: String? = null
    var phone: String? = null
    var remark: String? = null
    var status: Int? = null
}

open class TenantQuery : PageQuery() {
    var parentId: String? = null

    /** 传入商户 path 时查询其下所有层级子孙 */
    var path: String? = null
    var name: String? = null
    var code: String? = null
    var status: Int? = null
}

open class TenantMoveReq {
    var id: String = ""
    var newParentId: String = "0"
}
