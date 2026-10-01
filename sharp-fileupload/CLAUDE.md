# CLAUDE.md — sharp-fileupload

> 给 AI 助手的模块工作指南。读完即应能正确改动本模块。

## 模块边界

`sharp-fileupload` 负责"文件上传 / 存储 / 图片处理"全链路。对外契约面：

- **核心抽象层** `com.rick.fileupload.core`：`InputStreamStore` / `FileStore` / `FileMeta` / `StoreResponse` / `FileUploadAutoConfig`。任何业务方都只面向这一层编程。
- **后端实现层** `com.rick.fileupload.impl`：local / oos(OSS+MinIO) / fastdfs。可按需选一个 `@Bean InputStreamStore` 注入。
- **插件层** `com.rick.fileupload.plugin.image`：`ImageService` + `ImageParam` + `NameImageCreator`。
- **客户端层** `com.rick.fileupload.client`：`DocumentController` / `ImageController` / `Document`(+DAO+Service)。可选；引入即拥有 `/documents` `/images` 两套 HTTP 接口和 `sys_document` 表映射。

不在本模块职责内：权限控制、CDN 分发、断点续传、分片上传、病毒扫描。

## 目录约定

```
sharp-fileupload/
├── src/main/java/com/rick/fileupload/
│   ├── core/             # 抽象与配置 —— 改动需保持后端无关
│   ├── impl/             # 后端实现 —— 一个后端一个子包
│   │   ├── local/
│   │   ├── oos/
│   │   └── fastdfs/
│   ├── plugin/image/     # 图片处理
│   └── client/           # 可选 HTTP+DB 客户端层
├── src/main/resources/
│   ├── application.yml        # 模块自带示例配置（独立运行用）
│   └── fdfs_client.properties # FastDFS 配置样例
└── src/test/java/com/rick/fileupload/  # SpringBootTest，依赖本地路径与 MySQL
```

约定：
- 后端实现一律放 `impl/<后端>/`，属性类放 `impl/<后端>/property/`。
- 新增工具放 `core/support/`，新增模型放 `core/model/`。
- Controller 一律在 `client/controller/`，DB 实体/DAO/Service 在 `client/support/`。

## 编码约定

- 实现类用 `@RequiredArgsConstructor` + `final` 字段做构造注入。
- 属性类用 `@Data` + `@ConfigurationProperties(prefix=...)`，前缀统一 `fileupload.*`。
- 后端实现继承 `AbstractInputStreamStore`，只实现 `store(group,storeName,ext,is)` / `delete` / `getServerUrl()`；能直接给流的覆盖 `getInputStream`，避免走 OkHttp。
- object key 一律拼成 `groupName/storeName.extension`，URL 一律 `getServerUrl() + groupName + "/" + path`（`getServerUrl()` 带尾斜杠）。
- 元数据走 `FileMeta`，业务子类（如 `Document`）继承它扩展持久化字段。
- 图片处理入口统一走 `ImageService`，参数对象 `ImageParam`，阈值用 `Constants.COMPRESS_THRESHOLD`。
- 异常：非图片类型用 `NotImageTypeException`（RuntimeException）；IO 异常向上抛 `IOException`。
- 不要在本模块写业务校验（大小/类型白名单）——这是使用方的职责，模块只提供 `max-file-size` 透传。

## 常见改动清单

### 加一种存储后端

1. 新建 `impl/<name>/<Name>InputStreamStore extends AbstractInputStreamStore`。
2. 实现 `store` / `delete` / `getServerUrl()`；覆盖 `getInputStream`（若 SDK 直给流）。
3. （可选）`impl/<name>/property/<Name>Properties` + `@ConfigurationProperties("fileupload.<name>")`。
4. 在 `FileUploadAutoConfig` 的 `@EnableConfigurationProperties` 列表里加上新 Properties 类。
5. 文档：在 `API.md` / `ARCHITECTURE.md` 后端实现章节追加。
6. 不动 `FileStore` / `DocumentService` / Controller。

### 切换默认后端

不要改 `FileUploadAutoConfig`（默认本地是 `@ConditionalOnMissingBean` 的兜底）。在业务方工程建 `@Configuration` + `@Bean InputStreamStore` 即覆盖。

### 加一个上传/下载接口

改 `client/controller/DocumentController` 或在业务方自己的 Controller 注入 `DocumentService` / `FileStore`。优先复用 `FileMetaUtils.parse` 解析。

### 加图片处理参数

`ImageParam` 加字段 → 在 `ImageService.handleImage` 里读取并作用到 `Thumbnails.Builder` → 同步更新 `ImageParam.isEmpty()/isSource()` 的判定。

### 改 sys_document 表结构

改 `client/support/Document` 字段 + `@Column`；`DocumentDAO` 一般无需动。若改主键类型，同步 `DocumentController` 的 `@PathVariable Long id`。

## 构建与测试

