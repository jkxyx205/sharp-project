# sharp-common API

> 一句话定位：sharp 项目的公共基础库，提供统一返回结果、业务异常、JSON/时间/反射等工具类、Spring MVC 转换器与 Jackson 序列化扩展、Bean Validation 注解与雪花 ID 生成器。

模块坐标：`com.rick.common:sharp-common`，版本 `${sharp.version}`（当前 `3.0-SNAPSHOT`），父 POM `com.rick:sharp-dependencies`。

## 一、核心 public 入口

### 1. 统一返回结果

#### `com.rick.common.http.model.Result<T>`（`@Data @AllArgsConstructor`）
统一 API 响应体，字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `success` | `boolean` | 是否成功 |
| `code` | `int` | 业务码 |
| `message` | `String` | 提示信息 |
| `data` | `T` | 业务数据，`@JsonInclude(NON_NULL)` 即为 null 时序列化忽略 |

#### `com.rick.common.http.model.ResultUtils`（final 工具类）
Result 的工厂方法，全部 `static`：

- `Result success()` / `<T> Result<T> success(T data)` —— 成功，code=200
- `Result fail()` / `<T> Result<T> fail(T data)` —— 失败，code=500
- `Result fail(String message)` / `Result fail(int code, String message)` —— 失败，自定义码/消息
- `<T> Result fail(String message, T data)` / `<T> Result<T> fail(int code, String message, T data)` —— 失败带 data

#### `com.rick.common.http.model.ResultCode`（enum）
内置业务码：`OK(200)`、`ARGUMENT_NOT_VALID(400)`、`ACCESS_FORBIDDEN_ERROR(403)`、`RESOURCE_NOT_EXISTS_ERROR(404)`、`UNPROCESSABLE_ENTITY_ERROR(422)`、`ERROR(500)`。

### 2. 异常体系

#### `com.rick.common.http.exception.BizException extends RuntimeException`
业务异常，携带 `Result` 与可选 `Object[] params`（用于 `String.format` 国际化消息）。常用构造：

```java
new BizException("编号已经存在")
new BizException("%s codes %s 不存在", new Object[]{comment, codes})
new BizException(int code, String msg)
new BizException(ExceptionCode exceptionCode)
new BizException(ExceptionCode exceptionCode, Object[] params)
new BizException(Result result)
```

#### `com.rick.common.http.exception.ExceptionCode`（interface）
让 enum 实现的业务码契约。实现类需提供 `int getCode()` 与 `String getMessage()`，并自带默认方法：

- `throwException()` / `throwException(Object[] params)` —— 直接抛 `BizException`
- 静态工具：`isNull(obj,msg)`、`notNull(obj,msg)`、`notExists(msg[,data])`、`state(boolean,msg[,data])`

示例（sharp-admin 中的真实用法）：
```java
public enum ExceptionCodeEnum implements ExceptionCode {
    USER_NOT_FOUND(400019, "用户不存在！"),
    LOW_STOCKS_ERROR(504001, "「%s」库存不足！");
    // getCode() / getMessage()
}
```

#### `com.rick.common.http.exception.ApiExceptionHandler`（`@RestControllerAdvice`）
全局异常处理器，由消费方通过 `@Import(ApiExceptionHandler.class)` 或 `@ComponentScan` 注册。处理：

| 异常 | HTTP 状态 | 行为 |
|---|---|---|
| `BizException` | 422 | 走 `MessageUtils` 做 i18n；Ajax 返回 `Result`，非 Ajax forward 到 `/error/index` |
| `MaxUploadSizeExceededException` | 422 | 返回 code=5001「文件大小不能超出5M」 |
| `AccessDeniedException` | 403 | `ResultCode.ACCESS_FORBIDDEN_ERROR` |
| `BindException`/`ConstraintViolationException`/`IllegalArgumentException`/`MethodArgumentNotValidException` | 400 | 收集 field/message/rejectedValue 列表放入 data |
| 其它 `Exception` | 500 | `ResultCode.ERROR` |

Ajax 判定走 `HttpServletRequestUtils.isAjaxRequest`；非 Ajax 请求 forward 到 `/error`。

### 3. Web MVC 配置基类

#### `com.rick.common.http.web.SharpWebMvcConfigurer implements WebMvcConfigurer`
**消费方应让自己的 `@Configuration` 继承此类**（见 sharp-admin/sharp-demo 的 `MvcConfig`）。它做了三件事：

