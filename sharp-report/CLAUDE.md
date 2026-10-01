# sharp-report 模块开发指南

## 模块边界

`sharp-report` 只负责"基于 `sys_report` 元数据的列表报表"：列定义、查询表单、分页查询、合计、Thymeleaf 渲染、Excel 导出。**不**包含：
- 报表元数据的 CRUD 管理界面（无 Controller 写接口，`saveOrUpdate/delete` 在 Service 层，靠测试或 `sharp-generator` 写入数据）。
- 图表/仪表盘/交叉表/主子报表等复杂形态。
- PDF/CSV 导出（仅 xlsx）。
- 底层 SQL 引擎与 POI 封装（在 `sharp-database2`/`sharp-excel`）。

跨模块边界：业务方通过实现 `ReportAdvice`、`ValueConverter`、提供 `templates/{tplName}.html` 来扩展，**不要**在本模块内塞业务逻辑。

## 目录约定

- 源码只在 `src/main/java/com/rick/report/{config,core}` 下。`core` 内按 `controller/service/dao/entity/model/support` 分包，职责单一，新增类请归入对应子包。
- 模板放 `src/main/resources/templates/`。默认 `list.html` 是模块自带基线，**不要**在业务模块直接改它；要自定义就新写 `templates/{tplName}.html` 并在 `Report.tplName` 指定。
- `sql/report.sql` 仅为参考 DDL，实际建表由 `sharp-database2` 自动完成，不要手动维护此文件。

## 编码约定

- 实体 `Report` 用 lombok `@SuperBuilder`，构造走 builder；列定义 `ReportColumn` 走构造器 + 链式 setter；**不要**给这些模型加新业务方法。
- `ReportAdvice` 实现类命名以 `XxxReportAdvice` 结尾，注册为 `@Component`，bean 名即首字母小写形式（如 `UserReportAdvice` → `userReportAdvice`），与 `Report.reportAdviceName` 对应。多个 Advice 之间可继承（见 `StudentReportAdvice extends OperatorReportAdvice`）。
- HTTP 入参统一用 `HttpServletRequestUtils.getParameterMap(request)` 拿 `Map<String,Object>`，分页参数 key 走 `PageModel.PARAM_*` 常量，不要硬编码 `"page"`/`"size"`。
- SQL 用命名参数 `:paramName`，由 `GridUtils.list` 绑定，**禁止**字符串拼接外部输入构造 SQL。
- 模板内遍历 `grid.rows`（`List<Object[]>`）按列序取值，不要假设列名；JSON 接口走 `gridMap` 才能按列名取。

## 常见改动清单

### 加一种导出格式（如 PDF）
1. 在 `ReportController`（或子类）加新端点，复用 `reportService.list(id, params)` 拿 `gridMap`/`gridArray`。
2. 自行引入 PDF 库（不在本模块依赖），用 `ReportColumn` 列表渲染。导出前若需自定义取数，在 `ReportAdvice.getMapExcelTable` 同思路加方法（但接口当前未抽象 PDF 入口，属硬扩展）。
3. 在 `list.html` 模板加对应导出按钮，或在业务方自定义模板里加。
验证：跑 `sharp-admin` 的 `ReportRenderTest`，确认导出文件可生成。

### 加一个数据集类型（自定义取数）
1. 业务模块实现 `ReportAdvice.fetchDataWithoutSql`，返回 `Grid<Map<String,Object>>` 并填 `summaryMap`。
2. 报表 `querySql` 留空，`reportAdviceName` 填该 bean 名。
3. 若导出也走自定义取数，再实现 `getMapExcelTable`。
验证：`reportService.list(id, params)` 不抛 `reportAdvice is needed!`，返回非空 `gridMap`。

### 加一列（字典翻译列）
1. 在 `Report.reportColumnList` 加 `new ReportColumn(name, label, context, Arrays.asList("dictConverter"))`，`context` 为字典 type。
2. 若 `ValueConverter` 不存在，在业务模块实现 `ValueConverter` 并注册 bean。
无需改本模块。

### 改默认列表页样式
不要改 `list.html`，写 `templates/{tplName}.html` 并设 `Report.tplName`。仅当确需全局改默认页才动 `list.html`。

## 构建测试命令

```bash
# 在仓库根目录
mvn -pl sharp-report -am clean install -DskipTests
# 跑依赖该模块的集成测试（sharp-admin 提供 ReportRenderTest / 各 *ReportAdvice）
mvn -pl sharp-admin -am test -Dtest=ReportRenderTest
```
模块自身**无**单元测试，验证依赖 `sharp-admin`/`sharp-demo` 的 `*Test`。

## 陷阱

- **SQL 注入面**：`querySql` 本身是开发期写死的（存库），不接受用户输入拼接；查询条件走 `:paramName` 命名参数绑定。但 `ReportService.saveOrUpdate` 仅校验 `delete from`，**不**拦截 `update`/`drop`/`insert`——若暴露报表管理后台写入接口，需自行加白名单校验。`summaryColumnNames` 解析依赖 `SELECT ... FROM` 子串正则切分，含子查询/别名带空格的复杂 SQL 可能匹配失败，配汇总时尽量保持 `SELECT` 区间简洁。
- **大数据量内存**：导出强制 `size=-1` 全量查，`GridUtils.list` 一次性把所有行载入内存，再 `MapExcelTable.write` 全量写 POI。**无流式/分批导出**。预计行数 > 几万时务必先做限制或自己实现 `getMapExcelTable` 走 `QueryRollingExportTable`（sharp-excel 提供）。
- **并发导出**：`ReportService.reportCacheMap` 是 `static HashMap` 非线程安全，并发 `findById` 写入可能丢；查询返回深拷贝避免元数据被改，但缓存本身有并发风险。
- **缓存失效**：`reportCacheMap` 无 TTL/失效机制，改 `sys_report` 后旧实例仍命中（除非进程重启）。生产改报表元数据后需重启或手动 `reportCacheMap.clear()`。
- **模板路径**：`Report.getUrl()` 返回 `/report/{id}/`（单数），与 Controller 实际路径 `/reports/{id}`（复数）不一致，直接用 `getUrl()` 会 404，待确认前端是否改写。前端链接一律用 `/reports/{id}`。
- **导出文件名时间戳**：用 `yyyyMMddHHmmSSS`，`SSS` 是毫秒且与大写 `HHmm` 混排，仅做唯一性用，不要当时间字段解析。
- **`ReportColumn.columnWidth` 单位**：模板里按像素用（`width: 80px`），Excel 里 `*50` 转 POI 列宽单位，两边数值语义不同，改时注意。
- **`tplName` 含 `ajax`** 触发 `index` 转发到 `ajaxIndex`，是字符串包含判断，命名模板名时小心误触发（如 `ajax_list`）。