```bash
# 在 sharp-project 根目录
mvn -pl sharp-fileupload -am clean install -DskipTests

# 跑测试（需要本地 MySQL + 配好 application.yml 的路径/OSS 凭据）
mvn -pl sharp-fileupload test

# 单测
mvn -pl sharp-fileupload test -Dtest=InputStreamStoreTest
mvn -pl sharp-fileupload test -Dtest=FileStoreTest
mvn -pl sharp-fileupload test -Dtest=ImageServiceTest
mvn -pl sharp-fileupload test -Dtest=DocumentServiceTest
```

测试为 `@SpringBootTest`，会拉起完整上下文，依赖：
- MySQL（`application.yml` 指向 `jdbc:mysql://localhost:3306/fastdfs`，库需建好 `sys_document` 表，schema 由 `sharp-database2` 自动建表待确认）。
- 本地路径 `/Users/rick/Space/tmp/fileupload/demo/1.jpg`（硬编码，跨机器需改）。
- 切 OSS/MinIO 时需在测试配置里给真实凭据 + `@Bean InputStreamStore`（参考 `sharp-demo/config/SharpConfig`，里面的三个 `@Bean` 默认被注释，按需打开）。

## 陷阱

- **路径安全**：本地实现 `storeName` 用 `IdGenerator.getSequenceId()` 序列号而非用户原文件名，规避路径穿越；但 `Document.name` 存的是原名，前端回显时注意 XSS。`groupName` 直接拼路径，不要让用户可控。
- **文件名编码**：依赖 `MultipartFile.getOriginalFilename`（取决于客户端 `Content-Disposition` 编码）。中文文件名在某些浏览器下乱码属常见问题，本模块不做转码。
- **`getServerUrl()` 尾斜杠**：本地/OSS/MinIO 实现都返回带尾斜杠的根 URL，`getURL` 拼接依赖这一点。新增后端必须保持。
- **FastDFS 读取依赖 HTTP**：`FastDFSInputStreamStore` 未覆盖 `getInputStream`，走 `AbstractInputStreamStore` 的 OkHttp GET，需要 FastDFS tracker 自带 HTTP 服务（`http_tracker_http_port`）可达。
- **MinIO 异常吞栈**：`MinioInputStreamStore.store/delete/getInputStream` 把 `Exception` 包成 `new IOException()`（无 message）。改这块时优先把原始异常 attach 上去。
- **临时文件清理**：`DocumentServiceImpl.download` 批量下载在 `fileupload.tmp` 下建临时目录，最后 `FileUtils.deleteQuietly(home)` 清理；若中途抛异常会泄漏临时目录。`downloadFolder` 不清理本地。
- **`FileMeta.data` 内存占用**：`FileMetaUtils.parse` 一次性 `IOUtils.toByteArray`，大文件全在内存。`upload3` 接 `@RequestBody List<FileMeta>`，data 走 Jackson base64 解码，超大 base64 会撑爆堆。
- **`@ConditionalOnMissingBean` 顺序**：业务方声明 `@Bean InputStreamStore` 会替换默认本地实现，但要确保业务 `@Configuration` 被 `@ComponentScan` 扫到（在 `scanBasePackages` 范围内）。
- **provided 依赖**：OSS / FastDFS / pdfbox / sharp-database2 / mysql 驱动都是 `provided`，使用方 POM 不引入对应 SDK 时，调用对应实现会 `ClassNotFoundException`。MinIO 是 compile 会被传递。
- **CORS 全放开**：`FileUploadAutoConfig.corsFilter` 配置 `*` origin/header/method，生产环境应按需收紧或覆盖。
- **`downloadFolder` 仅 OSS 实现**：本地/MinIO/FastDFS 调用为空操作（default 方法空体），不会报错但也不会下载，使用前确认后端类型。
- **PDF 转图字体**：`FileConvertUtils` 用 pdfbox 渲染，服务器缺中文字体时 PDF 转图会乱码，与本模块代码无关，需系统层装字体。

## 核心入口速查

| 想做的事 | 注入/调用 |
|---|---|
| 上传 `MultipartFile` | `FileStore.upload(List<MultipartFile>, groupName)` |
| 上传 `File` | `FileStore.storeFiles(List<File>, groupName)` |
| 上传已 parse 的 `FileMeta` | `FileStore.storeFileMeta(List<FileMeta>, groupName)` |
| 直接存流 | `InputStreamStore.store(group, storeName, ext, is)` |
| 取流 / 取字节 / 删 / 拼 URL | `InputStreamStore.getInputStream/getByteArray/delete/getURL` |
| 含 DB 持久化的上传/下载/删除 | `DocumentService.store/download/delete` |
| 图片实时处理 | `ImageService.write(FileMeta, ImageParam, OutputStream)` |
| 姓名头像 | `ImageService.createImage(text, groupName)` |
| PDF 转图 | `FileConvertUtils.pdf2Image(byte[], dpi)` |
| 解析 MultipartFile → FileMeta | `FileMetaUtils.parse(multipartFile)` |
