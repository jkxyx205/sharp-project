# sharp-mail API

## 模块定位

`com.rick.mail:sharp-mail` 是基于 `spring-boot-starter-mail` 的邮件收发封装。提供：

- SMTP 发送：纯文本 / HTML / 附件 / 内嵌图片，支持抄送密送。
- IMAP 收取：列出文件夹、未读遍历、按 Message-ID 查询、按文件夹分页遍历。
- 收信辅助工具：附件下载、附件判定、正文文本提取（HTML 经 Jsoup 转纯文本）。

**不含模板渲染**：HTML 正文由调用方直接传字符串。

依赖：`spring-boot-starter-mail`、`org.jsoup:jsoup`（HTML→text）、`lombok`。

## 核心 public 入口

### 1. `com.rick.mail.core.MailHandler`（接口）

模块对外主入口。Spring Bean，注入即用。

```java
public interface MailHandler {
    // —— 发送 ——
    MimeMessage send(Email email);                       // 同步发送，失败抛 MailException
    MimeMessage sendAndSaveToOutbox(Email email);       // 同步发送并 append 到 IMAP "已发送" 文件夹
    MimeMessage send(MimeMessage message);              // 直接发送 javax.mail MimeMessage
    MimeMessage sendAndSaveToOutbox(MimeMessage message);

    // —— 收取（IMAP） ——
    Optional<MimeMessage> searchByMessageId(String folderName, String messageId) throws MessagingException;
    Optional<MimeMessage> searchByMessageId(Folder folder, String messageId) throws MessagingException;
    Folder[] listFolders() throws MessagingException;
    void listUnreadMessage(Consumer<MimeMessage> consumer) throws MessagingException;  // 遍历 INBOX 未读，处理后标记已读
    void listMessages(String folderName, Function<MimeMessage, Boolean> consumer) throws MessagingException;  // 返回 true 终止遍历
    void listMessages(Folder folder, Function<MimeMessage, Boolean> consumer) throws MessagingException;
}
```

行为说明：
- `send(Email)`：同步阻塞，底层调 `JavaMailSender.send`。内部 `convertEmailToMimeMessage` 构造 `MimeMessage`，按 `plainText`/`htmlText`/附件/内嵌图片组装。失败抛 `MailSendException`（包装 `MessagingException`/`UnsupportedEncodingException`）。
- `sendAndSaveToOutbox`：发送成功后通过 IMAP `appendMessages` 写入名为 `"已发送"` 的文件夹（**中文硬编码**，非中文邮箱需适配）。失败抛 `MailSendException`。
- `listUnreadMessage`：打开 INBOX，遍历未读，回调消费后置 `SEEN` 标记。`store.close()` 在结束时关闭。
- `listMessages`：按每页 `SIZE=50` 倒序（最新优先）分页遍历；`Function` 返回 `true` 终止。文件夹内默认 `READ_ONLY`。
- `searchByMessageId`：按 RFC Message-ID 搜索，返回首个匹配。**163 有已知问题**（源码 TODO）。
- `listFolders`：返回默认层下所有文件夹；调用后立即 `store.close()`，163 下返回的 Folder 不可再读邮件。

异常：发送方法抛 `org.springframework.mail.MailException` 子类；收取方法抛 `javax.mail.MessagingException`。

### 2. `com.rick.mail.core.mail.Email`（邮件实体 + 内部 Builder）

```java
public class Email {
    public static EmailBuilder builder();          // 推荐入口

    // 内部 Builder（嵌套在 Email 内）
    public static class EmailBuilder {
        public EmailBuilder from(String address, String personal);
        public EmailBuilder to(String address);                       // personal=null
        public EmailBuilder to(String address, String personal);
        public EmailBuilder cc(String address);                       // personal=null
        public EmailBuilder cc(String address, String personal);
        public EmailBuilder bcc(String address);                      // personal=null
        public EmailBuilder bcc(String address, String personal);
        public EmailBuilder subject(String subject);
        public EmailBuilder plainText(String plainText);              // 纯文本正文（UTF-8, text/plain）
        public EmailBuilder htmlText(String htmlText);                // HTML 正文（UTF-8, text/html）
        public EmailBuilder attachment(String fileName, byte[] byteArray, String type);     // type 如 "text/plain"
        public EmailBuilder attachments(List<Object[]> attachments);                       // 每项 {fileName, byteArray, type}
        public EmailBuilder embeddedImage(String fileName, byte[] byteArray, String type);  // fileName 同时作 Content-ID
        public Email build();
    }
}
```

注意：另有一个独立的 `com.rick.mail.core.mail.EmailBuilder`（顶层类），方法不全且无 `build()`，**为早期遗留，请勿使用**，统一用 `Email.builder()`。

