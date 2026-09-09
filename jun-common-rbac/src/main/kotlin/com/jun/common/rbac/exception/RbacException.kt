package com.jun.common.rbac.exception

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
enum class RbacErrorCode(val code: Int, val msg: String) {
    UNAUTHORIZED(401, "未登录或登录已失效"),
    FORBIDDEN(403, "没有该操作权限"),
    BAD_REQUEST(400, "参数错误"),

    TENANT_NOT_FOUND(41001, "商户不存在"),
    TENANT_CODE_DUPLICATE(41002, "商户编码已存在"),
    TENANT_HAS_CHILDREN(41003, "存在子商户，不能删除"),
    TENANT_HAS_USER(41004, "存在用户，不能删除"),
    TENANT_HAS_ROLE(41005, "存在角色，不能删除"),
    TENANT_MOVE_TO_CHILD(41006, "不能移动到自己的子商户下"),

    USER_NOT_FOUND(42001, "用户不存在"),
    USER_PASSWORD_ERROR(42002, "用户名或密码错误"),
    USER_DISABLED(42003, "账号已停用"),
    USER_LOCKED(42004, "账号已锁定，请稍后再试"),
    USER_NAME_DUPLICATE(42005, "用户名已存在"),
    USER_ROLE_INVALID(42006, "角色不存在或跨商户"),

    ROLE_NOT_FOUND(43001, "角色不存在"),
    ROLE_CODE_DUPLICATE(43002, "角色编码已存在"),
    ROLE_HAS_CHILDREN(43003, "存在子角色，不能删除"),
    ROLE_HAS_USER(43004, "存在用户引用该角色，不能删除"),
    ROLE_MOVE_TO_CHILD(43005, "不能移动到自己的子角色下"),
    ROLE_BUILTIN(43006, "内置角色不允许删除"),
    ROLE_CROSS_TENANT(43007, "角色不能跨商户"),

    PERMISSION_NOT_FOUND(44001, "权限不存在"),
    PERMISSION_CODE_DUPLICATE(44002, "权限编码已存在"),
    PERMISSION_HAS_CHILDREN(44003, "存在子权限，不能删除"),
    PERMISSION_MOVE_TO_CHILD(44004, "不能移动到自己的子权限下"),
    PERMISSION_NOT_IN_TENANT(44005, "权限不在该商户可用范围内")
}

open class RbacException(
    val errorCode: RbacErrorCode,
    val detail: String? = null,
    override val message: String = detail?.let { "${errorCode.msg}: $it" } ?: errorCode.msg
) : RuntimeException(message)
