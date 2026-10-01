# sharp-fileupload API

## 模块定位

`sharp-fileupload` 是 sharp 体系的"文件上传 / 存储 / 图片处理"模块。它提供：

- 一套存储抽象（`InputStreamStore`），屏蔽本地磁盘、阿里云 OSS、MinIO、FastDFS 四种后端。
- 上层服务 `FileStore`：把 `MultipartFile` / `File` / `FileMeta` 落到后端，返回带访问 URL 的元数据。
- 图片处理 `ImageService`：基于 thumbnailator 的裁剪/压缩/旋转/格式转换，以及"姓名头像"生成。
- 一个开箱即用的 HTTP 客户端层（`DocumentController` / `ImageController`），含上传、下载、预览、删除、重命名。
- 一个可选的数据库映射 `Document`（继承 `FileMeta`，落 `sys_document` 表）。

依赖方典型用法（见 `sharp-admin` / `sharp-demo`）：引入本模块 → 在主应用 `@SpringBootApplication` 上 import `DocumentController` / `DocumentServiceImpl` / `DocumentDAO` → 通过 `DocumentService` 或 `FileStore` 完成上传/下载，无需关心底层后端。

坐标：`com.rick.fileupload:sharp-fileupload:${sharp.version}`，父 POM `com.rick:sharp-dependencies:3.0-SNAPSHOT`。

---

## 核心 public 入口

### 1. `com.rick.fileupload.core.InputStreamStore`（接口）

存储抽象的根。所有存储后端实现它；`FileStore` 也实现它并作为委托门面。

```java
// 上传：自动生成存储名（IdGenerator 序列号）
StoreResponse store(String groupName, String extension, InputStream is) throws IOException;

// 上传：指定存储名（不含扩展名）+ 扩展名
StoreResponse store(String groupName, String storeName, String extension, InputStream is) throws IOException;

// 删除
void delete(String groupName, String path) throws IOException;

// 拼访问 URL = getServerUrl() + groupName + "/" + path
String getURL(String groupName, String path);

// 取文件流（默认实现走 OkHttp GET getURL；本地实现走 FileInputStream）
InputStream getInputStream(String groupName, String path) throws IOException;

// 取字节数据
byte[] getByteArray(String groupName, String path) throws IOException;

// OSS：下载某前缀下所有文件到本地目录（仅 OSSInputStreamStore 真正实现）
default void downloadFolder(String path, String localDir) throws InterruptedException {}

// OSS：下载单个文件到本地（仅 OSSInputStreamStore 真正实现）
default void downloadFile(String path, String localPath) {}
```

参数语义：
- `groupName`：分组/桶内子目录（如 `upload`、`header`、`group`）。本地实现会拼到 `rootPath/groupName`；OSS/MinIO 实现作为 object key 的前缀。
- `storeName`：存储文件名（**不含扩展名**）。本地实现会拼成 `storeName.extension`；FastDFS 忽略此参数（由服务端生成），仅打 warn。
- `extension`：扩展名，不带点（如 `jpg`、`png`）。
- `path`：存储返回的相对路径（即 `StoreResponse.path`），后续 delete/getURL/getInputStream 都用它。

### 2. `com.rick.fileupload.core.AbstractInputStreamStore`（抽象类）

实现 `InputStreamStore` 的公共部分：
- `store(group, ext, is)` 默认委托到 `store(group, IdGenerator.getSequenceId(), ext, is)`。
- `getURL(g, p)` = `getServerUrl() + g + "/" + p`。
- `getInputStream` 默认用 OkHttp GET `getURL(...)` 拉流（适合走 HTTP 暴露的后端）。
- `getByteArray` = `IOUtils.toByteArray(getInputStream(...))`。
- 抽象钩子 `protected abstract String getServerUrl()`：子类返回根访问 URL（带尾斜杠）。

子类只需实现 `store(...storeName...)`、`delete`、`getServerUrl()`，可选覆盖 `getInputStream`。

### 3. `com.rick.fileupload.core.FileStore`（门面，`@Primary` Bean）

面向业务的高层入口。构造注入一个 `InputStreamStore`，自身也实现 `InputStreamStore`（逐方法委托）。

```java
// 批量上传 MultipartFile，返回 FileMeta（含 groupName/path/url）
List<? extends FileMeta> upload(List<MultipartFile> multipartFileList, String groupName) throws IOException;

// 批量上传 java.io.File
List<? extends FileMeta> storeFiles(List<File> fileList, String groupName) throws IOException;

// 批量上传已 parse 出的 FileMeta（要求 data 字段已填充）
List<? extends FileMeta> storeFileMeta(List<? extends FileMeta> fileMetaList, String groupName) throws IOException;

// 以下全部委托到底层 InputStreamStore：
StoreResponse store(...); void delete(...); String getURL(...);
InputStream getInputStream(...); byte[] getByteArray(...);
void downloadFolder(...); void downloadFile(...);
```

