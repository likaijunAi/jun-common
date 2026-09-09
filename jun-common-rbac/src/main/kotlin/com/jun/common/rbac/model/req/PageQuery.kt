package com.jun.common.rbac.model.req

import com.jun.common.core.web.Resp

/**
 * 分页查询基类
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class PageQuery {
    var page: Int = 1
    var size: Int = 20
    var sortField: String? = null

    /** asc / desc */
    var sort: String? = null

    fun safePage(): Int = if (page <= 0) 1 else page

    fun safeSize(): Int = when {
        size <= 0 -> 20
        size > 100 -> 100
        else -> size
    }

    fun offset(): Long = (safePage() - 1L) * safeSize()

    fun asc(): Boolean = "asc".equals(sort, true)
}

/** 分页结果统一使用 [Resp]：results = 当前页数据，count = 总数 */
fun <T> pageResp(records: List<T>, total: Long): Resp<T> =
    Resp(Resp.success, results = records, count = total)

/** 单对象成功响应 */
fun <T> ok(result: T): Resp<T> = Resp(Resp.success, result = result)

/** 失败响应 */
fun <T> fail(error: String, code: Int = Resp.fail): Resp<T> = Resp(code, error = error)
