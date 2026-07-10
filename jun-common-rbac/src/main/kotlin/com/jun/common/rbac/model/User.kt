package com.jun.common.rbac.model

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/7/10
 **/
interface User {
    val id: String
    val username: String
    val roleIds: List<String>
}