1. `addFormatters(registry)` —— 注册 `CodeToEnumConverterFactory`（String→Enum，按 `valueOfCode` 反查）+ 子类 `converterFactories()` 返回的额外工厂。
2. `register(ObjectMapper)` —— 给容器 `ObjectMapper` 装 `SimpleModule`：`Long`/`long` 用 `ToStringSerializer`（避免前端精度丢失）、`LocalDateTime`/`LocalDate` 按指定 pattern 序列化、`Enum.class` 用 `EnumCustomizeDeserializer`、`NON_NULL` 包含策略、空串当 null。pattern 取 `JacksonProperties.getDateFormat()`，默认 `yyyy-MM-dd HH:mm:ss`。
3. `addArgumentResolvers` —— 把 `ParamNameProcessor` 插到最前，支持 `@ParamName` 重命名绑定参数。

子类可覆盖 `List<ConverterFactory> converterFactories()`（默认返回 null）追加自定义工厂。

#### `com.rick.common.http.web.annotation.UnWrapped`（方法注解）
仅当启用了 `@EnableResultWrapped` 时生效：标记 Controller 方法返回值**不**被 `ResultUtils.success(o)` 自动包裹。

#### `com.rick.common.http.web.config.EnableResultWrapped`（类注解）
`@Import(ResultWrappedConfig.class)`，启用「Controller 返回值自动包裹 Result」机制。`ResultWrappedConfig` 把 `ResultWrappedResponseBodyReturnValueHandler` 插到 `RequestResponseBodyMethodProcessor` 之前；handler 会在返回值不是 `Result` 且方法未标 `@UnWrapped` 时，用 `ResultUtils.success(o)` 包一层。
> 注意：当前仓库内 sharp-admin / sharp-demo 都将其注释掉，默认不启用。需要统一包裹时再放开。

### 4. Servlet 请求/响应工具

#### `com.rick.common.http.HttpServletRequestUtils`（final，全 static）
- `boolean isAjaxRequest(HttpServletRequest)` / `isNotAjaxRequest(...)` —— 根据 `accept`、`X-Requested-With`、URI 后缀、`__ajax` 参数判定
- `String getClientIpAddress(HttpServletRequest)` —— 遍历 `X-Forwarded-For` 等代理头，回退 `getRemoteAddr()`
- `Map<String,String> getParameterStringMap(request[, boolean skipBlank])` —— 多值参数用 `,` 拼接；`name[]` 自动去 `[]`
- `Map<String,Object> getParameterMap(request[, skipBlank])` —— 多值返回 `List`
- `Map<String,Object> getParameterMap(request, Map extendParams)` —— 合并扩展参数
- `String getBodyString(request)` —— 读请求体为字符串（UTF-8）

#### `com.rick.common.http.HttpServletResponseUtils`（final，全 static）
- `OutputStream getOutputStreamAsAttachment(request, response, fileName)` / `getOutputStreamAsView(...)` —— 处理中文文件名编码（FF/IE/Edge/其它分别处理），设置 `Content-disposition` 与 `Content-Type`，返回输出流
- `void writeJSON(response, String value)` / `writeJSON(response, Result result)` —— 写 JSON 响应
- `void write(response, contentType, String value)` —— 通用写文本
- `boolean isMSBrowser(request)` —— 是否 IE/Trident/Edge

#### `com.rick.common.http.HttpRequestDeviceUtils`
- `boolean isMobileDevice(request)` / `boolean isIPadDevice(request)` —— 基于 Via 与 User-Agent 的老式判定（轻量场景可用，复杂判定建议用 `DeviceUtils`）

#### `com.rick.common.http.util.HttpUtils`
- `String postJson(String url, Object dataObj)` —— 用 `HttpURLConnection` 发 JSON POST，200 才返回响应体，否则返回 `"Error: HTTP <code>"`
- `String get(String url, Object params)` —— 反射把对象字段拼成 form-urlencoded 发 GET
- `byte[] readAllBytes(InputStream)` —— 读全量字节

### 5. 工具类（`com.rick.common.util`）

