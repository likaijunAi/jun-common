# jun-common 手脚架项目

一个基于Spring Boot的通用手脚架项目，提供快速开发企业级应用的基础功能。

## 核心功能

- **自动配置**：提供开箱即用的Web MVC、安全、缓存等配置
- **安全认证**：集成JWT认证、AES加密、请求安全过滤
- **统一响应**：标准化API响应格式和错误处理
- **工具类**：提供加密/解密、签名、脱敏等常用工具
- **缓存管理**：Redis缓存统一配置和管理
- **异常处理**：全局异常处理和统一错误响应

## 快速开始

1. 添加依赖到你的项目：

```kotlin
dependencies {
    implementation("com.jun:jun-common-starter:v20250703-SNAPSHOT")
}
```

2. 在application.yml中配置基本属性：

```yaml
jun:
  web:
    jwt:
      secret: your-jwt-secret
      expire: 3600
    security:
      enabled: true
```

## 核心组件

### 1. 安全认证

使用JWT进行认证：

```kotlin
// 生成JWT token
val payload = JwtPayload(userId = "123", username = "test")
val token = Signer.generateToken(payload, "your-secret", 3600)

// 验证JWT token
val verifiedPayload = Signer.verifyToken(token, "your-secret")
```

### 2. 加密工具

AES加密/解密：

```kotlin
val encrypted = AesUtil.encrypt("plain text", "your-secret-key")
val decrypted = AesUtil.decrypt(encrypted, "your-secret-key")
```

### 3. 统一响应

控制器返回统一响应格式：

```kotlin
@GetMapping("/test")
fun test(): Resp<String> {
    return Resp.success("Hello World")
}
```

### 4. 异常处理

抛出业务异常：

```kotlin
throw JunErrorException(400, "参数错误")
```

## 配置选项

| 配置项 | 描述 | 默认值 |
|--------|------|-------|
| jun.web.jwt.secret | JWT密钥 | 无 |
| jun.web.jwt.expire | JWT过期时间(秒) | 3600 |
| jun.web.security.enabled | 是否启用安全过滤 | true |
| jun.web.trace.enabled | 是否启用请求追踪 | true |

## 最佳实践

1. 所有API返回Resp统一格式
2. 敏感数据使用@Sensitive注解标记
3. 业务异常使用JunErrorException抛出
4. 缓存操作使用JunRedisCache统一管理
