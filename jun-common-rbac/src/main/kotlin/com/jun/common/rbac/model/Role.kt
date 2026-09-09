package com.jun.common.rbac.model

/**
 * 角色（多级继承，同一商户内成树）
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class Role {
    var id: String = ""

    var tenantId: String = "0"

    var parentId: String = "0"

    /** 物化路径，如 /0/3/7/ */
    var path: String = "/0/"

    var level: Int = 0

    var code: String = ""

    var name: String = ""

    var description: String? = null

    /** 是否继承父角色权限，false 表示自成一棵权限树 */
    var inherit: Boolean = true

    var dataScope: String = DataScope.TENANT.value

    /** 是否可被子商户引用为父角色（权限模板语义），子商户可在此基础上本地增减权限 */
    var shareToChildren: Boolean = false

    var status: Int = 1

    /** 内置角色不允许删除 */
    var builtin: Boolean = false

    var createAt: Long = 0

    var updateAt: Long = 0

    var delAt: Long = 0

    fun scope(): DataScope = DataScope.of(dataScope)
}
