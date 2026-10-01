# sharp-generator 架构

## 包结构

```
com.rick.generator
├── Generator                         # 核心生成器，唯一对外入口
├── config/
│   └── GeneratorServiceAutoConfiguration   # Spring Boot 自动装配，注册 Generator Bean
└── control/                          # 表单控件（HTML）生成子体系
    ├── ControlGeneratorManager       # 按 RenderTypeEnum 调度的工具类
    ├── AbstractControlGenerator      # 抽象基类：label/布局/通用控件 HTML
    ├── RenderTypeEnum                # THYMELEAF / VUE / REACT
    ├── FormLayoutEnum                # INLINE / HORIZONTAL / FLUID
    ├── DictCategoryEnum              # ENUM / DICT_VALUE（控件取值表达式分支）
    └── generator/
        ├── ThymeleafControlGenerator # 完整实现
        ├── VueControlGenerator       # 完整实现
        └── ReactControlGenerator     # 占位（TODO，全部返回注释）
```

## 关键抽象与数据流

生成过程围绕三段能力展开，但本模块**不引入独立模板引擎**，三段都内联在 `Generator` 中：

**1. 元数据读取**（依赖 `sharp-database`）
- `EntityDAOManager.getEntityDAO(entityClass)` → `EntityDAO` → `TableMeta`，拿到 `columnNameMap`、`fieldMap`、`columnNameToPropertyNameMap`、`getSortedColumns()`、`getTableName()`、`getTable().comment()`。
- `TableGenerator.createTable(entityClass)` 由 `sharp-database` 解析 `@Table`/`@Column` 生成 DDL 并执行（含 ID 列、各业务列、建表前后钩子）。
- 私有方法 `tableResolver(entityClass, Consumer<ResolverInfo>)` 是核心遍历器：跳过 `id`、`is_deleted` 列；对每列解析出 `Field`、列名、属性名、驼峰名、注释、字典类别（`DictCategoryEnum`）、字典类型值，组装成内部 `ResolverInfo`（`@Builder`，包级私有）回调给代码/控件生成逻辑。

**2. 模板渲染**
- Java 代码模板：纯字符串常量 + `String.replace("${PACKAGE_NAME}", rootPackageName).replace("${NAME}", entityName)`。三个模板方法 `daoCodeTemplate` / `serviceCodeTemplate` / `controllerCodeTemplate`，Report 模板 `reportCodeTemplate` 用 `StringBuilder` 拼 `QueryField`/`ReportColumn` 列表。
- 控件 HTML：`ControlGeneratorManager.generate(...)` 按 `RenderTypeEnum` 从 `registerMap` 取 `AbstractControlGenerator` 实例调用。基类负责 label、表单布局包裹（`form-group row` / `col-4`）、日期/多选/搜索下拉/文件等通用 HTML 与配套 JS 引导（以 HTML 注释输出）；其余 `cpnType` 委托子类。字段类型→控件类型映射在 `Generator.generatorHtml` 内完成。

**3. 文件写入**
- 代码文件：`writeCodeTemplate(overwrite, rootPackagePath, curPackageName, fileName, content)` → `mkdirPackage` 建子目录（`dao`/`service`/`controller`）→ `FileUtils.writeStringToFile(file, content, "UTF-8")`。覆盖策略：`!exists || overwrite`。
- Report 测试类：直接 `FileUtils.writeStringToFile(new File(reportTestPath, entityName + "Test.java"), ...)`，随后异步线程 `Runtime.exec("mvn test -Dtest={Entity}Test#testReport")`。
- HTML 控件：`Jsoup.parseBodyFragment(...)` 解析 + `outputSettings().indentAmount(4)` 美化，写入 `new File(controlPath, "control-{renderType}.html")`，**总是覆盖**。

### 一张表→一组文件的数据流

`execute(Student, rootPath, config)`：

