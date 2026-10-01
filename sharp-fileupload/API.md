# sharp-fileupload API

## 模块定位

文件上传 / 存储 / 预览 / 下载的 Spring Boot starter 模块。提供统一的存储抽象（`InputStreamStore`），可切换本地文件系统、阿里云 OSS、MinIO、FastDFS 四种后端；并提供基于数据库表 `sys_document` 的"文档管理"上层封装（`DocumentService` + Controller）和图片处理插件（`ImageService`）。

坐标：`com.rick.fileupload:sharp-fileupload:${sharp.version}`，父 pom 为 `sharp-dependencies`。被 `sharp-admin`、`sharp-demo`、`sharp-formflow`、`sharp-generator` 等模块引用。

## 核心入口一览

| 类型 | 全名 | 职责 |
|---|---|---|
| 接口 | `com.rick.fileupload.core.InputStreamStore` | 存储后端抽象，所有后端实现该接口 |
| 抽象类 | `com.rick.fileupload.core.AbstractInputStreamStore` | 提供基于 OkHttp 的默认读取实现 + URL 拼接 |
| 类 | `com.rick.fileupload.core.FileStore` | 高层封装，支持 `MultipartFile` / `byte[]` / `File`，返回 `FileMeta` |
| 类 | `com.rick.fileupload.plugin.image.ImageService` | 图片裁剪 / 压缩 / 名字头像生成 |
| 接口 | `com.rick.fileupload.client.support.DocumentService` | 文档 CRUD + 下载 + 预览，落库 `sys_document` |
| 类 | `com.rick.fileupload.client.support.Document` | 文档实体，继承 `FileMeta` |
| 配置 | `com.rick.fileupload.core.config.FileUploadAutoConfig` | 自动装配入口 |
| 模型 | `com.rick.fileupload.core.model.FileMeta` / `StoreResponse` | 文件元数据 / 存储返回值 |

## 存储抽象

### `InputStreamStore`（核心接口）

```java
StoreResponse store(String groupName, String storeName, String extension, InputStream is) throws IOException;
StoreResponse store(String groupName, String extension, InputStream is) throws IOException;  // storeName 自动用 IdGenerator 雪花 id
void        delete(String groupName, String path) throws IOException;
String      getURL(String groupName, String path);
InputStream getInputStream(String groupName, String path) throws IOException;
byte[]      getByteArray(String groupName, String path) throws IOException;
default void downloadFolder(String path, String localDir) throws InterruptedException {}  // 仅 OSS 实现
default void downloadFile(String path, String localPath) {}  // 仅 OSS 实现
```

参数语义（全后端通用）：
- `groupName`：顶层分组（类似一级目录 / bucket 前缀），如 `"upload"`、`"ckeditor"`、`"header"`。
- `storeName`：磁盘存储文件名（不含扩展名），如雪花 id `"893483237785997312"`。FastDFS 后端会忽略此参数（由 FastDFS 服务端生成文件名，会打 warn 日志）。
- `extension`：扩展名（不带点），如 `"png"`、`"docx"`；可为 `null`（FastDFS 调用 `upload_file` 时传 null）。
- `path`：存储后返回的相对路径，即 `storeName + "." + extension`（FastDFS 后端为服务端返回的 file_id）。

返回 `StoreResponse`（不可变，`@Value`）字段：
- `groupName`：入参回填。
- `path`：相对路径（用于后续 get/delete 的 `path` 参数）。
- `fullPath`：`groupName + "/" + path`；本地后端为磁盘绝对路径。
- `url`：可直访问的 HTTP URL，由 `getServerUrl() + groupName + "/" + path` 拼接。

URL 拼接规则（`AbstractInputStreamStore#getURL`）：
- 本地：`LocalProperties.serverUrl + groupName + "/" + path`。
- OSS：`https://{bucket}.{endpoint}/{groupName}/{path}`。
- MinIO：`{endpoint}/{bucket}/{groupName}/{path}`。
- FastDFS：`http://{trackerHost}:{httpPort}/{groupName}/{path}`。

