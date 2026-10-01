# sharp-notification API

## 模块定位

`com.rick.notification:sharp-notification:3.0-SNAPSHOT` 是一个**轻量级多渠道推送 SDK**，封装三类外部推送服务的 HTTP 调用：

- **Bark**（iOS 推送，经自建或官方 bark-server）
- **WeChat 模板消息**（公众号 template message）
- **FCM**（Firebase Cloud Messaging，经自定义 Cloud Function 中转）

它**不是站内信 / 消息中心**：没有消息持久化、用户收件箱、未读计数、已读状态、订阅、模板引擎、统一渠道抽象接口、Controller HTTP 入口。它只是"把消息发到某个外部推送服务"的工具集合，调用即发，无回执、无状态。

与 `sharp-mail` / `sharp-sms` 的关系：三者是平级的独立渠道 SDK，sharp-notification 内部**不依赖**它们，也不做聚合路由。如需统一调度，由调用方自行组合。

## 依赖

- `spring-boot-starter`（Bean 装配）
- `sharp-common`（`JsonUtils`）
- `okhttp3`（HTTP 客户端）

## 核心 public 入口

### 1. `com.rick.notification.bark.PushNotificationService`

Bark 推送服务。注入 `DeviceKey` 配置。同步调用 OkHttp POST 到 `{bark.url}/push`。

```java
public void push(PushNotification pushNotification)
```

- `pushNotification`：推送内容模型（见下）。若未显式设置 `deviceKey` / `deviceKeys`，自动回填配置中的默认值；若同时存在，则合并配置默认 keys 与入参 keys。

行为说明：
- **同步**阻塞 HTTP 调用（连接超时 5s，读取超时 10s）。
- 失败仅 `log.error`，**不抛异常**（IO 异常被吞）。调用方无法感知发送失败——如需可靠投递请自行包裹。
- 批量推送（`deviceKeys`）需 bark-server ≥ v2.1.9，且必须 JSON 请求（本服务即用 JSON）。
- 请求头强制 `User-Agent: Mozilla/5.0`（bark 官方服务用 Cloudflare，会拒绝 curl UA）。

### 2. `com.rick.notification.bark.PushNotification`

Bark 推送内容模型，`@Data @Builder`。字段对应 Bark API（参考 https://bark.day.app/#/tutorial ）：

| 字段 | 类型 | 说明 |
|---|---|---|
| `title` | String | 推送标题 |
| `subtitle` | String | 推送副标题 |
| `body` | String | 推送内容 |
| `deviceKey` | String | 单设备 key（JSON 序列化为 `device_key`） |
| `deviceKeys` | String[] | 批量推送 key 数组（`device_keys`） |
| `level` | String | 中断级别：`critical`/`active`/`timeSensitive`/`passive` |
| `volume` | Integer | 重要警告音量 0-10 |
| `badge` | Integer | 角标 |
| `call` | String | `"1"` 时铃声重复 |
| `autoCopy` | String | `"1"` 自动复制 |
| `copy` | String | 指定复制内容 |
| `sound` | String | 自定义铃声 |
| `icon` | String | 自定义图标 URL |
| `group` | String | 分组 |
| `ciphertext` | String | 加密推送密文 |
| `isArchive` | Integer | `1` 保存 |
| `url` | String | 点击跳转 URL |
| `action` | String | 点击动作，`none` 不弹窗 |

### 3. `com.rick.notification.wechat.TemplateMessageSender`

微信公众号模板消息发送。依赖 `TokenService` 获取 access_token。

```java
public void send(String openId, String templateId, Map<String, String> data)
```

- `openId`：接收者 openid
- `templateId`：微信模板 ID
- `data`：模板变量键值对，每个会被序列化为 `{"value":"..."}`

行为说明：
- **同步**调用 `https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=...`。
- **手工拼接 JSON 字符串**（非 ObjectMapper），`data` 的 value **未做 JSON 转义**，含 `"`/`\`/换行会破坏 JSON——待确认是否需调用方自行规避。
- HTTP 失败仅 `log.error`；IO 异常**抛 `RuntimeException`**（与 Bark 不同）。

### 4. `com.rick.notification.wechat.TokenService`

微信 access_token 缓存。`final` 类，注入 `WechatKey`。

```java
public String getToken()
```

- 内存缓存 token，有效期判断为获取后 ≤ 7000 秒（微信实际 7200s，提前 200s 刷新）。**非线程安全**（无同步）。多线程并发刷新可能重复请求。
- 失败返回 `null`（HTTP 失败或响应缺 `access_token`），调用方未处理 null 会 NPE。

### 5. `com.rick.notification.fcm.MessagingSendService`

FCM 推送。**无配置、无依赖注入**，URL 硬编码为 `https://us-central1-fir-cloudmessaging-4e2cd.cloudfunctions.net/send`（一个 Cloud Function 中转）。