#### `JsonUtils`（final，全 static，内部持单例 `ObjectMapper`）
- `String toJson(Object)` —— 序列化（`@SneakyThrows`）
- `<T> T toObject(String json, Class<T>)` / `toObject(InputStream, Class<T>)` / `toObject(String, TypeReference<T>)` / `toObject(JsonNode, Class<T>)`
- `<T> List<T> toList(String json, Class<T>)` / `toList(JsonNode, Class<T>)`
- `<T> Set<T> toSet(String json, Class<T>)` / `toSet(JsonNode)`
- `JsonNode toJsonNode(Object)` / `toJsonNode(String json)`
- `Map<String,?> objectToMap(Object obj)`
- `String beautifyJSON(String json)` —— 美化
- `<T> Object toObjectFromFile(String fileName, Class<T>)`
- 配置：`FAIL_ON_UNKNOWN_PROPERTIES=false`、注册 `JavaTimeModule`、`NON_NULL`

#### `StringUtils`（final，全 static）
注意：**不是** apache 的 StringUtils，方法独立。常用：`isChinese`、`getContent(html)` 提取纯文本、`formatURLSeparator`、`stringToCamel`/`camelToSnake`/`camelToSpinal`/`camelToDot`、`toBoolean(Object)`、`setMethodName`/`getMethodName`/`uncapitalize`、`generateImgName`、`appendValue`。

#### `ClassUtils`（`@UtilityClass`）
反射工具：`getField`、`getAllFields`、`getFieldGenericClass([subClass,] field)`（解析泛型真实 Class，含 `T`/`List<T>`/继承链上的 TypeVariable）、`getClassGenericsTypes`、`setFieldValue`/`getPropertyValue`/`setPropertyValue`（支持嵌套 `a.b.c`，用 `BeanWrapperImpl`+注册 sharp 转换器）。

#### `ReflectUtils`（`@UtilityClass`）
`getAllFields(Class)` —— 自身+父类全部 `Field`。

#### `ObjectUtils`（`@UtilityClass`）
`mayPureObject(Object|Class)` —— 判定是否「纯对象」（非 Number/CharSequence/Boolean/Enum/Array/Temporal/Collection/Map/Primitive）。用于决定是否按 JSON 存储等。

#### `EnumUtils`（`@UtilityClass`）
- `Enum valueOfCode(Class enumType, String code)` —— 优先调用枚举的静态 `valueOfCode(...)`（参数为 String 或 int），失败回退 `Enum.valueOf`；找不到返回 null
- `List<String> getCodes(Class)` —— 所有常量 toString
- `Object getCode(Enum)` —— 反射调 `getCode()`，失败返回 `name()`

#### `BigDecimalUtils`（`@UtilityClass`）
`eq/neq/lt/le/gt/ge(BigDecimal,BigDecimal)`（用 `compareTo`）与 `formatBigDecimalValue(value[, scale, RoundingMode])`（默认 2 位四舍五入、去尾零）。

#### `String2TimeUtils`（`@UtilityClass` final）
字符串→时间，支持 `yyyy-MM-dd`、`yyyy-MM-dd HH`、`yyyy-MM-dd HH:mm`、`yyyy-MM-dd HH:mm:ss` 自动补全：
- `LocalDateTime toLocalDateTime(String)` / `LocalDate toLocalDate(String)` / `LocalTime toTime(String)`(`HH:mm`)/`toTime2(String)` / `Instant toInstant(String)`
- `String appendStartSuffix(String)`（补 `00:00:00`）/ `appendEndSuffix(String)`（补 `23:59:59`）

#### `Time2StringUtils`（`@UtilityClass` final）
`format(Date|LocalDate|LocalDateTime|Instant|long milliseconds[, DateTimeFormatter])` —— 默认 `yyyy-MM-dd HH:mm:ss` / `yyyy-MM-dd`。

#### `DateConvertUtils`（`@UtilityClass`）
`unixTimeToLocalDate(Long)` / `unixTimeToLocalDateTime(Long)`。

#### `FileUtils`（`@UtilityClass`）
`getContentType(String fileName)`（按后缀查 MIME，未命中返回 `application/octet-stream`）、`getFilenameExtension`/`stripFilenameExtension`/`getFilename`/`fullName(name, ext)`、`isImageType(path[, contentType])`（正则匹配 `bmp|png|jpeg|jpg|gif|ico`）、`isImageType(request, name)`、`maximumSize(request, name, int sizeMB)`。

