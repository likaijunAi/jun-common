package com.jun.common.web.spring

import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerExceptionResolver

/**
 * @author leolee
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * create 2026/1/26 17:54
 **/
@Configuration
class ApplicationHolder(
) : ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext

    companion object {
        @Volatile
        private var holder: ApplicationHolder? = null

        private var handlerExceptionResolver: HandlerExceptionResolver? = null

        fun applicationContext() = holder?.applicationContext

        @JvmStatic
        fun <T : Any> getBean(clazz: Class<T>): T {
            return holder?.applicationContext?.getBean(clazz)
                ?: throw IllegalStateException("ApplicationHolder not initialized")
        }

        @JvmStatic
        fun <T : Any> getBeanOrNull(clazz: Class<T>): T? {
            return runCatching {
                holder?.applicationContext?.getBean(clazz)
            }.getOrNull()
        }

        @JvmStatic
        fun getHandlerExceptionResolver(): HandlerExceptionResolver? {
            holder?.applicationContext ?: throw IllegalStateException("ApplicationHolder not initialized")
            if (handlerExceptionResolver == null && holder!!.applicationContext.getBeanNamesForType(
                    HandlerExceptionResolver::class.java
                ).contains(exceptionResolver)
            ) {
                handlerExceptionResolver = holder!!.applicationContext.getBean(
                    exceptionResolver,
                    HandlerExceptionResolver::class.java
                )
            }
            return handlerExceptionResolver
        }
    }

    @PostConstruct
    fun init() {
        holder = this
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}

private const val exceptionResolver = "handlerExceptionResolver"