`AbstractInputStreamStore` 的 `getInputStream` / `getByteArray` 默认用 OkHttp 对 `getURL(...)` 发 GET 拉流。本地 / OSS / MinIO 后端各自重写了 `getInputStream` 以走原生客户端或文件系统，不走 OkHttp。

### `FileStore`（高层封装，`@Primary` Bean）

构造注入一个 `InputStreamStore`，把存储操作包装成面向 `MultipartFile` / `File` / `byte[]` 的批量上传。`FileUploadAutoConfig` 以 `@Primary` 注册它，注入时默认拿到 `FileStore` 而非底层 `InputStreamStore`。

关键方法：

```java
// 批量上传 MultipartFile，逐个解析元数据并存储
List<? extends FileMeta> upload(List<MultipartFile> multipartFileList, String groupName) throws IOException;

// 批量上传 java.io.File
List<? extends FileMeta> storeFiles(List<File> fileList, String groupName) throws IOException;

// 批量上传已含 byte[] data 的 FileMeta 列表（path/url/groupName 会被回填）
List<? extends FileMeta> storeFileMeta(List<? extends FileMeta> fileMetaList, String groupName) throws IOException;
```

`store(...)` / `delete(...)` / `getURL(...)` / `getInputStream(...)` 等同接口方法，直接转发给底层 `InputStreamStore`。

## 存储后端实现

| 后端 | 类 | 客户端 | 是否默认 |
|---|---|---|---|
| 本地 FS | `com.rick.fileupload.impl.local.LocalInputStreamStore` | JDK `FileInputStream` / `FileOutputStream` | 是（`@ConditionalOnMissingBean`） |
| 阿里云 OSS | `com.rick.fileupload.impl.oos.OSSInputStreamStore` | `com.aliyun.oss.OSS`（aliyun-sdk-oss，provided） | 否 |
| MinIO | `com.rick.fileupload.impl.oos.MinioInputStreamStore` | `io.minio.MinioClient`（minio，非 provided） | 否 |
| FastDFS | `com.rick.fileupload.impl.fastdfs.FastDFSInputStreamStore` | `org.csource.fastdfs`（fastdfs-client-java，provided） | 否 |

OSS / MinIO / FastDFS 后端**不会**被模块自动注册为 Bean。使用者需在自己的 `@Configuration` 里手动构造对应客户端并 `return new XxxInputStreamStore(client, props)`，从而覆盖默认的 `LocalInputStreamStore`。参考 `sharp-demo` 的 `com.rick.demo.config.SharpConfig`（三个 `@Bean` 方法，按需取消注释）。构造签名：

```java
new OSSInputStreamStore(OSS ossClient, OSSProperties ossProperties);   // OSSClientBuilder().build(endpoint, ak, sk)
new MinioInputStreamStore(MinioClient client, OSSProperties ossProperties); // builder().endpoint(...).credentials(ak,sk).build()
new FastDFSInputStreamStore(String propertyFilePath);  // 如 "fdfs_client.properties"，构造时 ClientGlobal.init
```

本地后端存储路径：`{LocalProperties.rootPath}/{groupName}/{storeName}.{extension}`；目录不存在会 `mkdirs()`。

OSS 后端 `downloadFolder(path, localDir)`：分页列举 bucket 内 prefix=`path` 的对象，10 线程并发下载到本地，保留相对目录结构；`downloadFile(path, localPath)`：单文件下载到本地。

## 文件元数据

### `FileMeta`（`com.rick.fileupload.core.model.FileMeta`）

字段：`name`（原始名，不含扩展）、`extension`（不含点）、`contentType`、`size`（Long，未设时若 `data!=null` 则取 `data.length`）、`groupName`、`path`、`data`（byte[]，`@Transient @JsonIgnore`，不入库不序列化）、`url`（`@Transient`）。

派生方法：
- `getFullName()` → `name + "." + extension`。
- `getFullPath()` → `groupName + "/" + path`。
- `setFullName(String)` → 拆分为 name/extension。

`FileMetaUtils`（`com.rick.fileupload.core.support.FileMetaUtils`，final 工具类）：
- `parse(MultipartFile)` → 读取字节流填入 `data`，size=文件大小，contentType=请求 contentType，fullName=originalFilename。
- `parse(File)` → 读字节数组，contentType 通过 `file.toURI().toURL().openConnection().getContentType()` 探测。
- `parse(List<MultipartFile>)` / `parse(MultipartHttpServletRequest, String formFileName)` → 批量解析。

