package com.jun.common.rbac.model

/**
 * 用户附加/临时权限
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class UserPermission {
    var id: String = ""

    var userId: String = ""

    var permissionCode: String = ""

    /** true=追加；false=临时禁用 */
    var granted: Boolean = true

    /** 过期时间戳(ms)，0 表示永久 */
    var expireAt: Long = 0

    var createAt: Long = 0

    fun expired(now: Long = System.currentTimeMillis()): Boolean = expireAt > 0 && expireAt <= now
}
