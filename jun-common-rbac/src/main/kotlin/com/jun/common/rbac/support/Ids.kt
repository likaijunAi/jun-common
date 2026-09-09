package com.jun.common.rbac.support

import java.util.UUID

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
object Ids {
    fun next(): String = UUID.randomUUID().toString().replace("-", "")
}
