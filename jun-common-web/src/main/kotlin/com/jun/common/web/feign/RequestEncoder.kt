 package com.jun.common.web.feign

 import cn.hutool.core.util.RandomUtil
 import com.jun.common.core.web.Req
 import feign.RequestTemplate
 import feign.codec.EncodeException
 import org.springframework.beans.factory.ObjectFactory
 import org.springframework.beans.factory.annotation.Autowired
 import org.springframework.boot.autoconfigure.http.HttpMessageConverters
 import org.springframework.cloud.openfeign.support.SpringEncoder
 import java.lang.reflect.Type

 /**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/6/3 23:23
 **/
open class RequestEncoder(private val appId:String?=null)  : feign.codec.Encoder {

     @Autowired
     private val messageConvertersObjectFactory: ObjectFactory<HttpMessageConverters>? = null
     private val encoder: feign.codec.Encoder by lazy {
         SpringEncoder(messageConvertersObjectFactory)
     }

     override fun encode(bodyReq: Any, bodyType: Type, template: RequestTemplate) {

         if(bodyReq is Req){
             bodyReq.appId     = appId
             bodyReq.nonce     = RandomUtil.randomString(20)
             bodyReq.timestamp = (System.currentTimeMillis() / 1000L).toInt()
         }
         springEncoder(bodyReq,bodyType,template)
     }

     @Throws(EncodeException::class)
     fun springEncoder(bodyReq: Any, bodyType: Type, template: RequestTemplate){
         encoder.encode(bodyReq,bodyType,template)
     }

 }