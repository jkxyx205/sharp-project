# sharp-report 架构

## 包结构

```
com.rick.report
├── config
│   └── ReportServiceAutoConfiguration     # Spring Boot 自动装配：依赖 GridService，扫描 com.rick.report.core
└── core
    ├── controller
    │   └── ReportController               # 唯一 HTTP 入口，@RequestMapping("reports")
    ├── service
    │   ├── ReportService                 # 核心查询/导出 Service
    │   └── ReportAdvice                   # 扩展点接口（默认实现全空）
    ├── dao
    │   └── ReportDAO                      # 继承 EntityCodeDAOImpl<Report,Long>，仅声明类型
    ├── entity
    │   └── Report                         # @Table("sys_report")，报表元数据
    ├── model
    │   ├── ReportDTO                      # 查询结果聚合：report + gridArray + gridMap + summaryMap
    │   ├── ReportColumn                   # 列定义
    │   ├── HiddenReportColumn             # 隐藏列（id 透传用）
    │   ├── QueryField                     # 查询表单字段
    │   ├── AlignEnum                      # LEFT/CENTER/RIGHT
    │   └── SordEnum                       # DESC/ASC
    └── support
        └── ReportConstants                # additionalInfo 的 key 约定
```

资源：
- `src/main/resources/templates/list.html` —— 默认 Thymeleaf 列表页（Bootstrap4 + jQuery + datepicker + multiple-select）。报表未配 `tplName` 时用它。
- `sql/report.sql` —— `sys_report` 建表 DDL（实际生产库由 `BaseCodeEntity`/`sharp-database2` 自动建表，此文件为参考）。

## 关键抽象：报表定义 ↔ 数据集 ↔ 导出器

```
┌─────────────┐   reportColumnList   ┌──────────────┐
│   Report    │ ───────────────────▶ │ ReportColumn │  列名/标签/转换器/宽度/对齐/类型/隐藏
│  (元数据)    │ ◀─────────────────── │              │
│             │   queryFieldList     │  QueryField   │  查询表单字段类型/字典 key
│             │ ───────────────────▶ │              │
│  querySql    │                     └──────────────┘
│  summaryCols │                            │
└─────┬───────┘                            │ ValueConverter 链（context + value）
      │                                    ▼
      │  ReportService.list(id, params)
      │                                    ┌──────────────┐
      │   ┌──── ReportAdvice（可选）──────▶│ 装饰/取数/汇总 │
      │   │                                └──────────────┘
      ▼   ▼
┌─────────────────┐   GridUtils.list(sql, params)   ┌──────────────────┐
│  ReportService  │ ───────────────────────────────▶│  sharp-database2 │
│   .list/export  │                                  │  分页 SQL + 计数 │
│                 │ ◀──────── Grid<Map<String,Object>> ──────────────────┘
│   convert → Grid<Object[]>
│   summaryMap (dialect.summaryFun + removeOrders)
└────────┬────────┘
         │
         │ export 流程：pageModel.size = -1 全量 → MapExcelTable
         ▼
   ┌──────────────────────┐
   │  sharp-excel          │  MapExcelTable / HtmlExcelTable → .xlsx
   │  (POI 二次封装)        │
   └──────────────────────┘
```

关系要点：
- `Report` 是**纯元数据**，持久化在 `sys_report`。`querySql` 可为空，为空时数据来源完全交给 `ReportAdvice.fetchDataWithoutSql`，把"报表定义"与"取数实现"解耦。
- `ReportColumn` 同时描述**查询输出列**和**导出列**——同一份列定义服务两条路径；`ValueConverter` 链在 `toObjectArrayListAndConvert` 中对两条路径都生效。
- `ReportDTO` 把同一份查询结果以两种结构暴露：`gridArray` 给 Thymeleaf 模板按列序遍历，`gridMap` 给 JSON 接口按列名访问。
- `ReportAdvice` 是横切扩展点，bean 名与 `Report.reportAdviceName` 匹配；一个报表配一个 Advice，多个 Advice 不能并存（按名取一个）。

## 数据流：一次 `GET /reports/{id}/export` 从查询到文件

1. `ReportController.export(id, request, response)` → `ReportService.export(request, response, id)`。
2. 取请求参数 → 设 `export=true` → 调重载 `export(requestMap, osSupplier, id)`。
3. `osSupplier`：`HttpServletResponseUtils.getOutputStreamAsAttachment`，文件名 `report.name + yyyyMMddHHmmSSS + ".xlsx"`，设好响应头。
4. `findById(id)` + 取 `ReportAdvice`（按 `reportAdviceName`）。
5. 优先走 `ReportAdvice.getMapExcelTable(report, params, pageModel(size=-1), null)` —— 让 Advice 自己查数并构造 `MapExcelTable`（适合多源/复杂 SQL）。
6. 否则走默认：`list(report.id, requestMap)`（此时 `size=-1` 全量） → `new MapExcelTable(convert(reportColumnList), gridMap.getRows())`。`convert` 跳过 `hidden` 列、按 `type`/`columnWidth` 推断列宽（DATE 3000、DATETIME 5000）。
7. `ReportAdvice.beforeExportAndReturnBeforeToFileConsumer` 返回可选 `Consumer<AbstractExportTable>`，用于写完数据后改样式/追加内容。
8. 设 sheet 名 = `report.getName()` → `excelTable.write(os, consumer)` 写 POI → 关闭流。