```java
public void send(String token, String title, String message)
```

- `token`：客户端 FCM token
- `title`：标题
- `message`：内容

行为说明：
- **同步** POST，手工拼接 JSON。IO 异常**抛 `RuntimeException`**。
- 不读取响应体、不校验结果。`ttl=60`、`priority=high`、`clipboard=true` 硬编码。
- 该 Cloud Function URL 为项目私有的中转地址，外部不可直接复用。

## 配置项

通过 `@ConfigurationProperties` 自动装配，前缀如下：

```yaml
bark:
  url: https://api.day.app        # bark-server 地址（推送拼成 {url}/push）
  device-key: xxx                 # 默认单设备 key（可选）
  device-keys:                    # 默认批量 keys（可选）
    - key1
    - key2

wechat:
  appid: xxx                      # 公众号 appid
  secret: xxx                     # 公众号 secret
```

`fcm` 无配置项（URL 硬编码）。

未配置 `bark.url` 时 `PushNotificationService.push` 会 NPE；未配置 `wechat.*` 时 `TokenService` 拼出的 URL 含 `null`，微信侧返回错误。

## 自动装配

`META-INF/spring.factories` 与 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 同时注册 `NotificationServiceAutoConfiguration`（兼容 Spring Boot 2.x / 3.x）。

`NotificationServiceAutoConfiguration` 内部 `NotificationServiceConfiguration`：
- `@Import({DeviceKey.class, WechatKey.class})` 装配两个配置属性 Bean
- 注册 `PushNotificationService`、`TemplateMessageSender`（内部构造 `TokenService`）、`MessagingSendService` 三个 Bean

引入依赖即生效，无需 `@EnableXxx`。无开关条件注解，无法按需关闭单渠道（待确认是否需加 `@ConditionalOnProperty`）。

## 使用示例

### 1. 发送一条 Bark 通知

```java
@Autowired
PushNotificationService pushNotificationService;

pushNotificationService.push(PushNotification.builder()
        .title("服务器异常通知")
        .subtitle("NPE")
        .body("堆栈摘要...")
        .badge(1)
        .sound("alarm")
        .group("default")
        .url("https://example.com/")
        .build());
// deviceKey/deviceKeys 未传则用配置默认值
```

### 2. 发送微信模板消息

```java
@Autowired
TemplateMessageSender templateMessageSender;

Map<String, String> data = new HashMap<>();
data.put("thing7", "code");
data.put("thing8", "type");   // 单项最多 20 字符（微信侧限制）
templateMessageSender.send("oNB4Jv-...", "QXVUgqdISx-AhKJ...", data);
```

### 3. FCM 推送

```java
@Autowired
MessagingSendService messagingSendService;

messagingSendService.send("f3dNejKHR...", "标题", "内容");
```

### 真实用例（sharp-admin 异常告警）

`SharpAdminApiExceptionHandler` 在捕获未处理 `Exception` 时，**新开线程**异步推送 Bark，避免阻塞响应：

```java
new Thread(() -> pushNotificationService.push(PushNotification.builder()
        .title("sharp-admin：服务器异常通知")
        .subtitle(ex.getMessage())
        .body("url:" + request.getRequestURI() + "\n" + stackTrace)
        .badge(1).sound("alarm").group("default")
        .build())).start();
```

## 查询用户未读 / 标记已读

**不支持**。本模块无消息持久化与已读状态模型。如需站内信能力，需另行实现，本模块不提供。

## 陷阱速查

- Bark/FCM 失败静默（只 log），WeChat 失败抛 `RuntimeException`——混用时注意异常一致性。
- `TemplateMessageSender` 手拼 JSON，value 含特殊字符会破 JSON；`MessagingSendService` 同样手拼。
- `TokenService` 非线程安全，token 缓存无同步。
- 无重试、无幂等、无回调；网络抖动即丢消息。
- README.MD 中提到的 `TelegramSendService` 在当前源码中**不存在**（仅在 README 示例出现）——待确认是否计划中或已移除。
