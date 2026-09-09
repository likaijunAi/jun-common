package com.jun.common.rbac.mysql.controller

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Permission
import com.jun.common.rbac.model.TenantPermission
import com.jun.common.rbac.model.TreeNode
import com.jun.common.rbac.model.req.PermissionCreateReq
import com.jun.common.rbac.model.req.PermissionMoveReq
import com.jun.common.rbac.model.req.PermissionQuery
import com.jun.common.rbac.model.req.PermissionUpdateReq
import com.jun.common.rbac.model.req.TenantGrantPermissionReq
import com.jun.common.rbac.model.req.ok
import com.jun.common.rbac.service.PermissionService
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
 * 权限
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/9
 **/
@RestController
@RequestMapping("/api/rbac/permissions")
class PermissionController(private val permissionService: PermissionService) {

    @PostMapping
    fun create(@RequestBody req: PermissionCreateReq): Resp<Permission> = ok(permissionService.create(req))

    @PutMapping
    fun update(@RequestBody req: PermissionUpdateReq): Resp<Permission> = ok(permissionService.update(req))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Resp<Boolean> {
        permissionService.delete(id)
        return ok(true)
    }

    @PutMapping("/{id}/move")
    fun move(@PathVariable id: String, @RequestBody req: PermissionMoveReq): Resp<Permission> =
        ok(permissionService.move(id, req.newParentId))

    @GetMapping("/{id}")
    fun detail(@PathVariable id: String): Resp<Permission?> = ok(permissionService.findById(id))

    /** 平台级 + 指定商户的权限树 */
    @GetMapping("/tree")
    fun tree(@RequestParam tenantId: String): Resp<List<TreeNode<Permission>>> =
        ok(permissionService.tree(tenantId))

    /** 商户可用权限白名单 */
    @GetMapping("/tenant/{tenantId}")
    fun tenantPermissions(@PathVariable tenantId: String): Resp<List<TenantPermission>> =
        ok(permissionService.tenantPermissions(tenantId))

    /** admin 向商户下发可用权限 */
    @PostMapping("/tenant/grant")
    fun grantToTenant(@RequestBody req: TenantGrantPermissionReq): Resp<List<TenantPermission>> =
        ok(permissionService.grantToTenant(req.tenantId, req.permissionCodes))

    /** 收回商户某项可用权限 */
    @DeleteMapping("/tenant/{tenantId}/{code}")
    fun revokeFromTenant(@PathVariable tenantId: String, @PathVariable code: String): Resp<Boolean> {
        permissionService.revokeFromTenant(tenantId, code)
        return ok(true)
    }

    @GetMapping
    fun page(query: PermissionQuery): Resp<Permission> = permissionService.page(query)
}
