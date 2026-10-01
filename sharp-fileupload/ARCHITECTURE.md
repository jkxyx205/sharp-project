# sharp-fileupload 架构

## 分层

```
┌──────────────────────────────────────────────────────────┐
│  Controller 层  (client.controller)                       │
│  DocumentController  ImageController                      │
│  ↓ 注入                                                    │
├──────────────────────────────────────────────────────────┤
│  业务层  (client.support)                                  │
│  DocumentService(Impl)  →  DocumentDAO  →  sys_document  │
│  Document extends FileMeta                                │
│  ↓ 注入                                                    │
├──────────────────────────────────────────────────────────┤
│  核心抽象层  (core)                                        │
│  FileStore ── 委托 ──► InputStreamStore(接口)             │
│                         ▲                                  │
│              AbstractInputStreamStore(OkHttp 默认读取)    │
│  FileMeta / StoreResponse / FileMetaUtils / 常量          │
│  ↓ 选择实现                                                │
├──────────────────────────────────────────────────────────┤
│  存储后端实现层  (impl)                                    │
│  local.LocalInputStreamStore    (默认, JDK FS)            │
│  oos.OSSInputStreamStore        (阿里云 OSS)              │
│  oos.MinioInputStreamStore      (MinIO)                   │
│  fastdfs.FastDFSInputStreamStore(FastDFS)                 │
├──────────────────────────────────────────────────────────┤
│  插件层  (plugin.image)                                   │
│  ImageService  ImageParam  NameImageCreator               │
│  (依赖 thumbnailator；通过 InputStreamStore 读写)         │
└──────────────────────────────────────────────────────────┘
```

## 包结构

```
com.rick.fileupload
├── FileUploadApplication              # 仅用于模块独立启动/测试；依赖方不使用
├── core
│   ├── InputStreamStore               # 存储接口（核心抽象）
│   ├── AbstractInputStreamStore       # OkHttp 默认读取 + URL 拼接模板
│   ├── FileStore                      # 高层封装，@Primary Bean
│   ├── Constants                      # COMPRESS_THRESHOLD=500KB
│   ├── config
│   │   └── FileUploadAutoConfig       # @Configuration 自动装配
│   ├── support
│   │   ├── FileUploadProperties       # fileupload.tmp
│   │   ├── FileMetaUtils              # MultipartFile/File → FileMeta
│   │   ├── FileConvertUtils           # PDF→图片 (pdfbox + itext, provided)
│   │   └── FileCheckUtils             # 空占位类（待确认用途）
│   ├── model
│   │   ├── FileMeta                   # 文件元数据（可被实体继承）
│   │   └── StoreResponse              # 存储返回值（不可变）
│   └── exception
│       └── NotImageTypeException
├── impl
│   ├── local
│   │   ├── LocalInputStreamStore
│   │   └── property.LocalProperties   # fileupload.local.*
│   ├── oos
│   │   ├── OSSInputStreamStore
│   │   ├── MinioInputStreamStore
│   │   └── property.OSSProperties     # fileupload.oss.*
│   └── fastdfs
│       └── FastDFSInputStreamStore    # 构造时加载 fdfs_client.properties
├── plugin.image
│   ├── ImageService                   # 图片处理（write/cropPic/createImage）
│   ├── ImageParam                     # zimg 风格参数
│   └── NameImageCreator               # 名字头像
└── client
    ├── controller
    │   ├── DocumentController         # /documents
    │   └── ImageController            # /images
    └── support
        ├── Document                   # @Table(sys_document) extends FileMeta
        ├── DocumentDAO                # extends EntityDAOImpl<Document,Long>
        ├── DocumentService(Impl)
```

## 关键抽象：`InputStreamStore` 与后端实现

`InputStreamStore` 是唯一的存储后端抽象。`AbstractInputStreamStore` 用模板方法固定 `getURL = getServerUrl() + groupName + "/" + path`，并提供基于 OkHttp 的 `getInputStream` / `getByteArray` / `store(groupName, extension, is)`（自动生成 storeName = 雪花 id）默认实现。各后端只需重写 `store(groupName, storeName, extension, is)` / `delete` / `getServerUrl`，并视情况重写 `getInputStream`（避免 OkHttp 回环请求）。

后端选择发生在 Spring 容器装配阶段，而非运行时分支：

- `FileUploadAutoConfig#inputStreamStore` 注册 `LocalInputStreamStore` 并标 `@ConditionalOnMissingBean`。
- 应用若需要 OSS/MinIO/FastDFS，在自己的 `@Configuration` 中声明一个返回 `InputStreamStore` 的 `@Bean`（参考 `sharp-demo` 的 `SharpConfig`），即可覆盖默认。
- `FileStore` 与 `ImageService` 都通过构造注入 `InputStreamStore`，对后端透明。

`FileStore` 是面向调用方的高层门面，标 `@Primary`，使 `@Autowired` 默认拿到 `FileStore` 而非裸 `InputStreamStore`。它把 `MultipartFile`/`File` 解析成 `FileMeta`（含 `byte[] data`）再委托底层 store，并回填 `groupName`/`path`/`url`。

## 数据流

### 上传

