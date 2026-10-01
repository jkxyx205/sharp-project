# sharp-report 架构

## 包结构

```
com.rick.report
├── config
│   └── ReportServiceAutoConfiguration        # Spring Boot 自动配置入口（spring.factories 注册）
│       └── ReportServiceConfiguration        # @ComponentScan("com.rick.report.core")
└── core
    ├── controller
    │   └── ReportController                 # HTTP 入口，/reports/**
    ├── service
    │   ├── ReportService                    # 报表元数据 CRUD + 查询 + 导出主流程
    │   └── ReportAdvice                     # 横切扩展点（接口，全 default）
    ├── dao
    │   └── ReportDAO                        # extends EntityCodeDAOImpl<Report,Long>，无自定义方法
    ├── entity
    │   └── Report                           # @Table("sys_report")
    ├── model
    │   ├── ReportColumn                     # 列定义（含 ValueConverter 链、类型、对齐、宽度等）
    │   ├── HiddenReportColumn               # hidden=true 的 ReportColumn 便捷构造
    │   ├── QueryField                       # 查询表单字段（8 种 type）
    │   ├── AlignEnum / SordEnum
    │   └── ReportDTO                        # 查询结果聚合：report + gridArray + gridMap + summaryMap
    └── support
        └── ReportConstants                   # additionalInfo Map 约定 key

src/main/resources
├── META-INF/spring.factories                # 注册 ReportServiceAutoConfiguration
├── templates/list.html                      # 默认 Thymeleaf 列表模板
└── static/                                  # 前端 jQuery 插件（table/pageTable/form/exportTable 等）
```

## 关键抽象关系

```
Report (sys_report 元数据)
  ├─ List<ReportColumn>     列定义 → 控制「行渲染顺序」「ValueConverter 链」「Excel 列宽/对齐」
  ├─ List<QueryField>      查询表单 → 由模板渲染为 <input>/<sp:dict>，参数名与 querySql :param 对齐
  ├─ querySql              列表 SQL（命名参数 :xxx） → 走 GridUtils.list
  ├─ summaryColumnNames    汇总列 → 自动拼 SELECT CONVERT(sum(col),DECIMAL(20,3)) FROM ... WHERE ...
  ├─ reportAdviceName ──> Spring Bean ─> ReportAdvice（生命周期回调）
  └─ additionalInfo Map ──> ReportConstants.key ─> 模板/前端读取（操作栏、endpoint、自定义 JS/CSS）

ReportService.list(id, params)
  ├─> ReportAdvice.init            (页面加载态)
  ├─> mergeParams                  (默认 sidx/sord/size)
  ├─> ReportAdvice.fetchDataWithoutSql   (querySql 为空时接管)
  ├─> ReportAdvice.beforeQuery
  ├─> GridUtils.list(querySql, params)    [sharp-database]
  ├─> ReportAdvice.beforeSetRow    (回填关联字段，如 createBy→姓名)
  ├─> summarySQL + GridUtils.numericObject (若配置 summaryColumnNames)
  ├─> ReportAdvice.combineSummaryList
  ├─> convert(Grid<Map> -> Grid<Object[]>)  (按 ReportColumn 顺序 + ValueConverter 链)
  └─> ReportAdvice.beforeReturn(ReportDTO)

ReportService.export(request, response, id)
  ├─> params.put("size", -1)             全量
  ├─> ReportAdvice.getMapExcelTable ?    自定义导出表（含 SQL 重查）
  │       else list() → new MapExcelTable(visibleColumns, rows)
  ├─> ReportAdvice.beforeExportAndReturnBeforeToFileConsumer   (返回 Consumer<AbstractExportTable> 后处理样式)
  └─> excelTable.write(OutputStream, consumer)                 [sharp-excel POI]
```

## 数据流（一次列表查询）

```
HTTP GET /reports/{id}?page=1&size=15&title=foo
  └─ ReportController.value(id, request)
       ├─ reportService.list(id, parameterMap)
       │    ├─ findById(id) → reportCacheMap 静态缓存命中或查 sys_report
       │    ├─ init / mergeParams / 格式化 SQL
       │    ├─ ReportAdvice.beforeQuery
       │    ├─ GridUtils.list(sql, params)  →  GridService.query  [sharp-database 分页]
       │    │       └─ PageModel(page,size,sidx,sord) + 命名参数绑定
       │    ├─ ReportAdvice.beforeSetRow(rows)
       │    ├─ summarySQL（若配置） → GridUtils.numericObject
       │    ├─ convert → Object[] + ValueConverter 链
       │    └─ ReportAdvice.beforeReturn
       └─ Result<Grid<Map>> 返回 JSON
```

## 数据流（一次 Excel 导出）

