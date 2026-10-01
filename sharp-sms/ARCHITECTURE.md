# sharp-sms 架构

## 包结构

```
com.rick.sms
├── core
│   ├── Sender                 # 渠道抽象接口
│   ├── AliSender              # 阿里云 dysmsapi 实现（唯一渠道）
│   └── ValidateCodeSender     # 验证码场景的便捷封装（委托 Sender）
└── conf
    └── SmsServiceAutoConfiguration   # Spring Boot 自动配置入口
        ├── AliAccessKey              # @ConfigurationProperties("ali.access-key") 内部类
        └── SmsServiceConfiguration   # static 内部类：装配 IAcsClient / Sender / ValidateCodeSender
```

资源：

```
src/main/resources/META-INF/spring.factories   # 注册 EnableAutoConfiguration
```

## 关键抽象

```
ValidateCodeSender ──委托──▶ Sender ◀──implements── AliSender
                                  │                       │
                                  │                       └─持有 IAcsClient（阿里云 SDK 客户端）
                                  ▼
                            （调用方注入）
```

- `Sender` 是**渠道抽象**：签名 `send(phone, sign, templateCode, params)`。新增渠道实现此接口即可。
- `AliSender` 是**渠道实现**：持有一个阿里云 `IAcsClient`，构造 `CommonRequest` 调用 `SendSms`。
- `ValidateCodeSender` **不是渠道**，而是面向"验证码"场景的薄封装：把单个 `code` 字符串包成 `Map{"code": code}` 再调 `Sender`。营销/通知类短信直接用 `Sender`。

注意：`AliSender` 上既有 `@Component` 又在 `SmsServiceConfiguration` 里以 `@Bean` 显式注册。下游若未扫描 `com.rick.sms.core` 包，则只有 `@Bean` 生效（推荐路径）；若扫描到，**待确认**是否产生 Bean 冲突（按 Spring 行为同名注册通常以 `@Bean` 覆盖，但需 `spring.main.allow-bean-definition-overriding=true` 才稳定生效）。

## 数据流：一次发送如何到渠道

```
调用方
  │ send(phone, sign, templateCode, code)
  ▼
ValidateCodeSender
  │ 构造 Map{"code": code}
  │ sender.send(phone, sign, templateCode, params)
  ▼
Sender (接口) ──实际为── AliSender
  │ 1. ObjectMapper 把 params 序列化为 JSON（TemplateParam）
  │ 2. log.info 打印手机号 + params（明文验证码会进日志）
  │ 3. 构造 CommonRequest：
  │    Method=POST  Domain=dysmsapi.aliyuncs.com
  │    Version=2017-05-25  Action=SendSms  RegionId=cn-hangzhou
  │    PhoneNumbers / SignName / TemplateCode / TemplateParam
  │ 4. client.getCommonResponse(request)
  │    ├─ 成功：log.info(response.getData())  ← 不解析 Code 字段
  │    └─ 失败：ServerException/ClientException → RuntimeException("短信发送失败")
  ▼
阿里云 dysmsapi（cn-hangzhou）
```

整条链路**同步阻塞**，无异步、无队列、无重试。调用方拿到正常返回只代表 HTTP/SDK 层面无异常，不代表阿里云业务成功。

## 对外依赖

| 依赖 | 用途 |
|---|---|
| `org.springframework.boot:spring-boot-starter` | 自动装配、`@ConfigurationProperties` |
| `com.rick.common:sharp-common` | 项目公共库（**待确认**本模块实际是否用到——源码未显式 import sharp-common 类，可能仅用于版本对齐） |
| `com.aliyun:aliyun-java-sdk-core:4.5.3` | 阿里云 SDK，提供 `IAcsClient`/`DefaultAcsClient`/`CommonRequest` |
| Jackson（经 spring-boot 传递） | `AliSender` 用 `ObjectMapper` 序列化模板参数 |
| Lombok（传递） | `@Slf4j`、`@RequiredArgsConstructor`、`@Data` |

注意：未引入 `aliyun-java-sdk-dysmsapi` 专用包，而是用 `CommonRequest` + 通用 `SendSms` Action，避免对 dysmsapi 专用模型的强依赖。

## 扩展点：新增一个短信渠道

1. 在 `com.rick.sms.core` 下新增 `XxxSender implements Sender`，实现 `send`。
2. 若该渠道需要客户端（HTTP client、SDK client），在 `SmsServiceAutoConfiguration.SmsServiceConfiguration` 中加 `@Bean` 装配客户端。
3. 决定渠道选择策略：
   - 单渠道替换：直接把 `Sender` 的 `@Bean` 改为返回新实现。
   - 多渠道共存：当前**未提供** `SmsService` 路由层与渠道注册表，需自行引入 `Map<String, Sender>` 或策略模式并加配置开关——本模块不内置此能力。
4. 若需配置项，新增 `@ConfigurationProperties` 类并挂到 `SmsServiceAutoConfiguration`。

无需改 `spring.factories`（自动配置类已注册）。

## 配置与启动

启动流程（应用引入 `sharp-sms` 后）：

1. Spring Boot 读取 `spring.factories` → 装配 `SmsServiceAutoConfiguration`。
2. `AliAccessKey` 绑定 `ali.access-key.id/secret`。
3. `SmsServiceConfiguration` 依次创建：
   - `IClientProfile`：`DefaultProfile.getProfile("cn-hangzhou", id, secret)`
   - `IAcsClient`：`new DefaultAcsClient(profile)`
   - `Sender`：`new AliSender(client)`
   - `ValidateCodeSender`：`new ValidateCodeSender(sender)`
4. 调用方注入 `Sender` 或 `ValidateCodeSender` 即可。

启动前置条件：必须配置 `ali.access-key.id` 与 `ali.access-key.secret`，否则 `DefaultProfile` 以 null 构造，运行时调用才报错（启动期不强制校验）。

## 数据库表

**不涉及**。本模块无任何持久化、无发送记录表、无 ID 生成。若需落库（审计/重试/回执匹配）须由调用方在调用前后自行实现。
