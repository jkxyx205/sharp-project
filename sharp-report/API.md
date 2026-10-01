# sharp-report API

## 模块定位

**轻量报表封装层**（不是报表引擎）。架在：

- `sharp-database`（SQL 分页查询，`GridUtils` / `GridService` / `PageModel`）
- `sharp-excel`（POI 导出，`MapExcelTable` / `HtmlExcelTable`）
- `sharp-meta`（`ValueConverter` 字典/类型转换，provided 依赖）

无 Jasper / BIRT / ureport。核心能力：基于元数据（`sys_report` 表）驱动「列表查询 + 列定义 + 查询表单 + Excel 导出」。一次 `Report` 定义即一个 CRUD/查询页面，配合 Thymeleaf 模板 `list.html` 渲染。

`groupId: com.rick.report`，`artifactId: sharp-report`。

---

## 核心 public 入口

### ReportService

`com.rick.report.core.service.ReportService`（`@Service`）

报表元数据的 CRUD、查询、导出。内建进程缓存 `reportCacheMap`（见下「陷阱」）。

```java
// 创建或更新报表元数据；querySql 不允许包含 "delete from"
int saveOrUpdate(Report report);

// 按 id 删除报表元数据
int delete(Long id);

// 按 id 取报表；命中静态缓存 reportCacheMap，缺失则查库并写入缓存；找不到抛 BizException
Optional<Report> findById(Long id);

// 列表查询主入口。requestMap 既入又出（会被 mergeParams / ReportAdvice 修改）
// 1) init(report) 2) 合并默认分页/排序 3) 若 querySql 为空 → 走 ReportAdvice.fetchDataWithoutSql
// 4) 否则格式化 SQL 关键字 → reportAdvice.beforeQuery → GridUtils.list(querySql, requestMap)
// 5) reportAdvice.beforeSetRow → 计算 summary → convert 为 Object[] → reportAdvice.beforeReturn
ReportDTO list(long id, Map<String, Object> requestMap);

// 调用 reportAdvice.init(report)；list 流程前会先调用
void init(Report report);

// 导出到 Excel：从 request 取参数 → export(requestMap, supplier, id)
void export(HttpServletRequest request, HttpServletResponse response, long id);

// 通用导出：size=-1 一次性全量
// 若 ReportAdvice 提供 getMapExcelTable → 用其返回的 MapExcelTable
// 否则 list() 取数后 new MapExcelTable(visibleColumns, rows)
// sheet 名 = report.getName()；写到 osSupplier 提供的 OutputStream
void export(Map<String, Object> requestMap,
            Function<Report, OutputStream> osSupplier,
            Long id);
```

`requestMap` 约定 key（来自 `PageModel`）：
- `page` 当前页，默认 1
- `size` 每页条数，默认 15；`-1` 表示不分页全量
- `sidx` 排序字段、`sord` `DESC`/`ASC`
- 业务过滤参数：与 `querySql` 的命名参数（`:name`、`:title` 等）或 `queryFieldList` 的 `name` 对应

### ReportController

`com.rick.report.core.controller.ReportController`（`@Controller @RequestMapping("reports")`）

| 方法 | 路径 | 出参 / 行为 |
|---|---|---|
| `index` | `GET /reports/{id}` | 渲染列表页（默认视图 `list`，可被 `tplName` 覆盖）。返回 `report`/`grid`(Object[])/`summary`/`summaryIndex`/`params`/`pageInfo`。若 `tplName` 含 `ajax` 则转发到 `ajaxIndex` |
| `ajaxIndex` | `GET /reports/{id}/page` | 渲染 AJAX 列表壳页面，`summaryIndex` 置空 |
| `value` | `GET /reports/{id}/json` | `Result<Grid<Map>>`，行内 `id` 转字符串 |
| `valueAndSummary` | `GET /reports/{id}/json/summary` | `{grid, summaryData}` |
| `count` | `GET /reports/{id}/json/count` | `Result<List<BigDecimal>>` summary 值列表 |
| `detailById` | `GET /reports/{id}/{instanceId}` | 单行 `Map<String,Object>`（强制 `size=-1`） |
| `detailByIds` | `GET /reports/{id}/more/{instanceIds}` | 多行 List（`instanceIds` 逗号分隔） |
| `export` | `GET /reports/{id}/export` | 直接写 `xlsx` 到 response，文件名 `{name}{yyyyMMddHHmmSSS}.xlsx` |
| `exportHtml` | `POST /reports/html` | 入参 `name`/`html`/`columnsWidth[]`；用 `HtmlExcelTable.write` 把 HTML 表格转 xlsx 下载 |

### Report（实体 / 元数据）

`com.rick.report.core.entity.Report`（`@Table("sys_report")`，继承 `BaseCodeEntityWithLongId`）

