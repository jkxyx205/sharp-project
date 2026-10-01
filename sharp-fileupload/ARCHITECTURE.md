# sharp-fileupload 架构

## 包结构

```
com.rick.fileupload
├── FileUploadApplication              # 可独立启动的 Spring Boot 入口（演示/测试用）
├── core                                # 核心抽象，与后端无关
│   ├── InputStreamStore                # 存储接口（根抽象）
│   ├── AbstractInputStreamStore        # 公共实现：OkHttp 拉流、URL 拼接、自动命名 store
│   ├── FileStore                       # 业务门面：MultipartFile/File/FileMeta → store
│   ├── Constants                       # 压缩阈值等常量
│   ├── config
│   │   └── FileUploadAutoConfig        # 自动装配：CorsFilter + 默认本地 InputStreamStore + FileStore + ImageService
│   ├── support
│   │   ├── FileUploadProperties        # @ConfigurationProperties("fileupload")，仅 tmp
│   │   ├── FileMetaUtils               # MultipartFile/File → FileMeta 解析
│   │   └── FileConvertUtils            # PDF→图片（pdfbox + itext，provided）
│   ├── model
│   │   ├── FileMeta                    # 文件元数据 POJO
│   │   └── StoreResponse              # store 返回值（不可变）
│   └── exception
│       └── NotImageTypeException       # 非图片类型
├── impl                                # 后端实现
│   ├── local
│   │   ├── LocalInputStreamStore       # 本地磁盘
│   │   └── property/LocalProperties    # fileupload.local.{serverUrl,rootPath}
│   ├── oos
│   │   ├── OSSInputStreamStore         # 阿里云 OSS
│   │   ├── MinioInputStreamStore       # MinIO（与 OSS 共用 OSSProperties）
│   │   └── property/OSSProperties      # fileupload.oss.{endpoint,accessKeyId,accessKeySecret,bucketName}
│   └── fastdfs
│       └── FastDFSInputStreamStore     # FastDFS（构造时读 fdfs_client.properties 初始化 ClientGlobal）
├── plugin
│   └── image
│       ├── ImageService                # 图片处理（裁剪/压缩/旋转/格式转换/姓名头像）
│       ├── ImageParam                  # 处理参数（zimg 风格）
│       └── NameImageCreator            # 100x100 姓名头像生成
└── client                              # 开箱即用的 HTTP + DB 客户端层
    ├── controller
    │   ├── DocumentController          # /documents 上传/下载/预览/删除/重命名
    │   └── ImageController             # /images 图片预览/裁剪/姓名头像
    └── support
        ├── Document                   # @Table("sys_document") extends FileMeta
        ├── DocumentDAO                # extends EntityDAOImpl<Document, Long>
        ├── DocumentService             # 接口
        └── DocumentServiceImpl         # @Service，组合 FileStore+DocumentDAO+ImageService
```

## 关键抽象

### InputStreamStore ↔ 实现的关系

```
            <<interface>> InputStreamStore
                     │
        AbstractInputStreamStore (abstract)
        - getURL: serverUrl + group + "/" + path
        - getInputStream: OkHttp GET getURL  (可被覆盖)
        - store(g, ext, is): 委托 store(g, seqId, ext, is)
                     │
        ┌────────────┼──────────────┬────────────────────┐
        │            │              │                    │
 LocalInputStream  OSSInput       MinioInput         FastDFSInput
 Store            StreamStore     StreamStore         Store
 (rootPath/       (OSS client,   (MinioClient,       (tracker+storage,
  groupName)       bucket)        bucket)             忽略 storeName)
```

- `LocalInputStreamStore` 覆盖 `getInputStream` 走本地 `FileInputStream`，不走 OkHttp。
- `OSSInputStreamStore` / `MinioInputStreamStore` 覆盖 `getInputStream` 走各自 SDK。
- `FastDFSInputStreamStore` 没覆盖 `getInputStream`，因此读取走 OkHttp GET `getURL`（依赖 FastDFS 自带 HTTP）。
- 三种对象存储后端的 `store` 都把 object key 拼成 `groupName/storeName.extension`，访问 URL 拼成 `{serverUrl}/{groupName}/{path}`。本地实现 `fullPath` 字段是磁盘绝对路径，对象存储实现 `fullPath` 是 `groupName/path`。

