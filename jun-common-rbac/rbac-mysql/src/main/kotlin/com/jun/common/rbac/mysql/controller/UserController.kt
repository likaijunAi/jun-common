package com.jun.common.rbac.mysql.controller

import com.jun.common.core.web.Resp
import com.jun.common.rbac.model.User
import com.jun.common.rbac.model.req.AssignRoleReq
import com.jun.common.rbac.model.req.ChangeStatusReq
import com.jun.common.rbac.model.req.ResetPasswordReq
import com.jun.common.rbac.model.req.UserCreateReq
import com.jun.common.rbac.model.req.UserQuery
import com.jun.common.rbac.model.req.UserUpdateReq
import com.jun.common.rbac.model.req.ok
import com.jun.common.rbac.service.UserService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 用户
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/9
 **/
@RestController
@RequestMapping("/api/rbac/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun create(@RequestBody req: UserCreateReq): Resp<User> = ok(userService.create(req))

    @PutMapping
    fun update(@RequestBody req: UserUpdateReq): Resp<User> = ok(userService.update(req))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Resp<Boolean> {
        userService.delete(id)
        return ok(true)
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: String): Resp<User?> = ok(userService.findById(id))

    @GetMapping("/by-name/{username}")
    fun byUsername(@PathVariable username: String): Resp<User?> = ok(userService.findByUsername(username))

    @PostMapping("/{id}/role")
    fun assignRole(@PathVariable id: String, @RequestBody req: AssignRoleReq): Resp<Boolean> {
        userService.assignRole(id, req.roleId)
        return ok(true)
    }

    @PostMapping("/{id}/password")
    fun resetPassword(@PathVariable id: String, @RequestBody req: ResetPasswordReq): Resp<Boolean> {
        userService.resetPassword(id, req.password)
        return ok(true)
    }

    @PostMapping("/{id}/status")
    fun changeStatus(@PathVariable id: String, @RequestBody req: ChangeStatusReq): Resp<Boolean> {
        userService.changeStatus(id, req.status)
        return ok(true)
    }

    @PostMapping("/{id}/unlock")
    fun unlock(@PathVariable id: String): Resp<Boolean> {
        userService.unlock(id)
        return ok(true)
    }

    @GetMapping("/{id}/permissions")
    fun permissions(@PathVariable id: String): Resp<Set<String>> = ok(userService.permissions(id))

    @GetMapping
    fun page(query: UserQuery): Resp<User> = userService.page(query)
}
