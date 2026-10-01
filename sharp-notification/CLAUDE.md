# sharp-notification

## 模块边界

外部移动端推送 SDK，封装 Bark（iOS）、WeChat 模板消息、FCM 三个外部 HTTP 推送 API。**非站内信、非消息中心、非通知中心**：无 DB、无收件箱、无渠道抽象接口、无 Controller、无持久化、无已读未读。仅负责"把一条 payload 发到外部 API"。

与 `sharp-mail` / `sharp-sms` 并列，互不依赖。

## 目录约定

- 每个渠道独立子包：`bark` / `wechat` / `fcm`，不混用。
- 配置 Bean 与推送实体放在渠道包内（如 `DeviceKey` 与 `PushNotification` 都在 `bark` 下），不单建 `config` / `entity` 子包。
- 仅 `conf` 是跨渠道的，放自动装配类。
- 新增渠道时遵循同样结构：`com.rick.notification.<channel>/<Sender>.java` + `<Key>.java`，并在 `conf.NotificationServiceAutoConfiguration` 内注册 Bean。

## 编码约定

- 配置类用 `@Configuration @ConfigurationProperties(prefix=...) @Data`（Lombok）。
- 发送类用 `@RequiredArgsConstructor + @Slf4j`，构造注入 `DeviceKey` / `WechatKey`；`TokenService` 是 `final class`。
- 实体用 `@Data @Builder`（如 `PushNotification`），便于链式构造。
- JSON：Bark 用 `sharp-common` 的 `JsonUtils.toJson`；WeChat 与 FCM 用**字符串拼接**（历史代码，未做转义）。
- 日志：成功 `log.info`，HTTP 失败 `log.error` 状态码。
- HTTP 客户端：OkHttp。WeChat 用静态共享 `OkHttpClient`；Bark 每次 `new`（待改进，见陷阱）。

## 常见改动清单

### 加一种推送渠道（如 DingTalk 机器人）
1. 新建 `com.rick.notification.dingtalk.DingTalkKey`（`@ConfigurationProperties(prefix="dingtalk")`）。
2. 新建 `com.rick.notification.dingtalk.DingTalkSender`（`@RequiredArgsConstructor @Slf4j`），注入 `DingTalkKey`，实现 `send(...)` 方法，OkHttp POST。
3. 在 `NotificationServiceAutoConfiguration.NotificationServiceConfiguration`：
   - `@Import` 列表加入 `DingTalkKey.class`；
   - 新增 `@Bean public DingTalkSender dingTalkSender(DingTalkKey key) { return new DingTalkSender(key); }`。
4. 在使用方 yml 加 `dingtalk.*` 配置。
5. （可选）为使用方写一个测试用例，仿 `sharp-demo` 的 `PushNotificationServiceTest`。

### 修改 Bark 默认 key
仅改 yml `bark.device-key` / `bark.device-keys`，无需改代码。

### 修改 FCM 的 URL 或 payload
直接改 `MessagingSendService.URL` 常量与 `send` 内的字符串模板（**硬编码，无配置项**）。

## 构建测试命令

```bash
# 在仓库根目录
mvn -pl sharp-notification -am clean install -DskipTests

# 跑 sharp-demo 中的推送测试（需本机配好 bark.*/wechat.* 真实密钥）
mvn -pl sharp-demo -am test -Dtest=PushNotificationServiceTest
```

模块自身无单元测试目录（`src/test` 为空），推送测试位于 `sharp-demo`。

## 陷阱

### 1. 失败处理三处不一致
- **Bark `PushNotificationService`**：HTTP 失败与 `IOException` 均仅 `log.error`，**完全静默，不抛异常**。调用方拿不到失败信号。
- **WeChat `TemplateMessageSender`**：HTTP 非 2xx `log.error` 不抛；`IOException` **抛 `RuntimeException`**。
- **FCM `MessagingSendService`**：`IOException` **抛 `RuntimeException`**；HTTP 非 2xx **既不检查也不抛**（响应体未读，看似成功）。
- 调用方若按"失败必抛异常"假设封装，Bark/FCM 的 HTTP 层失败会被吞掉。

### 2. JSON 未转义（WeChat / FCM）
`TemplateMessageSender.send` 与 `MessagingSendService.send` 的请求体是 `+` 字符串拼接。若 `openId` / `templateId` / `data` 值 / FCM `title` / `message` 含双引号、换行、反斜杠，会破坏 JSON 结构，可能导致请求被微信/FCM 拒绝或语义错乱。需转义时建议改用 `JsonUtils.toJson`（Bark 已是正确做法）。

### 3. TokenService 线程安全待确认
`token` 与 `tokenGetTime` 为普通实例字段，**非 `volatile`、未同步**。`OkHttpClient` 为 final 共享是安全的，但多线程并发刷新 token 时存在可见性问题与重复刷新竞态。当前唯一调用方是 `TemplateMessageSender`，单线程场景下无碍；若改并发使用需加同步或换 `AtomicReference`。

### 4. token 缓存失败返回 null 的连锁
`TokenService.getToken()` 在 HTTP 失败或响应无 `access_token` 时返回 `null`（或旧 token）。`TemplateMessageSender` 直接拼到 URL：`...?access_token=null`，请求会失败但仅 `log.error`，调用方无感。

### 5. Bark 每次 new OkHttpClient
`PushNotificationService.postJson` 内 `new OkHttpClient.Builder()...build()`，不复用连接池。高频推送会泄漏连接、增加延迟。WeChat 侧用 `static final` 共享是正确做法。

### 6. 无 `@ConditionalOnProperty`
即使 `bark.*` / `wechat.*` 配置全缺，三 Bean 仍会被创建。Bark 在 `push` 时因 `deviceKey.getUrl()` 为 null 抛 NPE；WeChat 同理。装配阶段不会提前失败，问题在使用时才暴露。

### 7. 密钥泄露面
- `wechat.secret` 明文存 yml（标准 Spring 风格，敏感配置需配 Vault / 环境变量外置，本模块不提供机制）。
- `TokenService.getToken()` `log.info("成功获取 token：{}", token)` **打印完整 access_token 到日志**。
- `TemplateMessageSender.send` `log.info` 打印 `data`（模板变量内容），可能含业务敏感信息。
- 生产若日志被聚合采集，token 与模板内容会进入日志系统。改前需评估脱敏。

### 8. FCM 无配置化
FCM 的 Cloud Function URL 与 payload 模板（`ttl=60, priority=high, clipboard=true`）硬编码于 `MessagingSendService`，多环境无法区分。如需多环境，要么改源码，要么在本模块补一个 `@ConfigurationProperties` + 构造注入（当前未做）。
