# sharp-sms API

## 模块定位

`sharp-sms` 是一个 Spring Boot 自动装配的**短信发送基础库**，打包为 jar，通过 `spring.factories` 自动装配。当前仅对接**阿里云短信服务（dysmsapi，`SendSms` 接口）**，定位为"最小可用发短信"——同步发送、无回执、无限流、无模板/签名管理、无发送记录持久化。

下游应用（如 `sharp-admin`、`sharp-notification` 之类）只需在 `pom.xml` 引入 `com.rick.sms:sharp-sms` 并配置 AK/SK 即可注入使用。当前项目内无模块在 Java 源码中直接引用本模块——它作为可选能力库由 `sharp-dependencies` 统一管理版本。

## 核心 public 入口

### 1. `com.rick.sms.core.Sender`（接口）

渠道抽象。所有短信发送渠道实现此接口。当前唯一实现是 `AliSender`。

```java
void send(String phone, String sign, String templateCode, Map<String, String> params);
```

- `phone`：手机号，阿里云要求 11 位国内手机号；国际号码格式与适用性**待确认**（代码未做校验，直接透传给 dysmsapi）。
- `sign`：短信签名，须与阿里云控制台审核通过的签名一致。
- `templateCode`：模板 CODE，须与阿里云控制台模板 ID 一致。
- `params`：模板变量，`key` 为占位符名（如 `code`），`value` 为替换内容。`AliSender` 会用 Jackson 序列化为 JSON 作为 `TemplateParam`。

行为：**同步**调用，无返回值（`void`）。成功仅记日志，不解析阿里云业务码（`Code` 字段如 `isv.BUSINESS_LIMIT_CONTROL` 不会被识别为失败——见"陷阱"）。失败抛 `RuntimeException`。

### 2. `com.rick.sms.core.AliSender`（唯一渠道实现）

```java
@Component
public class AliSender implements Sender {
    public AliSender(IAcsClient client);
    @Override
    public void send(String phone, String signName, String templateCode, Map<String, String> params);
}
```

行为细节：
- 渠道：阿里云 `dysmsapi.aliyuncs.com`，API 版本 `2017-05-25`，Action `SendSms`，HTTP POST。
- Region 硬编码 `cn-hangzhou`（杭州），不支持配置切换。
- 模板变量通过 `ObjectMapper.writeValueAsString(params)` 序列化为 JSON。`params` 为 `null` 时会抛 NPE（代码未判空）。
- 异常映射：
  - `JsonProcessingException` → `RuntimeException("短信参数错误")`
  - `ServerException` / `ClientException` → `RuntimeException("短信发送失败", e)`
- **不解析** `CommonResponse.getData()` 中的业务 JSON（仅 `log.info` 打印）；阿里云返回 `OK` 之外的业务错误（如 `isv.MOBILE_NUMBER_ILLEGAL`、`isv.BUSINESS_LIMIT_CONTROL`）**不会**被识别为失败。
- 无重试、无超时配置（沿用 `DefaultAcsClient` 默认值，**待确认**具体超时）。

### 3. `com.rick.sms.core.ValidateCodeSender`（验证码便捷封装）

```java
public class ValidateCodeSender {
    public ValidateCodeSender(Sender sender);
    public void send(String phone, String signName, String templateCode, String code);
}
```

- `code`：验证码字符串。内部构造 `Map.of("code", code)` 委托给 `Sender.send`。
- 仅适用于模板变量名为 `code` 的验证码模板。营销/通知类短信请直接使用 `Sender`。
- 注意：此类**未加** `@Component`，仅由自动配置类 `@Bean` 注入；不在容器扫描路径下也能生效。

### 4. `com.rick.sms.conf.SmsServiceAutoConfiguration`（自动配置）

```java
@Configuration
public class SmsServiceAutoConfiguration {
    // 内部 @ConfigurationProperties(prefix = "ali.access-key") 类 AliAccessKey { String id; String secret; }
    // 内部 static class SmsServiceConfiguration：装配 IClientProfile / IAcsClient / Sender / ValidateCodeSender
}
```

通过 `META-INF/spring.factories` 注册到 `EnableAutoConfiguration`。注入的 Bean：`IClientProfile`、`IAcsClient`（`DefaultAcsClient`）、`Sender`（`AliSender`）、`ValidateCodeSender`。

## 配置项

唯一前缀 `ali.access-key`（`AliAccessKey` 内部类上的 `@ConfigurationProperties`）：

| 配置键 | 类型 | 说明 |
|---|---|---|
| `ali.access-key.id` | `String` | 阿里云 AccessKey ID（必填） |
| `ali.access-key.secret` | `String` | 阿里云 AccessKey Secret（必填） |

其他均为硬编码不可配置：Region=`cn-hangzhou`、Domain=`dysmsapi.aliyuncs.com`、Version=`2017-05-25`、Action=`SendSms`。签名、模板 CODE、模板变量在调用处传入，不在配置中。

`application.yml` 示例：

```yaml
ali:
  access-key:
    id: LTAI5tXXXXXXXX
    secret: XXXXXXXXXXXXXXXXXXXX
```

## 使用示例

### 发一条验证码短信

```java
@RequiredArgsConstructor
@Service
public class CaptchaService {
    private final ValidateCodeSender validateCodeSender;

    public void sendCaptcha(String phone) {
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        // 模板须为含 ${code} 占位符的验证码模板
        validateCodeSender.send(phone, "rick科技", "SMS_123456789", code);
    }
}
```

### 发一条营销短信

```java
@RequiredArgsConstructor
@Service
public class MarketingService {
    private final Sender sender; // 直接注入渠道抽象

    public void push(String phone) {
        Map<String, String> params = new HashMap<>(2);
        params.put("product", "Pro会员");
        params.put("price", "9.9元");
        sender.send(phone, "rick科技", "SMS_987654321", params);
    }
}
```

## 陷阱速查

- **业务码不解析**：`AliSender` 只捕获 SDK 异常，`response.getData()` 中 `Code != "OK"` 的业务错误（频控、号码非法、模板变量不匹配等）会被当作成功。生产环境需自行解析响应。
- **模板变量缺失**：`params` 的 key 与模板占位符不符时阿里云返回 `isv.MISSING_TEMPLATE_PARAM` / `isv.INVALID_JSON_PARAM`，本模块不会感知。
- **密钥泄露面**：`AliSender.send` 的 `log.info` 会打印 `params`（含验证码），但不会打印 AK/SK。日志可见明文验证码。
- **Region/Domain 硬编码**：国际短信、港澳台短信、其它 Region 部署需改源码。
