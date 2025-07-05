 package com.jun.common.core.web

 import io.swagger.annotations.ApiModelProperty
 import java.io.Serializable

 /**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/3 23:19
 **/
open class Req : Serializable {
     @ApiModelProperty(value = "appId")
     var appId: String? = null

     @ApiModelProperty(value = "timestamp 精确到秒")
     var timestamp: Int? = null

     @ApiModelProperty(value = "随机数")
     var nonce: String? = null
}
