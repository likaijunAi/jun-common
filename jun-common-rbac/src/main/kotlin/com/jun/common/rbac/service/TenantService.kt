package com.jun.common.rbac.service

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.Tenant
import com.jun.common.rbac.model.TreeNode
import com.jun.common.rbac.model.req.TenantCreateReq
import com.jun.common.rbac.model.req.TenantMoveReq
import com.jun.common.rbac.model.req.TenantQuery
import com.jun.common.rbac.model.req.TenantUpdateReq

/**
 * 商户（多级树，根为 admin）
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
interface TenantService {

    /**
     * 新建商户。若 [TenantCreateReq.adminUsername] 不为空，
     * 同时创建该商户的内置「管理员」角色 + 登录账号，并按
     * [TenantCreateReq.permissionCodes] 分配权限（为空则继承父商户全部可用权限）
     */
    fun create(req: TenantCreateReq): Tenant

    fun update(req: TenantUpdateReq): Tenant

    /** 校验：无子商户、无用户、无角色 */
    fun delete(id: String)

    fun move(id: String, newParentId: String): Tenant

    fun move(req: TenantMoveReq): Tenant

    fun findById(id: String): Tenant?

    /** 自身 + 所有子孙 id */
    fun selfAndDescendantIds(id: String): List<String>

    fun descendantIds(id: String): List<String>

    fun ancestorIds(id: String): List<String>

    /** 从 rootId 起的商户树，rootId 为空则整棵树 */
    fun tree(rootId: String?): List<TreeNode<Tenant>>

    fun page(query: TenantQuery): Resp<Tenant>
}
