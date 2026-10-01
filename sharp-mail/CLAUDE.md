# CLAUDE.md — sharp-mail

## 模块边界

`sharp-mail` 只做两件事：**SMTP 发邮件** + **IMAP 收/读邮件**，外加收取侧的解析工具。不做模板渲染、不管理发送队列、不做异步/重试。HTML 正文由调用方传字符串，附件以 `byte[]` 传入。被 `sharp-admin`、`sharp-demo` 作为依赖引入（`sharp-demo` 中有但已注释的测试用例可参考真实调用形态）。

## 目录约定

```
src/main/java/com/rick/mail/
  config/   # 配置绑定 + 自动配置（不写业务）
  core/     # MailHandler 接口 + 唯一实现 + Email 数据模型
  util/     # 静态工具（收取/解析侧）
src/main/resources/
  META-INF/spring.factories          # 自动装配注册，改动需慎重
  application-{163,qq,gmail,aliyun}.yml  # 服务商示例 profile
```

## 编码约定

- 邮件 DTO 用 `Email.builder()`（嵌套 builder），**不要**用顶层遗留 `EmailBuilder`（其 `cc/bcc` 为空实现，且缺 `htmlText/attachment`）。
- 收件人/附件用 `List<Object[]>` 存储，约定顺序见 `Email` 字段注释；新增字段须同步改 `convertEmailToMimeMessage`。
- 字符编码统一 UTF-8（正文 `setText(..., "UTF-8", ...)`）。
- 中文附件名经 `MimeUtility.encodeText` 编码后再设置。
- `MailHandlerImpl` 依赖 `JavaMailSender` / `MailProperties` / `ImapMailProperties` 三个 Bean，构造器注入（`@RequiredArgsConstructor`）。

## 常见改动清单

- 新增服务商示例：在 `resources/` 加 `application-<provider>.yml`，填 SMTP+IMAP 参数即可。
- 调整收取分页大小：`MailHandlerImpl.SIZE`（当前 50）。
- 适配非中文「已发送」文件夹：`MailHandlerImpl.saveToOutbox` 中 `"已发送"` 字面量。
- 增加异步/重试：在 `MailHandler` 之上包一层，**勿**直接改 `MailHandlerImpl` 的同步语义。
- 自定义 IMAP 客户端 ID：`getStore()` 内 `clientParams` Map。

## 构建与测试

```bash
# 在 sharp-project 根目录
mvn -pl sharp-mail -am compile -DskipTests      # 仅编译
mvn -pl sharp-mail -am test                      # 跑测试（模块本身无 src/test）
mvn -pl sharp-mail -am clean install -DskipTests # 安装到本地仓库
```

模块自身**无单元测试**；端到端用例在 `sharp-demo/src/test/java/com/rick/demo/mail/` 下（`AliyunMailTest`、`One63MailTest`），但代码当前全部注释，需真实邮箱授权码才能启用。

## 陷阱

- **遗留 `EmailBuilder` 勿用**：顶层 `com.rick.mail.core.mail.EmailBuilder` 的 `cc`/`bcc` 是 `return this;` 空实现，且无 `plainText/htmlText/attachment`。永远用 `Email.builder()`。
- **plainText/htmlText 互斥**：同时设置两者，`htmlText` 被静默丢弃（`if/else if` 优先 plainText）。
- **SMTP 认证**：`password` 是邮箱开启 SMTP/IMAP 服务后下发的**授权码**，不是登录密码。各服务商开启入口见 `application-*.yml` 内的注释链接。
- **附件大小**：附件整体以 `byte[]` 在内存中加载，大附件会占堆内存并可能触发 OOM；当前无大小限制、无流式接口。
- **163 IMAP**：
  - `getStore()` 已内置 `IMAPStore.id(clientParams)` 发送客户端 ID，否则会被服务端拒为 "Unsafe Login"。
  - `searchByMessageId` 在 163 下不稳定（源码 TODO）。
  - `listFolders()` 调用后 `store.close()`，返回的 `Folder` 对象无法再 open/read。
- **异步发送与失败重试**：模块**无**异步、**无**重试。`send` 同步阻塞，失败抛 `MailSendException`，调用方自行决定是否重试。
- **「已发送」文件夹名硬编码**：`saveToOutbox` 写死中文 `"已发送"`，Gmail/Outlook 等需改源码（Gmail=`[Gmail]/Sent Mail`）。
- **线程安全**：`MailHandlerImpl.cachedStore` 为共享实例字段，并发收件未隔离。
- **`sendAndSaveToOutbox` 依赖 IMAP 配置**：仅配 SMTP 不配 `spring.mail.imap.*` 时该方法会抛 `MessagingException`。
- **模板变量**：模块不渲染模板，不存在"模板变量未定义"问题；HTML 字符串由调用方完全负责。