### `StoreResponse`（`com.rick.fileupload.core.model.StoreResponse`，`@Value` 不可变）

字段：`groupName`、`path`、`fullPath`、`url`。

## 图片插件

### `ImageService`（`com.rick.fileupload.plugin.image.ImageService`）

构造注入 `InputStreamStore`。`FileUploadAutoConfig` 自动注册为 Bean。

关键方法：

```java
// 按 imageParam 处理图片并写入 os；无参数时按阈值压缩
void write(FileMeta fileMeta, ImageParam imageParam, OutputStream os) throws IOException;

// 中心裁剪到指定比例
FileMeta cropPic(FileMeta fileMeta, int aspectRatioW, int aspectRatioH) throws IOException;
// 自定义坐标裁剪
FileMeta cropPic(FileMeta fileMeta, int x, int y, int w, int h, int aspectRatioW, int aspectRatioH) throws IOException;
// 直接传 ImageParam 裁剪
FileMeta cropPic(FileMeta fileMeta, ImageParam imageParam) throws IOException;
FileMeta cropPic(FileMeta fileMeta, ImageParam imageParam, OutputStream os) throws IOException;

// 名字头像：text=名字（中文取后两字、英文取首字母大写），groupName 分组，storeName 文件名
String createImage(String text, String groupName) throws IOException;            // storeName=雪花id
String createImage(String text, String groupName, String storeName) throws IOException; // 返回 url
```

行为：
- `write`：若 `fileMeta.data` 为 null，先调 `inputStreamStore.getInputStream` 拉取；非图片类型且 `cropPic(...)` 调用会抛 `com.rick.fileupload.core.exception.NotImageTypeException`（RuntimeException，message "文件不是图片类型"）。
- 压缩阈值：`Constants.COMPRESS_THRESHOLD` = `500 * 1024`（500KB）。超阈值按 `threshold/size` 比例降质量。
- 输出格式可由 `ImageParam.f` 指定，否则沿用原扩展名。

### `ImageParam`（`com.rick.fileupload.plugin.image.ImageParam`，`@Data`）

参数语义参考 zimg（`http://zimg.buaa.us`）：`p`（resize 类型：0=拉伸到 w/h、2=放大2倍、3=按百分比裁剪、null=普通裁剪并按阈值压缩）、`w`/`h`（宽高像素）、`r`（旋转角度）、`x`/`y`（裁剪起点）、`f`（输出格式）、`q`（清晰度 0-100）、`rw`/`rh`（目标比例，如 1:1 则 rw=1,rh=1）、`position`（裁剪锚点，默认 CENTER，有 x/y 时自动构造 `Coordinate`）。

`isEmpty()`：全部参数为 null。`isSource()`：仅 `p` 有值（用于获取原图）。

### `NameImageCreator`（静态工具）

`generateImg(String name)`：生成 100×100、随机背景色、圆角的 PNG 名字头像字节数组。中文取最后两字、英文取首字母大写。

## 文档管理层（client 层）

仅当应用同时引入 `sharp-database`、`mysql-connector-java` 并声明 `DocumentDAO`/`DocumentServiceImpl`（通过 `@SpringBootApplication(scanBasePackages ...)` 或 `@Import`）时才生效。`sharp-admin`、`sharp-demo` 在启动类上 import 了 `DocumentController`、`DocumentDAO`、`DocumentServiceImpl`。

### `Document`（`com.rick.fileupload.client.support.Document`，`@Table("sys_document")`）

继承 `FileMeta`，新增：`id`（Long 主键，雪花 id，JSON 序列化为字符串）、`createBy`、`createTime`、`updateBy`、`updateTime`、`deleted`（逻辑删除列）。表结构：

```sql
CREATE TABLE sys_document (
  id bigint PRIMARY KEY,
  name varchar(255) NOT NULL,
  extension varchar(16),
  content_type varchar(128),
  size int,
  group_name varchar(255) NOT NULL,
  path varchar(255) NOT NULL,
  create_by bigint, create_time datetime NOT NULL,
  update_by bigint, update_time datetime,
  is_deleted bit(1)
);
```