```
Student.class
   │
   ├─(CREATE_TABLE≠false)─► TableGenerator.createTable → DB 表 t_student
   │
   ├─(GENERATOR_CODE null|true)
   │     ├─► dao/StudentDAO.java        (EntityCodeDAOImpl<Student, Long>)
   │     ├─► service/StudentService.java(BaseServiceImpl<StudentDAO, Student>)
   │     └─► controller/StudentController.java (仅 PROJECT+FORM_PAGE 非空)
   │            extends com.rick.{project}.common.api.BaseFormController<Student, StudentService>
   │            @RequestMapping("{camelEntityName}s")  // student→students
   │
   ├─(REPORT==true)─► {reportTestPath}/StudentTest.java  (@SpringBootTest, 注册 Report)
   │                   └─► 异步 mvn test -Dtest=StudentTest#testReport
   │
   └─(CONTROL_PATH 非空)─► {controlPath}/control-thymeleaf.html
                           {controlPath}/control-vue.html
                           {controlPath}/control-react.html
                          (每个字段按类型→CpnTypeEnum→对应控件 HTML)
```

## 对外依赖

- `sharp-database`：`TableGenerator`、`EntityDAOManager`/`EntityDAO`/`TableMeta`、`SimpleEntity`/`BaseCodeEntity`、`@Column`/`@Table`/`@Embedded`、`SharpDbConstants`、`Params`。**核心依赖**，元数据与建表都来自它。
- `sharp-meta`：`DictType`、`DictValue`、`DictTypeModel`，用于识别字典字段。
- `sharp-formflow`：`CpnTypeEnum`（控件类型枚举语义来源）、`CheckBox.parseValue`（多选回显）。
- `sharp-report`：生成的 Report 测试类引用 `Report`、`QueryField`、`ReportColumn`、`ReportService`、`HiddenReportColumn`、`SordEnum`、`AlignEnum`。
- `sharp-fileupload`：`com.rick.fileupload.client.support.Document`，用于识别 `List<Document>` → FILE 控件。
- `org.jsoup:jsoup`：HTML 控件片段的解析与格式化缩进。
- `commons-io` / `commons-lang3`：文件写入、字符串处理。

未使用 FreeMarker / Velocity / Thymeleaf 作为模板引擎（Thymeleaf 仅是生成物的渲染目标风格之一）。

## 扩展点

- **新增渲染风格**：新增 `RenderTypeEnum` 值 + 继承 `AbstractControlGenerator` 的实现 + 在 `ControlGeneratorManager` 静态块 `registerMap.put(...)` 注册。
- **自定义控件 HTML**：覆盖/新增子类的抽象 `generate(...)` 分支（按 `CpnTypeEnum`）。
- **改 Java 代码模板**：直接改 `Generator` 内 `daoCodeTemplate`/`serviceCodeTemplate`/`controllerCodeTemplate`/`reportCodeTemplate`。无外部 `.ftl`/`.vm` 文件可改。
- **改字段→控件映射**：改 `Generator.generatorHtml` 内的 `if/else if` 链。

注意：`ControlGeneratorManager.registerMap` 是包级 `static` 字段，无正式 SPI/Spring 注入机制，外部包扩展需反射（待确认是否有计划改造）。

## 配置与启动

- 自动装配：`META-INF/spring.factories` → `GeneratorServiceAutoConfiguration`，条件 `@ConditionalOnSingleCandidate(TableGenerator.class)` + `@AutoConfigureAfter(DataSourceAutoConfiguration.class)`。引入 `sharp-database` 且 DataSource 就绪后，`Generator` 自动成为 Bean。
- 无 `application.yml` 配置项；所有生成参数通过 `execute(...)` 的 `config` map 传入。
- 不提供 HTTP Controller；典型触发方式是业务项目里写一个 `@SpringBootTest` 用例调用 `generator.execute(...)`（参考 `sharp-admin` 的 `GeneratorTest`）。
