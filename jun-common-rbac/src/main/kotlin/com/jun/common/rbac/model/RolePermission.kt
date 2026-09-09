package com.jun.common.rbac.model

/**
 * 角色直接权限：只存本层授予/拒绝，继承结果读时展开
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class RolePermission {
    var id: String = ""

    var roleId: String = ""

    var permissionCode: String = ""

    /** true=授予；false=显式拒绝(deny)，deny 优先 */
    var granted: Boolean = true

    var createAt: Long = 0
}
