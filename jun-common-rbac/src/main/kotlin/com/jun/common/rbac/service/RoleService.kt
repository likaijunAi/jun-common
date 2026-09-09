package com.jun.common.rbac.service

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Role
import com.jun.common.rbac.model.RolePermission
import com.jun.common.rbac.model.TreeNode
import com.jun.common.rbac.model.req.RoleBindPermissionReq
import com.jun.common.rbac.model.req.RoleCreateReq
import com.jun.common.rbac.model.req.RoleMoveReq
import com.jun.common.rbac.model.req.RoleQuery
import com.jun.common.rbac.model.req.RoleUpdateReq

/**
 * 角色（多级继承）
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface RoleService {

    fun create(req: RoleCreateReq): Role

    fun update(req: RoleUpdateReq): Role

    /** 校验：非内置、无子角色、无用户引用 */
    fun delete(id: String)

    fun move(id: String, newParentId: String): Role

    fun move(req: RoleMoveReq): Role

    fun findById(id: String): Role?

    /** 全量覆盖绑定：grants 授予 / denies 显式拒绝 */
    fun bindPermissions(roleId: String, grants: List<String>, denies: List<String>): List<RolePermission>

    fun bindPermissions(req: RoleBindPermissionReq): List<RolePermission>

    fun addPermission(roleId: String, permissionCode: String, granted: Boolean): RolePermission

    fun removePermission(roleId: String, permissionCode: String)

    /** 本层直接权限（不含继承） */
    fun directPermissions(roleId: String): List<RolePermission>

    /** 沿父链展开后的有效权限 */
    fun effectivePermissions(roleId: String): Set<String>

    /** 自身 + 所有子孙角色 id */
    fun selfAndDescendantIds(id: String): List<String>

    /** 该商户可选作父角色的角色：自有角色 + 平台/祖先商户共享的角色 */
    fun availableParentRoles(tenantId: String): List<Role>

    /** 该角色是否可被指定商户的用户绑定（自有 / 平台或祖先共享） */
    fun isAssignableTo(roleId: String, tenantId: String): Boolean

    fun tree(tenantId: String): List<TreeNode<Role>>

    fun page(query: RoleQuery): Resp<Role>
}
