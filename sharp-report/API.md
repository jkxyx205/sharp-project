# sharp-report API

## 模块定位

`sharp-report` 是一个**轻量级通用报表封装层**，不是底层报表引擎。它不依赖 Jasper/BIRT/ureport，而是在 `sharp-database2`（SQL 分页查询）与 `sharp-excel`（POI 二次封装）之上，对"列表查询 + 列定义 + 查询表单 + Excel 导出"这一常见场景做约定式封装：把报表元数据（列、查询字段、SQL、汇总列）存入 `sys_report` 表，运行时按 HTTP 参数动态拼装分页 SQL，渲染 Thymeleaf 列表页或导出 `.xlsx`。

底层依赖：
- `sharp-database2`：`GridUtils.list` / `numericObject` 跑 SQL，`PageModel` 分页参数，`AbstractDialect` 提供 `summaryFun`/`removeOrders`。
- `sharp-excel`：`MapExcelTable` / `HtmlExcelTable` 生成 xlsx。
- `sharp-meta`（provided）：`ValueConverter` 用于列值转换（字典、布尔、日期等）。
- Spring MVC + Thymeleaf 渲染。

## 核心 public 入口

### 1. `ReportController` —— HTTP 入口
全名 `com.rick.report.core.controller.ReportController`，`@Controller @RequestMapping("reports")`。所有报表前端/导出都走这里。可被子类继承扩展（见 `sharp-admin` 中 `ReportLayoutController`）。

| 方法 | HTTP | 入参（路径/请求参数） | 出参 / 行为 |
|---|---|---|---|
| `index(Long id, Boolean readonly, Model, HttpServletRequest)` | `GET /reports/{id}` | `id` 报表主键；`readonly` 是否只读（默认 false）；其余 query 参数透传给查询 | 渲染 Thymeleaf 模板（默认 `list`，取 `report.tplName` 覆盖）。Model 含 `report`、`grid`(`Grid<Object[]>`，行是列值数组)、`summary`、`summaryIndex`、`pageInfo`、`params`。若 `tplName` 含 `ajax` 则转走 `ajaxIndex`。 |
| `ajaxIndex(Long id, Model, HttpServletRequest)` | `GET /reports/{id}/page` | `id` | 只加载报表元数据（不查数据），返回模板 `list`（或 `tplName`），供前端 ajax 异步拉数据。 |
| `value(Long id, HttpServletRequest)` | `GET /reports/{id}/json` | 路径 `id`；其余 query 即查询参数 | `Result<Grid<Map<String,Object>>>`，分页数据。`id` 列被强转为字符串。 |
| `valueAndSummary(Long id, HttpServletRequest)` | `GET /reports/{id}/json/summary` | 同上 | `{grid, summaryData}`，数据和汇总同时返回。 |
| `count(Long id, HttpServletRequest)` | `GET /reports/{id}/json/count` | 同上 | `Result<List<BigDecimal>>`，按 `summaryColumnNames` 顺序返回汇总值。 |
| `detailById(Long id, Long instanceId)` | `GET /reports/{id}/{instanceId}` | `instanceId` 行记录 id | `Map<String,Object>` 单行数据（强制 `size=-1` 不分页取首行）。 |
| `detailByIds(Long id, String instanceIds)` | `GET /reports/{id}/more/{instanceIds}` | `instanceIds` 逗号分隔的 id | `List<Map<String,Object>>` 多行。 |
| `export(Long id, HttpServletRequest, HttpServletResponse)` | `GET /reports/{id}/export` | `id`；其余 query 作为查询条件 | 直接写 `.xlsx` 到 response 流。文件名 = `report.name + yyyyMMddHHmmSSS + .xlsx`。 |
| `exportHtml(String name, String html, Integer[] columnsWidth, HttpServletRequest, HttpServletResponse)` | `POST /reports/html` | `name` 文件名；`html` 表格 HTML；`columnsWidth` 列宽数组（像素，按列序） | 用 `HtmlExcelTable` 把一段 HTML 表格转 xlsx 下载。 |