#### `HtmlTagUtils`（`@UtilityClass`）
`isTagPropertyTrueAndPut(Map<String,String> attrMap, String key)` —— 处理 `<input readonly>` 这类布尔属性，判定并回填 map。

#### `IdGenerator`（`@UtilityClass`）
- `Long getSimpleId()` —— 毫秒时间戳 + 3 位随机数（19 位）
- `Long getSequenceId()` —— 内部单例 `Sequence(0)`（Snowflake，workerId 取本机 IP 末字节，dataCenterId=0），`synchronized nextId()`

#### `DeviceUtils`
`Device getCurrentDevice(HttpServletRequest)` —— 解析 UA 返回 `Device`（`normal`/`mobile`/`tablet` 三个布尔 + `Platform{IOS,ANDROID,UNKNOWN}`，平板优先于手机判定）。

#### `ZipUtils`（final，全 static）
`zipFiles(srcDirName, fileName, descFileName)` 压缩、`unZipFiles(zipFileName, descFileName)` 解压、`zipDirectoryToZipFile`/`zipFilesToZipFile`（流式写入指定 `ZipOutputStream`）。

#### `Maps`（final，全 static）
JDK8 友好的不可变 Map 构造：`of()`、`of(k1,v1)`…`of(k1,v1,k2,v2,k3,v3)`、`ofEntries(Map.Entry...)`、`of(Object... keyValues)`、`entry(k,v)`。

### 6. 函数式接口（`com.rick.common.function`）

`SFunction<T,R>` / `SConsumer<T>` —— 继承 `SInfo` 与 `Serializable` 的可序列化 Lambda，能通过 `SerializedLambda` 反射拿到：
- `String getMethodName()` —— impl 方法名（如 `getName`）
- `String getPropertyName()` —— 由 getter 推属性名（如 `name`）
- `boolean isMethodReference()` —— 是否为方法引用（非 `lambda$` 且 kind 为虚/静态/特殊）

被 sharp-database2 的 DAO/`GridService` 用来按属性名取列名、做 `map`/`groupMap`。

### 7. Bean Validation（`com.rick.common.validate`）

- `@EnumValid(target=EnumClass.class[, message])` —— 字段/方法参数校验，校验值是否为枚举的合法 code（走 `EnumUtils.valueOfCode`）。`null` 视为合法。
- `@PhoneValid([message])` —— 校验 11 位大陆手机号
- `ValidatorHelper`（构造注入 `javax.validation.Validator`）—— `validate(T)`、`validate(T, Method, Object[])`（方法参数校验）、`validateProperty(T, propertyName)`；失败抛 `ConstraintViolationException`
- `ServiceMethodValidationInterceptor`（`@Aspect`）—— 切 `com.rick..service..*` 中 `*Service` 类的 public 方法，方法上有 `@Validated` 时走 `ValidatorHelper` 校验参数。**需消费方 `@Import` 注册为 Bean**

### 8. Spring 转换器（`com.rick.common.http.convert`，由 `SharpWebMvcConfigurer.addFormatters` 注册）

- `CodeToEnumConverterFactory` —— String→Enum（按 code）
- `StringToLocalDateConverterFactory` —— String→`LocalDate`（兼容多种格式）
- `JsonStringToMapConverterFactory` —— JSON String→`Map`
- `JsonStringToObjectConverterFactory` —— JSON String→实现了其内部接口 `JsonValue` 的对象
- `JsonStringToCollectionConverter` —— JSON String→`Collection<T>`（按泛型）
- `JsonStringToListMapConverter` / `JsonStringToSetMapConverter` —— JSON String→`List<Map>` / `Set<Map>`
- `LocalDateTimeToInstantConverter` —— `LocalDateTime`→`Instant`

### 9. Jackson 序列化/反序列化（`com.rick.common.http.json`）

由 `SharpWebMvcConfigurer.register(ObjectMapper)` 注册 `EnumCustomizeDeserializer`，其余按字段 `@JsonDeserialize(using=...)` 使用：