| 字段 | 类型 | 说明 |
|---|---|---|
| `pageable` | `Boolean` | 是否分页，默认 `true`；`false` 时查询强制 `size=-1` |
| `sidx` | `String` | 默认排序字段 |
| `sord` | `SordEnum` | `DESC`/`ASC` |
| `name` | `String` | 报表名称（必填），同时作为导出 sheet 名 |
| `querySql` | `String` | 列表 SQL；命名参数 `:xxx`；可为空（走 `ReportAdvice.fetchDataWithoutSql`） |
| `summaryColumnNames` | `String` | 逗号分隔的汇总列名，触发 `SELECT CONVERT(sum(col), DECIMAL(20,3)) ...` |
| `reportColumnList` | `List<ReportColumn>` | 列定义（必填），存为 text JSON |
| `queryFieldList` | `List<QueryField>` | 查询表单字段 |
| `tplName` | `String` | 视图模板名；默认 `list`，可填 `tpl/list/ajax_list` 等 |
| `additionalInfo` | `Map<String,Object>` | 模板/前端扩展槽位（见 `ReportConstants`） |
| `reportAdviceName` | `String` | Spring Bean 名，对应一个 `ReportAdvice` 实现 |
| `code` | 继承自基类 | 建议与业务表名一致 |

便捷方法：`getUrl()` 生成 `/report/{id}/?page=1&size=15&sidx=..&sord=..`；`getVisibleColumnSize()`。

### ReportColumn

`com.rick.report.core.model.ReportColumn`

字段：`name` / `label` / `valueConverterNameList`(List) / `context`(传给 ValueConverter 的上下文，如字典 type) / `sortable` / `columnWidth` / `hidden` / `align`(`AlignEnum`) / `tooltip` / `type`(`TypeEnum`)。

`TypeEnum`: `TEXT` / `NUMERIC` / `DECIMAL` / `DATE` / `DATETIME`（JSON 序列化为小写）。
`AlignEnum`: `LEFT` / `CENTER` / `RIGHT`。

常用构造：
```java
new ReportColumn(name, label)
new ReportColumn(name, label, sortable)
new ReportColumn(name, label, valueConverterNameList)
new ReportColumn(name, label, sortable, context, valueConverterNameList)
new ReportColumn(name, label, sortable, context, valueConverterNameList,
                 columnWidth, align, hidden, tooltip, type)  // 全参
```
链式：`.setColumnWidth(int)` `.setAlign(AlignEnum)` `.setType(TypeEnum)` `.setTooltip(Boolean)`（返回 `this`）。

`HiddenReportColumn`：构造即 `hidden=true`，用于隐藏主键 `id` 等列。

`columnWidth` 单位为「格」，导出 Excel 时按 `*50` 转 POI 宽度（`MapTableColumn`）；类型为 DATE/DATETIME 时未显式设置则分别取 3000/5000。

### QueryField

`com.rick.report.core.model.QueryField`

字段：`name` / `label` / `type` / `extraData` / `value` / `placeholder`。

`Type`: `TEXT` / `SELECT` / `SEARCH_SELECT` / `CHECKBOX` / `DATE` / `GROUP_SELECT` / `MULTIPLE_SELECT` / `DATE_RANGE`。

`extraData` 语义随 `type` 变：`SELECT`/`MULTIPLE_SELECT` → 字典 type（配合 `<sp:dict>` 标签）；其他见前端模板。

### ReportAdvice（横切扩展点）

`com.rick.report.core.service.ReportAdvice`（接口，全部方法 default 空实现）

实现类注册为 Spring `@Component`，`Report.reportAdviceName` 填其 Bean 名即可绑定。`ReportService` 通过 `Map<String, ReportAdvice> reportAdviceMap`（Spring 自动注入）按名查找。

```java
default void init(Report report);
default void beforeQuery(Report report, Map<String,Object> requestMap);
default void beforeSetRow(Report report, List<Map<String,Object>> rows, Map<String,Object> requestMap);
default void beforeReturn(ReportDTO reportDTO);
default void combineSummaryList(Report report, List<BigDecimal> summaryList,
                                Map<String,Object> requestMap, String conditionSql);
default Grid<Map<String,Object>> fetchDataWithoutSql(Report report, Map<String,Object> requestMap,
                                                     Map<String,BigDecimal> summaryMap);  // 返回 null 走默认 SQL 流程
default Consumer<AbstractExportTable> beforeExportAndReturnBeforeToFileConsumer(
        Report report, MapExcelTable excelTable, Map<String,Object> requestMap);  // 返回 null 不后处理
default MapExcelTable getMapExcelTable(Report report, Map<String,Object> params,
                                       PageModel pageModel, List<Map<String,Object>> rows);  // 自定义导出表
```

典型用途（见 `sharp-admin`）：
- `OperatorReportAdvice`：`beforeSetRow` 批量回填 `createBy`/`updateBy` 用户名
- `LinkReportAdvice`：根据文件 `extension` 标记 `isImageType`
- `StudentReportAdvice`：`beforeSetRow` 解析 JSON 附件/头像；`init` 注入额外 JS

### ReportConstants