### `DocumentDAO`（`com.rick.fileupload.client.support.DocumentDAO`）

`@Repository` 继承 `com.rick.db.plugin.dao.core.EntityDAOImpl<Document, Long>`，提供 `insert` / `selectById` / `updateById` / `deleteByIds` 等。

### `DocumentService` / `DocumentServiceImpl`

```java
Document store(FileMeta fileMeta, String groupName) throws IOException;        // 存储 + 落库，返回含 id 的 Document
List<Document> store(List<FileMeta> fileMetaList, String groupName) throws IOException;
void download(HttpServletRequest req, HttpServletResponse resp, long... ids) throws IOException; // 单文件直发；多文件打包 zip
void delete(Long... ids);                // 先删存储再删库记录（逻辑删除）
void rename(long id, String name);
Document findById(long id);               // 返回的 url 已通过 getURL(id) 回填
void preview(long id, ImageParam imageParam, OutputStream os) throws IOException; // 图片走 ImageService.write，其余直接写字节
String getURL(long id);
```

批量下载临时目录：`FileUploadProperties.tmp`（prefix `fileupload.tmp`）。下载完成后 `FileUtils.deleteQuietly` 清理临时目录。

### Controller

`com.rick.fileupload.client.controller.DocumentController`（`@RequestMapping("/documents")`）：
- `GET /documents/{id}` → 文档详情。
- `POST /documents/upload`（multipart，表单字段名默认 `file`，可选 `name` 覆盖；query `groupName` 默认 `"upload"`）。
- `POST /documents/upload2`（`@RequestParam("file") List<MultipartFile>`）。
- `POST /documents/upload3`（`@RequestBody List<FileMeta>`，需自行填 `data`）。
- `GET /documents/download/{id}` 单文件下载；`GET /documents/download?id=&id=` 批量 zip 下载。
- `GET /documents/preview/{id}` 302 重定向到 `getURL(id)`（适合 nginx 直发）。
- `GET /documents/preview2/{id}` 或 `/preview2/{id}/{fileName}.{docx|xlsx|pptx}` 流式预览（图片可带 `ImageParam`）。
- `PUT /documents/{id}/rename?name=`。
- `DELETE /documents/{id}`。

`com.rick.fileupload.client.controller.ImageController`（`@RequestMapping("/images")`）：
- `GET /images/{id}?p=&w=&h=&r=&x=&y=&f=&q=&rw=&rh=` → 按 `ImageParam` 输出图片。
- `POST /images/cropPic`（`file` + x/y/w/h/aspectRatioW/aspectRatioH）。
- `POST /images/cropPic2`（`file` + aspectRatioW/aspectRatioH，中心裁剪）。
- `POST /images/create?text=` → 名字头像，存到 `"header"` 组，返回 url。

## 配置项

### `fileupload`（`FileUploadProperties`）

| 属性 | 说明 |
|---|---|
| `fileupload.tmp` | 批量下载 / 临时目录路径。例 `/Users/rick/Space/tmp/fileupload/tmp`。 |

### `fileupload.local`（`LocalProperties`，默认后端使用）

| 属性 | 说明 |
|---|---|
| `fileupload.local.server-url` | 文件访问 URL 前缀（需指向 `root-path` 的静态服务，如 nginx / `http-server`）。例 `http://localhost:7892/`。 |
| `fileupload.local.root-path` | 本地存储根目录。例 `/Users/rick/Space/tmp/fileupload`。实际存到 `{root-path}/{groupName}/`。 |

### `fileupload.oss`（`OSSProperties`，OSS / MinIO 后端共用）

| 属性 | 说明 |
|---|---|
| `fileupload.oss.endpoint` | OSS：如 `oss-cn-beijing.aliyuncs.com`；MinIO：如 `http://localhost:9000`。 |
| `fileupload.oss.access-key-id` | AK。 |
| `fileupload.oss.access-key-secret` | SK。 |
| `fileupload.oss.bucket-name` | bucket 名。 |

### Spring multipart 限制

