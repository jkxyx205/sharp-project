# sharp-fileupload 开发指南

本文件面向在本模块内做修改 / 扩展的工程师与 AI 助手。配合 `API.md`（接口签名与行为）与 `ARCHITECTURE.md`（分层与数据流）使用。

## 模块边界

- 职责：文件存储抽象 + 文档管理 + 图片处理。不负责权限校验、不负责鉴权（CorsFilter 全开 `*`，由上层网关/拦截器控制）。
- 边界：
  - 核心层 `core` + 后端实现层 `impl` + 插件层 `plugin` 是**通用**的，任何依赖方可用。
  - client 层（`client.controller` / `client.support`）依赖 `sharp-database`，仅适合需要把文件元数据落 `sys_document` 表的应用。不需要落库的应用只用 `FileStore` / `ImageService` 即可。
- 不做：CDN 刷新、断点续传、分片上传、秒传、病毒扫描。这些需要上层自建。

## 目录约定

- 核心抽象放 `core`，后端实现放 `impl.<vendor>`，与存储无关的插件放 `plugin.<topic>`，落库业务放 `client`。新增代码遵循此分层，不要把后端实现塞进 `core`。
- 配置属性类与对应实现同包或 `*.property` 子包，命名 `<Vendor>Properties` / `<Vendor>InputStreamStore`。
- 模型类放 `core.model`，异常放 `core.exception`，工具放 `core.support`。

## 编码约定

- 存储后端一律继承 `AbstractInputStreamStore`，复用 OkHttp 默认读取与 URL 拼接；仅当后端有原生读流能力时才重写 `getInputStream`。
- 文件名生成统一用 `com.rick.common.util.IdGenerator.getSequenceId()`（雪花 id），不要用 `UUID` 或原文件名作为存储名。
- 路径拼接用 `groupName + "/" + path` 模型，`fullPath` 字段在本地后端为磁盘绝对路径，其余后端为对象 key。
- `@RequiredArgsConstructor` + `final` 字段做构造注入，不用 `@Autowired` 字段注入（client 层 Controller 历史代码有少量 `@Autowired`，新增不要沿用）。
- 配置类用 `@ConfigurationProperties` + `@Data`，前缀统一 `fileupload.<vendor>`。
- 实体继承 `FileMeta` 复用元数据字段（`Document` 即如此），避免重复定义 name/extension 等。

## 常见改动清单

### 加一种存储后端（如 S3、七牛、COS）

动以下文件：
1. `impl/<vendor>/<Vendor>InputStreamStore.java`（新建，继承 `AbstractInputStreamStore`）。
2. `impl/<vendor>/property/<Vendor>Properties.java`（新建，`@ConfigurationProperties(prefix="fileupload.<vendor>")`）。
3. `core/config/FileUploadAutoConfig.java`：`@EnableConfigurationProperties` 加入新类。
4. （可选）`pom.xml` 增加对应 SDK 依赖，scope 视情况 `provided` 或 `compile`。
5. 应用侧 `@Configuration` 构造客户端 `@Bean` 返回 `InputStreamStore`。
不改动 `FileStore` / `DocumentService` / `ImageService` / Controller。

### 改默认后端

`FileUploadAutoConfig#inputStreamStore` 标了 `@ConditionalOnMissingBean`，无需改模块——在应用侧声明一个 `InputStreamStore` Bean 即覆盖。

### 加一个 Controller 接口

优先在 `client.controller` 内的现有 Controller 上加方法；若属于新业务域（如视频转码），新建 Controller 但放 `client` 包下，复用 `DocumentService` / `FileStore`。

### 改图片处理逻辑

集中在 `plugin.image.ImageService#handleImage`，参数语义见 `ImageParam`。注意 `Constants.COMPRESS_THRESHOLD` 控制压缩阈值。

### 改文档表结构

`Document` 实体（`client.support.Document`）加字段 + `@Column`，并同步更新 `sys_document` 建表 SQL（`sharp-admin/deploy/docker/init/sharp-admin.sql`、`sharp-admin/init/mysql-init-*.sql`）。`@Table` 注解指向表名，`@Transient` 标记不入库字段。

## 构建与测试

```bash
# 编译（在项目根目录）
mvn -pl sharp-fileupload -am compile

# 打包
mvn -pl sharp-fileupload -am package

# 跑测试（需本机 MySQL + 配置好 application.yml 的 fileupload.local / datasource）
mvn -pl sharp-fileupload test
```

