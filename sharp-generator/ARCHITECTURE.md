# sharp-generator 架构

## 包结构

```
com.rick.generator
├── Generator                              核心生成器，唯一对外入口
├── config/
│   └── GeneratorServiceAutoConfiguration  Spring Boot 自动配置，注册 Generator Bean
└── control/
    ├── ControlGeneratorManager            控件生成调度器（@UtilityClass）
    ├── AbstractControlGenerator           模板方法基类（label 包裹 / form-group / 公共控件）
    ├── RenderTypeEnum                     THYMELEAF / VUE / REACT
    ├── FormLayoutEnum                     INLINE / HORIZONTAL / FLUID
    ├── DictCategoryEnum                   ENUM / DICT_VALUE
    └── generator/
        ├── ThymeleafControlGenerator      实现
        ├── VueControlGenerator            实现
        └── ReactControlGenerator          TODO 占位
```

源码 11 个 Java 文件，无 `src/test`，无 `.ftl`/`.vm`/`.html` 资源模板——模板均为 Java 字符串内联。

## 关键抽象与数据流

```
实体类 (Class<? extends EntityId>)
   │
   ▼  TableMetaResolver.resolve(entityClass)   [sharp-database2]
TableMeta（表名、列→属性映射、Field、Column.comment、id/logic_delete 列）
   │
   ├──► TableGenerator.createTable()            建表 DDL，jdbc 执行
   │
   ├──► 内联字符串模板 replace                   生成 DAO / Service / Controller.java
   │        writeCodeTemplate(overwrite, path, subPkg, fileName, content)
   │        overwrite = (GENERATOR_CODE == true)；否则"不存在才写"
   │
   ├──► tableResolver(entityClass, consumer)    逐列遍历，构造 ResolverInfo
   │        跳过 id 列、logic_delete 列
   │        识别字典：枚举 / @DictType / DictValue / Collection<枚举或DictValue>
   │        └─► 报表：拼 QueryField + ReportColumn → 写 XxxTest.java → mvn test
   │        └─► HTML：按字段类型选 CpnTypeEnum → ControlGeneratorManager.generate
   │                  → AbstractControlGenerator.generate（label/form-group 包裹）
   │                  → 子类 generate（按 RenderType 出 th:/v-model/...）
   │                  → Jsoup 格式化 → 写 control-{renderType}.html
```

`ResolverInfo`（`Generator` 私有 `@Builder` 内部类）是单列元数据载体：`tableMeta / column / field / columnName / propertyName / camelPropertyName / camelEntityName / comment / isDictValue / dictType / dictTypeValue / dictCategory`。报表与 HTML 共用这一遍历结果。

### 元数据来源（对外依赖）

- **sharp-database2**：`TableMetaResolver` 反射实体（`@Table`/`@Column`/`@Embedded`/`@ManyToMany`/`@Id` 等）产出 `TableMeta`；`TableGenerator` 据 `TableMeta` 拼 DDL 并 `jdbcTemplate.execute`。`EntityId` / `BaseCodeEntity` 是实体基类。
- **sharp-meta**：`DictType` 注解、`DictValue` 模型，决定字段是否字典、字典 type。
- **sharp-formflow**：`CpnTypeEnum`，控件类型语义。
- **sharp-fileupload**：`Document`，识别 `List<Document>` 字段为文件上传控件。
- **sharp-report**：报表生成的目标框架，`XxxTest` 中 `ReportService.saveOrUpdate`。
- **jsoup**：HTML 控件输出前格式化缩进。

## 扩展点

1. **新增渲染类型**：新增 `RenderTypeEnum` 值 → 新增 `AbstractControlGenerator` 子类 → 在 `ControlGeneratorManager` 静态块注册。当前 React 即占位待实现。
2. **新增控件类型**：在 `Generator.generatorHtml` 的字段类型分支里增加 `CpnTypeEnum` 调用；在三个 `*ControlGenerator.generate` 里增加对应 HTML 片段。
3. **自定义模板**：无外部模板文件机制；改模板即改 `Generator` 内的字符串字面量（`daoCodeTemplate`/`serviceCodeTemplate`/`controllerCodeTemplate`/`reportCodeTemplate`）。

## 模板清单（每个模板生成什么）

| 内联模板 | 产物 | 输出位置 |
|---|---|---|
| `daoCodeTemplate` | `XxxDAO extends Entity[Code]DAOImpl<Xxx, Long>`，`@Repository` | `{rootPackagePath}/dao/XxxDAO.java` |
| `serviceCodeTemplate` | `XxxService extends BaseServiceImpl<XxxDAO, Xxx>`，构造注入 DAO，`@Service` | `{rootPackagePath}/service/XxxService.java` |
| `controllerCodeTemplate` | `XxxController extends BaseFormController<Xxx, XxxService>`，`@RequestMapping("{camel}s")`，`GET {id}` | `{rootPackagePath}/controller/XxxController.java`（需 PROJECT+FORM_PAGE） |
| `reportCodeTemplate` | `XxxTest`：`@SpringBootTest`，`testReport()` 构造 `Report` 并 `saveOrUpdate` | `{reportTestPath}/XxxTest.java` |
| 控件 HTML（三套） | `<form>` + 逐字段控件，按字段类型生成多种候选控件 | `{controlPath}/control-{thymeleaf|vue|react}.html` |

字段→控件映射（`generatorHtml`）：String→TEXT+TEXTAREA；`LocalDate`→DATE；Number→NUMBER_TEXT；Boolean→SWITCH+SINGLE_CHECKBOX；字典标量→SELECT+SEARCH_SELECT+RADIO；字典集合→MULTIPLE_SELECT+CHECKBOX；`List<Document>`→FILE。

## 配置与启动

- 自动配置通过 `spring.factories` 与 `AutoConfiguration.imports` 双注册，引入依赖即在 Spring Boot 应用中注册 `Generator` Bean，前提是容器有唯一 `TableGenerator`（由 `sharp-database2` 的数据源自动配置提供，故需 `DataSourceAutoConfiguration` 已生效）。
- 无任何 `application.yml` 配置项；所有参数通过 `execute` 的 `config` Map 传入。
- 典型调用方为宿主模块的 `@SpringBootTest` 测试类（如 `sharp-admin` 的 `GeneratorTest`），在测试里建表→生成→插数据→校验。
