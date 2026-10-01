# sharp-notification API

## 模块定位

`sharp-notification` **不是站内信 / 消息中心 / 通知中心**，而是**三个外部推送 API 的薄封装 SDK**：

| 渠道 | 用途 | 协议 |
|---|---|---|
| Bark | iOS 设备推送（自建或官方 bark-server） | HTTP POST JSON 到 `{url}/push` |
| WeChat 模板消息 | 微信公众号模板消息 | HTTP POST 到 `api.weixin.qq.com` |
| FCM | Firebase Cloud Messaging（经 Cloud Function 中转） | HTTP POST 到硬编码 URL |

模块特征：**无持久化、无收件箱、无消息表、无渠道抽象接口（无 `Channel`/`NotificationService` 接口）、无 Controller、无 HTTP 入参路由**。三个渠道互相独立，各自有一个 Service 类，调用方直接注入对应 Service 发送。

与 `sharp-mail`（邮件）、`sharp-sms`（短信）是三个并列的外部通讯 SDK，互不依赖，均由 `sharp-dependencies` 统一管理版本。

## 核心 public 入口

### `com.rick.notification.bark.PushNotificationService`

Bark 推送入口。由 `NotificationServiceAutoConfiguration` 注入 `DeviceKey` 后构造为 Bean。

```java
public void push(PushNotification pushNotification)
```
- `pushNotification`：推送内容载体（见下）。`deviceKey` / `deviceKeys` 为空时自动回填配置中的默认值；两者同时存在时合并数组。
- 行为：**同步** HTTP 调用（OkHttp，连接超时 5s，读取超时 10s）。
- 失败处理：HTTP 非 2xx 仅 `log.error` 状态码，**不抛异常**；`IOException` 仅 `log.error`，**不抛异常**。即 **Bark 推送失败完全静默**。
- 注意：每次调用 `new OkHttpClient.Builder()...build()`，**未复用连接池**。

### `com.rick.notification.bark.PushNotification`

Bark 推送内容实体，`@Data @Builder`，字段与 bark-server API 一一对应（参考 https://bark.day.app/#/tutorial）：

| 字段 | 类型 | 说明 |
|---|---|---|
| `title` | String | 推送标题 |
| `subtitle` | String | 副标题 |
| `body` | String | 推送内容 |
| `deviceKey` | String | 单设备 key（`@JsonProperty("device_key")`） |
| `deviceKeys` | String[] | 批量 key 数组（`@JsonProperty("device_keys")`），批量推送需 bark-server ≥ v2.1.9 |
| `level` | String | 中断级别：`critical` / `active` / `timeSensitive` / `passive` |
| `volume` | Integer | 重要警告音量 0–10 |
| `badge` | Integer | 角标 |
| `call` | String | `"1"` 时铃声重复 |
| `autoCopy` | String | `"1"` 自动复制 |
| `copy` | String | 指定复制内容 |
| `sound` | String | 自定义铃声 |
| `icon` | String | 图标 URL |
| `group` | String | 分组 |
| `ciphertext` | String | 加密推送密文 |
| `isArchive` | Integer | `1` 保存推送 |
| `url` | String | 点击跳转 URL |
| `action` | String | 点击动作，`"none"` 不弹窗 |

### `com.rick.notification.bark.DeviceKey`

`@Configuration @ConfigurationProperties(prefix = "bark")`，配置类兼 Bean。

| 字段 | 类型 | 说明 |
|---|---|---|
| `url` | String | bark-server 根地址，如 `https://api.day.app` |
| `deviceKey` | String | 默认单设备 key |
| `deviceKeys` | String[] | 默认批量 key 数组 |

### `com.rick.notification.wechat.TemplateMessageSender`

微信模板消息发送入口。

```java
public void send(String openId, String templateId, Map<String, String> data)
```
- `openId`：接收者 openid
- `templateId`：公众号模板 ID
- `data`：模板变量键值对，会被序列化为 `{"key":{"value":"val"},...}` 结构
- 行为：**同步** HTTP（共享静态 `OkHttpClient`）。调用 `tokenService.getToken()` 取 access_token 拼到 URL。
- 失败处理：HTTP 非 2xx `log.error` 状态码；`IOException` **抛 `RuntimeException`**。
- **JSON 由字符串拼接生成，未做转义**：若 `openId` / `templateId` / `data` 值含双引号或换行，会破坏 JSON 结构。

