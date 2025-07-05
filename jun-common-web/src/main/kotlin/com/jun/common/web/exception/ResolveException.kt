package com.jun.common.web.exception

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/1 23:06
 **/
class ResolveException(e:Exception) : RuntimeException(e.message,e.cause)