行为：每个文件调 `FileMetaUtils.parse` 解析元数据 → 调 `inputStreamStore.store(group, extension, inputStream)` → 回填 `groupName/path/url` 到 FileMeta。`storeFileMeta` 走的是 `ByteArrayInputStream(fileMeta.getData())`。

### 4. `com.rick.fileupload.core.model.FileMeta`（元数据）

```java
String name;          // 不含扩展名的原文件名
String extension;     // 扩展名（不带点）
String contentType;   // MIME
Long size;            // 字节数；为空但 data 非空时取 data.length
String groupName;     // 存储分组
String path;          // 存储相对路径（store 返回的）
byte[] data;          // 文件二进制；@Transient @JsonIgnore，序列化不输出
String url;           // 访问 URL；@Transient

String getFullName(); // name + "." + extension
String getFullPath(); // groupName + "/" + path
void setFullName(String); // 同时拆分 name / extension
```

`com.rick.fileupload.client.support.Document` 继承自 `FileMeta`，追加 `id`、`createBy/Time`、`updateBy/Time`、`deleted`，映射表 `sys_document`，配合 `DocumentDAO`（`EntityDAOImpl<Document, Long>`）持久化。

### 5. `com.rick.fileupload.core.model.StoreResponse`（不可变返回值）

`@Value`：`groupName`、`path`（存储相对名，如 `123456.jpg`）、`fullPath`（`groupName/path` 或本地绝对路径）、`url`（完整访问 URL）。

### 6. `com.rick.fileupload.plugin.image.ImageService`（图片处理 Bean）

```java
// 按 imageParam 处理图片并写出到 os；fileMeta.data 为空时从存储后端拉
void write(FileMeta fileMeta, ImageParam imageParam, OutputStream os) throws IOException;

// 中心裁剪到指定宽高比
FileMeta cropPic(FileMeta fileMeta, int aspectRatioW, int aspectRatioH) throws IOException;
// 自定义坐标裁剪
FileMeta cropPic(FileMeta fileMeta, int x, int y, int w, int h, int aspectRatioW, int aspectRatioH) throws IOException;
FileMeta cropPic(FileMeta fileMeta, ImageParam imageParam) throws IOException;          // 写回 data
FileMeta cropPic(FileMeta fileMeta, ImageParam imageParam, OutputStream os) throws IOException; // 写出，不改 data

// 姓名头像（中文取末两字，英文取首字母大写），生成 100x100 PNG 存到 groupName
String createImage(String text, String groupName) throws IOException;
String createImage(String text, String groupName, String storeName) throws IOException; // 指定 storeName
```

非图片类型抛 `com.rick.fileupload.core.exception.NotImageTypeException`（RuntimeException，message="文件不是图片类型"）。

压缩阈值 `Constants.COMPRESS_THRESHOLD = 500 * 1024`（约 500KB）。超过阈值且未显式指定质量时自动按比例降质量。

### 7. `com.rick.fileupload.plugin.image.ImageParam`（图片参数，zimg 风格）

请求参数绑定对象，字段均可空：

| 字段 | 含义 |
|---|---|
| `p` | 处理类型：`0`=按 w/h 强制拉伸；`2`=放大 2 倍；`3`=按百分比（w/h 为 0-100）裁剪；空=普通裁剪 |
| `w` / `h` | 目标宽/高（像素；`p=3` 时为百分比） |
| `rw` / `rh` | 宽高比，二者非空时按比例裁剪（裁出最大可用比例矩形） |
| `x` / `y` | 裁剪左上角坐标 |
| `r` | 旋转角度 |
| `q` | 质量 0-100，映射 `outputQuality(q/100f)` |
| `f` | 输出格式（如 `png`/`jpg`），空则沿用原图格式 |
| `position` | 裁剪定位；空且 x/y 非空时自动构造 `Coordinate(x,y)` |

`isEmpty()` / `isSource()`（仅 p 有值，其余全空）。

### 8. 客户端 Controller 层

