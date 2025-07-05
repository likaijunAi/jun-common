package com.jun.common.core.web

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/17 20:10
 **/
open class JunUserInfo {
    var tenantId: String? = null

    var parentTenantId: String? = null

    var username: String? = null

    var nickname: String? = null

    var userType: String? = null

    var roleId: String? = null

    var permissions: List<String>? = null
}