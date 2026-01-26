package com.jun.common.core.db.mybatis

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 10:53
 **/
class ArrayStringTypeHandler : MyGsonTypeHandler<Array<String>>(Array<String>::class.java)