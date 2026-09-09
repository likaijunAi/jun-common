package com.jun.common.rbac.model

/**
 * 用户（只属于一个角色）
 *
 * username 全平台唯一，登录不需要租户信息
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class User {
    var id: String = ""

    var tenantId: String = "0"

    var username: String = ""

    var password: String = ""

    var salt: String = ""

    var nickname: String? = null

    var phone: String? = null

    var email: String? = null

    var avatar: String? = null

    /** 用户只属于一个角色 */
    var roleId: String = "0"

    var status: Int = UserStatus.ENABLED.value

    var loginFailCount: Int = 0

    /** 锁定截止时间戳(ms)，0 表示未锁定 */
    var lockUntil: Long = 0

    var lastLoginAt: Long = 0

    var lastLoginIp: String? = null

    var createAt: Long = 0

    var updateAt: Long = 0

    var delAt: Long = 0

    fun enabled(): Boolean = status == UserStatus.ENABLED.value && delAt == 0L

    fun locked(now: Long = System.currentTimeMillis()): Boolean = lockUntil > now
}
