# jun-common 开发框架

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-blue.svg)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![JVM](https://img.shields.io/badge/JVM-17-orange.svg)](https://www.oracle.com/java/technologies/)

## 框架简介

`jun-common` 是一套基于 **Kotlin + Spring Boot 3.4.5 + Java 17** 的企业级公共基础库，采用 Gradle 多模块组织，业务方只需引入一个聚合门面即可获得 Web 通用能力、统一响应、安全过滤、对象存储上传、Redis 缓存与 RBAC 权限等能力。

### 技术栈

| 组件 | 版本 |
| --- | --- |
| Kotlin | 1.9.25 |
| Spring Boot | 3.4.5 |
| Spring Cloud | 2024.0.1 |
| JVM Target | 17 |
| MyBatis-Plus | 3.5.9 |
| Hutool | 5.8.28 |
| java-jwt | 4.4.0 |
| Gson | 2.10 |
| 腾讯云 COS | 5.6.261 |
| AWS S3 SDK | 1.12.762 |
| 百度云 BCE | 0.10.380 |
| 华为云 OBS | 3.25.5 |
| 阿里云 OSS | 3.18.2 |

## 模块结构

```
jun-common
├── jun-common-core          基础核心（不依赖其他内部模块）
├── jun-common-web           Web / 通用能力（依赖 core）
│     ├── 统一响应 Resp
│     ├── 安全过滤器（Basic / JWT / 签名 / 自定义）
│     ├── 请求追踪过滤器
│     ├── 全局异常处理
│     ├── OpenFeign 增强
│     └── 敏感字段脱敏注解
├── jun-common-upload        上传抽象父模块（依赖 core）
│     ├── upload-local-file  本地文件存储（无外部 SDK）
│     ├── upload-tencent-cos 腾讯云 COS
│     ├── upload-aws-s3      AWS S3
│     ├── upload-baidu-obs   百度云 BOS
│     ├── upload-huawei-obs  华为云 OBS
│     └── upload-ali-oss     阿里云 OSS
├── jun-common-rbac          RBAC 权限模型与评估（依赖 core）
├── jun-common-autoconfigure Spring Boot 自动装配（依赖 core/web/upload/rbac）
└── jun-common-starter       聚合门面（api 透传以上所有模块）
```

> 业务方只需依赖 `jun-common-starter`，即可获得全部能力；也可按需单独引入某个子模块。

## 快速开始

### 引入依赖

Maven Central 发布坐标为 `io.github.likaijunai:jun-common-starter`：

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.likaijunai:jun-common-starter:1.0.4.2")
}
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.github.likaijunai</groupId>
    <artifactId>jun-common-starter</artifactId>
    <version>1.0.4.2</version>
</dependency>
```

### 启用自动装配

`jun-common-autoconfigure` 已通过 `META-INF/spring.factories`（`AutoConfiguration.imports`）注册，引入 starter 后自动生效，无需额外注解。

## 核心功能

### 1. 统一响应体（Resp）

位于 `com.jun.common.core.web.Resp`，提供统一的 API 返回结构：

```kotlin
open class Resp<T>(
    var code: Int,        // 状态码（默认成功 0，失败 -1）
    var error: String?,   // 错误信息
    var result: T?,       // 单个对象
    var results: List<T>?,// 对象列表
    var count: Long?      // 总数（分页用）
)
```

常用静态方法：

```kotlin
Resp.success(result)                 // 返回单个对象
Resp.success(results, count)         // 返回列表 + 总数
Resp.success<Unit>()                 // 仅成功，无数据
Resp.fail("错误信息")                 // 失败，默认 code = -1
Resp.fail(errorResp)                 // 透传已有 Resp 的 code/error
resp.isSuccess()                     // 判断成功
```

可配置项（见 [配置参考](#完整配置参考)）：
- `jun.web.resp.successCode` / `jun.web.resp.failCode`：自定义成功/失败码
- `jun.web.resp.queryPageSizeMax`：分页最大页大小（默认 20）

### 2. 安全过滤器

`jun-common-web` 提供四类可组合的安全过滤器，由 `SecurityFilterRegistration` 根据配置动态注册，支持多实例、可排序：

| 过滤器 | 说明 | 配置开关 |
| --- | --- | --- |
| `JunBasicSecurityFilter` | HTTP Basic 鉴权 | `jun.web.security.basicEnable` |
| `JunJwtSecurityFilter` | JWT 令牌鉴权 | `jun.web.security.jwtEnable` |
| `JunSignatureSecurityFilter` | 签名校验（MD5/RSA） | `jun.web.security.signatureEnable` |
| `JunCustomSecurityFilter` | 自定义规则 | `jun.web.security.customEnable` |

鉴权失败默认错误码：
- `errorNoToken`：`4011`（请登录）
- `errorTokenExpired`：`4012`（登录已过期）
- `errorTokenInvalid`：`4013`（登录已失效）

### 3. 请求追踪过滤器

`JunWebRequestTraceFilter` 为每个请求生成唯一追踪标识并记录生命周期：

```yaml
jun:
  web:
    trace:
      enable: true   # 是否启用（默认 true）
      header: false  # 是否将追踪 ID 透传到响应头（默认 false）
