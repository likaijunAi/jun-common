package com.jun.common.rbac.cache

import java.util.concurrent.TimeUnit

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
object RbacCacheKeys {

    const val PREFIX = "rbac"

    /** 角色沿父链展开后的有效权限 */
    const val ROLE_PERM = "$PREFIX:role:eff"

    /** 用户最终权限（角色权限 + 附加权限） */
    const val USER_PERM = "$PREFIX:user:perm"

    /** 商户子孙 id */
    const val TENANT_CHILDREN = "$PREFIX:tenant:children"

    /** 登出 token 黑名单 */
    const val TOKEN_BLACKLIST = "$PREFIX:token:blacklist:"

    /** 权限过期时间：10 分钟 */
    val EXPIRE_SECONDS: Long = TimeUnit.MINUTES.toSeconds(10)

    fun rolePerm(roleId: String): String = "$ROLE_PERM:$roleId"

    fun userPerm(userId: String): String = "$USER_PERM:$userId"

    fun tenantChildren(tenantId: String): String = "$TENANT_CHILDREN:$tenantId"
}
