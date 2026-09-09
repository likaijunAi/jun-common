package com.jun.common.rbac.model

/**
 * 商户（多级树，根为 admin）
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class Tenant {
    var id: String = ""

    /** 父商户 id，根为 [PathTree.ROOT_ID] */
    var parentId: String = "0"

    /** 物化路径，如 /0/1/5/ */
    var path: String = "/0/"

    /** 层级，根为 0 */
    var level: Int = 0

    var name: String = ""

    /** 商户编码，全局唯一 */
    var code: String = ""

    var status: Int = TenantStatus.ENABLED.value

    var contact: String? = null

    var phone: String? = null

    var remark: String? = null

    var createAt: Long = 0

    var updateAt: Long = 0

    /** 逻辑删除时间戳(ms)，0 表示未删除 */
    var delAt: Long = 0

    fun enabled(): Boolean = status == TenantStatus.ENABLED.value && delAt == 0L
}