```

### 4. 全局异常处理

异常体系位于 `com.jun.common.web.exception`：

- `JunErrorException(code, msg)`：基础业务异常，提供 `toResp()` 转 `Resp`
- `UnauthorizedException`：未授权（401）
- `ResolveException`：参数解析异常

在控制器中直接抛出即可被统一转换为 `Resp` 结构。

### 5. 敏感字段脱敏

通过注解标记响应中需要脱敏的字段：

```kotlin
data class User(
    val name: String,
    @Sensitive(SensitiveType.PHONE) val phone: String
)
```

相关注解：`@Sensitive`、`@SensitiveData`、`@IgnoreLog`。

### 6. OpenFeign 增强

`com.jun.common.web.feign` 提供：
- `RequestInterceptor` / `DefaultRequestInterceptor`：请求拦截，自动附加鉴权信息
- `RequestEncoder` / `ResponseDecoder`：基于 Gson 的编解码

### 7. 对象存储上传

`jun-common-upload` 提供统一的上传抽象 `UploadManager`，屏蔽底层存储差异：

```kotlin
@Autowired
lateinit var uploadManager: UploadManager

// 上传 MultipartFile
val resp: Resp<Media?> = uploadManager.upload(
    dataName = "avatar",          // 对应 provider 配置中的 name
    createBy = "user-001",
    file = multipartFile
)

// 上传 InputStream
val resp2 = uploadManager.upload(
    dataName = "doc",
    inputStream = stream,
    fileName = "a.pdf",
    type = "pdf",
    size = 1024,
    createBy = "user-001"
)

