package com.jun.common.rbac.mysql.controller

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Role
import com.jun.common.rbac.model.RolePermission
import com.jun.common.rbac.model.TreeNode
import com.jun.common.rbac.model.req.RoleBindPermissionReq
import com.jun.common.rbac.model.req.RoleChangePermissionReq
import com.jun.common.rbac.model.req.RoleCreateReq
import com.jun.common.rbac.model.req.RoleMoveReq
import com.jun.common.rbac.model.req.RoleQuery
import com.jun.common.rbac.model.req.RoleUpdateReq
import com.jun.common.rbac.model.req.ok
import com.jun.common.rbac.service.RoleService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 角色
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/9
 **/
@RestController
@RequestMapping("/api/rbac/roles")
class RoleController(private val roleService: RoleService) {

    @PostMapping
    fun create(@RequestBody req: RoleCreateReq): Resp<Role> = ok(roleService.create(req))

    @PutMapping
    fun update(@RequestBody req: RoleUpdateReq): Resp<Role> = ok(roleService.update(req))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Resp<Boolean> {
        roleService.delete(id)
        return ok(true)
    }

    @PutMapping("/{id}/move")
    fun move(@PathVariable id: String, @RequestBody req: RoleMoveReq): Resp<Role> =
        ok(roleService.move(id, req.newParentId))

    @GetMapping("/{id}")
    fun detail(@PathVariable id: String): Resp<Role?> = ok(roleService.findById(id))

    @GetMapping("/tree")
    fun tree(@RequestParam tenantId: String): Resp<List<TreeNode<Role>>> = ok(roleService.tree(tenantId))

    /** 该商户可选作父角色的角色：自有 + 平台/祖先商户共享出来的 */
    @GetMapping("/available")
    fun available(@RequestParam tenantId: String): Resp<List<Role>> =
        ok(roleService.availableParentRoles(tenantId))

    /** 全量覆盖绑定：grants 授予 / denies 显式拒绝 */
    @PostMapping("/{id}/permissions")
    fun bindPermissions(
        @PathVariable id: String,
        @RequestBody req: RoleBindPermissionReq
    ): Resp<List<RolePermission>> {
        req.roleId = id
        return ok(roleService.bindPermissions(req))
    }

    /** 增删单个权限 */
    @PostMapping("/{id}/permissions/one")
    fun changePermission(
        @PathVariable id: String,
        @RequestBody req: RoleChangePermissionReq
    ): Resp<RolePermission> = ok(roleService.addPermission(id, req.permissionCode, req.granted))

    @DeleteMapping("/{id}/permissions/{code}")
    fun removePermission(@PathVariable id: String, @PathVariable code: String): Resp<Boolean> {
        roleService.removePermission(id, code)
        return ok(true)
    }

    /** 本层直接权限（不含继承） */
    @GetMapping("/{id}/permissions/direct")
    fun directPermissions(@PathVariable id: String): Resp<List<RolePermission>> =
        ok(roleService.directPermissions(id))

    /** 沿父链展开后的有效权限 */
    @GetMapping("/{id}/permissions")
    fun effectivePermissions(@PathVariable id: String): Resp<Set<String>> =
        ok(roleService.effectivePermissions(id))

    @GetMapping
    fun page(query: RoleQuery): Resp<Role> = roleService.page(query)
}
