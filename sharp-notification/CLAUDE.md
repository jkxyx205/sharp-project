# sharp-notification CLAUDE.md

## 模块边界

- 定位：**多渠道推送 SDK**（Bark / WeChat 模板消息 / FCM），封装外部 HTTP 调用。
- **不是**站内信、消息中心、统一通知网关。无持久化、无收件箱、无已读状态、无订阅、无模板引擎、无渠道抽象接口、无 Controller。
- 不依赖 `sharp-mail` / `sharp-sms`，三者平级独立。
- 仅向外暴露 3 个 Service Bean + 2 个配置属性类，通过 Spring Boot AutoConfiguration 自动装配。

## 目录约定

```
src/main/java/com/rick/notification/
├── bark/        # Bark 推送：Service + PushNotification(DTO) + DeviceKey(配置)
├── wechat/      # 微信模板消息：Sender + TokenService + WechatKey(配置)
├── fcm/         # FCM 推送：MessagingSendService（无配置）
└── conf/        # NotificationServiceAutoConfiguration 自动装配
src/main/resources/META-INF/spring/
├── spring.factories                              # Boot 2.x 自动装配注册
└── org.springframework.boot.autoconfigure.AutoConfiguration.imports  # Boot 3.x
```

约定：每个渠道一个子包，包内含 `Service` + 必要的 `Key` 配置类 + DTO。新增渠道照此布局。

## 编码约定

- Service 类**不加 Spring 注解**（`@Service` 等），统一在 `NotificationServiceAutoConfiguration` 用 `@Bean` 显式注册——保持 POJO 风格，便于单测。
- 配置类用 `@Configuration` + `@ConfigurationProperties`（非 `@Component`），由 `@Import` 导入。
- HTTP 客户端统一用 `okhttp3`，不用 `sharp-common` 的 HttpUtils（Bark 因 Cloudflare 拒 curl UA 改用 OkHttp 直发）。
- 日志用 `lombok.@Slf4j`。
- 字段不可变用 `@FieldDefaults(makeFinal=true, level=PRIVATE)` + `@RequiredArgsConstructor`（Bark）。
- 序列化：Bark 用 `JsonUtils.toJson`；WeChat / FCM **手工拼接 JSON 字符串**（历史遗留，勿轻易改以免破坏与现有模板的兼容）。

## 常见改动清单

### 加一种通知渠道（如钉钉、Telegram）

1. 新建子包 `com.rick.notification.<channel>`。
2. 写 `XxxSendService`（POJO，加 `@Slf4j`，构造注入配置 Key）。
3. 若需配置，写 `XxxKey extends @ConfigurationProperties(prefix="<channel>")`。
4. 在 `NotificationServiceAutoConfiguration.NotificationServiceConfiguration` 加 `@Bean` 方法；若新增了 `XxxKey`，加入 `@Import` 列表。
5. 在 `sharp-demo` 加测试，并在调用方模块 `application.yml` 配置对应前缀。
6. 更新 `API.md` / `ARCHITECTURE.md`。

> README.MD 中提到的 `TelegramSendService` 当前源码中**不存在**——若要实现，参考上述步骤。

### 调整 Bark 推送字段

直接改 `PushNotification`（`@Data @Builder`），字段名与 Bark API JSON 对齐（用 `@JsonProperty` 控制 key）。勿改 `PushNotificationService` 逻辑。

### 修改 token 缓存策略

`TokenService` 是 `final`，无法继承。直接改源码，或替换 `NotificationServiceAutoConfiguration` 中 `templateMessageSender` Bean 的 `TokenService` 构造。

## 构建测试命令

```bash
# 在项目根目录
mvn -pl sharp-notification -am clean install -DskipTests

# 运行 demo 模块中的推送测试（需配置真实 bark/wechat 凭据）
mvn -pl sharp-demo -am test -Dtest=PushNotificationServiceTest
```

测试需真实外部凭据（`bark.url`/`bark.device-key`、`wechat.appid`/`secret`、FCM token），无 mock，CI 中通常跳过。

## 陷阱

- **消息丢失无感知**：Bark / FCM 失败只 `log.error`，不抛异常，调用方以为成功。WeChat 失败抛 `RuntimeException`。三个渠道异常语义不一致，混用要小心。
- **无重试 / 无幂等**：网络抖动即丢消息，无补偿。重要告警建议调用方自行重试或落库后异步重试。
- **异步靠调用方**：模块本身同步阻塞；`sharp-admin` 用裸 `new Thread()` 异步推送，无线程池、无异常处理——线程内异常会被吞。
- **WeChat JSON 注入风险**：`TemplateMessageSender` 手拼 JSON，`data` value 未转义；含 `"`、`\`、换行会破坏 JSON 结构。FCM 的 title/message 同样手拼。
- **TokenService 非线程安全**：token 字段无同步，并发首次获取会重复请求微信接口（不致命但浪费配额）。
- **TokenService 返回 null**：HTTP 失败或微信返回 `errmsg` 时返回 null，`TemplateMessageSender` 直接拼到 URL，请求必然失败但异常被吞。
- **无用户偏好 / 免打扰**：模块不知晓"用户"概念，无静默时段、无渠道偏好，全部由调用方决定。
- **模板变量限制**：WeChat 模板单项 value 限 20 字符（微信侧限制，非本模块），超出微信会拒。
- **FCM URL 是私有中转**：`MessagingSendService.URL` 硬编码为某 Cloud Function，外部环境无法直接复用，迁移需改源码。
- **无开关**：引入 jar 即装配全部三渠道 Bean，无法按需关闭单个渠道（无 `@ConditionalOnProperty`）。
- **`DeviceKey` / `WechatKey` 配置缺失不报错**：Bean 仍创建，调用时才 NPE 或请求失败。

## 文档同步要求

修改 Service 方法签名、配置前缀、新增渠道时，**必须同步更新 `API.md`**；改包结构、装配方式、数据流时同步 `ARCHITECTURE.md`。代码示例须与真实签名逐字一致，勿编造。
