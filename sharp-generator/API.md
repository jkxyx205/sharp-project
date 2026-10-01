# sharp-generator API

## 模块定位

sharp-generator 是 sharp 体系的“脚手架/代码生成器”。给定一个实体类（`Class<? extends SimpleEntity>`），它根据实体的 `@Table`/`@Column`/`@DictType`/字段类型等元数据，一次性产出该实体的一整套 CRUD 落地物：

- 数据库表（`CREATE TABLE` DDL，由 `TableGenerator` 执行）
- Java 代码：`EntityDAO`、`Service`、`Controller`（继承目标项目的 `BaseFormController`）
- Report 测试类（向 `sharp-report` 注册一张报表）
- 表单 HTML 控件片段（Thymeleaf / Vue / React 三种渲染风格）

它不是运行时 HTTP 服务，而是通过注入 `Generator` Bean、在 `@SpringBootTest` 测试里调用 `generator.execute(...)` 触发的开发期工具。

## 核心 public 入口

### `com.rick.generator.Generator`

生成器核心类。构造需要一个 `com.rick.db.plugin.dao.core.TableGenerator`（由 `sharp-database` 提供，负责建表）。

```java
public Generator(TableGenerator tableGenerator)

public void execute(Class<? extends SimpleEntity> entityClass,
                    String rootPackagePath,
                    Map<String, Object> config) throws IOException
```

- `entityClass`：实体类，必须继承 `SimpleEntity`；若继承 `BaseCodeEntity` 则生成的 DAO 用 `EntityCodeDAOImpl`，否则用 `EntityDAOImpl`。
- `rootPackagePath`：生成物根目录的**文件系统绝对路径**（如 `.../src/main/java/com/rick/admin/module/student`）。DAO/Service/Controller 会在其下分别建 `dao`、`service`、`controller` 子目录。
- `config`：配置项 map，键取 `Generator` 上的常量（见下文“配置项”）。

行为顺序：
1. 建表：`config.get(CREATE_TABLE) != false` 时调用 `tableGenerator.createTable(entityClass)`。
2. 生成代码：`config.get(GENERATOR_CODE)` 为 `null` 或 `true` 时生成 DAO/Service/Controller；`GENERATOR_CODE == true` 时**覆盖**已存在文件，否则“不存在才创建”。Controller 仅当 `PROJECT` 与 `FORM_PAGE` 都非空时生成。
3. 生成 Report 测试类：`config.get(REPORT) == true` 时生成，写入 `REPORT_TEST_PATH` 目录，并异步执行 `mvn test -Dtest={Entity}Test#testReport`。
4. 生成 HTML 控件：`CONTROL_PATH` 非空时生成。`CONTROL_RENDER_TYPE` 指定则只生成该风格；未指定（见陷阱）则对 `RenderTypeEnum` 三个值各生成一份。HTML 文件**总是覆盖**。

#### 配置常量（`Generator` 内 `public static final String`）

| 常量 | 含义 | 取值/类型 |
|---|---|---|
| `CREATE_TABLE` | 是否建表 | `Boolean`；缺省视为 `true`，显式 `false` 跳过 |
| `GENERATOR_CODE` | 是否生成 Java 代码及覆盖策略 | `Boolean`；`null`→仅不存在时创建，`true`→覆盖，`false`→不生成 |
| `PROJECT` | 目标项目名（生成 Controller 时拼接包路径 `com.rick.{project}.common.api.BaseFormController` 等） | `String`，必填（生成 Controller 时） |
| `FORM_PAGE` | 编辑页面路径（Thymeleaf 模板路径，如 `demos/student/edit-thymeleaf`） | `String`；空则不生成 Controller |
| `REPORT` | 是否生成 Report 测试类 | `Boolean`，必须为 `true` 才生成 |
| `REPORT_TEST_PATH` | Report 测试类输出目录绝对路径 | `String`，`REPORT=true` 时必填 |
| `REPORT_TEST_PACKAGE` | Report 测试类 package | `String`，`REPORT=true` 时必填 |
| `CONTROL_PATH` | HTML 控件输出目录绝对路径 | `String`；空则不生成 HTML |
| `CONTROL_LABEL` | 控件是否带 `<label>` | `Boolean`；缺省 `false` |
| `CONTROL_RENDER_TYPE` | 指定单一渲染风格 | `RenderTypeEnum`；不传则生成全部三种（见陷阱） |
| `FORM_LAYOUT` | 表单布局 | `FormLayoutEnum`；缺省 `HORIZONTAL` |

另有三个内部使用的 `additionalInfo` 键（无需外部配置）：`ADDITIONAL_INFO_INPUT_PATTERN`、`ADDITIONAL_DICT_CATEGORY`、`ADDITIONAL_DICT_FIELD`。

### `com.rick.generator.control.ControlGeneratorManager`

HTML 控件生成调度器（`@UtilityClass`）。按 `RenderTypeEnum` 注册三个实现：`ThymeleafControlGenerator`、`VueControlGenerator`、`ReactControlGenerator`。

