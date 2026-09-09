package com.jun.common.rbac.model

/**
 * 商户可用权限白名单（admin 下发 / 商户自建自动写入）
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class TenantPermission {
    var id: String = ""

    var tenantId: String = ""

    var permissionCode: String = ""

    /** 权限来源商户（下发方），平台为 0 */
    var sourceTenantId: String = "0"

    var createAt: Long = 0
}
