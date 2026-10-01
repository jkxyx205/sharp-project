# sharp-sms 架构

## 包结构

```
com.rick.sms
├── core
│   ├── Sender               # 短信发送接口（渠道抽象）
│   ├── AliSender            # 阿里云实现
│   └── ValidateCodeSender   # 验证码便捷封装
└── conf
    └── SmsServiceAutoConfiguration   # Spring Boot 自动配置：AliAccessKey + Bean 装配
```

resources：
```
META-INF/spring.factories                                        # Boot 2 自动配置注册
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports   # Boot 3 自动配置注册
```

模块根 `pom.xml` 依赖：
- `spring-boot-starter`
- `com.rick.common:sharp-common`
- `com.aliyun:aliyun-java-sdk-core:4.5.3`（阿里云 SDK 核心，提供 `IAcsClient` / `CommonRequest` 等）

---

## 关键抽象与关系

```
                ┌────────────────────────┐
                │  ValidateCodeSender    │  (验证码场景：把 code 包成 {"code":code})
                └───────────┬────────────┘
                            │ 委托
                            ▼
                     ┌──────────────┐          implements
                     │   Sender     │ ◄──────────────┐
                     │ (interface)  │                 │
                     └──────┬───────┘                 │
                            │                         │
                       注入到                       AliSender
                            │                  (持有 IAcsClient)
                            ▼                         │
                     Spring 容器                     │
                                                  ▼
                                          阿里云 dysmsapi
                                          SendSms (HTTP POST)
```

- `Sender` 是唯一的渠道抽象点。当前只有 `AliSender` 一个实现。
- `ValidateCodeSender` 不实现 `Sender`，是 `Sender` 的策略化封装，专用于验证码模板。
- `AliSender` 依赖 `IAcsClient`（阿里云 SDK 客户端），由配置类构造。

---

## 数据流：一次发送如何路由到渠道

模块内**无数据库**、**无发送记录表**、**无异步队列**、**无回执回调**。一次发送的全流程：

1. 业务调用 `ValidateCodeSender.send(phone, sign, tpl, code)` 或 `Sender.send(phone, sign, tpl, params)`。
2. `ValidateCodeSender` 将 `code` 包装为 `Map{"code":code}`，转调 `Sender.send`。
3. Spring 注入的 `Sender` 实例运行时为 `AliSender`。
4. `AliSender`：
   - Jackson 序列化 `params` 为 JSON（`TemplateParam`）。
   - 构造 `CommonRequest`：`SendSms` / `dysmsapi.aliyuncs.com` / `2017-05-25` / `cn-hangzhou`。
   - `IAcsClient.getCommonResponse(request)` 发起 HTTP POST 到阿里云。
   - 打印响应 `CommonResponse.getData()` 到日志。
   - SDK 异常（`ServerException` / `ClientException`）抛 `RuntimeException`；业务层失败码不解析。
5. 调用返回（或抛异常）。无落库、无回执。

> 因此"发送记录 / 回执 / 状态报告"在 sharp-sms 内部不存在；如业务需要，须由调用方自行持久化。

---

## 配置与启动

启动入口：`SmsServiceAutoConfiguration`（被 `spring.factories` 与 Boot 3 imports 文件双注册）。

装配链（构造顺序）：
1. `AliAccessKey` 绑定 `ali.access-key.id` / `ali.access-key.secret`。
2. `getProfile(AliAccessKey)` → `DefaultProfile.getProfile("cn-hangzhou", id, secret)`。
3. `getClient(IClientProfile)` → `new DefaultAcsClient(profile)`。
4. `getAliSender(IAcsClient)` → `new AliSender(client)`，作为 `Sender` Bean。
5. `getValidateCodeSender(Sender)` → `new ValidateCodeSender(sender)`。

前置条件：配置文件中存在合法的 `ali.access-key.id` / `ali.access-key.secret`，且能访问阿里云 OpenAPI。无独立开关，引入依赖即装配。

---

## 对外依赖

| 依赖 | 用途 |
|---|---|
| `com.aliyun:aliyun-java-sdk-core` | 阿里云 SDK 核心，`IAcsClient` / `DefaultAcsClient` / `CommonRequest` / `CommonResponse` |
| `com.fasterxml.jackson` (经 spring-boot 传递) | `AliSender` 内部 `ObjectMapper` 序列化模板参数 |
| `spring-boot-starter` | 自动配置、`@ConfigurationProperties` |
| `sharp-common` | 平台公共库（具体使用面未在本模块源码中体现，待确认） |

> 注意：`aliyun-java-sdk-core` 仅是 SDK 核心，发送短信不一定需要额外引入 `dysmsapi` 专用包，因为 `AliSender` 用的是通用 `CommonRequest` + `SendSms` action，而非强类型的 `SendSmsRequest`。版本 `4.5.3` 较旧，待确认是否需要随 Boot 3 / JDK 17 升级。

---

## 扩展点

### 新增一个短信渠道（如腾讯云 / 华为云）

实现 `com.rick.sms.core.Sender` 接口，提供渠道构造与发送逻辑，并在 `SmsServiceAutoConfiguration`（或新配置类）中按条件注册：

1. 新建 `com.rick.sms.core.TencentSender implements Sender`，注入腾讯云 SDK 客户端。
2. 在配置类中加 `@Bean @ConditionalOnProperty(prefix="sms.channel", name="tencent", ...)` 等 Bean，或在 `Sender` 上加 `@Primary` / `@ConditionalOnMissingBean` 做多渠道切换。
3. 模板 ID / 签名仍由调用方传入；若要模板由本模块管理，需新增模板模型与持久化层（当前不存在）。

> 当前 `Sender` 接口只有一个 `send` 方法且无返回值，扩展时建议：
> - 引入返回值（发送 ID / 状态）以支持回执；
> - 抽出渠道枚举或 `Channel` 接口；
> - 加入 `@ConditionalOnProperty` 做渠道选择，避免硬编码单一渠道。

### 增强 `AliSender` 的回执/业务码判定

当前 `AliSender` 仅 `log.info(response.getData())`，不判定阿里云业务码。增强方向：
- 解析 `CommonResponse.getData()` 的 JSON，检查 `Code` 字段（成功为 `OK`）。
- 业务失败时抛业务异常或返回结构化结果。

### 增加发送记录 / 回执

模块内无表、无 Mapper、无 Service。需从 0 设计：建 `sms_send_log` 表（phone、sign、template_code、params、渠道、发送时间、状态、回执码），在 `AliSender.send` 前后写入，并提供回调端点接收阿里云回执（阿里云侧需配置上行/下行回执 URL）。

---

## 数据库表

无。本模块不接触数据库，无任何 Mapper / Entity / 表定义。发送记录（如需要）由调用方负责。

---

## 与其他模块的关系

- 被依赖：在 `sharp-dependencies/pom.xml` 中声明为 `${sharp.version}` 版本，供 sharp 全家桶引用。
- 当前**未发现**任何其他模块（含 `sharp-notification`）在 Java 源码中 import `com.rick.sms.*`。即该模块目前是"基础设施 + 待消费"状态，实际接入方以 demo / 业务自建服务为主。使用前需在调用方模块 `pom.xml` 显式引入 `com.rick.sms:sharp-sms`。
