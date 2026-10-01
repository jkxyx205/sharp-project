# sharp-common 架构

## 一、模块定位

sharp 项目的公共基础库（jar）。聚合三件事：① Web 层统一约定（返回结果、异常处理、MVC 配置基类、参数绑定）；② 通用工具（JSON、时间、反射、文件、ID、压缩）；③ Spring/Jackson/Validation 扩展点。无数据库、无 Spring 自动配置（`spring.factories`），由消费方显式 `@Import` / 继承。

## 二、包结构

```
com.rick.common
├── constant            常量（FileConstants：图片扩展名/正则）
├── function            可序列化 Lambda（SFunction/SConsumer + SInfoHelper 反射解析）
├── http
│   ├── convert         Spring Converter/ConverterFactory（String→Enum/LocalDate/Map/Collection/Object）
│   ├── exception       BizException / ExceptionCode 接口 / ApiExceptionHandler 全局处理
│   ├── json
│   │   ├── deserializer Jackson 反序列化器（Enum/Entity by id or code/Name/Boolean）
│   │   └── serializer   Jackson 序列化器（EntityWithLongId → 只输出 id）
│   ├── model           Result / ResultCode / ResultUtils（统一响应）
│   ├── util            HttpUtils（原生 HttpURLConnection）、MessageUtils（i18n）
│   ├── web
│   │   ├── annotation   @UnWrapped
│   │   ├── config       @EnableResultWrapped + ResultWrappedConfig（返回值自动包裹 Result）
│   │   └── param        @ParamName + ParamNameProcessor + ParamNameDataBinder（参数别名绑定）
│   ├── HttpServletRequestUtils / HttpServletResponseUtils / HttpRequestDeviceUtils
│   └── SharpWebMvcConfigurer（对外暴露的 WebMvc 配置基类）
└── util
    ├── model/Device     DeviceUtils 返回的设备信息 POJO
    ├── sequence/        Sequence（Snowflake）+ SystemClock（高并发时间戳缓存）
    └── (root)           JsonUtils, StringUtils, ClassUtils, ReflectUtils, ObjectUtils,
                         EnumUtils, BigDecimalUtils, String2TimeUtils, Time2StringUtils,
                         DateConvertUtils, FileUtils, HtmlTagUtils, IdGenerator,
                         DeviceUtils, ZipUtils, Maps
└── validate
    ├── annotation/      @EnumValid / @PhoneValid
    ├── EnumValidator / PhoneValidator（ConstraintValidator 实现）
    ├── ValidatorHelper（封装 javax.validation.Validator）
    └── ServiceMethodValidationInterceptor（@Aspect，Service 方法参数校验）
```

## 三、关键抽象与关系

```
消费方 MvcConfig ──extends──> SharpWebMvcConfigurer ──implements──> WebMvcConfigurer
                                     │
                ┌────────────────────┼─────────────────────┐
                ▼ addFormatters      ▼ register(ObjectMapper) ▼ addArgumentResolvers
        CodeToEnumConverterFactory   SimpleModule                ParamNameProcessor
        + 子类 converterFactories()  ├ Long→String              └─ ParamNameDataBinder
                                   ├ LocalDateTime/LocalDate
                                   └ EnumCustomizeDeserializer

ApiExceptionHandler (@RestControllerAdvice)
        └─> BizException ──> Result(result, params)
                          └─> MessageUtils.getMessage (i18n)

@EnumValid/@PhoneValid ─> EnumValidator/PhoneValidator ─> EnumUtils.valueOfCode
ServiceMethodValidationInterceptor (@Aspect) ─> ValidatorHelper ─> ConstraintViolationException

IdGenerator ─> Sequence(dataCenterId=0, workerId=IP末字节)
                  └─> SystemClock.INSTANCE (可选高并发时间戳)

SFunction<T,R> / SConsumer<T> ─ Serializable + SInfo
                  └─> SInfoHelper.getSerializedLambda → 方法名/属性名/方法引用判定
```

## 四、分层与数据流

1. **请求入参 → Controller**
   - `ParamNameProcessor`（插在 resolvers 最前）拦截非 `@RequestBody`、含 `@ParamName` 字段的 POJO，用 `ParamNameDataBinder` 把别名映射到字段名再绑定。
   - Spring `FormatterRegistry` 里的 `CodeToEnumConverterFactory` 等把 String 参数转成 `Enum`/`LocalDate`/`Map`/`Collection`/`JsonValue`。

2. **JSON body → 实体（Jackson）**
   - `ObjectMapper`（容器单例）由 `SharpWebMvcConfigurer.register` 装入 `SimpleModule`：`Enum` 全局走 `EnumCustomizeDeserializer`；字段级 `@JsonDeserialize(using=EntityWithLongIdPropertyDeserializer.class)` 等把 `123`/`"123"`/数组还原成实体或集合；`Long` 序列化为 String 防前端精度丢失。

3. **Service 调用**
   - `ServiceMethodValidationInterceptor`（`@Aspect`，pointcut `within(com.rick..service..*) && execution(public * *Service.*(..))`）在方法标 `@Validated` 时调 `ValidatorHelper.validate(target, method, args)`，失败抛 `ConstraintViolationException`。

