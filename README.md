# jun-common 开发框架

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-blue.svg)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 框架简介

jun-common 是基于Spring Boot的企业级开发框架，提供了一套完整的Web开发解决方案，包含：

✔ **统一响应格式** - 标准化API响应结构  
✔ **全局异常处理** - 自动捕获并转换异常  
✔ **安全过滤器链** - 多层次请求过滤与验证  
✔ **微服务支持** - 增强的Feign客户端能力

## 核心功能

### 1. 过滤器系统

#### 1.1 请求追踪过滤器 (RequestTraceFilter)
- **功能**：为每个请求生成唯一ID，记录请求生命周期
- **配置项**：
  ```yaml
  jun:
    web:
      trace:
        enabled: true  # 启用/禁用过滤器
        header: X-Request-ID  # 请求ID头部名称
  ```
- **使用效果**：
  ```text
  [2023-01-01 10:00:00] [X-Request-ID: abc123] 请求开始
  [2023-01-01 10:00:01] [X-Request-ID: abc123] 请求结束 耗时100ms
  ```

#### 1.2 安全控制过滤器 (SecurityFilter)
- **功能**：
  - JWT令牌验证
  - 权限基础校验
  - 请求来源检查
- **配置示例**：
  ```yaml
  jun:
    web:
      security:
        jwt:
          secret: "your-jwt-secret"
          expire: 3600  # 过期时间(秒)
        ip-check: true  # 启用IP白名单检查
  ```

### 2. 统一响应体 (Resp)

#### 2.1 基础结构
```kotlin
data class Resp<T>(
    val code: Int,      // 状态码
    val message: String,// 提示信息
    val data: T?,       // 响应数据
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun <T> success(data: T) = Resp(200, "成功", data)
        fun failure(code: Int, message: String) = Resp<Void>(code, message, null)
    }
}
```

#### 2.2 使用示例
```kotlin
@GetMapping("/users/{id}")
fun getUser(@PathVariable id: Long): Resp<User> {
    val user = userService.findById(id)
    return Resp.success(user)
}

@PostMapping("/users")
fun createUser(@RequestBody user: User): Resp<Void> {
    if (user.name.isBlank()) {
        return Resp.failure(400, "用户名不能为空")
    }
    userService.save(user)
    return Resp.success(null)
}
```

### 3. 统一异常处理

#### 3.1 异常体系结构
```text
JunErrorException (基础异常)
├── UnauthorizedException (401未授权)
├── ForbiddenException (403禁止访问)
├── NotFoundException (404资源不存在)
└── BusinessException (自定义业务异常)
```

#### 3.2 异常处理器
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(JunErrorException::class)
    fun handleBaseException(e: JunErrorException): ResponseEntity<Resp<Void>> {
        return ResponseEntity
            .status(e.statusCode)
            .body(Resp.failure(e.code, e.message))
    }
    
    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(e: Exception): ResponseEntity<Resp<Void>> {
        log.error("系统异常", e)
        return ResponseEntity
            .status(500)
            .body(Resp.failure(500, "系统繁忙"))
    }
}
```

#### 3.3 自定义异常示例
```kotlin
// 抛出标准异常
throw UnauthorizedException("请先登录")

// 自定义业务异常
class PaymentException(code: Int, message: String) : 
    BusinessException(code, message)

// 使用示例
throw PaymentException(1001, "余额不足")
```

## 完整配置参考

### 基础配置
```yaml
jun:
  web:
    # 请求追踪配置
    trace:
      enabled: true
      header: X-Trace-ID
      log-level: INFO
    
    # 安全配置
    security:
      enabled: true
      jwt:
        secret: "your-secret-key"
        expire: 86400
      cors:
        allowed-origins: "*"
    
    # 响应配置
    response:
      wrap-all: true  # 是否包装所有响应
      ignore-paths: /health,/metrics  # 不包装的路径
```

### 日志配置
```yaml
logging:
  level:
    com.jun: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 最佳实践

### 控制器开发
```kotlin
@RestController
@RequestMapping("/api/v1")
class UserController : BaseController() {
    
    @GetMapping("/users")
    fun listUsers(): Resp<List<User>> {
        return Resp.success(userService.findAll())
    }
    
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(e: UserNotFoundException): Resp<Void> {
        return Resp.failure(404, e.message)
    }
}
```

### 自定义过滤器
```kotlin
@Component
class RateLimitFilter : Filter {
    
    override fun doFilter(request: ServletRequest, 
                         response: ServletResponse,
                         chain: FilterChain) {
        if (rateLimiter.tryAcquire()) {
            chain.doFilter(request, response)
        } else {
            throw TooManyRequestsException("请求过于频繁")
        }
    }
}
```

## 许可证

Copyright 2024 jun-common 开发者

遵循 Apache License 2.0 开源协议，详情见 LICENSE 文件。