### 3. `com.rick.mail.util.MailUtils`（收信辅助工具类）

`@UtilityClass`，方法静态调用 `MailUtils.xxx(...)`。

```java
public List<String> downloadAttachments(Message message, String downloadFolder)
        throws IOException, MessagingException;   // 保存附件到磁盘，返回文件名列表
public static boolean hasAttachment(Part part)
        throws MessagingException, IOException;   // 判断邮件是否含附件
public static String contentText(Object object)
        throws IOException, MessagingException;   // 提取正文：String 直返；MimeMultipart 递归取 text/plain，text/html 经 Jsoup 转 text
```

### 4. `com.rick.mail.config.MailServiceAutoConfiguration`（自动装配）

Spring Boot 自动配置类，注册 `MailHandler` 单例 Bean。Spring Boot 3 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册；同时 `META-INF/spring.factories` 也保留一份（兼容旧版）。

```java
@Configuration
@EnableConfigurationProperties({ImapMailProperties.class})
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
public class MailServiceAutoConfiguration {
    @Bean
    public MailHandler createSharpSenderImpl(JavaMailSender sender,
                                            MailProperties mailProperties,
                                            ImapMailProperties imapMailProperties);
}
```

### 5. `com.rick.mail.config.ImapMailProperties`（IMAP 配置）

```java
@ConfigurationProperties(prefix = "spring.mail.imap")
@Data
public class ImapMailProperties {
    private String host;
    private Integer port;
}
```

## 配置项

`spring.mail.*`（Spring Boot `MailProperties`，SMTP 发送）：

| 键 | 说明 |
|---|---|
| `spring.mail.host` | SMTP 主机，如 `smtp.163.com` |
| `spring.mail.port` | SMTP 端口，如 `25`/`465`/`587` |
| `spring.mail.username` | 邮箱账号 |
| `spring.mail.password` | **授权码**（非登录密码） |
| `spring.mail.default-encoding` | 默认 `UTF-8` |
| `spring.mail.properties.mail.smtp.auth` | `true` |
| `spring.mail.properties.mail.smtp.starttls.enable` | `true` |

`spring.mail.imap.*`（本模块自定义，IMAP 收取）：

| 键 | 说明 |
|---|---|
| `spring.mail.imap.host` | IMAP 主机，如 `imap.163.com` |
| `spring.mail.imap.port` | IMAP 端口，如 `143`/`993` |

模板路径：**无**（不使用模板引擎）。

仓库内置 4 个服务商样例 profile（`application-163.yml` / `application-qq.yml` / `application-gmail.yml` / `application-aliyun.yml`），按需激活对应 profile。

## 使用示例

### 发一封简单邮件（HTML）

```java
@Autowired
private MailHandler mailHandler;

mailHandler.send(Email.builder()
        .from("jkxyx205@163.com", "Rick.Xu")
        .to("xu.xu@yodean.com", "JIM")
        .to("1050216579@qq.com")                      // personal 可省
        .subject("no attachment")
        .htmlText("hello <span style='color:red'>world</span>")
        .build());
```

纯文本版把 `.htmlText(...)` 换成 `.plainText("...")` 即可。

### 发带附件的 HTML 邮件（含内嵌图片）

```java
import java.nio.file.Files;
import java.nio.file.Paths;

mailHandler.send(Email.builder()
        .from("jkxyx205@163.com", "Rick.Xu")
        .to("xu.xu@yodean.com", "JIM")
        .cc("154894898@qq.com", "JK")                  // 抄送
        .subject("has attachment")
        .htmlText("hello <img src=\"cid:learn.png\" /><span style='color:red'>world</span>")
        .embeddedImage("learn.png",
                Files.readAllBytes(Paths.get("/path/learn.png")), "image/png")
        .attachment("pom_extend.txt",
                Files.readAllBytes(Paths.get("/path/pom_extend.txt")), "text/plain")
        .attachment("部分合作单位.html",
                Files.readAllBytes(Paths.get("/path/部分合作单位.html")), "text/html")
        .build());
```

发送并保存到 IMAP "已发送" 文件夹：把 `mailHandler.send(...)` 换成 `mailHandler.sendAndSaveToOutbox(...)`。

### 收信（参考）

```java
// 遍历未读，自动标记已读
mailHandler.listUnreadMessage(message -> {
    try {
        System.out.println(message.getSubject());
        System.out.println(MailUtils.contentText(message.getContent()));
        System.out.println(MailUtils.hasAttachment(message));
        MailUtils.downloadAttachments(message, "/Users/rick/Documents/mail");
    } catch (Exception e) { e.printStackTrace(); }
});

// 按 Message-ID 查询
Optional<MimeMessage> hit = mailHandler.searchByMessageId("INBOX", "<475871799.1.1668661823168@[192.168.2.7]>");
```
