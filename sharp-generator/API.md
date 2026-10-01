# sharp-generator API

## 模块定位

`com.rick:sharp-generator` 是 sharp 体系的"脚手架"模块。给定一个实体类（`Class<? extends EntityId>`），它通过反射读取实体元数据（表名、列、注释、字典类型、字段类型），自动产出：

1. 建表 DDL（委托 `sharp-database2` 的 `TableGenerator` 执行）；
2. 后端 Java 代码：`XxxDAO` / `XxxService` / `XxxController`；
3. 报表测试类 `XxxTest`（`sharp-report` 的 `Report` 定义），并异步触发 `mvn test` 运行；
4. 前端表单控件 HTML（Thymeleaf / Vue / React 三种渲染），输出到指定目录。

模板不是 `.ftl`/`.vm` 文件，而是内嵌在 Java 字符串里的占位符模板（`${PACKAGE_NAME}`、`${NAME}`），由代码做 `replace` 渲染。本模块**不提供 HTTP 接口**，只能以 Bean 注入后编程式调用 `Generator.execute(...)`。

## 核心 public 入口

### `com.rick.generator.Generator`

生成器核心类，Spring Bean（由 `GeneratorServiceAutoConfiguration` 注册）。构造依赖 `TableGenerator`（来自 sharp-database2）。

```java
public void execute(Class<? extends EntityId> entityClass,
                    String rootPackagePath,
                    Map<String, Object> config) throws IOException
```

- `entityClass`：实体类全限定类。必须继承 `EntityId`；若继承 `BaseCodeEntity` 则生成的 DAO 走 `EntityCodeDAOImpl`，否则走 `EntityDAOImpl`。包名取 `entityClass.getPackage().getName()` 去掉最后一段作为 `rootPackageName`（即 entity 的上一级包）。
- `rootPackagePath`：实体所在模块目录的文件系统绝对路径，DAO/Service/Controller 文件会写入其下的 `dao/`、`service/`、`controller/` 子目录。
- `config`：配置键值表，键为 `Generator` 上的 `public static final String` 常量（见下文"配置项"）。

行为顺序：
1. **建表**：除非 `config.get(CREATE_TABLE) == false`，否则 `tableGenerator.createTable(entityClass)`。
2. **生成代码**：若 `GENERATOR_CODE` 为 `null` 或 `true` 才进入。`overwrite = (GENERATOR_CODE == true)`，即只有显式传 `true` 才覆盖已存在的 java 文件；否则"不存在才写"。
   - `generatorDAO` → 写 `{rootPackagePath}/dao/{Name}DAO.java`
   - `generatorService` → 写 `{rootPackagePath}/service/{Name}Service.java`
   - `generatorBaseFormController` → 仅当 `PROJECT`、`FORM_PAGE` 均非空时写 `{rootPackagePath}/controller/{Name}Controller.java`；`FORM_PAGE` 为空则跳过。
3. **报表**：仅当 `config.get(REPORT) == true` 时执行。`REPORT_TEST_PATH`、`REPORT_TEST_PACKAGE` 必填。写 `{reportTestPath}/{Name}Test.java`，随后新起线程执行 `mvn test -Dtest={Name}Test#testReport`。
4. **HTML 控件**：若 `CONTROL_PATH` 为空则跳过。根据 `CONTROL_RENDER_TYPE` 生成对应渲染类型的控件；未指定则生成全部三种（见"陷阱"）。

#### 配置常量（`Generator` 上）

| 常量 | 默认行为 | 含义 |
|---|---|---|
| `CREATE_TABLE` | 未设=建表 | 设为 `false` 跳过建表 |
| `GENERATOR_CODE` | `null`=`true` | `true`=生成并强制覆盖代码；`false`=跳过代码生成；`null`=不存在才写 |
| `PROJECT` | — | 项目短名，用于 Controller 模板里 `com.rick.{project}.common.api.BaseFormController` 等包路径 |
| `FORM_PAGE` | — | 编辑页模板路径，如 `demos/student/edit-thymeleaf`；空则不生成 Controller |
| `REPORT` | `false` | `true` 才生成报表测试类 |
| `REPORT_TEST_PATH` | — | 测试类输出目录绝对路径 |
| `REPORT_TEST_PACKAGE` | — | 测试类 package |
| `CONTROL_PATH` | — | 控件 HTML 输出目录；空则不生成 HTML |
| `CONTROL_LABEL` | `false` | 是否在控件外包裹 `<label>` |
| `CONTROL_RENDER_TYPE` | `null`=全部 | `RenderTypeEnum`，指定只生成一种渲染 |
| `FORM_LAYOUT` | `HORIZONTAL` | `FormLayoutEnum`：`INLINE`/`HORIZONTAL`/`FLUID` |
| `ADDITIONAL_INFO_INPUT_PATTERN` | — | 内部用，把 `@Pattern` 正则注入到 input |
| `ADDITIONAL_DICT_CATEGORY` | — | 内部用，`DictCategoryEnum` |
| `ADDITIONAL_DICT_FIELD` | — | 内部用，当前 `Field` |

