package com.jun.common.rbac.model

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/

/** 商户状态 */
enum class TenantStatus(val value: Int) {
    DISABLED(0),
    ENABLED(1);

    companion object {
        fun of(value: Int?): TenantStatus = entries.firstOrNull { it.value == value } ?: ENABLED
    }
}

/** 用户状态 */
enum class UserStatus(val value: Int) {
    DISABLED(0),
    ENABLED(1),
    LOCKED(2);

    companion object {
        fun of(value: Int?): UserStatus = entries.firstOrNull { it.value == value } ?: ENABLED
    }
}

/** 权限类型（原 Resource 已合并进来） */
enum class PermissionType(val value: String) {
    MENU("MENU"),
    BUTTON("BUTTON"),
    API("API"),
    DATA("DATA");

    companion object {
        fun of(value: String?): PermissionType = entries.firstOrNull { it.value.equals(value, true) } ?: API
    }
}

/** 数据范围 */
enum class DataScope(val value: String) {
    /** 全部数据（admin） */
    ALL("ALL"),

    /** 本商户及所有子商户 */
    TENANT_AND_CHILDREN("TENANT_AND_CHILDREN"),

    /** 仅本商户 */
    TENANT("TENANT"),

    /** 仅本人创建的数据 */
    SELF("SELF");

    companion object {
        fun of(value: String?): DataScope = entries.firstOrNull { it.value.equals(value, true) } ?: TENANT
    }
}
