# sharp-common 改造指南（给 LLM 的工作说明）

## 一、模块定位与边界

**做什么**：提供 sharp 全家桶的公共基础设施——统一返回结果（`Result`/`ResultUtils`）、业务异常体系（`BizException`/`ExceptionCode`/`ApiExceptionHandler`）、Web MVC 配置基类（`SharpWebMvcConfigurer`）、Spring 转换器、Jackson ser/deser、Bean Validation 注解与切面、以及 JSON/时间/反射/文件/ID/压缩等纯工具类。

**不该做什么**：
- 不写数据库访问、不依赖 JPA/MyBatis。
- 不依赖任何 sharp-* 模块（保持最底层）。
- 不放业务逻辑、不放特定模块的 DTO/Service。
- 不引入 Spring Boot 自动配置（`spring.factories` / `@AutoConfiguration`）——本模块设计为「消费方显式 `@Import` / 继承」装配，**不要**新增自动配置破坏这层约定。

## 二、目录约定

| 内容 | 位置 |
|---|---|
| 工具类 | `com.rick.common.util`（`@UtilityClass` 或 final + 私有构造）|
| 函数式接口 | `com.rick.common.function` |
| Web/MVC 相关 | `com.rick.common.http` 及子包 |
| Spring 转换器 | `com.rick.common.http.convert` |
| Jackson ser/deser | `com.rick.common.http.json.{serializer,deserializer}` |
| 异常 | `com.rick.common.http.exception` |
| 统一响应 | `com.rick.common.http.model` |
| Web 配置基类/注解 | `com.rick.common.http.web`、`.web.annotation`、`.web.config`、`.web.param` |
| Servlet 工具 | `com.rick.common.http`（`HttpServletRequestUtils` 等）|
| Validation | `com.rick.common.validate`、`.validate.annotation` |
| 常量 | `com.rick.common.constant` |
| ID 生成 | `com.rick.common.util.sequence` |

注意：模块**没有** `src/main/resources`、**没有** `src/test`。新增需要资源/测试时自建。

## 三、编码约定（从现有代码归纳）

- **语言**：类/方法/字段名英文；Javadoc/注释中文。
- **工具类风格**：`final class` + 私有构造 + 全 static 方法，或 Lombok `@UtilityClass`（此时方法自动 static）。
- **Result 构造**：禁止 `new Result(...)` 散落业务代码，统一走 `ResultUtils.success/fail`。
- **抛异常**：业务错误一律 `throw new BizException(...)` 或 `enum implements ExceptionCode` 后 `throwException()`；不要在工具类里吞异常 `e.printStackTrace()`（历史代码有此问题，新代码应抛或日志后抛）。
- **i18n**：面向用户的消息用 `MessageUtils.getMessage(code, params)` 解析，`BizException` 构造时传 `Object[] params` 走 `String.format`。
- **Jackson 扩展**：自定义反序列化器优先实现 `ContextualDeserializer`（带 `JavaType`/`BeanProperty` 上下文），序列化器 `ContextualSerializer`，参考 `EntityWithLongIdProperty*`。
- **转换器**：String→目标类型用 `ConverterFactory`，带泛型判定用 `ConditionalGenericConverter`。
- **Lombok**：POJO `@Data`，工具 `@UtilityClass`，依赖注入构造 `@RequiredArgsConstructor`。
- **空值/集合**：序列化默认 `NON_NULL`；返回集合不返回 null，用 `Collections.emptyList()`。
- **长整型前端精度**：`Long` 一律经 `ToStringSerializer` 输出为字符串（已在 `SharpWebMvcConfigurer.register` 全局配置）。
- **命名**：类名驼峰；工具方法动宾结构（`toJson`/`toObject`/`getContentType`）；常量全大写下划线。

## 四、常见改动清单

### 加一个工具方法
- 找到合适的现有工具类（`JsonUtils`/`StringUtils`/`ClassUtils`...）追加 `static` 方法；若属新领域再新建 `com.rick.common.util.XxxUtils`，`@UtilityClass`。

### 加一个业务异常码
- 消费方模块自己建 `enum XxxExceptionCode implements ExceptionCode`（见 sharp-admin `ExceptionCodeEnum`）。**不要**在 sharp-common 里堆业务码。

### 加一个 Spring 转换器
- 在 `com.rick.common.http.convert` 新建 `Converter`/`ConverterFactory`/`ConditionalGenericConverter`；
- 若要全局生效，在 `SharpWebMvcConfigurer.addFormatters` 或子类 `converterFactories()` 注册。

