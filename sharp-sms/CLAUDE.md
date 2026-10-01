# CLAUDE.md — sharp-sms

> 给大模型/开发者的工作守则：只读本文 + `API.md` + `ARCHITECTURE.md` 即可上手改本模块。

## 模块边界

- 范围：**仅短信发送**，单渠道（阿里云 dysmsapi `SendSms`）。
- 不做：多渠道路由、发送记录持久化、回执回调、频控/限流、模板/签名管理、签名审核流程、异步队列、重试、国际短信支持。
- 调用方期望：拿到 `Sender`/`ValidateCodeSender` 自己发；业务层自己存记录、自己限频、自己解析阿里云返回。

## 目录约定

```
sharp-sms/
├── pom.xml
├── API.md                    # 对外 API 速查 + 使用示例
├── ARCHITECTURE.md           # 包结构 / 数据流 / 扩展点
├── CLAUDE.md                 # 本文件
└── src/main/
    ├── java/com/rick/sms/
    │   ├── core/              # 接口与实现：Sender / AliSender / ValidateCodeSender
    │   └── conf/              # 自动配置：SmsServiceAutoConfiguration
    └── resources/META-INF/
        └── spring.factories   # EnableAutoConfiguration 注册
```

约定：
- 渠道实现一律放 `core`，命名 `XxxSender implements Sender`。
- 配置装配一律放 `conf`，不要在 `core` 写 `@Bean`。
- 自动配置只注册 `SmsServiceAutoConfiguration` 一个入口；新增内部配置类挂在其下。

## 编码约定

- 构造注入 + `@RequiredArgsConstructor`，无字段可变状态（`AliSender` 即此模式）。
- `Sender` 接口保持单方法 `send`，返回 `void`；新渠道不引入新抽象（除非多渠道路由成为需求，届时再引入 `SmsService`）。
- 异常一律转 `RuntimeException` 上抛，不吞异常。
- 日志用 `@Slf4j`；**勿**在日志打印 AK/SK；模板参数当前会进日志（含验证码），改动时注意脱敏需求。
- 中文注释与 Javadoc 保留作者标签风格。

## 常见改动清单

### 加一个短信渠道（如腾讯云）

1. 新建 `src/main/java/com/rick/sms/core/TencentSender implements Sender`。
2. 在 `SmsServiceAutoConfiguration.SmsServiceConfiguration` 加 `@Bean` 装配腾讯 SDK 客户端与 `TencentSender`。
3. 决定渠道选择方式：
   - 替换默认渠道：把 `Sender` 的 `@Bean` 返回类型改 `TencentSender`，或加 `@ConditionalOnProperty` 切换。
   - 多渠道共存：本模块不内置路由层，需新增一个 `SmsService` + `Map<String, Sender>`，并在文档（`ARCHITECTURE.md` 扩展点）补充说明。**不要**悄悄引入未文档化的抽象。
4. 在 `API.md` 补充渠道特性，在 `ARCHITECTURE.md` 数据流补充新链路。

### 加发送记录持久化

- 不在本模块加。建议在调用方（业务层）用 AOP/装饰器包裹 `Sender`，落库后再委托发送。
- 若一定要在本模块加，须同时引入 `sharp-database` 依赖并新增模板表；需走架构评审，不要静默扩大边界。

### 改 Region / 国际短信

- 改 `AliSender` 中 `request.putQueryParameter("RegionId", "cn-hangzhou")`，或将 Region 提升为配置项（加到 `ali.access-key` 同级前缀，新增 `@ConfigurationProperties`）。
- 国际短信需在阿里云控制台单独申请，并改 `SysDomain` / 号码格式处理——代码当前不做号码校验。

### 解析业务返回码

- 在 `AliSender.send` 的 `response.getData()` 处用 Jackson 解析 JSON，取 `Code` 字段；非 `OK` 时抛业务异常或返回结果对象。
- 若改造，把 `send` 返回值从 `void` 改为结果对象是 breaking change，需同步更新 `API.md` 与调用方。

## 构建与测试

```bash
# 构建（在项目根目录）
./mvnw -pl sharp-sms -am clean package -DskipTests

# 仅编译
./mvnw -pl sharp-sms compile

# 安装到本地仓库
./mvnw -pl sharp-sms -am install -DskipTests
```

本模块**无单元测试目录**（`src/test` 不存在）。验证只能靠下游应用集成调用阿里云真实接口——需配置有效 `ali.access-key.*` 与已审核签名/模板。

版本由 `sharp-dependencies` 的 `<sharp.version>2.0-SNAPSHOT</sharp.version>` 统一管理；本模块 `pom.xml` 不写死版本。

## 陷阱

- **业务码不解析**：`AliSender` 仅捕获 SDK 抛出的 `ServerException`/`ClientException`，阿里云返回 `Code != "OK"`（如 `isv.BUSINESS_LIMIT_CONTROL` 频控、`isv.MOBILE_NUMBER_ILLEGAL` 号码非法、`isv.INVALID_TEMPLATE_PARAM` 模板变量缺失）时**不会**报错，调用方拿不到失败信号。生产强一致场景须自行解析 `response.getData()`。
- **模板变量缺失/不匹配**：`params` 的 key 与模板占位符不符，阿里云返回业务错误码但本模块不感知。`params` 传 `null` 会 NPE。
- **频控（限流）**：完全依赖阿里云侧频控（`isv.BUSINESS_LIMIT_CONTROL` 等），本模块不做任何节流/冷却。同一号码高频发送会被阿里云拒绝且不被本模块识别为失败。
- **回执幂等**：本模块不接收回执，无幂等问题；但调用方若做"发送→回执→落库"，需自行实现幂等键（用阿里云 `BizId` + 手机号）。
- **密钥泄露面**：`log.info` 不打印 AK/SK；但会打印 `params`（含验证码明文）。生产环境若日志被多方可见，须在 `AliSender` 改造脱敏。
- **Region/Domain 硬编码**：`cn-hangzhou` / `dysmsapi.aliyuncs.com` 写死，无法配置切换。国际短信、港澳台、其它 Region 部署需改源码。
- **`@Component` 与 `@Bean` 双重注册**：`AliSender` 既标 `@Component` 又被 `SmsServiceConfiguration` 以 `@Bean` 注册。下游若开启 `com.rick.sms.core` 包扫描，可能触发 Bean 覆盖；未开启扫描则只走 `@Bean`（推荐）。
- **`AliAccessKey` 是非静态内部类**：依赖 Spring CGLIB 代理才能实例化，不能用 `new SmsServiceAutoConfiguration().new AliAccessKey()` 手工创建。
- **`sharp-common` 依赖存疑**：pom 声明依赖但源码未见显式 import，**待确认**是否实际使用。
