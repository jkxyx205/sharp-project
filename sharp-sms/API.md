# sharp-sms API 参考

## 模块定位

`sharp-sms` 是 sharp 平台的短信发送基础模块。它对阿里云短信服务（dysmsapi）做了轻量封装，对外暴露统一的 `Sender` 接口与一个验证码便捷封装 `ValidateCodeSender`，由 Spring Boot 自动装配开箱即用。

能力边界（实际现状，非目标描述）：
- 仅支持阿里云一个短信渠道。
- 仅提供同步发送；无异步、无批量、无发送回执/回调处理、无频控、无发送记录落库。
- 不管理模板与签名，模板 ID、签名名称均由调用方在调用时传入。
- 不做模板变量校验，变量缺失/不匹配由阿里云侧报错。

> 待确认：模块名为 `sharp-sms`，但目前未发现任何"多渠道抽象"的演进代码，`Sender` 接口虽为扩展预留，实际仅 `AliSender` 一个实现。

---

## 核心 public 入口

### 1. `com.rick.sms.core.Sender`（接口）

短信发送的统一抽象。所有渠道实现该接口；上层调用面向 `Sender` 编程。

```java
void send(String phone, String sign, String templateCode, Map<String, String> params);
```

参数：
- `phone` — 接收手机号
- `sign` — 短信签名名称（如 `"某某科技"`，需在阿里云控制台审批通过）
- `templateCode` — 短信模板 code（如 `"SMS_123456789"`，需在阿里云控制台审批通过）
- `params` — 模板变量键值对，key 与模板中的占位符 `${key}` 对应；可为空 map

行为：同步发送。实现内部决定异常语义（见各实现）。本接口不返回回执、不返回发送 ID。

### 2. `com.rick.sms.core.AliSender`（阿里云实现，`@Component`）

`Sender` 的阿里云实现，调用阿里云 `SendSms` 接口。

```java
@Component
public class AliSender implements Sender {
    public AliSender(IAcsClient client) { ... }
    public void send(String phone, String signName, String templateCode, Map<String, String> params);
}
```

行为：
- 将 `params` 用 Jackson 序列化为 JSON，作为 `TemplateParam` 传给阿里云；序列化失败抛 `RuntimeException("短信参数错误")`。
- 调用 `IAcsClient.getCommonResponse` 发起 POST：
  - domain: `dysmsapi.aliyuncs.com`
  - version: `2017-05-25`
  - action: `SendSms`
  - RegionId: `cn-hangzhou`（硬编码）
- 发送日志：`log.info` 打印手机号与 params（注意：params 会被完整打印，含验证码，见 CLAUDE.md 陷阱）。
- 阿里云侧返回 `ServerException` / `ClientException` 时，`log.error` 后抛 `RuntimeException("短信发送失败", e)`。
- 不解析 `CommonResponse.getData()` 中的业务码（如 `isv.MOBILE_NUMBER_ILLEGAL`），仅 `log.info` 打印。即：HTTP/SDK 层面成功但业务失败时，调用方不会收到异常。

### 3. `com.rick.sms.core.ValidateCodeSender`（验证码便捷封装）

针对最常见的"验证码短信"场景的薄封装，把单个验证码字符串包装成 `{"code": code}` 后委托 `Sender`。

```java
public ValidateCodeSender(Sender sender) { ... }
public void send(String phone, String signName, String templateCode, String code);
```

参数：
- `phone` — 手机号
- `signName` — 签名名称
- `templateCode` — 模板 code（该模板需包含 `${code}` 占位符）
- `code` — 验证码字符串

行为：构造 `Map<String,String>{"code": code}` 后调用 `sender.send(...)`。同步、无返回值、异常透传 `Sender`。

> 注意：`ValidateCodeSender` 上没有 `@Component` / `@Service`，单独 new 不会进入 Spring 容器；它由 `SmsServiceAutoConfiguration` 显式注册为 Bean（见下）。

### 4. `com.rick.sms.conf.SmsServiceAutoConfiguration`（自动配置）

模块入口配置类，被 `META-INF/spring.factories` 与 Boot 3 的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 同时注册（兼容 Boot 2/3）。

内部静态结构：

#### `AliAccessKey`（内部 `@Configuration` + `@ConfigurationProperties(prefix="ali.access-key")`）