`com.rick.fileupload.client.controller.DocumentController`（`@RestController`，`/documents`）：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/{id}` | 文件详情（Document） |
| POST | `/upload` | `MultipartHttpServletRequest` 批量上传，表单字段名默认 `file`，参数 `groupName` 默认 `upload` |
| POST | `/upload2` | `@RequestParam("file") List<MultipartFile>` |
| POST | `/upload3` | `@RequestBody List<FileMeta>`（需带 data） |
| GET | `/download/{id}` | 单文件下载 |
| GET | `/download?id=..&id=..` | 批量下载（打包 zip） |
| GET | `/preview/{id}` | 302 重定向到 `getURL(id)`（建议前置 nginx 直出） |
| GET | `/preview2/{id}` 或 `/preview2/{id}/{fileName}.{docx|xlsx|pptx}` | 流式预览；图片走 ImageService，其它直接吐字节 |
| PUT | `/{id}/rename?name=` | 重命名（仅改 DB name） |
| DELETE | `/{id}` | 删存储 + 删 DB |

`com.rick.fileupload.client.controller.ImageController`（`/images`）：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/{id}?rw=&rh=&p=&w=&h=&r=&q=&f=&x=&y=` | 按 ImageParam 实时处理图片预览 |
| POST | `/cropPic` | 手动裁剪（上传 file + x/y/w/h/比例） |
| POST | `/cropPic2` | 按比例自动中心裁剪 |
| POST | `/create?text=` | 生成姓名头像，返回 URL |

### 9. `com.rick.fileupload.client.support.DocumentService`（接口）/ `DocumentServiceImpl`

业务服务，组合 `FileStore` + `DocumentDAO` + `ImageService` + `FileUploadProperties`。关键方法：

```java
Document store(FileMeta fileMeta, String groupName) throws IOException;       // 落盘 + insert
List<Document> store(List<FileMeta> fileMetaList, String groupName) throws IOException; // 批量 + insertOrUpdate
void download(HttpServletRequest, HttpServletResponse, long... ids) throws IOException; // 单文件直出 / 多文件 zip
void delete(Long... ids);     // 先删存储，再删 DB
void rename(long id, String name);
Document findById(long id);   // 同时回填 url
void preview(long id, ImageParam imageParam, OutputStream os) throws IOException; // 图片走 ImageService，其它吐原字节
String getURL(long id);
```

批量下载临时目录取自 `fileupload.tmp`，下载完 `FileUtils.deleteQuietly(home)`。

### 10. 工具类

- `com.rick.fileupload.core.support.FileMetaUtils`（final）：`parse(MultipartFile)` / `parse(File)` / `parse(List<MultipartFile>)` / `parse(MultipartHttpServletRequest, formFieldName)` —— 解析出 FileMeta（含 data 字节）。
- `com.rick.fileupload.core.support.FileConvertUtils`（`@UtilityClass`，依赖 pdfbox + itext，二者均为 `provided`）：`pdf2Image(byte[] data, int dpi)` 返回每页 PNG 字节列表；`pdf2Image(byte[], OutputStream, int dpi)` 把所有页纵向拼成一张 PNG。
- `com.rick.fileupload.core.Constants`：`COMPRESS_THRESHOLD`（图片自动压缩阈值，约 500KB）。

---

## 存储后端实现

### 本地：`com.rick.fileupload.impl.local.LocalInputStreamStore`

- `store` → 落盘到 `rootPath/groupName/storeName.extension`，目录不存在自动 `mkdirs`。
- `getInputStream` → `FileInputStream(rootPath/groupName/path)`（覆盖了 OkHttp 默认实现）。
- `delete` → `FileUtils.forceDelete`。
- `getServerUrl()` → `LocalProperties.serverUrl`。

配置类 `com.rick.fileupload.impl.local.property.LocalProperties`（`@ConfigurationProperties("fileupload.local")`）：

| 属性 | 含义 |
|---|---|
| `serverUrl` | 访问 URL 前缀（带尾斜杠），需由 nginx/静态服务映射到 `rootPath` |
| `rootPath` | 本地存储根目录绝对路径 |

### 阿里云 OSS：`com.rick.fileupload.impl.oos.OSSInputStreamStore`

- 构造注入 `com.aliyun.oss.OSS` + `OSSProperties`。
- `store` → `ossClient.putObject(bucket, groupName/path, is)`。
- `delete` → `deleteObject`。
- `getInputStream` → `ossClient.getObject(...).getObjectContent()`（覆盖默认 OkHttp）。
- `getServerUrl()` = `https://{bucket}.{endpoint}/`。
- `downloadFolder(path, localDir)`：分页列举（maxKeys 1000）+ 10 线程池并发下载，`awaitTermination` 最多 60 分钟。
- `downloadFile(path, localPath)`：单文件下载到本地。

### MinIO：`com.rick.fileupload.impl.oos.MinioInputStreamStore`

- 构造注入 `io.minio.MinioClient` + `OSSProperties`（共用 OSS 配置）。
- `store` → `putObject`，`stream(is, -1, 10485760)`（10MB part）。
- `delete` → `removeObject`。
- `getInputStream` → `getObject`。
- `getServerUrl()` = `{endpoint}/{bucket}/`。

