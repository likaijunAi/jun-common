package com.jun.common.web.config

import com.jun.common.core.web.Resp
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2024/7/7 13:19
 **/
@ConfigurationProperties("jun.web.resp")
class JunWebRespProperties {

    private var successCode: Int = Resp.success

    private var failCode: Int = Resp.fail

    private var queryPageSizeMax: Int = Resp.queryPageSizeMax

    @ConditionalOnProperty("jun.web.resp.enable")
    fun onConfigLoaded() {
        Resp.queryPageSizeMax = this.queryPageSizeMax
        Resp.success = this.successCode
        Resp.fail = this.failCode
    }
}