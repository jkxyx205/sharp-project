# sharp-common API

> sharp-project 的基础设施库：统一 HTTP 响应包装、异常处理、Jackson/JSON 工具、参数绑定与转换、校验注解、ID 生成、常用工具类。无 Spring 自动配置，需调用方显式 `@Import` / `@ComponentScan` / 继承 `SharpWebMvcConfigurer` 启用。

## 核心 public 入口

### 1. Result / ResultUtils / ResultCode — 统一响应模型

| 类 | 全名 | 职责 |
|---|---|---|
| `Result<T>` | `com.rick.common.http.model.Result` | 泛型响应体：`success`/`code`/`message`/`data`（data 为 null 不序列化） |
| `ResultUtils` | `com.rick.common.http.model.ResultUtils` | `Result` 工厂方法 |
| `ResultCode` | `com.rick.common.http.model.ResultCode` | 内置状态码枚举：`OK(200)`、`ARGUMENT_NOT_VALID(400)`、`ACCESS_FORBIDDEN_ERROR(403)`、`RESOURCE_NOT_EXISTS_ERROR(404)`、`UNPROCESSABLE_ENTITY_ERROR(422)`、`ERROR(500)` |

`ResultUtils` 关键方法：
- `Result success()` / `<T> Result<T> success(T data)` — 成功响应，code=200。
- `Result fail()` / `Result fail(String message)` / `Result fail(int code, String message)` / `<T> Result<T> fail(int code, String message, T data)` — 失败响应，默认 code=500。

线程安全：纯静态工厂，无状态。

### 2. BizException / ExceptionCode / ApiExceptionHandler — 异常体系

- `BizException extends RuntimeException`（`com.rick.common.http.exception.BizException`）：业务异常，持 `Result` 与 i18n `params`。构造器接收 `msg` / `(code,msg)` / `(ExceptionCode)` / `(Result)` 等，支持 `String.format` 国际化占位。
- `ExceptionCode`（`com.rick.common.http.exception.ExceptionCode`）：接口，实现 `getCode()/getMessage()` 后即可用 `throwException()` / `throwException(params)` 抛出。另含静态断言：`notNull`、`isNull`、`notExists`、`state`。
- `ApiExceptionHandler`（`com.rick.common.http.exception.ApiExceptionHandler`）：`@RestControllerAdvice`，捕获 `BizException`、`AccessDeniedException`、`BindException`/`ConstraintViolationException`/`MethodArgumentNotValidException`/`IllegalArgumentException`、`MaxUploadSizeExceededException` 及兜底 `Exception`。Ajax 请求返回 `Result`；非 Ajax 转发到 `/error` 或 `/error/index`。**需调用方 `@ComponentScan` 或 `@Import` 该类才生效**。

### 3. SharpWebMvcConfigurer — WebMvc 基类

`com.rick.common.http.web.SharpWebMvcConfigurer implements WebMvcConfigurer`。应用配置类继承它以获得：
- 注册 `CodeToEnumConverterFactory`（String code → 枚举，先按 `valueOfCode` 再降级 `Enum.valueOf`）+ 子类 `converterFactories()` 返回的额外工厂。
- 装配 `ParamNameProcessor`（支持 `@ParamName` 字段重命名绑定），插入到参数解析器最前。
- `register(ObjectMapper)`：注册 Long→String、`LocalDateTime`/`LocalDate` 格式化（pattern 取 `JacksonProperties.dateFormat`，默认 `yyyy-MM-dd HH:mm:ss`）、NON_NULL 序列化、`ACCEPT_EMPTY_STRING_AS_NULL_OBJECT`。

扩展点：覆写 `List<ConverterFactory> converterFactories()` 返回自定义转换工厂。

### 4. JsonUtils — Jackson 工具

