package com.jun.common.core.web

import io.swagger.annotations.ApiModelProperty
import java.io.Serializable

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/3 23:19
 **/
open class Resp<T> @JvmOverloads constructor(
    @ApiModelProperty(value = "错误码")
    var code: Int,
    @ApiModelProperty(value = "错误信息")
    var error: String? = null,
    @ApiModelProperty(value = "单个对象")
    var result: T? = null,
    @ApiModelProperty(value = "对象列表")
    var results: List<T>? = null,
    @ApiModelProperty(value = "总对象数目")
    var count: Long? = null
) : Serializable {

    companion object {
        var success = 0
        var fail = -1
        var queryPageSizeMax = 20

        @JvmOverloads
        fun <T> success(code: Int = success): Resp<T?> {
            return Resp(code)
        }

        @JvmOverloads
        fun <T> success(result: T, code: Int = success): Resp<T?> {
            return Resp(code, result = result)
        }

        @JvmOverloads
        fun <T> success(results: List<T>, count: Long? = null, code: Int = success): Resp<T?> {
            return Resp(code, results = results, count = count)
        }

        @JvmOverloads
        fun <T> fail(error: String?, code: Int = fail): Resp<T?> {
            return Resp(code, error = error)
        }
    }
}