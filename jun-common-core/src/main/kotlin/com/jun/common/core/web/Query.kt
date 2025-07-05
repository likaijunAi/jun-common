 package com.jun.common.core.web

 import io.swagger.annotations.ApiModelProperty

 /**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/3 23:19
 **/
open class Query:Req(){
     @ApiModelProperty(value = "当前页(默认 1)")
     var page: Int = 1
         get() {
             if (field == 0)
                 return 1
             return field
         }

     @ApiModelProperty(value = "页数(默认 20; max 50)")
     var size: Int = 20
         get() {
             if (field == 0)
                 return 20
             if (field > Resp.queryPageSizeMax)
                 return Resp.queryPageSizeMax
             return field
         }

     @ApiModelProperty(value = "排序 asc or desc")
     var sort: String? = null
         get() {
             if (field == "asc")
                 return "asc"
             else if (field == "desc")
                 return "desc"
             return null
         }

     @ApiModelProperty(value = "排序字段")
     var sortField: String? = null
}