### FileStore 门面

`FileStore` 同时实现 `InputStreamStore`（逐方法委托底层）并提供 `upload` / `storeFiles` / `storeFileMeta` 三个高层方法。它在 `FileUploadAutoConfig` 中标注 `@Primary`，业务方注入 `FileStore` 即可；想直接操作底层就注入 `InputStreamStore`。

### ImageService

`ImageService` 不直接落盘，而是写 `OutputStream`。`write` 方法：若 `fileMeta.data` 为空则先 `inputStreamStore.getInputStream` 拉回字节，再按 `ImageParam` 调 thumbnailator 处理。`cropPic(...)` 系列把结果写回 `fileMeta.data` 便于后续 `FileStore.storeFileMeta`。`createImage` 直接调 `inputStreamStore.store` 落 PNG 并返回 URL。

### 客户端层（client）

`DocumentController` / `ImageController` 是可选组件，**业务方在主应用类上 import 即启用**：

```java
// sharp-admin / sharp-demo 的启动类
@SpringBootApplication(scanBasePackages = "com.rick")
@Import({DocumentController.class, DocumentServiceImpl.class, DocumentDAO.class})
public class XxxApplication { ... }
```

> 待确认：实际用法见 sharp-admin `SharpAdminApplication` / sharp-demo `DemoApplication`，二者都显式 import 这三个 Bean。

`DocumentServiceImpl` 是 `DocumentService` 唯一实现，依赖：
- `FileStore`（落盘）
- `DocumentDAO`（DB，基于 `sharp-database2` 的 `EntityDAOImpl`）
- `FileUploadProperties.tmp`（批量下载 zip 临时目录）
- `ImageService`（图片预览路径）

`Document` 继承 `FileMeta` 并实现 `BaseEntityInfoGetter`，加 `@Table("sys_document")`，把上传元数据落到该表。

## 数据流

### 上传（MultipartFile）

```
HTTP /documents/upload
  → DocumentController.fileUpload
  → FileMetaUtils.parse(MultipartRequest) 得到 List<FileMeta>（data 字节已读）
  → DocumentService.store(fileMetaList, groupName)
     → FileStore.storeFileMeta(list, groupName)
        → 对每个 FileMeta：
            inputStreamStore.store(group, ext, new ByteArrayInputStream(data))
            → StoreResponse{path, url}
            回填 fileMeta.groupName/path/url
     → DocumentDAO.insertOrUpdate(documentList)  # 落 sys_document
  → 返回 List<Document>（含 id、url）
```

### 下载

```
GET /documents/download/{id}
  → DocumentService.download(request, response, id)
  → findById(id) 取 Document
  → HttpServletResponseUtils.getOutputStreamAsAttachment(fullName)
  → FileCopyUtils.copy(fileStore.getInputStream(group, path), os)

GET /documents/download?id=1&id=2  (批量)
  → 逐个 findById
  → 在 fileupload.tmp 下建临时目录
  → downloadDocument2Folder 把每个文件落到临时子目录
  → ZipUtils.zipDirectoryToZipFile 打包
  → 复制到 response，最后 FileUtils.deleteQuietly(home)
```

### 图片预览

```
GET /images/{id}?w=500&p=0
  → DocumentService.findById(id) → Document（FileMeta）
  → ImageService.write(document, imageParam, response os)
     → data 为空？ inputStreamStore.getInputStream 拉
     → thumbnailator 处理 → os
```

### 预览（非图片）

```
GET /documents/preview2/{id}
  → findById → Document
  → HttpServletResponseUtils.getOutputStreamAsView(fullName)
  → DocumentService.preview(id, imageParam, os)
     → isImageType(extension, contentType) ? ImageService.write : 直接吐原字节
```

## 对外依赖