### `com.rick.generator.config.GeneratorServiceAutoConfiguration`

Spring Boot 自动配置。`@ConditionalOnSingleCandidate(TableGenerator.class)` + `@AutoConfigureAfter(DataSourceAutoConfiguration.class)`。仅当容器中存在唯一 `TableGenerator` 时注册 `Generator` Bean。通过 `META-INF/spring.factories` 与 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 双注册（兼容 Spring Boot 2.x/3.x）。

### `com.rick.generator.control.ControlGeneratorManager`（`@UtilityClass`）

控件生成调度器。内部用 `Map<RenderTypeEnum, AbstractControlGenerator>` 注册三种实现。

```java
public String generate(FormLayoutEnum formLayout, CpnTypeEnum cpnType,
                       String camelEntityName, String name, String label,
                       String dictType, Map<String, Object> additionalInfo,
                       RenderTypeEnum renderTypeEnum, boolean ifGeneratorLabel)
```

- `cpnType`：来自 `sharp-formflow` 的 `CpnTypeEnum`（TEXT/SELECT/RADIO/CHECKBOX/SWITCH/DATE/FILE/...）。
- `ifGeneratorLabel`：`true` 走父类 `generate(...,label,...)` 含 label 包裹；`false` 走无 label 版本。
- 返回渲染后的 HTML 片段字符串。

### `com.rick.generator.control.AbstractControlGenerator`

模板方法基类。`public final String generate(...含label...)` 负责 label/`form-group` 包裹、DATE/SELECT/FILE 等特殊控件的公共结构，其余 `cpnType` 委派抽象方法 `generate(...无label...)` 给子类。子类需实现 `renderType()` 返回其 `RenderTypeEnum`。

子类：
- `ThymeleafControlGenerator`：Thymeleaf 模板（`th:`、`sp:select` 标签），实现完整。
- `VueControlGenerator`：Vue 模板（`v-model`、`v-for`），实现完整。
- `ReactControlGenerator`：**TODO 未实现**，全部返回 `<!-- xxx 没有找到模版 -->`。

### 枚举

- `RenderTypeEnum`：`THYMELEAF` / `VUE` / `REACT`。
- `FormLayoutEnum`：`INLINE`（默认一行 3 列）/ `HORIZONTAL`（垂直）/ `FLUID`（流式）。
- `DictCategoryEnum`：`ENUM`（枚举字典）/ `DICT_VALUE`（`DictValue` 字典）。

## 使用示例

最小调用（注入 `Generator` Bean，参考 `sharp-admin` 的 `GeneratorTest`）：

```java
@Autowired
private Generator generator;

generator.execute(Student.class,
    "/abs/path/to/module/student",                 // rootPackagePath
    Maps.of(
        Generator.PROJECT, "admin",
        Generator.FORM_PAGE, "demos/student/edit-thymeleaf",
        Generator.REPORT, true,
        Generator.REPORT_TEST_PATH,  "/abs/path/to/src/test/java/demo",
        Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo",
        Generator.CONTROL_PATH, "/abs/path/to/resources/templates/demos/student",
        Generator.CONTROL_LABEL, true,
        Generator.FORM_LAYOUT, FormLayoutEnum.HORIZONTAL));
```

只生成 Vue 控件、不建表、不覆盖代码：

```java
generator.execute(Student.class, rootPath, Maps.of(
    Generator.CREATE_TABLE, false,
    Generator.GENERATOR_CODE, false,                 // 跳过 java 代码
    Generator.CONTROL_PATH, "/abs/.../student",
    Generator.CONTROL_LABEL, true,
    Generator.CONTROL_RENDER_TYPE, RenderTypeEnum.VUE));
```

自定义模板：本模块未提供外部模板文件扩展点。新增渲染类型需继承 `AbstractControlGenerator`、实现 `generate(...无label...)` 与 `renderType()`，并在 `ControlGeneratorManager` 的 `static` 块里 `registerMap.put(RenderTypeEnum.XXX, new XxxControlGenerator())`。
