# sharp-sms CLAUDE.md

> 给 AI 助手 / 维护者的工作备忘。读这一份即可在不看源码的情况下安全改动本模块。

## 模块边界

- 模块路径：`/Users/rick/Space/Workspace/sharp-project/sharp-sms`
- groupId / artifactId：`com.rick.sms:sharp-sms`，版本由 `${sharp.version}` 统一管理（声明在 `sharp-dependencies/pom.xml`）。
- 父 pom：`com.rick:sharp-dependencies:3.0-SNAPSHOT`。
- 职责：阿里云短信（dysmsapi）发送的最小封装。仅发，不存、不限流、不回执、不回调、不管理模板/签名。
- 不要把模板管理、签名管理、发送记录、频控塞进本模块——这些应放在调用方业务模块。本模块保持"单一渠道发送器"形态。

## 目录约定

```
src/main/java/com/rick/sms/
├── core/     # 接口与实现：Sender / AliSender / ValidateCodeSender
└── conf/     # 自动配置：SmsServiceAutoConfiguration（含 AliAccessKey 配置绑定）
src/main/resources/META-INF/
├── spring.factories                                        # Boot 2 注册
└── spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports   # Boot 3 注册
```

- 渠道实现放 `core`，配置与装配放 `conf`，保持分离。
- 新增渠道：在 `core` 加实现类，在 `conf` 加 Bean 注册，必要时加 `@ConditionalOnProperty`。

## 编码约定

- 风格：lombok `@RequiredArgsConstructor` + `@Slf4j`；构造注入，不用字段注入。
- 异常：业务/SDK 异常一律包装为 `RuntimeException` 抛出，不在此层吞异常（当前业务码不解析属已知缺陷，不要在没有回执设计时贸然吞码）。
- 日志：`log.info("【发送短信】向手机{}发送信息{}", phone, params)` —— 注意 params 含验证码明文。
- 不要在本模块引入 MyBatis / JPA / 数据库连接池。
- 自动配置必须**同时**写入 `spring.factories` 与 `AutoConfiguration.imports`，以兼容 Boot 2/3。

## 常见改动清单

### 加一个短信渠道（如腾讯云）

1. `pom.xml`：加腾讯云 SDK 依赖。
2. `core/TencentSender.java`：新建，`implements Sender`，注入腾讯云客户端。
3. `conf/SmsServiceAutoConfiguration.java`（或新建 `TencentAutoConfiguration`）：
   - 加配置绑定类（前缀如 `tencent.sms`），字段 secretId / secretKey 等。
   - 加 Bean：客户端、`Sender`（用 `@ConditionalOnProperty` 或 `@Primary` 控制多渠道切换）。
4. `spring.factories` / `AutoConfiguration.imports`：若新建了独立配置类，注册进去。
5. 不需要动 `Sender` 接口、`ValidateCodeSender`、数据库（无表）。

### 让 `AliSender` 解析业务回执

1. `core/AliSender.java`：在 `client.getCommonResponse` 后解析 `response.getData()` 的 JSON。
2. 判定 `Code != "OK"` 时抛业务异常或返回结构体；如需返回值，要改 `Sender` 接口签名（影响所有实现，属破坏性变更）。

### 增加发送记录

不在本模块做。由调用方模块建表 + 在调用 `Sender.send` 前后写入。若确需平台级落库，应在 `sharp-notification` 或新建 `sharp-sms-log` 模块承接。

## 构建与测试

- 构建：在仓库根目录 `mvn -pl sharp-sms -am clean install`。
- 本模块无 `src/test`，无单测。改动后至少 `mvn -pl sharp-sms compile` 验证编译。
- 端到端验证需真实阿里云密钥与已审批模板，禁止把真实密钥提交进仓库（见陷阱）。

## 陷阱

1. **模板变量缺失 / 不匹配**：阿里云侧会返回业务错误码（如 `isv.MOBILE_NUMBER_ILLEGAL`、`isv.TEMPLATE_MISSING_PARAMETERS`），但当前 `AliSender` 仅 `log.info` 响应体，**不抛异常**。调用方拿到"成功"假象。任何依赖"发送是否真成功"的业务，必须先增强 `AliSender` 的回执判定。
2. **国际短信 / 号码格式**：本模块不做号码格式校验，国际号需阿里云侧模板支持，调用方自行处理 `+86` 前缀等问题。
3. **频控**：模块内无频控。验证码防刷、同一手机号间隔限制必须由调用方实现（如 Redis 计数）。
4. **回执幂等**：模块不接收回执。如未来加回执端点，阿里云会重试回调，必须按 `BizId` / `SendDate` 做幂等。
5. **密钥泄露面**：
   - `ali.access-key.secret` 走 `@ConfigurationProperties`，会以明文进入环境配置；切勿入库提交。
   - `AliSender.send` 会 `log.info` 打印 `params`，**验证码会被写进日志**。生产环境前应脱敏（mask `code` 字段）或下调日志级别。
   - 建议使用 RAM 子账号最小权限（`AliyunDysmsFullAccess` 或更小），禁用主账号 AccessKey。
6. **Bean 重复注册风险（待确认）**：`AliSender` 标了 `@Component`，同时 `SmsServiceAutoConfiguration#getAliSender` 又 `new` 了一个同名类型 Bean。Spring Boot 默认 `spring.main.allow-bean-definition-overriding=false`，可能产生冲突或覆盖行为；改动 `AliSender` 时留意此双重声明。
7. **硬编码 region**：`cn-hangzhou` 写死在 `DefaultProfile.getProfile` 与 `CommonRequest.RegionId`。海外 / 其他区域需改代码或参数化。
8. **SDK 版本旧**：`aliyun-java-sdk-core:4.5.3` 在 Spring Boot 3 / JDK 17 下未充分验证，升级时需回归。

## 公共入口速查

- `com.rick.sms.core.Sender#send(phone, sign, templateCode, params)`
- `com.rick.sms.core.AliSender`（`Sender` 的阿里云实现）
- `com.rick.sms.core.ValidateCodeSender#send(phone, signName, templateCode, code)`
- `com.rick.sms.conf.SmsServiceAutoConfiguration`（自动配置入口）
- `ali.access-key.{id,secret}`（唯一配置项）