`com.rick.report.core.support.ReportConstants`：`additionalInfo` Map 的约定 key。
`ADDITIONAL_ENDPOINT`/`ADDITIONAL_FORM_ID`/`ADDITIONAL_FORM_ACTION`/`ADDITIONAL_LINK`/`ADDITIONAL_CSS`/`ADDITIONAL_SCRIPT`/`ADDITIONAL_JS`/`ADDITIONAL_OPERATOR_BAR`/`ADDITIONAL_JS_OPERATOR_COLUMN`。

### 配置与启动

`com.rick.report.config.ReportServiceAutoConfiguration`（`@Configuration`，注册在 `META-INF/spring.factories` 的 `EnableAutoConfiguration`）。

- `@ConditionalOnSingleCandidate(GridService.class)`：依赖 `sharp-database` 的 `GridService`。
- `@AutoConfigureAfter(GridServiceAutoConfiguration.class)`
- 内部 `ReportServiceConfiguration` `@ComponentScan("com.rick.report.core")`：扫描出 `ReportService` / `ReportDAO` / `ReportController`。

无独立配置项（无 `@ConfigurationProperties`）。运行需宿主提供：`GridService`（来自 `sharp-database`）、`ValueConverter` Bean（来自 `sharp-meta`，provided）、`spring-boot-starter-web`（provided）、Thymeleaf 视图解析。

### ReportDTO

`com.rick.report.core.model.ReportDTO`：`report` / `gridArray`(`Grid<Object[]>`，列顺序对齐 `reportColumnList`，已过 ValueConverter) / `gridMap`(`Grid<Map<String,Object>>`，原始) / `summaryMap`(`Map<String,BigDecimal>`)。

---

## ValueConverter（来自 sharp-meta，sharp-report 直接消费）

`com.rick.meta.dict.convert.ValueConverter<C,T>`：`String convert(C context, T value)`。
`ReportService` 在 `toObjectArrayListAndConvert` 中按 `ReportColumn.valueConverterNameList` 顺序串行调用，`context` 取 `ReportColumn.context`，找不到 Bean 抛 `BizException(400)`。

Spring Bean 名（见 `MetaServiceAutoConfiguration`）：`dictConverter` / `arrayDictConverter` / `boolConverter` / `sqlDateConverter` / `sqlTimestampConverter` / `localDateTimeConverter`。

---

## 使用示例

### 1. 注册一张报表（Service 调用）

```java
@Autowired ReportService reportService;

reportService.saveOrUpdate(Report.builder()
    .id(619541501440958464L)
    .code("t_book")
    .name("图书报表")
    .querySql("SELECT t_book.id, t_book.title, t_person.name, sys_dict.label \"sexLabel\" " +
              "FROM t_book, t_person LEFT JOIN sys_dict ON t_person.sex = sys_dict.name AND type='sex' " +
              "WHERE t_book.person_id = t_person.id " +
              "  AND t_book.title LIKE :title AND t_person.name = :name AND t_person.sex = :sex")
    .reportColumnList(Arrays.asList(
        new ReportColumn("title", "书名", true),
        new ReportColumn("name", "作者", true),
        new ReportColumn("sexLabel", "性别")))
    .queryFieldList(Arrays.asList(
        new QueryField("title", "书名"),
        new QueryField("name", "作者"),
        new QueryField("sex", "性别", QueryField.Type.SELECT, "sex")))
    .pageable(true)
    .sidx("title")
    .sord(SordEnum.ASC)
    .build());
```

### 2. 预览（最小 HTTP 调用）

```
GET /reports/{id}?page=1&size=15
GET /reports/{id}/json?page=1&size=15&title=%java%      # AJAX 取数
```

`/reports/{id}` 渲染整页；`/reports/{id}/page` 渲染 AJAX 壳页 + `/reports/{id}/json` 取数。

### 3. 导出 Excel

```
GET /reports/{id}/export?title=%java%                  # 走 ReportAdvice.getMapExcelTable 或默认全量
```

文件名形如 `图书报表202601011200000.xlsx`。

### 4. HTML 表格直导 xlsx

```
POST /reports/html   form: name=xxx&html=<table>...</table>&columnsWidth[]=100&columnsWidth[]=200
```

---

## 备注 / 待确认

- `export` 的 `osSupplier` 模板时间戳格式为 `yyyyMMddHHmmSSS`（毫秒占 3 位，注意是 `SSS` 紧跟 `mm`，非 `HHmmssSSS`，按源码字面）。
- `querySql` 格式化只做 `select`/`from`/`where` 关键字大小写规整（正则首处替换）；`summarySQL` 通过 `substringBetween(sql,"SELECT "," FROM")` 提取列名，要求 SQL 必须含 `SELECT ... FROM` 关键字且大小写不敏感匹配——复杂子查询/含 `from` 子句的 SQL 可能误切（**待确认**边界）。
- `validateNonDeleteSql` 仅拦截 `delete from` 模式，不防 `update`/`truncate`/`drop`，且对大小写不敏感（见 CLAUDE.md 陷阱）。