模块自带 `application.yml` 示例设 `spring.servlet.multipart.max-file-size: 50MB`、`max-request-size: 50MB`。`sharp-admin` 实际配 `max-file-size: 5MB`。后端单文件大小上限以此为准；超限抛 `MaxUploadSizeExceededException`。

### FastDFS 配置

`src/main/resources/fdfs_client.properties`：`fastdfs.tracker_servers`、`fastdfs.http_tracker_http_port`。构造 `FastDFSInputStreamStore("fdfs_client.properties")` 时加载并 `ClientGlobal.init`。

## 使用示例

### 1. 默认本地存储（开箱即用）

`application.yml`：
```yaml
fileupload:
  tmp: /data/tmp/fileupload/tmp
  local:
    server-url: https://cdn.example.com/   # 需能映射到 root-path
    root-path: /data/fileupload
```

依赖方启动类需扫描 client 包：
```java
@SpringBootApplication(scanBasePackages = {"com.rick.admin", "com.rick.fileupload.client"})
@Import({DocumentController.class, DocumentDAO.class, DocumentServiceImpl.class})
public class App { ... }
```

### 2. 上传一个 MultipartFile

```java
@Autowired
private FileStore fileStore; // @Primary，默认拿到 FileStore

public List<? extends FileMeta> upload(List<MultipartFile> files) throws IOException {
    return fileStore.upload(files, "upload");   // groupName=upload
}
```

或直接用底层 store（自动生成 storeName）：
```java
StoreResponse resp = fileStore.store("upload", "jpg", multipartFile.getInputStream());
// resp.getPath() / resp.getUrl()
```

### 3. 读取 / 下载

```java
// 拉取文件字节
byte[] bytes = fileStore.getByteArray("upload", "893483237785997312.jpeg");

// 取输入流
try (InputStream is = fileStore.getInputStream("upload", "893483237785997312.jpeg")) {
    // 复制到 OutputStream
}

// 取访问 URL（不读内容）
String url = fileStore.getURL("upload", "893483237785997312.jpeg");
```

### 4. 删除

```java
fileStore.delete("upload", "893483237785997312.jpeg"); // throws IOException
```

### 5. 切换到 MinIO 后端

```java
@Configuration
@RequiredArgsConstructor
public class StorageConfig {
    private final OSSProperties props;   // fileupload.oss.*
    @Bean
    public InputStreamStore inputStreamStore() {
        MinioClient client = MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKeyId(), props.getAccessKeySecret())
                .build();
        return new MinioInputStreamStore(client, props);  // 覆盖默认 LocalInputStreamStore
    }
}
```

### 6. 图片裁剪 / 名字头像

```java
@Autowired
private ImageService imageService;

// 按比例裁剪并存储
FileMeta meta = FileMetaUtils.parse(multipartFile);
ImageParam param = new ImageParam();
param.setRw(1); param.setRh(1);   // 1:1
imageService.cropPic(meta, param); // meta.data 被替换为裁剪后字节
fileStore.storeFileMeta(List.of(meta), "avatar");

// 名字头像
String url = imageService.createImage("张三", "header", "user123");
```

## 异常

- `com.rick.fileupload.core.exception.NotImageTypeException`（RuntimeException）：对非图片文件调用 `ImageService.cropPic` / `write` 时抛。
- `java.io.IOException`：store / delete / getInputStream 链路抛，OSS/MinIO 实现将 SDK 的 `Exception` 包装为 `IOException`（注意：`MinioInputStreamStore.store` 中 `catch (Exception e) { throw new IOException(); }` 丢失了原异常信息——待确认是否为有意）。
- `MaxUploadSizeExceededException`：超出 multipart 限制时由 Spring 抛。

## 依赖说明

模块 pom 中 `sharp-database`、`mysql-connector-java`、`fastdfs-client-java`、`aliyun-sdk-oss`、`pdfbox`、`itext` 均为 `provided`：仅文档管理 / OSS / FastDFS / PDF 转换路径需要，不强制传递。`minio` 为非 provided（compile）。`thumbnailator` 提供图片缩放，`okhttp` 提供默认拉流实现，`commons-io` 提供字节流拷贝。
