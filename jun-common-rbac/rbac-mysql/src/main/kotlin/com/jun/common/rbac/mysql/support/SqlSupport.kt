package com.jun.common.rbac.mysql.support

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.metadata.OrderItem
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.jun.common.rbac.model.req.PageQuery
import java.util.UUID

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
object SqlSupport {

    /** 未删除条件 */
    private const val DEL_AT = "del_at"

    private val SAFE_COLUMN = Regex("^[A-Za-z_][A-Za-z0-9_]{0,63}$")

    fun <T> notDeleted(): QueryWrapper<T> = QueryWrapper<T>().eq(DEL_AT, 0)

    fun <T> page(query: PageQuery, defaultOrder: String = "id"): Page<T> {
        val p = Page<T>(query.safePage().toLong(), query.safeSize().toLong())
        val field = query.sortField?.takeIf { SAFE_COLUMN.matches(it) } ?: defaultOrder
        if (query.asc()) p.addOrder(OrderItem.asc(field)) else p.addOrder(OrderItem.desc(field))
        return p
    }

    fun <T> order(query: PageQuery, wrapper: QueryWrapper<T>, defaultOrder: String = "id"): QueryWrapper<T> {
        val field = query.sortField?.takeIf { SAFE_COLUMN.matches(it) } ?: defaultOrder
        return if (query.asc()) wrapper.orderByAsc(field) else wrapper.orderByDesc(field)
    }

    fun nextId(): String = UUID.randomUUID().toString().replace("-", "")
}
