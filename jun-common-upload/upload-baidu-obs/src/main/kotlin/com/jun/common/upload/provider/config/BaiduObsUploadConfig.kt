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
class BaiduObsUploadConfig {
    var baiduObs: List<BaiduObsUploadProperties>? = null

    fun of(name: String): BaiduObsUploadProperties? {
        return baiduObs?.firstOrNull { it.name == name }
    }
}