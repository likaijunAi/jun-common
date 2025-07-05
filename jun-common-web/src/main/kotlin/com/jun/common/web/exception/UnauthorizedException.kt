package com.jun.common.web.exception

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/6/14 17:53
 **/
class UnauthorizedException(code: Int, msg: String? = null) : JunErrorException(code,msg)