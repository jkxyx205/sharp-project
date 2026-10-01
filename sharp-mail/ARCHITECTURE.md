# sharp-mail 架构

## 包结构

```
com.rick.mail
├── config
│   ├── ImapMailProperties.java        # spring.mail.imap.* 配置绑定
│   └── MailServiceAutoConfiguration.java # 自动配置 + MailHandler Bean 注册
├── core
│   ├── MailHandler.java               # 对外接口（发送 + IMAP 收取）
│   ├── MailHandlerImpl.java           # 唯一实现：Email→MimeMessage + IMAP 操作
│   └── mail
│       ├── Email.java                 # 邮件 DTO + 嵌套 EmailBuilder（推荐入口）
│       └── EmailBuilder.java          # 【遗留】顶层 builder 类，勿用（见下）
└── util
    └── MailUtils.java                 # 静态工具：下载附件 / 判附件 / 取正文
```

资源：
```
src/main/resources/
├── META-INF/spring.factories         # 注册 MailServiceAutoConfiguration 到自动装配
└── application-{163,qq,gmail,aliyun}.yml  # 4 套 SMTP+IMAP 示例 profile
```

## 关键抽象

### MailHandler ↔ Email ↔ JavaMailSender

```
调用方
  │  Email.builder()...build()
  ▼
MailHandler.send(Email)
  │
  ▼
MailHandlerImpl.convertEmailToMimeMessage(Email)
  │  ── 构造 MimeMessage：from/to/cc/bcc/subject
  │  ── 正文 part：plainText OR htmlText（互斥，plainText 优先）
  │  ── 附件 part：MimeBodyPart + ByteArrayDataSource，文件名 MimeUtility.encodeText
  │  ── 内嵌图片 part：disposition=INLINE，Content-ID=文件名
  │  ── 仅当 multipart.getCount() > 1 时整体 setContent(multipart)
  ▼
JavaMailSender.send(MimeMessage)   ← SMTP 同步发送
  │
  ▼（仅 sendAndSaveToOutbox 路径）
IMAP Store.connect(...) → Folder("已发送").appendMessages(...)   ← 归档到已发送
```

- 发送侧完全依赖 Spring 的 `JavaMailSender`（由 `MailSenderAutoConfiguration` 装配），本模块不再造轮子。
- 收取侧直接复用 `JavaMailSenderImpl.getSession()`，再 `session.getStore("imap")` 拿到 `IMAPStore`。**Store 在 `MailHandlerImpl` 实例字段 `cachedStore` 中缓存复用**，未做并发隔离——非线程安全。

### Email 数据模型

`Email` 为不可变 DTO（`@AllArgsConstructor @Getter`），所有收件人/附件均以 `List<Object[]>` 存储：
- 收件人：`{address(String), personal(String)}`
- 附件：`{fileName(String), byteArray(byte[]), type(String)}`
- 内嵌图：同附件三元组，但 `disposition=INLINE` 且 `Content-ID=fileName`

### 遗留 EmailBuilder

`com.rick.mail.core.mail.EmailBuilder`（顶层类，与 `Email` 同包）是早期实现：
- `cc(...)` / `bcc(...)` 方法体**直接 `return this;`，未实际加入列表**——空实现。
- 无 `plainText / htmlText / attachment / embeddedImage` 方法。
- **勿用。** 推荐入口永远是 `Email.builder()`（即 `Email.EmailBuilder`）。

## 数据流（发送）

1. 调用方通过 `Email.builder()` 链式拼装邮件。
2. `MailHandlerImpl.send(Email)` → `convertEmailToMimeMessage`：构造 `javax.mail.internet.MimeMessage`，按 from/to/cc/bcc 设置地址，正文按 `plainText`/`htmlText` 二选一，附件与内嵌图依次 `addBodyPart`。
3. `sender.send(message)`（Spring `JavaMailSender`）走 SMTP 同步投递。失败抛 `MailSendException`。
4. （可选）`saveToOutbox`：通过 IMAP `getStore()` 打开「已发送」文件夹 `appendMessages`，再 `setFlag(SEEN, true)`。

## 数据流（收取）

- `listFolders()`：`store.getDefaultFolder().list()`，调用后立即 `store.close()`——**folder 对象返回后已不可再 open/read**。
- `listUnreadMessage(consumer)`：打开 INBOX (READ_WRITE)，按未读计数循环，逐封回调并置 SEEN。
- `listMessages(folder, fn)`：**分页倒序遍历**，每页 `SIZE=50`，从最新向最旧；`fn` 返回 `true` 终止。
- `searchByMessageId(folder, id)`：`MessageIDTerm` 搜索；命中后调用 `message.getMessageID()` 触发服务端 `loadEnvelope()` 拉信封数据，再关闭 folder 与 store。源码 `//TODO 163 读取有问题` 标注 163 下可能失败。

## 对外依赖

`pom.xml`：
- `org.springframework.boot:spring-boot-starter-mail`（传递引入 `jakarta.mail` / `javax.mail` 实现、Spring `JavaMailSender`）。
- `org.jsoup:jsoup` —— 仅 `MailUtils.contentText` 用于 HTML→纯文本。
- `org.projectlombok:lombok`（编译期）。

版本统一由父 POM `com.rick:sharp-dependencies:2.0-SNAPSHOT` 管理。**无模板引擎依赖**。

## 扩展点

- 替换实现：自行实现 `MailHandler` 并以 `@Primary` 或排除 `MailServiceAutoConfiguration` 的方式覆盖默认 Bean。
- 自定义 `MimeMessage`：直接调用 `send(MimeMessage)` / `sendAndSaveToOutbox(MimeMessage)` 绕过 `Email` 模型，自行构造 message。
- 自定义附件来源：`Email.attachment(...)` 接收 `byte[]`，文件流需调用方自行读取。

## 配置与启动

- 自动装配入口：`META-INF/spring.factories` → `MailServiceAutoConfiguration`。
- 装配顺序：`@AutoConfigureAfter(MailSenderAutoConfiguration.class)`，确保 `JavaMailSender` 与 `MailProperties` 先就绪。
- 启动必需配置：`spring.mail.host/username/password`（SMTP）+ `spring.mail.imap.host`（仅当使用收取或 `sendAndSaveToOutbox` 时）。
- 4 套示例 profile（163/qq/gmail/aliyun）可直接 `--spring.profiles.active=qq` 复用。

## 已知设计局限

- **IMAP Store 非线程安全**：`cachedStore` 为实例字段，多线程并发收件需外部加锁或每线程独立 Bean。
- **「已发送」文件夹名硬编码**为中文 `"已发送"`（`saveToOutbox`）；Gmail 等需 `"Sent"`，需改源码。
- **plainText/htmlText 互斥**，同时设置时 htmlText 静默丢弃。
- **163 IMAP**：源码 `getStore()` 内已带 `IMAPStore.id(clientParams)` 绕过 "Unsafe Login" 限制；但 `searchByMessageId` 在 163 下行为不稳定（TODO 标注）。
- 无异步、无重试、无发送队列。
