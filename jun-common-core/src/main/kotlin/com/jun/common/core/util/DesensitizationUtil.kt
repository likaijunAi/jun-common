package com.jun.common.core.util

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2025/5/11 23:00
 **/
object DesensitizationUtil {
    fun maskMobile(mobile: String): String {
        return mobile.replace("(\\d{3})\\d{4}(\\d{4})".toRegex(), "$1****$2")
    }

    fun maskIdCard(idCard: String): String {
        return idCard.replace("(\\d{4})\\d{10}(\\d{4})".toRegex(), "$1**********$2")
    }

    fun maskEmail(email: String): String {
        return email.replace("(\\w{2})\\w+(\\@\\w+\\.[a-z]{2,})".toRegex(), "$1****$2")
    }
}