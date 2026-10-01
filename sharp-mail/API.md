# sharp-mail API

## 模块定位

`com.rick.mail:sharp-mail`（版本 `${sharp.version}` = 2.0-SNAPSHOT）是一个基于 `spring-boot-starter-mail` 的轻量邮件组件，封装 **SMTP 发送 + IMAP 收取**两类能力：

- 发送：纯文本 / HTML / 附件 / 内嵌图片（CID），可选"发送并归档到已发送"。
- 收取：列文件夹、未读遍历、按 Message-ID 查找、文件夹分页遍历。
- 解析辅助：提取正文、判断附件、下载附件到磁盘。

模块**不含模板渲染**（无 Freemarker / Thymeleaf）。HTML 正文由调用方直接传字符串，附件/图片以 `byte[]` 传入。HTML→纯文本转换由 `jsoup` 在收取侧完成。

## 核心 public 入口

### `com.rick.mail.core.MailHandler`（接口）

模块对外最高抽象。Spring Bean，由 `MailServiceAutoConfiguration` 自动装配（Bean 名 `createSharpSenderImpl`）。注入方式：

```java
@Autowired
private MailHandler mailHandler;
```

方法签名（逐参数注释）：

| 方法 | 说明 |
|---|---|
| `MimeMessage send(Email email)` | **同步**发送。内部把 `Email` 转成 `MimeMessage` 再调用 `JavaMailSender.send`。失败抛 `org.springframework.mail.MailSendException`。返回原 message（含 Message-ID）。 |
| `MimeMessage sendAndSaveToOutbox(Email email)` | 同步发送后，再通过 IMAP `appendMessages` 归档到「已发送」文件夹并标记已读。需要配置 `spring.mail.imap.*`，否则 `getStore()` 抛 `MessagingException`。 |
| `MimeMessage send(MimeMessage message)` | 直接发送已构造好的 `MimeMessage`。 |
| `MimeMessage sendAndSaveToOutbox(MimeMessage message)` | 同上 + 归档到「已发送」。 |
| `Optional<MimeMessage> searchByMessageId(String folderName, String messageId)` | 在指定 IMAP 文件夹按 `Message-ID` 头查找。`folderName` 例如 `"INBOX"`、`"已发送"`；`messageId` 需带尖括号如 `<...@...>`。源码标注 `//TODO 163 读取有问题`——163 邮箱下行为不稳定，待确认。 |
| `Optional<MimeMessage> searchByMessageId(Folder folder, String messageId)` | 同上，但传入已打开的 `Folder`。 |
| `Folder[] listFolders()` | 列出邮箱全部顶层文件夹。调用后 store 会被关闭，之后 folder 内容不可再读。 |
| `void listUnreadMessage(Consumer<MimeMessage> consumer)` | 遍历 INBOX 未读，逐封回调；回调后将该邮件标记为 SEEN。consumer 抛异常会向上传播并中断遍历。 |
| `void listMessages(String folderName, Function<MimeMessage, Boolean> consumer)` | 在指定文件夹分页（每页 50 封，从最新向最旧）遍历。`consumer` 返回 `true` 终止遍历。 |
| `void listMessages(Folder folder, Function<MimeMessage, Boolean> consumer)` | 同上，已打开的 folder。 |

所有方法**同步**执行；模块内无异步、无重试、无连接池。IMAP `Store` 在 `MailHandlerImpl` 实例内单实例缓存（`cachedStore` 字段），**非线程安全**。

### `com.rick.mail.core.mail.Email`（数据载体）

`@Getter @AllArgsConstructor` 的不可变邮件 DTO，**唯一推荐构造方式**：

```java
Email.builder().from(...).to(...).subject(...).build()
```

嵌套 `Email.EmailBuilder` 方法（链式）：