- `EnumCustomizeDeserializer` —— 全局 Enum 反序列化，支持 String/Int/Object(`{code:...}`) 三种形态，优先调枚举的 `valueOfCode`
- `EntityWithLongIdPropertyDeserializer<T>` —— 把 `123` 或 `"123"`（或其数组）反序列化成 `setId(Long)` 的实体/集合
- `EntityWithLongIdPropertySerializer<T>` —— 反向：序列化时只输出 `getId()`
- `EntityWithCodePropertyDeserializer<T>` —— 把字符串 code 反序列化成 `setCode(String)` 的实体/集合
- `NamePropertyDeserializer<T>` —— 更通用：根据字段名/`@JsonAlias` 推断应 set 的属性名与类型（String 或 Long），支持集合
- `BooleanPropertyDeserializer` —— 宽松布尔：`null→false`、数组非空→true、`0/false/否`→false、非空文本→true
- `JsonValue`（`JsonStringToObjectConverterFactory` 内部接口）—— 标记「可由 JSON 字符串直接构造」的对象类型

### 10. 参数重命名（`com.rick.common.http.web.param`）

- `@ParamName({"user_id","userId"})` —— 标在字段上，绑定 request 参数时把别名映射到字段名
- `ParamNameProcessor extends ServletModelAttributeMethodProcessor` —— 由 `SharpWebMvcConfigurer` 注册到 `addArgumentResolvers` 最前；缓存 `Class→{from→to}` 映射，用 `ParamNameDataBinder` 完成绑定

## 二、配置项

模块本身**没有** `@ConfigurationProperties`，也**没有** `spring.factories` / 自动配置。唯一读取的外部配置：

| 配置 key | 默认值 | 来源 | 含义 |
|---|---|---|---|
| `spring.jackson.date-format` | `yyyy-MM-dd HH:mm:ss` | `JacksonProperties`（Spring Boot） | `SharpWebMvcConfigurer` 用它格式化 `LocalDateTime` 与 `Date` 序列化 |

> 组件注册靠消费方：`@Import({ApiExceptionHandler.class, MessageUtils.class, ServiceMethodValidationInterceptor.class})` + `@Configuration extends SharpWebMvcConfigurer`（典型见 sharp-admin `MvcConfig`）。

## 三、常用使用示例

```java
// 1. 返回成功结果
return ResultUtils.success(userService.findById(id));

// 2. 抛业务异常（配合 ExceptionCode enum）
throw new BizException(ExceptionCodeEnum.USER_NOT_FOUND);
throw new BizException("%s 不存在", new Object[]{name});

// 3. 校验 Bean
@Resource ValidatorHelper validatorHelper;
validatorHelper.validate(entity);

// 4. JSON
String json = JsonUtils.toJson(obj);
User u = JsonUtils.toObject(json, User.class);
List<User> list = JsonUtils.toList(json, User.class);

// 5. Controller 字段校验注解
@EnumValid(target = SexEnum.class) private Integer sex;
@PhoneValid private String phone;

// 6. 雪花 ID
Long id = IdGenerator.getSequenceId();

// 7. 继承 WebMvc 配置基类
@Configuration
@Import({ApiExceptionHandler.class, MessageUtils.class, ServiceMethodValidationInterceptor.class})
public class MvcConfig extends SharpWebMvcConfigurer {
    @Override public List<ConverterFactory> converterFactories() {
        return Arrays.asList(new IdToEntityConverterFactory());
    }
}
```

## 四、与其它 sharp-* 模块的关系

- **依赖方向**：sharp-common 是最底层公共库，不依赖任何 sharp-* 模块；仅依赖 Spring/Spring Boot（provided）、Spring Security core、Jackson、Guava、commons-lang3/collections4/codec、lombok、slf4j、validation-api、javax.servlet-api。
- **被依赖**：sharp-admin、sharp-demo、sharp-database / sharp-database2、sharp-fileupload、sharp-excel、sharp-generator、sharp-formflow、sharp-report、sharp-mail、sharp-sms、sharp-notification、sharp-meta 均直接或间接引用。最热门入口（按跨模块 import 频次）：`JsonUtils`(29)、`Maps`(23)、`ResultUtils`(23)、`HttpServletRequestUtils`(20)、`Result`(19)、`BizException`(11)、`Time2StringUtils`(10)、`HttpServletResponseUtils`(10)、`FileUtils`(8)、`ClassUtils`(6)。
- sharp-database2 提供 `ValidatorHelper` 的 `@Bean`（`@ConditionalOnMissingBean`+`@ConditionalOnBean(Validator.class)`）。
