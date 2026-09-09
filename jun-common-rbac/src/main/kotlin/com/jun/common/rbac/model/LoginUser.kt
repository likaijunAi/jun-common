package com.jun.common.rbac.model

import com.jun.common.core.util.JwtPayload

/**
 * 登录用户：token 中只放身份与版本，权限由缓存实时解析
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class LoginUser : JwtPayload() {

    var tenantId: String = "0"

    var tenantPath: String = "/0/"

    var parentTenantId: String = "0"

    var username: String? = null

    var nickname: String? = null

    var roleId: String = "0"

    var rolePath: String = "/0/"

    var dataScope: String = DataScope.TENANT.value

    /** 权限版本，父角色权限变更时递增，用于强制客户端刷新 */
    var permVersion: Long = 0

    fun scope(): DataScope = DataScope.of(dataScope)

    fun toUserInfo(): com.jun.common.core.web.JunUserInfo {
        val info = com.jun.common.core.web.JunUserInfo()
        info.tenantId = tenantId
        info.parentTenantId = parentTenantId
        info.username = username
        info.nickname = nickname
        info.roleId = roleId
        return info
    }
}
