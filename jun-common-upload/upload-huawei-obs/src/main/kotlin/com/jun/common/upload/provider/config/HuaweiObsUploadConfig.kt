package com.jun.common.upload.provider.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/23 16:51
 **/
@Component
@ConfigurationProperties("jun.upload.provider")
class HuaweiObsUploadConfig {
    var huaweiObs: List<HuaweiObsUploadProperties>? = null

    fun of(name: String): HuaweiObsUploadProperties? {
        return huaweiObs?.firstOrNull { it.name == name }
    }
}
