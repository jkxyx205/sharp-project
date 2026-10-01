# sharp-notification 架构

## 包结构

```
com.rick.notification
├── bark
│   ├── DeviceKey.java                 # @ConfigurationProperties(prefix="bark") 配置 Bean
│   ├── PushNotification.java          # Bark 推送内容实体（@Builder）
│   └── PushNotificationService.java   # Bark 发送入口（OkHttp）
├── wechat
│   ├── WechatKey.java                 # @ConfigurationProperties(prefix="wechat") 配置 Bean
│   ├── TokenService.java              # 微信 access_token 获取 + 本地缓存（final 类）
│   └── TemplateMessageSender.java     # 微信模板消息发送（OkHttp）
├── fcm
│   └── MessagingSendService.java      # FCM 发送（OkHttp，URL/payload 硬编码）
└── conf
    └── NotificationServiceAutoConfiguration.java   # Spring Boot 自动装配
```

`src/main/resources/META-INF/spring.factories`：

```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.rick.notification.conf.NotificationServiceAutoConfiguration
```

## 关键抽象：推送 ↔ 渠道 的真实关系

**没有渠道抽象**。三个渠道是三个独立 Service，各自直接持有 OkHttp 客户端，互不实现共同接口、互不共享基类。不存在 `Notification` / `Channel` / `MessageService` 抽象类型。调用方按渠道分别注入对应 Service：

```
PushNotificationService   ──> DeviceKey (bark.*)
TemplateMessageSender     ──> TokenService ──> WechatKey (wechat.*)
MessagingSendService      (无依赖)
```

这是 **SDK 风格而非应用层风格**：没有"发送一条通知由系统决定走哪条渠道"的抽象，调用方在代码中硬编码选用哪个渠道。

## 数据流

### Bark
```
调用方
  └─ push(PushNotification)
       ├─ 用 DeviceKey 回填缺失的 deviceKey/deviceKeys（合并数组）
       ├─ JsonUtils.toJson(pushNotification)  序列化
       └─ OkHttpClient POST {bark.url}/push
            header: User-Agent: Mozilla/5.0
            body:  application/json
            → 成功 log.info，失败 log.error，异常 log.error（均吞掉）
```

### WeChat 模板消息
```
调用方
  └─ send(openId, templateId, data)
       ├─ tokenService.getToken()
       │     ├─ 缓存有效（≤7000s）→ 返回 token
       │     └─ 失效 → GET api.weixin.qq.com/cgi-bin/token?appid=&secret=
       │           └─ 解析 access_token，缓存并记录获取时间
       └─ 拼接 JSON 字符串（注意：未转义）
            POST api.weixin.qq.com/cgi-bin/message/template/send?access_token=
            → HTTP 非 2xx log.error；IOException 抛 RuntimeException
```

### FCM
```
调用方
  └─ send(to, title, message)
       └─ 拼接 JSON 字符串（注意：未转义）
            POST https://us-central1-fir-cloudmessaging-4e2cd.cloudfunctions.net/send
            → IOException 抛 RuntimeException；非 2xx 不检查
```

## 对外依赖

| 依赖 | 用途 |
|---|---|
| `spring-boot-starter` | 自动配置、`@ConfigurationProperties` |
| `sharp-common` | `com.rick.common.util.JsonUtils`（Bark 序列化、WeChat token 响应解析） |
| `okhttp` | 三个渠道的 HTTP 客户端 |
| `lombok` | `@Data @Builder @RequiredArgsConstructor @Slf4j @FieldDefaults`（来自 `sharp-dependencies`） |
| `commons-lang3` | `StringUtils` / `ArrayUtils`（来自 `sharp-dependencies`，间接） |
| `jackson` | `JsonProperty` 注解、`JsonUtils` 底层（来自 `sharp-dependencies`） |

`pom.xml` 仅显式声明 `spring-boot-starter` / `sharp-common` / `okhttp`，其余通过父 POM `sharp-dependencies` 传递。

## 扩展点

模块未提供抽象接口，扩展方式只能是**改源码**：

- 新增渠道：在 `com.rick.notification.<channel>` 下新建 `XxxSender` 类，并在 `NotificationServiceAutoConfiguration.NotificationServiceConfiguration` 中追加 `@Bean`。
- 新增配置：仿照 `DeviceKey` / `WechatKey` 写 `@ConfigurationProperties` 类，加入 `@Import`。
- FCM 换 URL/payload：直接改 `MessagingSendService.URL` 常量与 `send` 中的字符串模板。

## 配置与启动

- 引入依赖即自动装配，无需 `@ComponentScan`，无需 `@EnableXxx`。
- 三 Bean（`PushNotificationService`、`TemplateMessageSender`、`MessagingSendService`）无条件注册——**未做 `@ConditionalOnProperty`**。即使 `bark.*` / `wechat.*` 配置缺失，Bean 仍会被创建（用到时才报 NPE / 拼出 `null`）。
- FCM 完全无配置，装配即生效。

## 数据库表

**无**。本模块不持久化任何内容，无 entity、无 mapper、无 schema。

## 与 sharp-mail / sharp-sms 的关系

三者是 `sharp` 平台并列的三个外部通讯 SDK：

| 模块 | 通讯域 | 抽象层级 |
|---|---|---|
| `sharp-mail` | 邮件 | 有 `MailHandler` 接口 + `MailHandlerImpl` |
| `sharp-sms` | 短信 | 有 `Sender` 接口 + `AliSender` 等多实现 |
| `sharp-notification` | 移动端推送（Bark/WeChat/FCM） | 无接口，三渠道独立类 |

三者互不依赖、无代码引用关系，仅通过 `sharp-dependencies` 统一版本与传递依赖。