```
MultipartFile
  → FileMetaUtils.parse  (读字节进 FileMeta.data, 探测 contentType/size)
  → FileStore.upload
  → InputStreamStore.store(groupName, extension, data 的 InputStream)
      本地: 写 {root}/{groupName}/{id}.{ext}
      OSS : ossClient.putObject(bucket, "{groupName}/{id}.{ext}", is)
      MinIO: client.putObject(bucket, "{groupName}/{id}.{ext}", is)
      FastDFS: storageClient.upload_file(groupName, bytes, ext, null)  # 服务端生成 file_id
  → StoreResponse{groupName, path, fullPath, url}
  → FileMeta.path/url 回填
```

### 文档上传（client 层）

```
DocumentController.fileUpload
  → FileMetaUtils.parse(multipartRequest)
  → DocumentService.store(fileMetaList, groupName)
      → FileStore.storeFileMeta(...)   # 存储 + 回填 path/url
      → DocumentDAO.insert(Document)    # 落 sys_document，含 groupName/path/size/contentType
```

### 读取 / 预览

```
getURL(groupName, path)              # 纯字符串拼接，不读存储
getInputStream(groupName, path)
  本地: FileInputStream({root}/{groupName}/{path})
  OSS : ossClient.getObject(...).getObjectContent()
  MinIO: client.getObject(...)
  默认: OkHttp GET getURL(...)         # 仅当后端未重写时
```

### 下载

单文件：`FileCopyUtils.copy(fileStore.getInputStream(...), response.getOutputStream())`。
多文件：每个文件先下载到 `fileupload.tmp` 下的临时子目录，再 `ZipUtils` 打包输出，最后 `deleteQuietly` 清理。

### 图片处理

`ImageService.write`：若 `FileMeta.data` 为空则 `inputStreamStore.getInputStream` 拉取；按 `ImageParam` 用 `thumbnailator` 缩放/裁剪/旋转/转格式后写流。非图片抛 `NotImageTypeException`。

## 对外依赖

| 依赖 | scope | 用途 |
|---|---|---|
| `com.rick.common:sharp-common` | compile | `IdGenerator` 雪花 id、`FileUtils`（fullName/contentType/isImageType）、`StringUtils` |
| `spring-boot-starter-web` | compile | multipart、Controller、CorsFilter |
| `commons-io` | compile | `IOUtils` / `FileUtils` 字节流拷贝 |
| `okhttp` | compile | `AbstractInputStreamStore` 默认拉流实现 |
| `net.coobird:thumbnailator` | compile | `ImageService` 图片缩放裁剪 |
| `io.minio:minio` 8.5.17 | compile | `MinioInputStreamStore`（非 provided，会传递） |
| `com.rick.db:sharp-database` | provided | client 层 `Document`/`DocumentDAO`/`@Table`/`@Transient` |
| `mysql:mysql-connector-java` 5.1.47 | provided | client 层示例数据源 |
| `com.aliyun.oss:aliyun-sdk-oss` 3.10.2 | provided | `OSSInputStreamStore`（应用自建 `OSS` Bean） |
| `org.csource:fastdfs-client-java` 1.29 | provided | `FastDFSInputStreamStore` |
| `org.apache.pdfbox:pdfbox` 2.0.12 | provided | `FileConvertUtils.pdf2Image` |
| `com.lowagie:itext` 2.0.7 | provided | `FileConvertUtils` 读取 PDF 页数 |

## 扩展点：新增一种存储后端

1. 在 `com.rick.fileupload.impl.<yourbackend>` 下新建 `XxxInputStreamStore extends AbstractInputStreamStore`，实现 `store` / `delete` / `getServerUrl`，并视情况重写 `getInputStream`。
2. （可选）在 `impl.<yourbackend>.property` 下新建 `@ConfigurationProperties(prefix="fileupload.xxx")` 配置类。
3. 在 `FileUploadAutoConfig` 的 `@EnableConfigurationProperties` 中加入新配置类（或在应用侧声明）。
4. 应用侧 `@Configuration` 中构造客户端并 `@Bean public InputStreamStore inputStreamStore(...)`，覆盖默认 `LocalInputStreamStore`。
5. 若新后端支持文件夹下载，重写 `downloadFolder` / `downloadFile`。

无需改动 `FileStore` / `DocumentService` / `ImageService` / Controller——它们只依赖 `InputStreamStore` 接口。

## 配置与启动

- 自动装配入口：`FileUploadAutoConfig`，`@EnableConfigurationProperties({FileUploadProperties, LocalProperties, OSSProperties})`。
- 注册的 Bean：`CorsFilter`（允许全跨域，`*`）、`InputStreamStore`（默认本地，`@ConditionalOnMissingBean`）、`FileStore`（`@Primary`）、`ImageService`。
- client 层 Bean（`DocumentController`/`DocumentDAO`/`DocumentServiceImpl`）**不在** `FileUploadAutoConfig` 注册，依赖方启动类需 `@Import` 或 `scanBasePackages` 包含 `com.rick.fileupload.client`（参考 `sharp-admin`/`sharp-demo` 的启动类）。
- 模块自带 `FileUploadApplication`（`@SpringBootApplication`）仅供模块独立运行/测试，生产应用不使用。
- `application.yml`（模块 src/main/resources）含示例 datasource 与 fileupload 配置，是文档示例，依赖方应使用自己的 profile yml。