| 依赖 | scope | 用途 |
|---|---|---|
| `sharp-common` | compile | `FileUtils`、`IdGenerator`、`StringUtils` 等工具 |
| `sharp-database2` | **provided** | `DocumentDAO` / `Document` 依赖 `EntityDAOImpl` / `@Table` 等。使用方需自备 |
| `mysql-connector-java` 5.1.47 | provided | 测试时驱动 |
| `spring-boot-starter-web` | compile | Controller、MultipartFile |
| `commons-io` | compile | IOUtils / FileUtils |
| `okhttp` | compile | `AbstractInputStreamStore` 默认 `getInputStream` 走 HTTP 拉流 |
| `thumbnailator` 0.4.8 | compile | `ImageService` 图片处理 |
| `aliyun-sdk-oss` 3.10.2 | **provided** | OSS 实现；用 OSS 时由使用方引入 |
| `minio` 8.5.17 | compile | MinIO 实现（默认带入） |
| `fastdfs-client-java` 1.29 | **provided** | FastDFS 实现 |
| `pdfbox` 2.0.12 + `itext` 2.0.7 | **provided** | `FileConvertUtils` PDF→图片 |
| `spring-boot-starter-test` | test | 测试 |

> 关键设计：OSS/FastDFS/PDFBox/数据库驱动全部 `provided`，模块本体零强依赖；使用方按需在自身 POM 引入对应 SDK 才能切到该后端。MinIO 反而是 compile，使用方即使不用 MinIO 也会被传递依赖。

## 扩展点

### 新增一种存储后端

1. 新建 `impl/xxx/XxxInputStreamStore extends AbstractInputStreamStore`。
2. 实现 `store(group, storeName, ext, is)`、`delete(group, path)`、`getServerUrl()`；可选覆盖 `getInputStream`（若 SDK 能直接给流就不要走 OkHttp）。
3. 若需配置，新建 `impl/xxx/property/XxxProperties` + `@ConfigurationProperties("fileupload.xxx")`，并在 `FileUploadAutoConfig` 的 `@EnableConfigurationProperties` 列表里追加。
4. 在业务方 `@Configuration` 里 new 出客户端（如 `OSS`/`MinioClient`）并 `@Bean InputStreamStore` 返回你的实现，覆盖默认本地实现。
5. 若是对象存储且支持列举，可覆盖 `downloadFolder` / `downloadFile`。

无需改动 `FileStore` / `DocumentService` / `Controller`——它们全部面向 `InputStreamStore` 接口编程。

### 新增图片处理能力

`ImageService.handleImage` 是私有方法，扩展需直接改本类或新建并行 Service 注入 `InputStreamStore`。

## 配置与启动

- 自动装配入口：`FileUploadAutoConfig`（被 Spring Boot 扫到即生效，无 `spring.factories` / `AutoConfiguration.imports`，依赖包扫描）。
- 默认后端：本地磁盘（`@ConditionalOnMissingBean(InputStreamStore.class)`）。
- 切换后端：业务方在 `@Configuration` 里声明一个 `@Bean InputStreamStore` 即覆盖默认。
- `application.yml` 必须配 `fileupload.local.*`（默认后端用）；切 OSS/MinIO 时配 `fileupload.oss.*`。
- 独立运行：`FileUploadApplication` 是个标准 Spring Boot main，配合 `application.yml` + MySQL（fastdfs 库）可跑测试。

## 已知设计权衡

- `AbstractInputStreamStore.getInputStream` 默认走 OkHttp 而非 SDK，意味着本地后端必须覆盖它（已覆盖），对象存储也覆盖了，但 FastDFS 没覆盖——FastDFS 依赖其内置 HTTP 服务才能读。
- `FileMeta.data` 是字节缓存，大文件会占内存；`FileMetaUtils.parse(MultipartFile)` 一次性 `IOUtils.toByteArray`。
- `DocumentController` 的 `upload3` 接 `@RequestBody List<FileMeta>`，要求 JSON 里带 base64 之类的 data —— 实际是否支持 base64 待确认（`data` 字段为 `byte[]`，Jackson 默认按 base64 解）。
- `OSSInputStreamStore.downloadFolder` 用固定 10 线程池、60 分钟超时；超大桶可能不够。
- `MinioInputStreamStore` 把 SDK 异常吞成空 `IOException`，丢失根因。
