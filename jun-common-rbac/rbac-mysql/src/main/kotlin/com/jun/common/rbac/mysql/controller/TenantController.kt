package com.jun.common.rbac.mysql.controller

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Tenant
import com.jun.common.rbac.model.TreeNode
import com.jun.common.rbac.model.req.TenantCreateReq
import com.jun.common.rbac.model.req.TenantMoveReq
import com.jun.common.rbac.model.req.TenantQuery
import com.jun.common.rbac.model.req.TenantUpdateReq
import com.jun.common.rbac.model.req.ok
import com.jun.common.rbac.service.TenantService
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
 * 商户
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/9
 **/
@RestController
@RequestMapping("/api/rbac/tenants")
class TenantController(private val tenantService: TenantService) {

    @PostMapping
    fun create(@RequestBody req: TenantCreateReq): Resp<Tenant> = ok(tenantService.create(req))

    @PutMapping
    fun update(@RequestBody req: TenantUpdateReq): Resp<Tenant> = ok(tenantService.update(req))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Resp<Boolean> {
        tenantService.delete(id)
        return ok(true)
    }

    @PutMapping("/{id}/move")
    fun move(@PathVariable id: String, @RequestBody req: TenantMoveReq): Resp<Tenant> =
        ok(tenantService.move(id, req.newParentId))

    @GetMapping("/{id}")
    fun detail(@PathVariable id: String): Resp<Tenant?> = ok(tenantService.findById(id))

    @GetMapping("/tree")
    fun tree(@RequestParam(required = false) rootId: String?): Resp<List<TreeNode<Tenant>>> =
        ok(tenantService.tree(rootId))

    @GetMapping("/{id}/descendants")
    fun descendants(@PathVariable id: String): Resp<List<String>> = ok(tenantService.descendantIds(id))

    @GetMapping("/{id}/ancestors")
    fun ancestors(@PathVariable id: String): Resp<List<String>> = ok(tenantService.ancestorIds(id))

    @GetMapping
    fun page(query: TenantQuery): Resp<Tenant> = tenantService.page(query)
}