```java
private String id;      // 阿里云 AccessKey ID
private String secret;  // 阿里云 AccessKey Secret
```

绑定配置前缀 `ali.access-key`，字段 `id` / `secret`。

#### `SmsServiceConfiguration`（内部静态类）

注册以下 Bean（按顺序，存在依赖链）：

| Bean 类型 | Bean 名 | 来源 |
|---|---|---|
| `IClientProfile` | `getProfile` | `DefaultProfile.getProfile("cn-hangzhou", id, secret)` |
| `IAcsClient` | `getClient` | `new DefaultAcsClient(profile)` |
| `Sender` | `getAliSender` | `new AliSender(client)` |
| `ValidateCodeSender` | `getValidateCodeSender` | `new ValidateCodeSender(sender)` |

> `AliSender` 类自身也标了 `@Component`，但 `getAliSender` 又显式 `new` 了一个。当前实际生效的是 `SmsServiceConfiguration` 里的显式 Bean（同类型同源，无冲突报错风险待确认；若 `@Component` 与显式 Bean 同时注册会产生 Bean 覆盖，行为依赖 `spring.main.allow-bean-definition-overriding`）。

---

## 配置项

| 配置 key | 说明 | 必填 |
|---|---|---|
| `ali.access-key.id` | 阿里云 AccessKey ID | 是 |
| `ali.access-key.secret` | 阿里云 AccessKey Secret | 是 |

无开关、无默认渠道选择（只有阿里云）、无 region 配置（硬编码 `cn-hangzhou`）、无超时配置（使用 SDK 默认）。

`application.yml` 示例：

```yaml
ali:
  access-key:
    id: LTAI5tXXXXXXXXXX
    secret: XXXXXXXXXXXXXXXXXXXX
```

> 待确认：AccessKey 应使用阿里云 RAM 子账号并仅授予 `AliyunDysmsFullAccess`（发送）或更小权限，避免主账号密钥泄露。

---

## 使用示例

### 引入依赖

```xml
<dependency>
    <groupId>com.rick.sms</groupId>
    <artifactId>sharp-sms</artifactId>
</dependency>
<!-- 版本由 sharp-dependencies 统一管理 -->
```

依赖引入 + 配置 `ali.access-key.*` 后，自动装配即注入 `Sender` 与 `ValidateCodeSender` 两个 Bean。

### 示例 1：发送一条验证码短信

```java
@RequiredArgsConstructor
public class RegisterService {

    private final ValidateCodeSender validateCodeSender; // 自动注入

    public void sendRegisterCode(String phone, String code) {
        // 模板需在阿里云控制台审批通过，且包含 ${code} 占位符
        validateCodeSender.send(phone, "签名名称", "SMS_123456789", code);
    }
}
```

### 示例 2：发送一条营销/通知短信（多变量）

营销、通知类短信通常有多个模板变量，直接使用 `Sender`：

```java
@RequiredArgsConstructor
public class MarketingService {

    private final Sender sender; // 自动注入，运行时为 AliSender

    public void sendActivitySms(String phone) {
        Map<String, String> params = new HashMap<>(2);
        params.put("product", "Sharp平台");
        params.put("amount", "88");
        sender.send(phone, "签名名称", "SMS_987654321", params);
    }
}
```

### 异常处理约定

- 参数 JSON 序列化失败 → `RuntimeException("短信参数错误")`，业务侧一般不可恢复。
- 网络/鉴权/阿里云 SDK 异常 → `RuntimeException("短信发送失败", e)`，可重试。
- 阿里云业务层失败（如手机号非法、模板变量缺失）→ 当前实现**不抛异常**，调用方无感知，仅能在日志中看到 `CommonResponse.getData()` 的内容。如需可靠判定成功，需自行增强 `AliSender`（见 ARCHITECTURE.md 扩展点）。

---

## 公共入口清单（最核心 5 个）

1. `com.rick.sms.core.Sender`（接口）
2. `com.rick.sms.core.AliSender`（阿里云实现）
3. `com.rick.sms.core.ValidateCodeSender`（验证码封装）
4. `com.rick.sms.conf.SmsServiceAutoConfiguration`（自动配置）
5. `com.rick.sms.conf.SmsServiceAutoConfiguration.AliAccessKey`（配置绑定）