## 数据流：一次 `GET /reports/{id}`（页面预览）

1. `index(id, readonly, model, request)`：若 `tplName` 含 `ajax` → 转走 `ajaxIndex`（只取元数据，数据由前端二次拉 `/reports/{id}/json`）。
2. 否则 `getReportDTO`：`reportService.list(id, paramMap)` → 把每行 `id` 列转字符串 → 取 `gridArray`。
3. 算 `summaryIndex`：可见列中匹配 `summaryMap` key，得到合计行单元格偏移（`multiple` mode 多 1 列偏移，待确认语义）。
4. `PaginationHelper.limitPages` 计算页码导航条范围（PC 15 页、移动端 5 页）。
5. 渲染 `list.html`（或 `tplName`）。

## 对外依赖

- `sharp-database2`：`GridUtils`、`Grid`、`PageModel`、`QueryModel`、`AbstractDialect`、`EntityCodeDAOImpl`、`SharpDatabaseAutoConfiguration`。
- `sharp-excel`：`MapExcelTable`、`HtmlExcelTable`、`AbstractExportTable`、`MapTableColumn`、`AlignEnum`(excel 包内同名枚举)、`ExcelWriter`。
- `sharp-meta`(provided)：`ValueConverter` 接口及内置转换器（`dictConverter`/`arrayDictConverter`/`boolConverter`/`localDateTimeConverter` 等，bean 名在 `ReportColumn.valueConverterNameList` 中引用）。
- `sharp-common`：`HttpRequestUtils`/`HttpServletResponseUtils`/`Result`/`ResultUtils`/`BizException`/`JsonUtils`/`Maps`。
- `spring-boot-starter-web`(provided)、Thymeleaf、lombok、commons-lang3、commons-collections4、guava。

## 扩展点

### 自定义数据集类型（不写 SQL）
实现 `ReportAdvice.fetchDataWithoutSql`，从任意数据源（其他服务、ES、NoSQL）构造 `Grid<Map<String,Object>>`，同时填 `summaryMap`。报表 `querySql` 留空。`getMapExcelTable` 同理用于自定义导出取数。

### 自定义导出格式
当前内置仅 `.xlsx`（`MapExcelTable`）和前端 HTML→xlsx（`HtmlExcelTable`）。新增 PDF/CSV 路径：实现一个 `ReportAdvice.beforeExportAndReturnBeforeToFileConsumer` 在 `Consumer` 里拿到 `AbstractExportTable` 后自行渲染，或直接在 `ReportController` 子类新增端点（像 `sharp-admin` 的 `ReportLayoutController` 继承扩展）。模块未抽象"导出器"接口，新增格式属硬编码扩展，非开箱即用。

### 自定义列值转换
在业务模块实现 `ValueConverter` 注册为 bean，`ReportColumn.valueConverterNameList` 引用 bean 名即可，无需改本模块。

### 自定义模板
`Report.tplName` 指向自定义 Thymeleaf 模板名（须在 `templates/` 下可解析）。模板内可用变量：`report`、`grid`(`Grid<Object[]>`，行是按列序的 `Object[]`)、`summary`、`summaryIndex`、`id`、`params`、`pageInfo`、`readonly`。

## 配置与启动

- 无独立配置文件。引入依赖后，容器存在 `GridService`（来自 `sharp-database2`）即触发 `ReportServiceAutoConfiguration`。
- `sys_report` 表由 `sharp-database2` 基于 `@Table` 注解自动建/改（`BaseCodeEntity` 提供基础列）。报表行通常通过 `sharp-generator` 生成代码或测试 `ReportTest` 写入，无管理后台 CRUD（待确认是否有 admin 页面）。
- 缓存：`ReportService.reportCacheMap` 是进程级 `static Map`，`findById` 命中即返回深拷贝副本。报表元数据更新后需重启或手动清缓存（无失效机制，待确认）。

## 报表模板存放约定

- 默认模板：`src/main/resources/templates/list.html`（模块自带，被子模块 classpath 覆盖）。
- 自定义模板：放在使用方模块的 `src/main/resources/templates/{tplName}.html`，`Report.tplName` 填文件名（不含扩展名）。`tplName` 含 `ajax` 子串会触发异步加载分支。
