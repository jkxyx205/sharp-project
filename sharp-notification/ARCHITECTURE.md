# sharp-notification 架构

## 总览

`sharp-notification` 是一个**薄封装推送 SDK**，不是消息中心。它把三类外部推送服务的 HTTP 调用各自封装成 Spring Bean，不持久化、不聚合、不路由。

```
调用方业务代码
   │  注入并调用
   ▼
┌──────────────────────────────────────────────┐
│ sharp-notification                            │
│  PushNotificationService ──► Bark HTTP API   │
│  TemplateMessageSender  ──► WeChat API       │
│  MessagingSendService   ──► FCM Cloud Func   │
│  (各自独立，无公共渠道接口，无路由表)          │
└──────────────────────────────────────────────┘
```

三个渠道服务彼此**完全独立**：没有公共 `Channel` 抽象、没有路由策略、没有"一次发送多渠道分发"机制。调用方要发哪个渠道，就注入对应 Service 直接调。

## 包结构

```
com.rick.notification
├── bark
│   ├── DeviceKey                  # @ConfigurationProperties(prefix="bark") 配置 Bean
│   ├── PushNotification          # 推送内容模型 (@Data @Builder)
│   └── PushNotificationService   # 推送服务（注入 DeviceKey）
├── wechat
│   ├── WechatKey                 # @ConfigurationProperties(prefix="wechat") 配置 Bean
│   ├── TokenService              # access_token 内存缓存（注入 WechatKey）
│   └── TemplateMessageSender     # 模板消息发送（注入 TokenService）
├── fcm
│   └── MessagingSendService      # FCM 发送（无状态、无配置，URL 硬编码）
└── conf
    └── NotificationServiceAutoConfiguration   # 自动装配入口
```

## 关键抽象关系

**不存在"通知↔渠道↔模板↔订阅"四元关系**，因为：

- **通知** = 直接的 Service 方法调用，无 Notification 实体（Bark 例外有 `PushNotification` DTO）。
- **渠道** = 三个互不相关的 Service 类，无公共接口。要新增渠道就新建一个并列的 Service。
- **模板** = 无模板引擎。WeChat 的 `templateId` 由调用方在每次调用时传入，模板变量 `data` Map 也是调用方组装。
- **订阅** = 无此概念。

各 Service 与配置/依赖的协作：

```
DeviceKey(bark.*) ──► PushNotificationService ──► PushNotification ──POST──► {bark.url}/push

WechatKey(wechat.*) ──► TokenService ──(缓存token)──► TemplateMessageSender ──POST──► api.weixin.qq.com/.../template/send

(none) ──► MessagingSendService ──POST──► 硬编码 Cloud Function URL
```

## 数据流（一次发送如何路由到各渠道）

**没有路由层**。一次发送 = 调用方直接选定某个 Service 的某个方法。以 Bark 为例：

1. 调用方注入 `PushNotificationService`，调用 `push(PushNotification)`。
2. Service 检查入参的 `deviceKey`/`deviceKeys`，为空则回填 `DeviceKey` 配置默认值，非空则合并。
3. 用 `JsonUtils.toJson` 序列化整个 `PushNotification`。
4. OkHttp 客户端 POST 到 `{bark.url}/push`，超时 5s/10s，UA 强制 `Mozilla/5.0`。
5. 响应成功 `log.info`、失败 `log.error`，IO 异常吞掉。返回 void。

WeChat / FCM 流程类似，但：WeChat 多一步先经 `TokenService.getToken()` 取/缓存 access_token；FCM 无配置直接 POST。

所有渠道均为**同步阻塞** HTTP 调用。生产中如 `sharp-admin` 的异常告警通过**业务侧新开 `Thread`** 实现异步——模块自身不提供异步。

## 对外依赖

| 依赖 | 用途 |
|---|---|
| `spring-boot-starter` | Bean 装配、`@ConfigurationProperties` |
| `sharp-common` (`com.rick.common.util.JsonUtils`) | Bark 序列化、WeChat token 响应解析 |
| `okhttp3` | 全部三渠道的 HTTP 客户端 |
| `commons-lang3`（随 sharp-common 传入） | `StringUtils`/`ArrayUtils` |

**不依赖** `sharp-mail` / `sharp-sms`，三者平级。

## 扩展点

当前**没有显式扩展点**（无 SPI、无接口、无事件）。常见扩展方式：

- **新增渠道**：在 `com.rick.notification.<channel>` 下新建 `XxxSendService`（无注解，由 `NotificationServiceAutoConfiguration` 显式 `@Bean` 注册），仿照现有三类的写法。目前没有公共 `Channel` 接口可继承——若希望统一调度，需自行抽象。
- **监听发送事件**：不支持。Service 直接发，不发布 Spring Event。如需监听需自行改造。
- **自定义 token 缓存**：`TokenService` 是 `final` 类，无法继承，需替换 Bean 定义。

## 配置与启动

- 配置前缀：`bark.*`、`wechat.*`（见 API.md）。
- 自动装配：`NotificationServiceAutoConfiguration` 通过 `spring.factories`（Boot 2.x）与 `AutoConfiguration.imports`（Boot 3.x）双注册。
- 无 `@ConditionalOnXxx`，引入 jar 即生效；不配置对应属性时 Bean 仍会创建，但调用会因字段为 null 失败。
- 无开关关闭单渠道——要关只能排除整个 AutoConfiguration 或不引依赖。

## 数据库表

**无**。模块不持久化任何消息、不读不写数据库，无 `mybatis`/`jpa` 依赖，无 schema/migration 脚本。

## 设计取舍

- 三个 Service 各自直连外部 API，无抽象层 → 简单直接，但渠道间行为不一致（异常处理、超时、配置）已显现。
- 全部同步阻塞 + 无重试 → 适合告警类低频场景，**不适合高频或需可靠投递场景**。
- 配置 Bean 用 `@Configuration` + `@ConfigurationProperties` 而非 `@Component`，由 `@Import` 显式导入，避免全包扫描。

## 真实使用面

- `sharp-admin` `SharpAdminApiExceptionHandler`：未捕获异常时新线程推 Bark（运维告警）。
- `sharp-demo` 测试：Bark + FCM 示例。
- 项目内未发现 WeChat 渠道的实际业务调用点（仅 demo/README 中有示例）。
