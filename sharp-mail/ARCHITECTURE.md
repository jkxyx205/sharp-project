# sharp-mail 架构

## 包结构

```
com.rick.mail
├── config               自动装配 + 配置属性
│   ├── MailServiceAutoConfiguration
│   └── ImapMailProperties
├── core                 核心抽象与实现
│   ├── MailHandler (接口)
│   ├── MailHandlerImpl (实现)
│   └── mail
│       ├── Email (邮件模型 + 内部 Builder)
│       └── EmailBuilder (早期遗留，勿用)
└── util
    └── MailUtils (收信解析辅助)
```

模块依赖：`spring-boot-starter-mail`（提供 `JavaMailSender` / `MailProperties` / 自动装配）、`org.jsoup:jsoup`（HTML→纯文本）、`lombok`。父 POM 为 `sharp-dependencies`（统一版本管理）。**不依赖任何模板引擎。**

## 关键抽象

### MailHandler（接口）↔ MailHandlerImpl（实现）

`MailHandler` 是唯一对外入口，承担两类职责：

1. **发送**：`send(Email)` / `sendAndSaveToOutbox(Email)` / `send(MimeMessage)` / `sendAndSaveToOutbox(MimeMessage)`。
2. **IMAP 收取**：`listFolders` / `listUnreadMessage` / `listMessages` / `searchByMessageId`。

`MailHandlerImpl` 构造注入三项：
- `JavaMailSender sender`：Spring Boot 提供，负责 SMTP 发送。
- `MailProperties mailProperties`：`spring.mail.*`，取 username/password 用于 IMAP 登录。
- `ImapMailProperties imapMailProperties`：`spring.mail.imap.*`，IMAP host/port。

### Email（邮件模型）↔ 邮件组装

`Email` 是不可变值对象，仅持有原始数据（from / to / cc / bcc / subject / plainText / htmlText / attachmentList / embeddedImageList），不参与 MIME 构造。组装由 `MailHandlerImpl#convertEmailToMimeMessage` 完成：

- 收件人用 `Object[]{address, personal}` 二元组，`personal` 可空。
- 正文：`plainText` 优先于 `htmlText`，二者互斥（实际只取其一写入 `textPart`）。
- 附件：`{fileName, byte[], mimeType}` → `MimeBodyPart` + `ByteArrayDataSource`，文件名 `MimeUtility.encodeText` 编码。
- 内嵌图片：`{fileName, byte[], mimeType}`，`setDisposition(INLINE)` 且 `setContentID(fileName)`，HTML 中以 `cid:fileName` 引用。
- Content-Type 判定：仅当 `multipart.getCount() > 1` 才 `message.setContent(multipart)`（即 multipart/mixed）；否则依赖 `message.setText(...)` 设置的 text/plain 或 text/html。

### 模板渲染

**无**。`htmlText` 由调用方直接提供完整 HTML 字符串。如需动态内容（变量替换、循环），由上层模块自行用 Freemarker/Thymeleaf 等渲染后传入 `htmlText`，本模块不感知模板。

### 附件处理

发送侧：`convertEmailToMimeMessage` 直接 `byte[]` → `ByteArrayDataSource`，全部加载进内存，无大小限制、无流式上传。

接收侧：`MailUtils.downloadAttachments(Message, downloadFolder)` 遍历 `Multipart`，对 `disposition = ATTACHMENT` 的 part 调 `part.saveFile(...)` 落盘；文件名经 `MimeUtility.decodeText(unfold(fileName))`。

## 数据流

### 发送流

```
调用方
  └─ Email.builder()...build()        // 构造 Email 值对象
  └─ mailHandler.send(email)
       └─ convertEmailToMimeMessage(email)   // Email → javax.mail.MimeMessage
            ├─ setFrom / addRecipient(TO|CC|BCC)
            ├─ setText(plainText|htmlText, "UTF-8", "plain"|"html")
            ├─ 附件 → MimeBodyPart(ByteArrayDataSource)
            ├─ 内嵌图片 → MimeBodyPart(INLINE, ContentID=fileName)
            └─ 若 multipart>1：message.setContent(multipart)
       └─ sender.send(message)            // JavaMailSender 同步 SMTP 发送
       └─ (可选) saveToOutbox：IMAP store.connect → folder("已发送").appendMessages
```

### 收取流

```
mailHandler.listUnreadMessage(consumer) / listMessages(folder, fn) / searchByMessageId(...)
  └─ getStore()
       ├─ session.getStore("imap")
       ├─ store.connect(imapHost, imapPort, mailUsername, mailPassword)
       └─ store.id(clientParams)   // 163 必需的 IMAP ID 协商
  └─ folder.open(READ_ONLY|READ_WRITE)
  └─ folder.search() / folder.getMessages(start,end)
  └─ consumer.apply(message)
  └─ folder.close() / store.close()
```

`getStore` 缓存 `cachedStore`，已连接则复用；`closeStore` 关闭并置空。注意 `listFolders` 内部立即 `store.close()`，返回的 `Folder` 在部分服务商（163）下不可再读取邮件。

## 扩展点

- **替换 `MailHandler` 实现**：自定义 `@Component` 实现 `MailHandler`，或覆盖 `MailServiceAutoConfiguration` 的 Bean（Spring `@ConditionalOnMissingBean` 未配置，需用 `@Primary` 或排除自动配置）。
- **新增服务商配置**：在 `resources/` 增加 `application-<vendor>.yml`，参考现有 163/qq/gmail/aliyun。
- **调整 IMAP 协商参数**：`MailHandlerImpl#getStore` 中 `clientParams`（name/version/vendor/support-email）。
- **收信分页大小**：`MailHandlerImpl.SIZE = 50`（每批从 INBOX 取 50 封倒序遍历）。

## 配置与启动

- Spring Boot 3：通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 `MailServiceAutoConfiguration`。
- 兼容旧版：`META-INF/spring.factories` 同样注册（`EnableAutoConfiguration=...`）。
- `@AutoConfigureAfter(MailSenderAutoConfiguration.class)`：确保 Spring Boot 的 `JavaMailSender` 先装配。
- `@EnableConfigurationProperties(ImapMailProperties.class)`：绑定 `spring.mail.imap.*`。
- Bean 顺序：`MailSenderAutoConfiguration` → `JavaMailSender` → `MailServiceAutoConfiguration` → `MailHandler`。
- 启动无前置条件；运行时调用收取方法时才建立 IMAP 连接，失败抛 `MessagingException`。
