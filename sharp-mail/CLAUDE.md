# sharp-mail 模块说明（给大模型/开发者）

## 模块边界

`sharp-mail`（`com.rick.mail:sharp-mail`，版本 `${sharp.version}`）是基于 `spring-boot-starter-mail` 的轻量邮件封装，提供 SMTP 发送 + IMAP 收取能力。**不包含模板渲染**：HTML 正文由调用方直接以字符串形式传入 `htmlText`，没有 Freemarker/Thymeleaf 等模板引擎依赖。如需模板，由上层模块自行渲染后传字符串。

被 `sharp-admin`、`sharp-demo` 作为依赖引入；下游通过 `MailHandler` Bean 发送/收取邮件。

## 目录约定

```
src/main/java/com/rick/mail/
├── config/
│   ├── MailServiceAutoConfiguration.java   # 自动装配，注册 MailHandler Bean
│   └── ImapMailProperties.java             # spring.mail.imap.* 配置（host/port）
├── core/
│   ├── MailHandler.java                   # 发送+收取对外接口
│   ├── MailHandlerImpl.java               # 接口实现（SMTP 发送 / IMAP 收取 / 附件 / 内嵌图片）
│   └── mail/
│       ├── Email.java                     # 邮件实体 + 内部 EmailBuilder（推荐入口 Email.builder()）
│       └── EmailBuilder.java              # 独立 EmailBuilder（早期遗留，方法不全，不要使用）
└── util/
    └── MailUtils.java                     # 收信辅助：附件下载、是否含附件、正文文本提取

src/main/resources/
├── application-{163,qq,gmail,aliyun}.yml   # 各邮箱服务商 SMTP/IMAP 配置样例
└── META-INF/spring/...AutoConfiguration.imports   # Spring Boot 3 自动装配注册
```

## 编码约定

- 发件人/收件人/抄送/密送统一用 `Object[]{address, personal}` 二元组（`personal` 可为 `null`）。`Email.Builder` 提供 `to(address)` / `to(address, personal)` 两套重载。
- 附件与内嵌图片以 `byte[]` 传入，必须带 MIME 类型字符串（如 `image/png`、`text/plain`）。文件名需原始文件名，会被 `MimeUtility.encodeText` 编码。
- 内嵌图片（`embeddedImage`）的 `fileName` 同时作为 `Content-ID`；HTML 中以 `cid:fileName` 引用，例如 `fileName=learn.png` → `<img src="cid:learn.png" />`。
- 纯文本与 HTML：`plainText` / `htmlText` 二选一，同时设置时 `plainText` 优先（实现中先判断 `plainText`）。
- 正文编码统一 UTF-8。
- `MailUtils` 标注了 `@UtilityClass`（Lombok），实例方法调用形式为 `MailUtils.downloadAttachments(...)`。

## 常见改动清单

- 新增邮箱服务商配置：在 `resources/` 增加 `application-<vendor>.yml`，参考现有 4 个样例。
- 调整 IMAP 连接（如 163 的 `IMAP ID` 协商参数）：改 `MailHandlerImpl#getStore` 中的 `clientParams`。
- 调整收信分页大小：`MailHandlerImpl.SIZE = 50`。
- 已发送文件夹名硬编码为中文 `"已发送"`：`saveToOutbox` 内。换其他邮箱需确认文件夹名。

## 构建测试命令

```bash
# 编译
mvn -pl sharp-mail -am clean compile
# 打包
mvn -pl sharp-mail -am clean install -DskipTests
# 运行现有测试（在 sharp-demo 模块，需先配置真实邮箱账号）
mvn -pl sharp-demo -am test -Dtest=com.rick.demo.mail.One63MailTest
```

注意：现有测试用例（`One63MailTest` / `AliyunMailTest`）方法体全部被注释，需要手动取消注释并填入真实 SMTP/IMAP 凭据才能跑通。

## 陷阱

1. **邮箱授权码不是登录密码**：163/QQ/Gmail 均需在邮箱后台开启 SMTP/IMAP 服务并生成授权码，授权码作为 `spring.mail.password`。硬编码真实密码会认证失败。
2. **163 IMAP "Unsafe Login"**：必须发送 `IMAP ID`（`store.id(clientParams)`），代码已处理；若改实现不要漏掉。
3. **163 读取已发送有已知问题**：源码 `searchByMessageId` 标注 `//TODO 163 读取有问题`，使用前需实测验证。
4. **`saveToOutbox` 文件夹名硬编码中文** `"已发送"`，非中文邮箱（Gmail/Aliyun）需要适配或不用此方法。
5. **`listFolders()` 调用后立即 `store.close()`**，返回的 `Folder[]` 在 163 下不能再读取邮件；如需读取需重新 `getStore()`。
6. **附件大小**：附件以 `byte[]` 全部加载到内存，大附件会占堆内存；无大小限制逻辑。
7. **同步发送**：`send` 是同步阻塞调用，失败抛 `MailSendException` / `MailException`；**无内置异步、无重试**。需要异步或重试请上层用 `@Async` / Spring Retry 自行包装。
8. **`plainText` 与 `htmlText` 互斥**：实现中 `plainText` 优先，`htmlText` 在 `plainText` 非空时被忽略。不要同时设置两个。
9. **Content-Type 判定**：仅当 `multipart.getCount() > 1`（有附件或内嵌图片）才设置 `multipart/mixed`；纯文本/HTML 邮件用 `setText` 的 `text/plain` 或 `text/html`。单独一个内嵌图片 + 无正文会出现 `multipart` 但 `count=1`，未进入 `setContent(multipart)` 分支——此时 Content-Type 由 `setText` 决定。**待确认**这是否影响纯内嵌图片邮件的渲染。
10. **`EmailBuilder`（独立类，`core/mail/EmailBuilder.java`）是早期遗留**，方法不全（无 `to(address)` 单参重载、无 `attachment`/`embeddedImage`/`build`）。**请始终使用 `Email.builder()`** 返回的内部 Builder。
11. **异步发送与失败重试**：模块本身不提供，需上层封装（见陷阱 7）。
12. **模板变量未定义**：模块无模板引擎，不存在该问题；但若上层用 Freemarker 渲染后传 `htmlText`，模板变量缺失由上层模板引擎处理。
