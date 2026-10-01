# sharp-common 架构

## 模块定位

sharp-project 全家桶的"地基"：HTTP 统一响应/异常、Jackson 与参数绑定增强、ID 生成、校验注解、零散工具类。无 Spring 自动配置——所有 Web 行为需上层显式引入。

## 包结构

```
com.rick.common
├── constant            # 文件类型等常量
├── http
│   ├── convert         # Spring Converter/ConverterFactory：String→Enum/LocalDate/Map/Collection
│   ├── exception       # BizException、ExceptionCode、ApiExceptionHandler(@RestControllerAdvice)
│   ├── json
│   │   ├── deserializer # 自定义 Jackson 反序列化器（id/code/name → 实体、宽松 Boolean）
│   │   └── serializer   # 实体 → id 序列化器
│   ├── model           # Result / ResultCode / ResultUtils
│   ├── util            # MessageUtils(i18n)、HttpUtils(原生 HttpURLConnection)
│   ├── web
│   │   ├── annotation  # @UnWrapped（跳过 Result 包装）
│   │   ├── config      # @EnableResultWrapped + ResultWrappedConfig（注册 ReturnValueHandler）
│   │   └── param       # @ParamName + ParamNameDataBinder + ParamNameProcessor（字段重命名绑定）
│   ├── HttpServletRequestUtils / HttpServletResponseUtils / HttpRequestDeviceUtils
├── util
│   ├── sequence        # Sequence(Snowflake)、SystemClock
│   └── (工具类)         # JsonUtils、StringUtils、Time2StringUtils、String2TimeUtils、FileUtils...
└── validate            # @EnumValid/@PhoneValid 校验器、ValidatorHelper、ServiceMethodValidationInterceptor
```

## 关键抽象与协作

```
                ┌──────────── Web 层（需上层启用）────────────┐
                │                                              │
   请求 → DispatcherServlet → SharpWebMvcConfigurer           │
            │   ├── addFormatters: CodeToEnumConverterFactory  │
            │   │     + converterFactories()（子类扩展）        │
            │   ├── addArgumentResolvers: ParamNameProcessor   │
            │   │     └─ @ParamName → ParamNameDataBinder       │
            │   └── register(ObjectMapper): Long→String、       │
            │        LocalDateTime/LocalDate 格式化             │
            │                                                  │
            ├─ ResultWrappedResponseBodyReturnValueHandler      │
            │   （由 @EnableResultWrapped 装配，包装返回值为     │
            │    Result；@UnWrapped 跳过）                      │
            │                                                  │
   异常 → ApiExceptionHandler(@RestControllerAdvice)            │
            └─ BizException / ConstraintViolation / ...         │
               → ResultUtils.fail(...)                          │
               （Ajax 返回 JSON；非 Ajax forward /error）        │
                └──────────────────────────────────────────────┘

   工具层（无状态，随处可调）
     JsonUtils ── 独立 ObjectMapper（与 Spring 容器的互不影响）
     IdGenerator ── Sequence(Snowflake) ── SystemClock(可选)
     ValidatorHelper ── javax.validation.Validator
     HttpServletRequestUtils / HttpServletResponseUtils / *Utils
```

## 分层与数据流

1. **入参绑定**：`ParamNameProcessor`（@ParamName 重命名）+ `CodeToEnumConverterFactory`（code→枚举）+ `StringToLocalDateConverterFactory` + 各 `JsonStringTo*Converter`（JSON 字符串→集合/Map），把 request 参数适配到 Controller 方法签名。
2. **业务执行**：`ServiceMethodValidationInterceptor`（@Aspect）对 `@Validated` 标注的 Service 方法做参数校验；业务层用 `BizException` / `ExceptionCode` 抛错。
3. **响应包装**：`ResultWrappedResponseBodyReturnValueHandler` 拦截 `@ResponseBody` 返回值，非 `Result` 且未标 `@UnWrapped` 时包装为 `ResultUtils.success(o)`。
4. **异常归口**：`ApiExceptionHandler` 统一捕获并转 `Result`。
5. **i18n**：`MessageUtils.getMessage` 用 Spring `MessageSource` 解析 message 占位。

## 对外依赖

| 依赖 | 用途 |
|---|---|
| jackson-core/databind/annotations | JsonUtils、自定义（反）序列化器 |
| spring-context / spring-webmvc / spring-boot-starter-web(provided) | WebMvc 配置、Converter、ReturnValueHandler |
| spring-boot-starter-aop (provided) | ServiceMethodValidationInterceptor 切面 |
| spring-security-core | `AccessDeniedException` 处理 |
| javax.validation-api (provided) | `@EnumValid`/`@PhoneValid`/`ValidatorHelper` |
| javax.servlet-api (provided) | HttpServletRequest/Response 工具 |
| guava / commons-lang3 / commons-collections4 / commons-codec | 通用工具 |
| lombok | `@UtilityClass`/`@Data`/`@RequiredArgsConstructor` |
| slf4j-api | 日志 |

无 sharp-* 模块依赖（最底层库）。

## 数据库表/实体

不涉及。本模块不含持久化。

## 扩展点

- `SharpWebMvcConfigurer.converterFactories()`：子类返回额外 `ConverterFactory` 列表（如 `IdToEntityConverterFactory`）。
- `ExceptionCode`：实现该接口的枚举/类即可获得 `throwException()` 能力。
- `JsonStringToObjectConverterFactory.JsonValue`：标记接口，实现后即可注册 `String → 该类` 的 JSON 转换。
- `EntityWithLongIdPropertyDeserializer` / `EntityWithCodePropertyDeserializer` / `NamePropertyDeserializer`：通过 `@JsonDeserialize(using=...)` 在任意实体字段启用 id/code 反序列化。
- `Sequence`：可自定义 `dataCenterId/workerId/clock/timeOffset/randomSequence` 构造。

## 配置与启动流程

**没有 META-INF/spring.factories，没有 @AutoConfiguration**。启用方式由上层决定：

| 能力 | 启用方式 |
|---|---|
| WebMvc 基础（Jackson + Converter + ParamName） | 配置类 `extends SharpWebMvcConfigurer` |
| 全局异常处理 | `@ComponentScan(basePackageClasses=ApiExceptionHandler.class)` 或 `@Import(ApiExceptionHandler.class)` |
| i18n MessageUtils | `@ComponentScan(basePackageClasses=MessageUtils.class)` 或 `@Import(MessageUtils.class)` |
| Service 方法级校验切面 | `@Import(ServiceMethodValidationInterceptor.class)` |
| 返回值自动包装 Result | 配置类标 `@EnableResultWrapped` |

`SharpWebMvcConfigurer` 在容器启动时：`addFormatters` 注册 ConverterFactory → `register(ObjectMapper)` 通过 `@Autowired(required=false)` 接收容器内 ObjectMapper 并注册 Long/时间序列化模块 → `paramNameProcessor()` 注册 Bean 并在 `addArgumentResolvers` 插入首位。