请求参数约定（来自 `PageModel`）：
- `page` 当前页，从 1 起；`size` 每页条数，`-1` 表示不分页（导出场景）；`sidx` 排序字段名；`sord` `desc`/`asc`。
- 查询字段名 = `QueryField.name`，多选/范围字段按模板约定（如 `name0`/`name1` 表示日期范围两端）。

### 2. `ReportService` —— Service 入口
全名 `com.rick.report.core.service.ReportService`，`@Service @Validated`。注入 `ReportDAO`、`Map<String, ValueConverter> valueConverterMap`、`Map<String, ReportAdvice> reportAdviceMap`、`AbstractDialect dialect`。

关键方法签名：

```java
// 创建/更新报表。校验 querySql 不能是 delete 语句
public Report saveOrUpdate(@Valid Report report)

// 按 id 删除报表
public int delete(Long id)

// 按 id 查询报表元数据。命中 reportCacheMap 进程缓存；不存在抛 BizException("Report not exists")
// 返回深拷贝副本（JsonUtils round-trip），调用方修改不影响缓存
public Optional<Report> findById(Long id)

// 核心：执行一次报表查询。requestMap 是可写的，会被合并默认参数（sidx/sord/size）
public ReportDTO list(long id, Map<String, Object> requestMap)

// 调用 reportAdvice.init(report) 完成运行前初始化
public void init(Report report)

// HTTP 导出入口：从 request 取参数，设 export=true，文件名时间戳，委托下面的 export
public void export(HttpServletRequest request, HttpServletResponse response, long id)

// 通用导出：自定义 OutputStream。若 reportAdvice 提供则用其 MapExcelTable；否则 list 查询后用默认 MapExcelTable
public void export(Map<String, Object> requestMap, Function<Report, OutputStream> osSupplier, Long id)
```

`list` 行为细节：
1. `findById` + `init`。
2. 合并参数：缺省 `sidx` 用 `report.sidx`；缺省 `sord` 用 `report.sord`；`pageable=false` 时强制 `size=-1`。
3. 若 `querySql` 为空 → 走 `fetchDataWithoutSql`，必须配 `ReportAdvice`，否则抛 `RuntimeException("reportAdvice is needed!")`。
4. 对 SQL 做大小写规整（`SELECT `/` FROM `/` WHERE `）。
5. 调 `reportAdvice.beforeQuery`（若有）。
6. 若配了 `summaryColumnNames`：从 `SELECT ... FROM` 区间解析列名，匹配出汇总列，用 `dialect.summaryFun(col)` 生成汇总 SQL（形如 `SELECT sum(col) FROM ...`）。
7. `GridUtils.list(querySql, requestMap)` 查分页数据 → `reportAdvice.beforeSetRow` → `convert(grid, report)` 把 `Map` 行转成 `Object[]` 行并跑 `ValueConverter` 链。
8. 跑汇总 SQL（`dialect.removeOrders` 去掉 ORDER BY）拿 `List<BigDecimal>` → `reportAdvice.combineSummaryList` → 组装 `summaryMap`。
9. `reportAdvice.beforeReturn(reportDTO)` → 返回 `ReportDTO`。

### 3. `Report` —— 报表实体/元数据
全名 `com.rick.report.core.entity.Report`，`@Table("sys_report")`，继承 `BaseCodeEntity<Long>`（带 `id`/`code`/`createId`/`createTime`/`updateId`/`updateTime`/`isDeleted`）。字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `name` | String | 报表名称（页面标题、导出文件名前缀）。`@NotNull` |
| `pageable` | Boolean | 是否分页，默认 true。false 时 list 强制 `size=-1` |
| `sidx` | String | 默认排序字段 |
| `sord` | `SordEnum`(DESC/ASC) | 默认排序方向 |
| `querySql` | String | 查询 SQL，命名参数 `:paramName`。可为空（此时必须配 ReportAdvice 自取数）。禁止 `delete from` |
| `summaryColumnNames` | String | 逗号分隔的汇总列名，需与 SQL 中列名一致。配了才会算合计行 |
| `reportColumnList` | `List<ReportColumn>` | 列定义（以 text 列存 JSON） |
| `queryFieldList` | `List<QueryField>` | 查询表单字段（以 text 列存 JSON） |
| `tplName` | String | 模板名。null 走默认 `list`；含 `ajax` 触发 `ajaxIndex` 分支 |
| `additionalInfo` | `Map<String,Object>` | 透传给模板的附加信息，常用 key 见 `ReportConstants` |
| `reportAdviceName` | String | 关联的 `ReportAdvice` Spring bean 名（如 `"userReportAdvice"`）。未配则无增强 |