`com.rick.common.util.JsonUtils`（final，私有构造，全静态）。内部 `ObjectMapper` 关闭未知字段失败、注册 `JavaTimeModule`、NON_NULL。
- `String toJson(Object)`
- `<T> T toObject(String json, Class<T>)` / `toObject(String, TypeReference<T>)` / `toObject(JsonNode, Class<T>)` / `toObject(InputStream, Class<T>)`
- `<T> List<T> toList(String json, Class<T>)` / `toList(JsonNode, Class<T>)`
- `<T> Set<T> toSet(String json, Class<T>)`
- `JsonNode toJsonNode(Object)` / `toJsonNode(String)`
- `Map<String,?> objectToMap(Object)`
- `String beautifyJSON(String)`
- `<T> Object toObjectFromFile(String fileName, Class<T>)`

所有方法用 `@SneakyThrows` 吞 checked 异常。注意：该 ObjectMapper 是 sharp-common 私有的独立实例，与 Spring 容器中的 ObjectMapper 相互独立。

### 5. IdGenerator / Sequence — ID 生成

- `IdGenerator`（`com.rick.common.util.IdGenerator`，`@UtilityClass`）：
  - `Long getSimpleId()` — 毫秒时间戳拼接 100-999 随机数（16-19 位）。
  - `Long getSequenceId()` — Snowflake ID（基于 `Sequence`，dataCenterId=0，workerId 取本机 IP 末字节）。
- `Sequence`（`com.rick.common.util.sequence.Sequence`）：Snowflake 实现，`synchronized nextId()` 线程安全；支持时间回拨容忍（默认 5ms）、可选 `SystemClock` 高并发取时、可选随机序列。

### 6. HttpServletRequestUtils / HttpServletResponseUtils / HttpRequestDeviceUtils

- `HttpServletRequestUtils`：`isAjaxRequest`、`getClientIpAddress`（多代理头探测）、`getParameterMap`/`getParameterStringMap`（数组 value 用 `,` 拼接，`[]` 后缀名识别多选）、`getBodyString`。
- `HttpServletResponseUtils`：`writeJSON(response, Result|String)`、`getOutputStreamAsAttachment/AsView`（处理中文/IE/Firefox 文件名编码）、`isMSBrowser`。
- `HttpRequestDeviceUtils`：`isMobileDevice`、`isIPadDevice`（基于 User-Agent/Via）。

### 7. 校验注解

- `@EnumValid(target=枚举类)`（`com.rick.common.validate.annotation.EnumValid`）：字段值经 `EnumUtils.valueOfCode` 校验；null 通过。
- `@PhoneValid`（`com.rick.common.validate.annotation.PhoneValid`）：中国大陆手机号校验；null 通过。
- `ValidatorHelper`（`com.rick.common.validate.ValidatorHelper`）：包装 `javax.validation.Validator`，`validate(target)` 与 `validate(target, method, args)`（方法级校验，失败抛 `ConstraintViolationException`）。
- `ServiceMethodValidationInterceptor`（`@Aspect`）：切 `com.rick..service..*Service` 的 public 方法，方法上标 `@Validated` 时走 `ValidatorHelper`。**需 `@Import` 该类才生效**。

### 8. Jackson 自定义（反）序列化器

| 类 | 用途 |
|---|---|
| `EntityWithLongIdPropertyDeserializer` | JSON 中的 Long/String id 反序列化为实体（调 `setId`）；支持 `List<实体>` |
| `EntityWithCodePropertyDeserializer` | JSON 中的 String code 反序列化为实体（调 `setCode`）；支持 `List<实体>` |
| `EntityWithLongIdPropertySerializer` | 实体/`List<实体>` 序列化为 id 数组（调 `getId`） |
| `NamePropertyDeserializer` | 根据字段名+`@JsonAlias` 推断目标属性（code/id/name），上下文敏感 |
| `BooleanPropertyDeserializer` | 宽松 Boolean 反序列化（支持 `"否"`/`0`/数组非空等） |
| `EnumJsonDeserializer`（`@Deprecated`） | 旧版枚举反序列化，已被 Jackson 自带逻辑替代 |