```
HTTP GET /reports/{id}/export?title=foo
  └─ ReportController.export(id, request, response)
       └─ reportService.export(request, response, id)
            ├─ HttpServletRequestUtils.getParameterMap
            ├─ Function<Report,OutputStream> supplier = HttpServletResponseUtils.getOutputStreamAsAttachment(name+".xlsx")
            └─ export(params, supplier, id)
                 ├─ findById(id) → report
                 ├─ ReportAdvice != null → pageModel.size=-1 → getMapExcelTable（自定义）
                 │      else params.size=-1 → list() → new MapExcelTable(visibleColumns, rows)
                 ├─ ReportAdvice.beforeExportAndReturnBeforeToFileConsumer → Consumer<AbstractExportTable>
                 ├─ book.setSheetName(0, report.name)
                 └─ excelTable.write(os, consumer)   [sharp-excel]
```

## 对外依赖

| 依赖 | 角色 | scope |
|---|---|---|
| `sharp-database` | `GridUtils`/`GridService`/`PageModel`/`QueryModel`/`Grid` SQL 分页查询；`EntityCodeDAOImpl` DAO 基类；`BaseCodeEntityWithLongId` 实体基类 | compile |
| `sharp-excel` | `MapExcelTable`/`HtmlExcelTable`/`MapTableColumn`/`AbstractExportTable` POI 导出 | compile |
| `sharp-meta` | `ValueConverter` 接口与实现（`dictConverter`/`arrayDictConverter`/`boolConverter`/`localDateTimeConverter`/`sqlDateConverter`/`sqlTimestampConverter`），通过 `Map<String,ValueConverter>` 注入 | provided |
| `spring-boot-starter-web` | Controller / Servlet | provided |
| `spring.factories` | `EnableAutoConfiguration=com.rick.report.config.ReportServiceAutoConfiguration` | — |

## 扩展点

### 自定义 ReportAdvice

1. `@Component` 实现 `ReportAdvice`（或继承既有实现如 `OperatorReportAdvice`）。
2. `Report.reportAdviceName = <beanName>`（默认小驼峰类名）。
3. 按需覆写 `init` / `beforeQuery` / `beforeSetRow` / `beforeReturn` / `combineSummaryList` / `fetchDataWithoutSql` / `beforeExportAndReturnBeforeToFileConsumer` / `getMapExcelTable`。

`ReportService` 通过 `Map<String, ReportAdvice>` 注入所有实现，按 `reportAdviceName` 取。`null` 安全（所有方法空实现）。

### 自定义 ValueConverter

1. 在 `sharp-meta` 侧（或宿主工程）`@Component class XxxConverter implements ValueConverter<C,T>`。
2. `new ReportColumn(name, label, false, context, Arrays.asList("xxxConverter"))` —— `valueConverterNameList` 可串联多个，按序对 `row[i]` 调用，前一个的输出是后一个的输入。

## 配置与启动

- `ReportServiceAutoConfiguration` 在 `spring.factories` 注册，宿主引入 `sharp-report` 即生效。
- 触发条件：classpath 存在单例 `GridService`（来自 `sharp-database`，需 `sharp-database` 自动配置已跑完）。
- `ReportService` / `ReportDAO` / `ReportController` 由 `@ComponentScan("com.rick.report.core")` 注册。
- 无 `@ConfigurationProperties`，无独立配置文件。所有运行参数随 `sys_report` 行数据与 HTTP 参数而来。

## 报表元数据表 sys_report

```sql
create table sys_report (
    id                  bigint not null primary key comment '主键',
    pageable            bit,                       -- 是否分页
    sidx                varchar(32),                -- 默认排序字段
    sord                varchar(16),                -- DESC/ASC
    name                varchar(32),                -- 报表名
    query_sql           text not null,              -- 列表 SQL
    summary             bit default b'0',           -- 历史字段，实际用 summary_column_names 待确认
    report_column_list  text,                       -- ReportColumn JSON
    query_field_list    text,                       -- QueryField JSON
    create_id           bigint,
    create_time         datetime,
    update_id           bigint,
    update_time         datetime,
    is_deleted          bit
) comment '报表';
```

注：DDL 与 `Report` 实体字段不完全一一对应——实体有 `summary_column_names`（`summaryColumnNames`，逗号分隔列名）、`tpl_name`、`additional_info`、`report_advice_name`、`code`（继承基类）等列，但 `sql/report.sql` 是早期建表脚本（含未使用的 `summary` bit 列、缺新列）。**以 `Report` 实体 `@Table/@Column` 为准**；`sql/report.sql` 仅供初始建表参考，待对齐（**待确认**）。

## 模板

`templates/list.html`：Thymeleaf 模板，渲染逻辑：
- `report.queryFieldList` → 查询表单（`TEXT` 输入框、`SELECT`/`MULTIPLE_SELECT` 用 `<sp:dict key=extraData>` 标签、`DATE_RANGE` 双日期输入）。
- `grid.rows` + `report.reportColumnList` → 表格，行序按 `reportColumnList`，`hidden=true` 的列不出现在 `<th>`/`<td>`。
- `summary` + `summaryIndex` → 表脚合计行。
- 分页条用 `pageInfo`（`PaginationHelper.limitPages`）。
- 前端 JS：`/js/table/jquery.table.js` 等，导出按钮调 `jquery.exportTable.js`。
- `additionalInfo` 注入操作栏、自定义 JS/CSS、`endpoint` 等（前端约定）。