派生方法：
- `getUrl()`：返回 `/report/{id}/?page=1&size=15&sidx=...&sord=...` 形式的预置访问 URL（注意路径前缀是 `/report/` 而非 `/reports/`，与 Controller 路径不同，待确认是否被前端改写）。
- `getVisibleColumnSize()`：未隐藏列数。

### 4. `ReportColumn` —— 列定义
全名 `com.rick.report.core.model.ReportColumn`。字段：`name`/`label`/`valueConverterNameList`(`List<String>`)/`context`/`sortable`/`columnWidth`/`hidden`/`align`(`AlignEnum`)/`tooltip`/`type`(`TypeEnum`)。`TypeEnum = TEXT|NUMERIC|DECIMAL|DATE|DATETIME`，JSON 序列化为小写。

构造器链（常用）：
```java
new ReportColumn(name, label)                                   // 默认不可排序
new ReportColumn(name, label, sortable)
new ReportColumn(name, label, context, valueConverterNameList) // context 传给 ValueConverter
new ReportColumn(name, label, sortable, context, valueConverterNameList,
                 columnWidth, align, hidden, tooltip, type)    // 全参
```
链式 setter：`setColumnWidth`/`setAlign`/`setType`/`setTooltip` 返回 `this`。

`HiddenReportColumn`：`hidden=true` 的列，常用于把 `id` 透传到前端但不显示。构造器 `new HiddenReportColumn("id")`。

### 5. `QueryField` —— 查询表单字段
全名 `com.rick.report.core.model.QueryField`。`Type = TEXT|SELECT|SEARCH_SELECT|CHECKBOX|DATE|GROUP_SELECT|MULTIPLE_SELECT|DATE_RANGE`。`extraData` 传给模板的 `<sp:dict key=...>` 字典 key（SELECT/MULTIPLE_SELECT 用）。`value`/`placeholder` 可链式 set。

### 6. `ReportAdvice` —— 报表增强扩展点（接口）
全名 `com.rick.report.core.service.ReportAdvice`。所有方法 default 空实现。注册为 Spring bean（`@Component`），bean 名即 `Report.reportAdviceName`。生命周期方法按 `list`/`export` 调用顺序：

```java
default void init(Report report)                                              // findById 之后、查询之前
default void beforeQuery(Report report, Map<String,Object> requestMap)       // SQL 拼装后、查询前，可改 requestMap
default void beforeSetRow(Report report, Grid<Map<String,Object>> grid, Map requestMap)  // 取数后、转 Object[] 前，可改 row Map
default void beforeReturn(ReportDTO reportDTO)                               // 最终返回前
default void combineSummaryList(Report report, List<BigDecimal> summaryList, Map requestMap, String conditionSql)  // 汇总算完但未组装前
default Grid<Map<String,Object>> fetchDataWithoutSql(Report report, Map requestMap, Map<String,BigDecimal> summaryMap)  // querySql 为空时取数
default Consumer<AbstractExportTable> beforeExportAndReturnBeforeToFileConsumer(Report report, MapExcelTable excelTable, Map requestMap)  // 导出前
default MapExcelTable getMapExcelTable(Report report, Map params, PageModel pageModel, List<Map<String,Object>> rows)  // 自定义导出 ExcelTable
```