### 加一个 Jackson 序列化/反序列化器
- 在 `com.rick.common.http.json.{serializer,deserializer}` 新建类；
- 全局生效：在 `SharpWebMvcConfigurer.register(ObjectMapper)` 的 `SimpleModule` 里 `addDeserializer/addSerializer`；字段级：`@JsonDeserialize(using=...)` / `@JsonSerialize(using=...)`。

### 加一个 Validation 注解
- `com.rick.common.validate.annotation` 建注解（`@Constraint(validatedBy=...)`）+ `com.rick.common.validate` 建 `ConstraintValidator` 实现。

### 改 MVC 配置基类行为
- 改 `SharpWebMvcConfigurer`：`addFormatters`/`addArgumentResolvers`/`register` 三处。**务必保持子类可覆盖**（如 `converterFactories()` 钩子）。

### 加一个公共常量
- 文件相关进 `FileConstants`；其它考虑新建 `com.rick.common.constant.XxxConstants`。

## 五、构建与测试

- 构建：在仓库根 `./mvn.sh -pl sharp-common -am install` 或 `mvn -pl sharp-common -am install`。
- 测试：模块当前**无测试源码**（`src/test` 不存在）。新增测试时放 `sharp-common/src/test/java/com/rick/common/...`，用 JUnit 4（依赖已在 pom，test scope）。
- 依赖版本：由父 POM `sharp-dependencies` 统一管理，**不要**在本模块 pom 写版本号。

## 六、陷阱与注意点

1. **不要新增 `spring.factories` / 自动配置**。消费方靠 `@Import` + `extends SharpWebMvcConfigurer` 装配，自动配置会与现有约定冲突、并把组件强加给不需要的模块。
2. **`SharpWebMvcConfigurer.register(ObjectMapper)` 是 `@Autowired(required=false)`**——容器没有 ObjectMapper 时不报错，但 Long→String、时间格式、Enum 反序列化都不会装。消费方必须确保有 ObjectMapper Bean（Spring Boot 默认有）。
3. **`@EnableResultWrapped` 默认未启用**（sharp-admin/sharp-demo 都注释掉了）。启用后所有 `@ResponseBody` 返回值会被 `ResultUtils.success(o)` 包裹，需要前端配合。开启前确认全项目 Controller 都期望这层包裹，或给不希望包裹的方法加 `@UnWrapped`。
4. **`HttpRequestDeviceUtils` 是历史代码**，UA 关键字陈旧；新需求用 `DeviceUtils.getCurrentDevice`。
5. **`ClassUtils.getFieldGenericClass(Field)` 单参版已 `@Deprecated`**，对 `T` 无法解析真实类型，必须传 `subClass`（双参版）。
6. **`Sequence` 默认 workerId 取本机 IP 末字节**，同 IP 多副本有冲突风险；要自定义请直接 `new Sequence(dataCenterId, workerId, ...)` 而不是改 `IdGenerator` 的单例。
7. **`ApiExceptionHandler` 对非 Ajax 请求会 `forward` 到 `/error` 或 `/error/index`**——消费方必须注册这两个 view/controller，否则 404。改异常处理流程时注意保留这条分支。
8. **`JsonUtils` 内部 `ObjectMapper` 与容器 `ObjectMapper` 不是同一个**：`JsonUtils` 的实例只装了 `JavaTimeModule` + `NON_NULL` + 忽略未知字段，**没有** `SharpWebMvcConfigurer` 装的 Long→String / EnumCustomize 反序列化。手动 `JsonUtils.toObject` 反序列化枚举/Long 时行为与 HTTP 入口不同，需注意。
9. **`BooleanPropertyDeserializer` 分支顺序**：先判 `isBoolean` 再判 `isArray`，疑似笔误；如需改请先确认现有调用方依赖。
10. **工具类里大量 `e.printStackTrace()`** 是历史包袱，新代码不要沿用，用 slf4j 或直接抛 `RuntimeException`。
11. **不要动** `com.rick.common.function.SInfoHelper`（package-private，被 `SFunction`/`SConsumer` 默认方法依赖），除非你同时改这两个接口。
12. **pom scope**：`javax.servlet-api`、`spring-*`、`validation-api` 都是 `provided`——新增依赖时，运行时由消费方（Spring Boot starter）提供的也用 `provided`，纯工具库（Jackson/Guava/commons）用 `compile`。