测试为 `@SpringBootTest`，启动完整上下文，依赖：
- 本地 MySQL（`jdbc:mysql://localhost:3306/fastdfs`，见模块 `application.yml`）。
- 本地静态目录 `/Users/rick/Space/tmp/fileupload` 及 `http-server -p 7892`（用于本地后端访问）。
- 测试用例路径硬编码了作者本机路径（`/Users/rick/Space/tmp/...`），换环境需改测试代码——这是已知痛点。

`DocumentServiceTest` / `ImageServiceTest` / `FileStoreTest` / `InputStreamStoreTest` 之间通过 `@Order` 依赖同一份静态 `path`/`id`，单独跑可能失败。

## 陷阱

### 路径安全
- `groupName` 与 `path` 直接拼进磁盘路径（本地）和对象 key（OSS/MinIO），**未做路径穿越校验**。`groupName` 若来自用户输入需在上层做白名单/字符过滤，避免 `../` 注入。Controller 层 `groupName` 当前直接取 query 参数。
- 本地后端用 `new File(rootPath, groupName)` + `new File(storePath, storeFullName)` 拼接，未规范化；`..` 会被解析。

### 文件名编码
- `FileMeta.name` 取自 `MultipartFile.getOriginalFilename()`，原文件名编码取决于客户端/Servlet 容器；存储名统一用雪花 id 规避编码问题。
- `contentType` 在 `parse(File)` 路径走 `URLConnection` 探测，可能返回 `null` 或 `application/octet-stream`，落库后预览图片可能被当作非图片处理。

### 异常信息丢失
- `MinioInputStreamStore.store` / `delete` / `getInputStream` 中 `catch (Exception e) { throw new IOException(); }` 抛出的 IOException **不带 message 和 cause**，排障困难。新增后端实现时应 `new IOException(e)` 保留原因。

### OkHttp 回环读取
- `AbstractInputStreamStore` 默认 `getInputStream` 用 OkHttp GET `getURL(...)`。本地后端若 `server-url` 指向不可达地址（如未启动 `http-server`），`getByteArray` 会失败——本地后端已重写 `getInputStream` 走文件系统规避；FastDFS 未重写，依赖 tracker http 服务可达。

### 临时文件清理
- 批量下载（`DocumentServiceImpl.download`）在 `fileupload.tmp` 下建临时目录与 zip，下载完成后 `deleteQuietly`。若 JVM 异常退出会残留临时文件，需运维定期清理 `tmp` 目录。
- `ImageService.write` 多处 `os.close()`，调用方不应再次关闭（可能重复关闭，待确认是否需要修正为只关一次）。

### 逻辑删除
- `Document.deleted` 列 `is_deleted bit(1)`，`delete(...)` 走 `DocumentDAO.deleteByIds` 是逻辑删除（基于 `SharpDbConstants.LOGIC_DELETE_COLUMN_NAME`），存储层文件**已被物理删除**但库记录保留——删除失败时（`e.printStackTrace()` 吞异常）会出现库里有记录但存储无文件的脏数据。

### FastDFS 文件名不可控
- `FastDFSInputStreamStore.store` 忽略入参 `storeName`，由服务端生成 file_id 并 warn 日志；`path` 返回的是 FastDFS file_id 而非 `name.ext`，下游依赖 `name.ext` 格式的代码（如 `preview2/{id}/{fileName}.{extension}`）可能失效。

### minio 依赖非 provided
- `io.minio:minio` 是 compile scope，会传递给所有依赖方（即便用本地存储）。若依赖方体积敏感，可考虑改 provided 并由应用侧显式声明。

### CORS 全开
- `FileUploadAutoConfig.corsFilter` 允许任意 origin/header/method。生产若部署在公网且无网关收口，存在 CSRF/跨域风险，应在上层覆盖或收紧。

## 待确认

- `core.support.FileCheckUtils` 当前为空类，用途未明（占位？计划中的校验工具？）。
- `ImageParam` 注释 `坐标x` 出现两次（`x` 和 `y` 都标了"坐标x"），`y` 注释应为"坐标y"——属文档性 bug。
- `AbstractInputStreamStore.getURL` 直接字符串拼接 `serverUrl + groupName + "/" + path`，未处理 `serverUrl` 结尾是否带 `/`；`LocalProperties.serverUrl` 示例值带尾 `/`，OSS/MinIO 的 `getServerUrl()` 返回值也带尾 `/`。若配置漏掉尾 `/` 会得到 `http://hostgroup/path`。