均通过字段上 `@JsonDeserialize(using=...)` / `@JsonSerialize(using=...)` 使用。

### 9. Spring 转换器（Converter / ConverterFactory）

`com.rick.common.http.convert` 下：
- `CodeToEnumConverterFactory` — String → Enum（按 code）
- `StringToLocalDateConverterFactory` — String → LocalDate（兼容 `yyyy-MM-dd`/`yyyy-MM-dd HH:mm:ss` 等）
- `LocalDateTimeToInstantConverter`
- `JsonStringToMapConverterFactory`、`JsonStringToObjectConverterFactory`（目标类须实现 `JsonValue` 标记接口）
- `JsonStringToCollectionConverter`、`JsonStringToListMapConverter`、`JsonStringToSetMapConverter` — JSON 字符串 → 范型集合

### 10. 其余工具类

`util` 包：`BigDecimalUtils`（比较与格式化）、`DateConvertUtils`（unix 毫秒 → LocalDate/LocalDateTime）、`EnumUtils`（`valueOfCode`/`getCode`/`getCodes`）、`FileUtils`（contentType/扩展名/图片判断）、`HtmlTagUtils`、`Maps`（JDK9 风格 `Map.of`）、`ObjectUtils.mayPureObject`、`ReflectUtils.getAllFields`、`String2TimeUtils`（字符串→时间，含 `appendStartSuffix`/`appendEndSuffix` 补 00:00:00/23:59:59）、`Time2StringUtils`（时间→字符串）、`StringUtils`（命名转换 `camelToSnake`/`stringToCamel` 等、HTML 提纯、URL 规整）、`ZipUtils`、`ClassUtils`（范型/字段反射、嵌套属性赋值）。

`constant.FileConstants`：`IMAGE_EXTENSION_VALUE`、`IMAGE_PATH_REGEX`。

`http.util.HttpUtils`：`postJson(url, obj)`、`get(url, params)` 基于原生 `HttpURLConnection`。

`http.util.MessageUtils`：`@Component`，静态 `getMessage(code, params)`，做 i18n 解析；无 MessageSource 时原样返回 code。**需 `@ComponentScan` 才注入**。

## 配置项

本模块不定义独立 `@ConfigurationProperties`。会读取 Spring Boot 标准 `spring.jackson.date-format`（经 `JacksonProperties`）作为 LocalDateTime/SimpleDateFormat 的 pattern，缺省 `yyyy-MM-dd HH:mm:ss`。

## 使用示例

继承 `SharpWebMvcConfigurer` 启用 Web 层基础设施：

```java
@Configuration
@ComponentScan(basePackageClasses = {ApiExceptionHandler.class, MessageUtils.class})
public class MvcConfig extends SharpWebMvcConfigurer {
    @Override
    public List<ConverterFactory> converterFactories() {
        return Arrays.asList(new IdToEntityConverterFactory());
    }
}
```

抛业务异常（被 `ApiExceptionHandler` 自动转 `Result`）：

```java
throw new BizException(404, "用户 {0} 不存在", new Object[]{userId});
// 或实现 ExceptionCode
```

直接返回业务对象，启用 `@EnableResultWrapped` 后自动包装成 `Result`（`@UnWrapped` 标注的方法跳过包装）：

```java
@GetMapping("/user/{id}")
public User user(@PathVariable Long id) { return userService.get(id); } // 序列化为 {success,code,message,data}
```

## 与其它 sharp-* 模块的关系

- 依赖：仅第三方（Jackson、Spring、Guava、commons-lang3/collections4/codec、spring-security-core），不依赖任何 sharp-* 模块。
- 被依赖：`sharp-database`、`sharp-excel`、`sharp-fileupload`、`sharp-notification`、`sharp-sms` 及上层应用模块（`sharp-admin`、`sharp-demo` 等）的基础库。