| 方法 | 参数 |
|---|---|
| `from(String address, String personal)` | 发件人邮箱地址、显示名（personal 可为 null） |
| `to(String address)` / `to(String address, String personal)` | 收件人，可多次调用追加 |
| `cc(String address)` / `cc(String address, String personal)` | 抄送，可多次调用 |
| `bcc(String address)` / `bcc(String address, String personal)` | 密送，可多次调用 |
| `subject(String subject)` | 主题 |
| `plainText(String plainText)` | 纯文本正文（UTF-8） |
| `htmlText(String htmlText)` | HTML 正文（UTF-8） |
| `attachment(String fileName, byte[] byteArray, String type)` | 附件：文件名（中文会自动 `MimeUtility.encodeText`）、字节内容、MIME 类型如 `text/plain` `image/png` |
| `attachments(List<Object[]> attachments)` | 批量附件，每个元素为 `{fileName, byteArray, type}` |
| `embeddedImage(String fileName, byte[] byteArray, String type)` | 内嵌图片，`Content-ID` = 文件名，HTML 中用 `<img src="cid:fileName">` 引用 |
| `build()` | 构造 `Email` |

行为说明：
- `plainText` 与 `htmlText` **互斥**；两者都设时 `plainText` 生效，`htmlText` 被丢弃（源码 `if/else if`）。
- 无附件/无内嵌图时正文为单一 `text/*` part；有任一附件时整体改为 `multipart/mixed`。

### `com.rick.mail.util.MailUtils`（静态工具）

`@UtilityClass`，收取/解析侧辅助。方法：

- `List<String> downloadAttachments(Message message, String downloadFolder)` — 把邮件附件保存到 `downloadFolder` 目录，返回已下载文件名列表。
- `boolean hasAttachment(Part part)` — 判断是否含附件。
- `String contentText(Object object)` — 提取正文文本。`text/plain` 直接拼；`text/html` 经 `Jsoup.parse(html).text()` 转纯文本；`MimeMultipart` 递归。

### `com.rick.mail.config.ImapMailProperties`（配置类）

`@ConfigurationProperties(prefix = "spring.mail.imap")`，仅两个字段：

- `String host` — IMAP 主机
- `Integer port` — IMAP 端口

### `com.rick.mail.config.MailServiceAutoConfiguration`（自动配置）

`@Configuration`，`@AutoConfigureAfter(MailSenderAutoConfiguration.class)`，`@EnableConfigurationProperties({ImapMailProperties.class})`。注册 `MailHandler` Bean。通过 `META-INF/spring.factories` 注册到 `EnableAutoConfiguration`，引入本模块即自动生效。

## 配置项

复用 Spring Boot 标准 `spring.mail.*`（SMTP）+ 模块自定义 `spring.mail.imap.*`（IMAP）：

```yaml
spring:
  mail:                      # SMTP 发邮件（spring-boot 标准）
    host: smtp.163.com
    port: 25                 # 465/SSL, 587/STARTTLS 视服务商
    username: xxx@163.com
    password: xxx            # 授权码，非登录密码
    default-encoding: UTF-8
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
  mail:
    imap:                    # IMAP 收邮件（sharp-mail 自定义）
      host: imap.163.com
      port: 143              # 993/SSL
```

模块自带 4 个示例 profile（`src/main/resources/application-*.yml`）：`application-163.yml`、`application-qq.yml`、`application-gmail.yml`、`application-aliyun.yml`，分别覆盖 163 / QQ / Gmail / 阿里云企业邮箱的 SMTP+IMAP 默认参数，可直接 `--spring.profiles.active=qq` 引用。

无独立"模板路径"配置项（不渲染模板）。

## 使用示例

### 发一封简单邮件（纯文本）

```java
@Autowired
private MailHandler mailHandler;

mailHandler.send(Email.builder()
        .from("me@example.com", "Rick")
        .to("friend@example.com", "Jim")
        .subject("hello")
        .plainText("一封简单邮件。")
        .build());
```

### 发一封带附件的 HTML 邮件（最小片段）

```java
mailHandler.send(Email.builder()
        .from("me@example.com", "Rick")
        .to("friend@example.com")
        .subject("带附件与内嵌图片")
        .htmlText("hello <img src=\"cid:logo.png\" /><b>world</b>")
        .embeddedImage("logo.png",
                Files.readAllBytes(Paths.get("/tmp/logo.png")),
                "image/png")
        .attachment("report.txt",
                Files.readAllBytes(Paths.get("/tmp/report.txt")),
                "text/plain")
        .build());
```

需要"发送并归档到已发送"时，把 `send(...)` 换成 `sendAndSaveToOutbox(...)`（前提：`spring.mail.imap.*` 已配置）。
