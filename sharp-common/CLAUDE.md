# sharp-common 工作指南

> 给修改 sharp-common 的 LLM 的工作规范。改之前先读完本文件。

## 模块定位与边界

- **做什么**：sharp-project 全家桶共用的 HTTP 响应/异常模型、Jackson 与 Spring 参数绑定增强、Snowflake ID、校验注解、通用工具类。
- **不做什么**：不做业务逻辑、不依赖任何 sharp-* 模块、不引入数据库/JPA/MyBatis、不声明 Spring Boot 自动配置（不写 `spring.factories` / `@AutoConfiguration`）。Web 行为一律由上层显式 `@Import`/`@ComponentScan`/`extends SharpWebMvcConfigurer` 启用。
- 改动原则：本模块被几乎所有上层模块依赖，**改一处影响全局**。新增 public API 须有充分理由，优先复用现有工具。

## 目录约定

```
src/main/java/com/rick/common/
├── constant/        # 常量（FileConstants）
├── http/
│   ├── convert/     # Spring Converter/ConverterFactory
│   ├── exception/   # BizException、ExceptionCode、ApiExceptionHandler
│   ├── json/{deserializer,serializer}/  # 自定义 Jackson (de)serializer
│   ├── model/       # Result/ResultCode/ResultUtils
│   ├── util/        # MessageUtils、HttpUtils
│   ├── web/
│   │   ├── annotation/  # @UnWrapped
│   │   ├── config/      # @EnableResultWrapped + ResultWrappedConfig
│   │   └── param/       # @ParamName 系列
│   └── *Utils.java      # HttpServletRequest/Response/DeviceUtils（直接放 http 根下）
├── util/
│   ├── sequence/    # Sequence、SystemClock
│   └── *Utils.java  # JsonUtils、StringUtils 等
└── validate/        # @EnumValid/@PhoneValid + ValidatorHelper + AOP 拦截器
    └── annotation/
```

无 DAO/Entity/Service/Mapper 分层——纯工具与基础设施。

## 编码约定（从现有代码归纳）

- **工具类**：用 `@UtilityClass`（lombok）或 `final class + private 构造`；方法 `static`。
- **Spring 组件**：`@Component`/`@Configuration`/`@RestControllerAdvice` 用 lombok `@RequiredArgsConstructor`/`@Slf4j`。
- **命名**：工具类以 `Utils` 结尾（`JsonUtils`、`FileUtils`）；HTTP 三件套 `Result`/`ResultCode`/`ResultUtils`。
- **注释语言**：中文为主，作者署名 `@author Rick` / `@author Rick.Xu` + `@createdAt`/`@date`。
- **异常**：业务异常统一用 `BizException`；校验失败抛 `ConstraintViolationException`；工具方法内部错误用 `RuntimeException` 包裹或 `e.printStackTrace()`（保持既有风格，不擅自重写）。
- **事务**：本模块无事务。
- **null 处理**：工具方法普遍判 null 返回 null/默认值；Jackson `Include.NON_NULL`。
- **泛型与反射**：自定义反序列化器大量使用 `ContextualDeserializer` + `BeanProperty.getType()`；改动时注意 `List` 分支与单值分支都要覆盖。
- **不要**新增 `spring.factories` 或 `@AutoConfiguration`——这会破坏"上层显式启用"的约定。

## 常见改动清单

| 改动 | 涉及文件 |
|---|---|
| 加一个工具方法 | 在对应 `*Utils.java` 追加 `public static` 方法；不要新建类除非主题独立 |
| 加一个校验注解 | `validate/annotation/` 新建 `@Constraint` 注解 + `validate/` 下 `ConstraintValidator` 实现 |
| 加一个 Spring Converter | `http/convert/` 新建 `Converter`/`ConverterFactory`；如需自动注册，在 `SharpWebMvcConfigurer.addFormatters` 或子类 `converterFactories()` 接入 |
| 加一个 Jackson (de)serializer | `http/json/deserializer|serializer/` 新建类，实现 `JsonDeserializer`/`JsonSerializer`（必要时 `Contextual*`） |
| 加一个状态码 | `ResultCode` 枚举追加常量 |
| 改默认响应包装行为 | `ResultWrappedResponseBodyReturnValueHandler.handleReturnValue` |
| 加配置项 | 本模块原则上不引入 `@ConfigurationProperties`；如需读配置，走 Spring 标准 `JacksonProperties` 等已有机制 |

## 构建与测试

- 构建：`mvn clean install -f sharp-common`（或在仓库根 `./mvn.sh` 全量构建，本模块排第 2）。
- 依赖管理：父 POM 是 `sharp-dependencies`（`com.rick:sharp-dependencies:2.0-SNAPSHOT`），版本由 `<sharp.version>` 统一管理。本模块 `version=${sharp.version}`。
- **无测试代码**（`src/test` 不存在）。改动后若涉及核心工具（JsonUtils/Sequence/转换器），建议本地写临时 main 或在上层模块 `sharp-demo` 验证。

## 陷阱与注意点

1. **两个独立的 ObjectMapper**：`JsonUtils` 内部持有一个自建 ObjectMapper；`SharpWebMvcConfigurer.register` 改的是 Spring 容器中的 ObjectMapper。两者配置不同步——序列化行为以"调用入口"为准。改 `JsonUtils` 的静态块不会影响 Spring MVC 的序列化，反之亦然。
2. **无自动配置**：新增 `@Component`/`@Configuration` 不会自动被上层扫描，必须告知调用方 `@Import` 或 `@ComponentScan`。
3. **`Sequence` 取 workerId 来自本机 IP 末字节**：多机部署若 IP 末字节相同会撞 ID，必要时改构造方式。
4. **`EnumJsonDeserializer` 已 `@Deprecated`**：枚举反序列化改由 Jackson 自带 + `@JsonValue`/`valueOfCode`；不要再依赖它，也不要在新代码里 `objectMapper.registerModule` 注册它（`SharpWebMvcConfigurer` 已注释掉）。
5. **`CodeToEnumConverterFactory` 注册顺序**：在 `addFormatters` 中先于子类工厂注册，匹配失败会降级到 `Enum.valueOf(name)`——枚举既可用 code 也可用 name 反序列化，改顺序会破坏该行为。
6. **`ResultWrappedResponseBodyReturnValueHandler` 装配方式**：通过 `@EnableResultWrapped` → `ResultWrappedConfig` 替换 `RequestMappingHandlerAdapter` 的返回值处理器链（在 `RequestResponseBodyMethodProcessor` 前插入）。替换发生在 `setReturnValueHandlers`，会重置整条链——改 Spring Boot 版本时需回归。
7. **`ApiExceptionHandler` 非 Ajax 路径**：会 `forward` 到 `/error` 或 `/error/index`，上层应用必须提供对应视图/控制器，否则 404。
8. **`ClassUtils` 用了 `sun.reflect.generics.reflectiveObjects.*`**：依赖 JDK 内部 API，升级 JDK 9+ 模块化后需关注可用性（当前可用）。
9. **不要删除既有看似"死"的代码**（如 `ValidatorHelper` 注释块、`MvcConfig` 里的注释）——上层 `sharp-demo` 里有大量注释样例引用，保持现状。

## 不要动的部分

- `Result` / `ResultCode` / `ResultUtils` 的字段结构（上层序列化契约依赖）。
- `Sequence` 的位数分配（已部署系统的存量 ID 依赖该结构）。
- `spring.factories`——本就不存在，不要新增。