4. **Controller 返回**
   - 默认不包裹。若启用 `@EnableResultWrapped`，`ResultWrappedResponseBodyReturnValueHandler`（插在 `RequestResponseBodyMethodProcessor` 前）把非 `Result` 且未标 `@UnWrapped` 的返回值用 `ResultUtils.success(o)` 包一层。

5. **异常 → 响应**
   - `ApiExceptionHandler` 拦 `BizException`（i18n → Ajax 返回 `Result` / 非 Ajax forward `/error/index`）、参数校验类（收集 field/message/rejectedValue）、`AccessDeniedException`、上传超限、兜底 `Exception`。

## 五、对外依赖

| 依赖 | 用途 | scope |
|---|---|---|
| `org.springframework:spring-context` | ApplicationContext / @Configuration | provided |
| `spring-boot-starter-web` | WebMvc / JacksonProperties / RequestMappingHandlerAdapter | provided |
| `spring-boot-starter-aop` | `ServiceMethodValidationInterceptor` 的 @Aspect | provided |
| `spring-security-core` | `AccessDeniedException` 处理 | compile |
| `javax.servlet-api` | HttpServletRequest/Response | provided |
| `validation-api` | `@EnumValid`/`@PhoneValid`/`ValidatorHelper` | provided |
| `jackson-core/databind/annotations` | JSON 序列化、自定义 ser/deser | compile |
| `commons-lang3` / `commons-collections4` / `commons-codec` | 通用工具、Base64 | compile |
| `guava` | `Maps`/`Lists` 容器 | compile |
| `lombok` | `@Data`/`@UtilityClass`/`@SneakyThrows` | compile |
| `slf4j-api` | 日志 | compile |
| `junit` | — | test（实际无测试源码） |

无 sharp-* 模块依赖。

## 六、数据库表/实体

不涉及。本模块不持久化、不定义 JPA 实体。

## 七、扩展点

1. **继承 `SharpWebMvcConfigurer`**：覆盖 `converterFactories()` 追加自定义 `ConverterFactory`（如 sharp-database2 的 `IdToEntityConverterFactory`）；覆盖 `addInterceptors`/`addArgumentResolvers` 等 `WebMvcConfigurer` 任意方法。
2. **实现 `ExceptionCode` 接口**：让业务 enum 暴露 `getCode()`/`getMessage()` 并获得 `throwException()` 默认方法，直接喂给 `BizException`。
3. **实现 `JsonStringToObjectConverterFactory.JsonValue`**：标记可由 JSON 字符串直接构造的值对象，配合转换器在 GET 请求参数里把 JSON 映射到对象。
4. **字段级 Jackson 扩展**：用 `@JsonDeserialize(using = ...)` 选 `EntityWithLongIdPropertyDeserializer` / `EntityWithCodePropertyDeserializer` / `NamePropertyDeserializer` / `BooleanPropertyDeserializer`；用 `@JsonSerialize(using = EntityWithLongIdPropertySerializer.class)` 反向。
5. **`SFunction`/`SConsumer`**：作为带方法名自省的 Serializable Lambda，供下游做按属性名取列、分组等。
6. **`@ParamName`**：给 POJO 字段加请求参数别名。
7. **`@UnWrapped` + `@EnableResultWrapped`**：控制返回值是否自动包裹 `Result`。

## 八、配置与启动流程

**无自动配置**（无 `spring.factories`、无 `@AutoConfiguration`）。启动期由消费方完成装配，典型路径（sharp-admin/sharp-demo `MvcConfig`）：

1. `@Configuration` 类 `extends SharpWebMvcConfigurer` → 容器初始化时回调：
   - `addFormatters` 注册 `CodeToEnumConverterFactory` + 子类 `converterFactories()`；
   - `register(ObjectMapper)` 给容器 ObjectMapper 装 `SimpleModule`（Long→String、时间格式、Enum 反序列化、NON_NULL、空串当 null）；
   - `addArgumentResolvers` 插入 `ParamNameProcessor`；并提供 `@Bean ParamNameProcessor`。
2. `@Import({ApiExceptionHandler.class, MessageUtils.class, ServiceMethodValidationInterceptor.class})` 显式注册全局异常处理、i18n 工具、Service 校验切面。
3. （可选）`@EnableResultWrapped` → `ResultWrappedConfig` 把 `ResultWrappedResponseBodyReturnValueHandler` 插入 `RequestMappingHandlerAdapter` 返回值处理器链。

`ValidatorHelper` Bean 由 sharp-database2 用 `@ConditionalOnMissingBean`+`@ConditionalOnBean(Validator.class)` 提供，或消费方自己 `@Bean`。

## 九、待确认 / 历史包袱

- `HttpRequestDeviceUtils` 的 UA 关键字列表非常老（含诺基亚/塞班/MTK 山寨机等），仅适用于粗略判定；新设备判定应优先用 `DeviceUtils`。
- `BooleanPropertyDeserializer` 中 `node.asBoolean()` 后紧跟 `if (node.isArray())` —— 逻辑分支顺序待确认是否有意为之（看起来是笔误，但行为：先判 boolean，再判 array）。
- `ClassUtils.getFieldGenericClass(Field)` 单参版本已 `@Deprecated`，对 `T`/泛型变量无法解析真实类型，应改用双参 `getFieldGenericClass(subClass, field)`。
- `Sequence` 默认 `dataCenterId=0`、`workerId=IP末字节`；多副本同 IP 段有冲突风险，需自定义时直接 `new Sequence(...)`。