// 获取文件流
val streamResp = uploadManager.getInputStream(dataName = "avatar", path = media.path!!)
```

`Media` 返回对象包含 `mediaId / name / bucket / type / md5 / contentType / size / path / createdBy / createdAt` 等字段。

支持的存储后端（按需引入对应模块）：

| 模块 | 后端 |
| --- | --- |
| `upload-local-file` | 本地磁盘（无外部 SDK） |
| `upload-tencent-cos` | 腾讯云 COS |
| `upload-aws-s3` | AWS S3 |
| `upload-baidu-obs` | 百度云 BOS |
| `upload-huawei-obs` | 华为云 OBS |
| `upload-ali-oss` | 阿里云 OSS |

上传监听器 `UploadListener` 可在上传完成后收到 `UploadEvent` 通知。

### 8. Redis 缓存

`jun-common-core` 提供基于 JSON 的 Redis 模板与缓存抽象：

- `JunJsonRedisTemplate`：以 JSON 序列化存储
- `JunCache` / `JunRedisCache`：统一缓存接口，自动装配（`JunRedisCacheConfig`）
- 支持 `@IgnoreCache`、`CacheReq`、`JunCacheKey` 等辅助能力

### 9. RBAC 权限

`jun-common-rbac` 提供权限模型与评估抽象，业务方实现 `RbacService` 后即可自动装配 `PermissionEvaluator`：

```kotlin
interface RbacService {
    fun getUserRoles(userId: String): List<Role>
    fun getUserPermissions(userId: String): Set<String>
    fun getResources(): List<Resource>
    fun getResourcesByType(type: ResourceType): List<Resource>
}
```

模型：`User` / `Role` / `Permission` / `Resource`；
评估：`PermissionEvaluator`（默认实现 `DefaultPermissionEvaluator`）。

### 10. 工具与常量

`jun-common-core` 还提供：
- `Signer`：Basic / JWT / MD5 / RSA 签名与校验
- `AesUtil`、`DesensitizationUtil`、`JwtPayload`、`TempFileUtil`
- MyBatis 类型处理器：`ArrayStringTypeHandler`、`DataTypeHandler`、`DynamicTypeHandler`、`MyGsonTypeHandler`
- 常量：`Jun.UserType`（USER=1, APP=2）

## 完整配置参考

### Web

```yaml
jun:
  web:
    trace:
      enable: true
      header: false
    resp:
      enable: true
      success-code: 0
      fail-code: -1
      query-page-size-max: 20
    security:
      basic-enable: false
      jwt-enable: false
      signature-enable: false
      custom-enable: false
      # 错误码可自定义
      error-no-token:
        code: 4011
        msg: "请登录"
      error-token-expired:
        code: 4012
        msg: "登录已过期"
      error-token-invalid:
        code: 4013
        msg: "登录已失效"
      # 以 jwt 为例，支持配置多个实例
      jwt:
        - name: jwtFilter
          order: 1
          # secret / expire 等由具体过滤器属性决定
```

### 上传

```yaml
jun:
  upload:
    enable: true
    provider:
      # 本地文件存储
      file:
        - name: local
          upload-path: /data/uploads
          store-dir: /data/uploads
          max-size: 10485760
          type: [jpg, png, pdf]
          prefix: ""
          split-bucket: 1
      # 腾讯云 COS
      cos:
        - name: cos-avatar
          upload-path: avatars
          secret-id: <secretId>
          secret-key: <secretKey>
          region: ap-guangzhou
          bucket: my-bucket-1250000000
      # AWS S3
      r3:
        - name: s3-doc
          upload-path: docs
          access-key-id: <accessKeyId>
          secret-access-key: <secretAccessKey>
          endpoint: https://s3.amazonaws.com
          region: us-east-1
          path-style-access-enabled: false
      # 百度云 BOS
      baidu-obs:
        - name: bos-media
          upload-path: media
          secret-id: <secretId>
          secret-key: <secretKey>
          region: bj
          bucket: my-bos-bucket
      # 华为云 OBS
      huawei-obs:
        - name: obs-media
          upload-path: media
          secret-id: <secretId>
          secret-key: <secretKey>
          region: cn-north-4
          bucket: my-obs-bucket
      # 阿里云 OSS
      ali-oss:
        - name: oss-media
          upload-path: media
          secret-id: <AccessKeyId>
          secret-key: <AccessKeySecret>
          region: cn-hangzhou
          bucket: my-oss-bucket
```

Provider 公共属性：`name`（必填标识）、`upload-path`（必填存储位置）、`max-size`、`type`（允许扩展名列表）、`prefix`、`split-bucket`（默认 1，按 `yyyy-MM/mediaId` 分子目录；为 0 时覆盖原文件）、`bucket`。

### 日志

```yaml
logging:
  level:
    com.jun: DEBUG
```

## 本地构建

```bash
# 编译所有模块
./gradlew build

# 仅编译指定模块
./gradlew :jun-common-starter:build

# 发布到 Maven Central（需配置 gradle.properties 中的签名与账号）
./gradlew publish
```

版本与仓库地址集中在根目录 `constants.gradle.kts` 与 `gradle.properties`，各子模块仅声明依赖，不写死版本号。

## 许可证

Copyright © jun-common 开发者

遵循 [Apache License 2.0](LICENSE) 开源协议。
