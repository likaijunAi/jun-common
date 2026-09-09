package com.jun.common.rbac.model.req

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class UserCreateReq {
    var tenantId: String = "0"
    var username: String = ""
    var password: String = ""
    var nickname: String? = null
    var phone: String? = null
    var email: String? = null
    var avatar: String? = null
    var roleId: String = "0"
    var status: Int? = null
}

open class UserUpdateReq {
    var id: String = ""
    var nickname: String? = null
    var phone: String? = null
    var email: String? = null
    var avatar: String? = null
    var roleId: String? = null
    var status: Int? = null
}

open class UserQuery : PageQuery() {
    var tenantId: String? = null

    /** 传入商户 path 时查询其下所有层级子孙商户的用户 */
    var tenantPath: String? = null
    var roleId: String? = null
    var username: String? = null
    var nickname: String? = null
    var phone: String? = null
    var status: Int? = null
}

open class ResetPasswordReq {
    var id: String = ""
    var password: String = ""
}

open class ChangeStatusReq {
    var status: Int = 1
}

open class AssignRoleReq {
    var userId: String = ""
    var roleId: String = ""
}
