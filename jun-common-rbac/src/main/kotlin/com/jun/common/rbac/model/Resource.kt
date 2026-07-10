package com.jun.common.rbac.model

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/7/10
 **/
enum class ResourceType {
    URL,
    MENU,
    BUTTON,
    DATA
}

interface Resource {
    val id: String
    val type: ResourceType
    val identifier: String
    val requiredPermission: String
}