### 7. `ReportDTO` —— 查询结果聚合
全名 `com.rick.report.core.model.ReportDTO`，`@Data @AllArgsConstructor`。字段：`report`、`gridArray`(`Grid<Object[]>`，模板用)、`gridMap`(`Grid<Map<String,Object>>`，json 接口用)、`summaryMap`(`Map<String,BigDecimal>`)。

### 8. `ReportConstants` —— additionalInfo 约定 key
`com.rick.report.core.support.ReportConstants`：`ADDITIONAL_ENDPOINT`/`ADDITIONAL_FORM_ID`/`ADDITIONAL_FORM_ACTION`/`ADDITIONAL_LINK`/`ADDITIONAL_CSS`/`ADDITIONAL_SCRIPT`/`ADDITIONAL_JS`/`ADDITIONAL_OPERATOR_BAR`/`ADDITIONAL_JS_OPERATOR_COLUMN`。

### 9. `ReportServiceAutoConfiguration` —— 自动装配
`com.rick.report.config.ReportServiceAutoConfiguration`，`@Configuration @ConditionalOnSingleCandidate(GridService.class) @AutoConfigureAfter(SharpDatabaseAutoConfiguration.class)`。内层 `ReportServiceConfiguration` `@ComponentScan("com.rick.report.core")`。即引入此模块 + 容器中存在 `GridService` 即自动启用，无需手动 `@EnableXxx`。

## 配置项

模块自身无 `application.yml` 配置项。行为由：
- `sys_report` 表行数据（运行期元数据，可热改）。
- `Report.reportAdviceName` 选择哪个 `ReportAdvice` bean。
- `ValueConverter` bean 注册（在 `sharp-meta` / 业务模块里定义，按 bean 名匹配 `ReportColumn.valueConverterNameList`）。
- `AbstractDialect` bean（由 `sharp-database2` 按数据库类型提供，决定 `summaryFun` 形态如 `SUM(col)`）。

## 使用示例

### 创建一张报表（Service 层）
```java
@Autowired private ReportService reportService;

reportService.saveOrUpdate(Report.builder()
    .code("t_book")
    .name("图书报表")
    .pageable(true)
    .sidx("title").sord(SordEnum.ASC)
    .querySql("SELECT t_book.id, t_book.title, t_person.name " +
              "FROM t_book, t_person " +
              "WHERE t_book.person_id = t_person.id " +
              "  AND t_book.title LIKE :title " +
              "  AND t_person.name = :name")
    .reportColumnList(Arrays.asList(
        new HiddenReportColumn("id"),
        new ReportColumn("title", "书名", true),
        new ReportColumn("name", "作者", true)))
    .queryFieldList(Arrays.asList(
        new QueryField("title", "书名"),
        new QueryField("name", "作者")))
    .build());
```

### 预览列表页（HTTP）
```
GET /reports/{id}?page=1&size=15&title=J&name=Rick
```
返回渲染后的 `list.html`（Thymeleaf）。前端通过 `?sidx=name&sord=desc` 排序、`?size=50` 改每页条数。

### 取 JSON 数据（带汇总）
```
GET /reports/{id}/json/summary?page=1&size=15
```
响应：`{ "code": ..., "data": { "grid": {...}, "summaryData": {...} } }`。

### 导出 Excel
```
GET /reports/{id}/export?title=J     # 文件名：图书报表20260101120000.xlsx
```
浏览器直接下载。导出强制 `size=-1` 全量，注意数据量。

### Service 编程式导出（自定义输出）
```java
reportService.export(paramsMap, report -> new FileOutputStream("/tmp/" + report.getName() + ".xlsx"), reportId);
```

### 自定义数据源（无 SQL，靠 ReportAdvice 取数）
```java
@Component
public class MyReportAdvice implements ReportAdvice {
    @Override
    public Grid<Map<String,Object>> fetchDataWithoutSql(Report report, Map<String,Object> requestMap,
                                                        Map<String,BigDecimal> summaryMap) {
        // 自行查数据源，填 summaryMap，返回 Grid
    }
}
```
报表配置 `querySql` 留空、`reportAdviceName="myReportAdvice"`。