### `com.rick.notification.wechat.TokenService`

微信公众号 access_token 获取与缓存。`final` 类，由 `WechatKey` 构造。

```java
public String getToken()
```
- 行为：本地缓存 token，距上次获取 ≤ 7000 秒直接返回缓存；否则 HTTP 调用 `api.weixin.qq.com/cgi-bin/token` 刷新。
- 失败处理：HTTP 失败或 `IOException` 返回 `null`；响应体无 `access_token` 时 `log.error` 后返回旧 token（可能为 null）。
- **线程安全待确认**：`token` / `tokenGetTime` 为非 volatile 普通字段，无同步；`OkHttpClient` 为 final 共享。多线程并发刷新存在可见性与重复刷新问题。

### `com.rick.notification.wechat.WechatKey`

`@Configuration @ConfigurationProperties(prefix = "wechat")`。

| 字段 | 类型 | 说明 |
|---|---|---|
| `appid` | String | 公众号 appid |
| `secret` | String | 公众号 secret（敏感） |

### `com.rick.notification.fcm.MessagingSendService`

FCM 推送入口，**无配置类**，Cloud Function URL 与 payload 模板**硬编码**在源码中。

```java
public void send(String to, String title, String message)
```
- `to`：FCM 设备注册 token
- `title`：消息标题
- `message`：消息正文
- 行为：**同步** HTTP POST 到 `https://us-central1-fir-cloudmessaging-4e2cd.cloudfunctions.net/send`（payload 固定 `ttl=60, priority=high, clipboard=true`）。
- 失败处理：`IOException` **抛 `RuntimeException`**；HTTP 非 2xx **不检查、不抛**（响应体未读）。
- **JSON 由字符串拼接生成，未做转义**：`to` / `title` / `message` 含引号或换行会破坏 JSON。

### `com.rick.notification.conf.NotificationServiceAutoConfiguration`

Spring Boot 自动配置入口，通过 `META-INF/spring.factories` 注册为 `EnableAutoConfiguration`。内部 `@Import({DeviceKey.class, WechatKey.class})` 加载两个属性 Bean，并注册 `PushNotificationService`、`TemplateMessageSender`（内含 `TokenService`）、`MessagingSendService` 三个 Bean。引入 `sharp-notification` 依赖即自动生效，无需 `@ComponentScan`。

## 配置项

```yaml
bark:
  url: https://api.day.app        # bark-server 根地址
  device-key: xxx                 # 默认单设备 key（可选）
  device-keys:                    # 默认批量 key 数组（可选）
    - k1
    - k2

wechat:
  appid: wxXXXXXXXX               # 公众号 appid
  secret: XXXXXXXXXXXXXXXX        # 公众号 secret
```

FCM 无配置项。Cloud Function URL 硬编码于 `MessagingSendService.URL`，需更换时改源码。

## 使用示例

最小片段——发送一条 Bark 推送（最常见用法，`sharp-admin` 异常处理即此模式）：

```java
@RequiredArgsConstructor
public class MyService {
    private final PushNotificationService pushNotificationService;

    public void notify(String title, String body) {
        // 异步发送，避免阻塞主流程
        new Thread(() -> pushNotificationService.push(PushNotification.builder()
                .title(title)
                .body(body)
                .sound("alarm")
                .group("default")
                .build())).start();
    }
}
```

微信模板消息：

```java
@RequiredArgsConstructor
public class WechatNotifier {
    private final TemplateMessageSender sender;

    public void notify(String openId, String templateId) {
        sender.send(openId, templateId, Map.of(
                "first", "您好",
                "keyword1", "订单已支付"));
    }
}
```

FCM：

```java
messagingSendService.send(fcmToken, "你好", "内容");
```

## 实际使用面

- `sharp-admin`：全局异常处理器 `SharpAdminApiExceptionHandler` 在 `Exception.class` 时新起线程发 Bark 推送（运维告警）。
- `sharp-demo`：测试用例覆盖 Bark 与 FCM。WeChat 模板消息无现成调用示例。