> 注意：MinIO 实现的异常被吞为 `new IOException()`（无 message），排错需看堆栈。

`com.rick.fileupload.impl.oos.property.OSSProperties`（`@ConfigurationProperties("fileupload.oss")`）：`endpoint`、`accessKeyId`、`accessKeySecret`、`bucketName`。

### FastDFS：`com.rick.fileupload.impl.fastdfs.FastDFSInputStreamStore`

- 构造接收 `fdfs_client.properties` classpath 路径，`ClientGlobal.initByProperties` 初始化。
- `store` 忽略 `storeName`（FastDFS 服务端生成），打 warn。
- `delete` → `storageClient.delete_file(groupName, path)`。
- `getServerUrl()` = `http://{trackerHost}:{trackerHttpPort}/`。

> FastDFS 客户端 `org.csource:fastdfs-client-java` 为 `provided` 依赖，使用方需自行引入。

---

## 配置项

`application.yml` 全集（含示例）：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB        # 单文件上限
      max-request-size: 50MB     # 单请求上限
fileupload:
  tmp: /path/to/tmp              # 批量下载 zip 临时目录（DocumentServiceImpl 用）
  local:
    server-url: http://localhost:7892/   # 必须带尾斜杠；映射到 root-path
    root-path: /path/to/fileupload       # 本地存储根目录
  oss:
    endpoint: oss-cn-beijing.aliyuncs.com
    accessKeyId: xxx
    accessKeySecret: xxx
    bucketName: sharp-fileupload
# FastDFS 单独用 classpath:fdfs_client.properties
```

`FileUploadAutoConfig`（`com.rick.fileupload.core.config`）默认装配：
- `CorsFilter`（全放开）。
- `InputStreamStore`：`@ConditionalOnMissingBean` → 默认 `LocalInputStreamStore(localProperties)`。**业务方覆盖此 Bean 即可切换到 OSS/MinIO/FastDFS**（见 sharp-demo `SharpConfig`）。
- `FileStore`（`@Primary`）。
- `ImageService`。

> 待确认：`@EnableConfigurationProperties` 仅注册 `FileUploadProperties` / `LocalProperties` / `OSSProperties`；切换 MinIO/OSS 时 `OSSClient` / `MinioClient` Bean 由业务方自己在 `@Configuration` 里 new（见 `sharp-demo/config/SharpConfig`）。

---

## 使用示例

### 切换存储后端（OSS）

```java
@Configuration
@RequiredArgsConstructor
public class StorageConfig {
    private final OSSProperties ossProperties;
    @Bean
    public InputStreamStore inputStreamStore() {
        OSS ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
        return new OSSInputStreamStore(ossClient, ossProperties);
    }
}
```

注册此 Bean 后，`FileUploadAutoConfig` 的默认本地实现因 `@ConditionalOnMissingBean` 不再生效。

### 上传一个 MultipartFile（直接用 FileStore）

```java
@Autowired FileStore fileStore;

List<FileMeta> metas = fileStore.upload(
        Collections.singletonList(multipartFile), "upload");
String url = metas.get(0).getUrl();   // 访问 URL
String path = metas.get(0).getPath(); // 后续 delete 用
```

### 通过 DocumentService（含 DB 持久化）

```java
@Autowired DocumentService documentService;

FileMeta meta = FileMetaUtils.parse(multipartFile);
Document doc = documentService.store(meta, "upload");
Long id = doc.getId();                 // 落 sys_document
```

### 读取 / 下载 / 删除

```java
// 取流
InputStream is = fileStore.getInputStream(groupName, path);
// 取字节
byte[] bytes = fileStore.getByteArray(groupName, path);
// 删除
fileStore.delete(groupName, path);
```

### 图片实时处理（HTTP）

```
GET /images/{id}?rw=1&rh=1&p=0&r=30&w=500
```

返回处理后的图片字节（写进 response OutputStream）。

### 姓名头像

```java
String url = imageService.createImage("张三", "header");
// 或指定文件名：imageService.createImage("张三", "header", userId);
```

---

## 限制与注意

- `max-file-size` / `max-request-size` 默认 50MB（`application.yml` 示例），业务方按需覆盖。
- 文件名编码依赖 `MultipartFile.getOriginalFilename`，无额外 sanitize；本地存储名实际是序列号，不直接落原名，规避了路径穿越。`Document.name` 仅存 DB 用于展示。
- FastDFS 后端忽略自定义 `storeName`。
- MinIO 后端异常被吞为空 `IOException`。
- `downloadFolder` 仅 `OSSInputStreamStore` 实现，本地/MinIO/FastDFS 调用为空操作。