```java
public String generate(FormLayoutEnum formLayout,
                       CpnTypeEnum cpnType,
                       String camelEntityName,  // 实体驼峰名（如 student）
                       String name,             // 字段属性名
                       String label,            // 字段中文注释
                       String dictType,         // 字典/枚举类型名；无则 null
                       Map<String, Object> additionalInfo, // 含 dict_category、entity_field、input_pattern
                       RenderTypeEnum renderTypeEnum,
                       boolean ifGeneratorLabel) // 是否带 label
```

`Generator.generatorHtml` 内部按字段类型推断 `CpnTypeEnum` 并调用此方法。映射规则：`String`→`TEXT`+`TEXTAREA`；`LocalDate`→`DATE`；`Number`→`NUMBER_TEXT`；`Boolean`→`SWITCH`+`SINGLE_CHECKBOX`；字典单值→`SELECT`+`SEARCH_SELECT`+`RADIO`；字典集合→`MULTIPLE_SELECT`+`CHECKBOX`；`List<Document>`→`FILE`。

### `com.rick.generator.control.AbstractControlGenerator`

控件生成抽象基类。`generate(...)`（带 label 重载，`final`）负责 label + 表单布局包裹 + 日期/多选/搜索下拉/文件等通用 HTML 与注释（依赖 JS 引导代码以 HTML 注释形式给出）；其余 `cpnType` 委托子类 `generate(...)`（无 label 重载，抽象）。子类：

- `ThymeleafControlGenerator`：输出 Thymeleaf 属性（`th:value`、`th:each`、`sp:select` 等），完整支持。
- `VueControlGenerator`：输出 Vue 指令（`v-model="form.{name}"`、`v-for` 引用 `datasource.dicts.{type}`），完整支持。
- `ReactControlGenerator`：占位实现，全部 `cpnType` 返回 `<!-- {name} 没有找到模版-->`（待实现）。

### 枚举

- `RenderTypeEnum`：`THYMELEAF` / `VUE` / `REACT`。`getCode()` 返回枚举名。
- `FormLayoutEnum`：`INLINE`（内联：默认一行 3 列）/ `HORIZONTAL`（垂直）/ `FLUID`（流式）。`@JsonFormat(shape = OBJECT)` 序列化为对象。`getCode()` 返回枚举名，用于拼 `form-{code.toLowerCase()}` CSS class。
- `DictCategoryEnum`：`ENUM`（Java 枚举字段）/ `DICT_VALUE`（`DictValue`/`@DictType` 字段）。决定控件取值表达式：`DICT_VALUE` 走 `camelToDot(name)`，`ENUM` 走 `name + ".name"`。

### 配置类 `com.rick.generator.config.GeneratorServiceAutoConfiguration`

```java
@Configuration
@ConditionalOnSingleCandidate(TableGenerator.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class GeneratorServiceAutoConfiguration {
    @Bean
    public Generator initGenerator(TableGenerator tableGenerator) {
        return new Generator(tableGenerator);
    }
}
```

通过 `META-INF/spring.factories` 注册为自动配置。只要 classpath 有 `TableGenerator` Bean（即引入 `sharp-database` 并配置了 `DataSource`），`Generator` 就会自动注入，无需手动声明。

## 使用示例

最小调用（生成 Student 实体的全套代码，复刻自 `sharp-admin` 的 `GeneratorTest`）：

```java
@SpringBootTest
class GeneratorTest {
    @Autowired
    private Generator generator;

    @Test
    void testGenerator() throws IOException {
        generator.execute(Student.class,
            "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/java/com/rick/admin/module/student",
            Params.builder()
                .pv(Generator.PROJECT, "admin")
                .pv(Generator.FORM_PAGE, "demos/student/edit-thymeleaf")
                .pv(Generator.REPORT, true)
                .pv(Generator.REPORT_TEST_PATH, "/.../sharp-admin/src/test/java/com/rick/admin/demo")
                .pv(Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo")
                .pv(Generator.CONTROL_PATH, "/.../sharp-admin/src/main/resources/templates/demos/student")
                .pv(Generator.CONTROL_LABEL, true)
                .pv(Generator.FORM_LAYOUT, FormLayoutEnum.HORIZONTAL)
                // .pv(Generator.CONTROL_RENDER_TYPE, RenderTypeEnum.THYMELEAF) // 指定则只生成一种
                // .pv(Generator.GENERATOR_CODE, true) // true=覆盖已有 Java 文件；不传=不存在才建
                .build());
    }
}
```

`Params` 来自 `com.rick.db.service.support.Params`，是构造 `Map<String,Object>` 的简易 builder：`Params.builder(int expectedSize).pv(key, value)...build()`。

自定义控件：实现一个继承 `AbstractControlGenerator` 的子类，重写抽象 `generate(...)` 与 `renderType()`，并在 `ControlGeneratorManager.registerMap` 中注册（当前 `registerMap` 为包级 `static Map`，需在同包或反射注入；待确认是否有更正式的 SPI）。自定义模板：本模块**未使用** FreeMarker/Velocity，Java 代码模板以字符串拼接 + `String.replace("${NAME}", ...)` 实现，改动需直接编辑 `Generator` 内的 `xxxCodeTemplate()` 方法。
