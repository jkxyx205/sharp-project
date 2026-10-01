# sharp-report CLAUDE.md

## 模块边界

- **不是报表引擎**，是 `sharp-database` + `sharp-excel` + `sharp-meta` 之上的元数据驱动封装层。
- 依赖 `sharp-database`（compile，注意当前主源码树用的是 `sharp-database`，不是 `sharp-database2`）、`sharp-excel`（compile）、`sharp-meta`（provided，ValueConverter 实现由宿主提供）、`spring-boot-starter-web`（provided）。
- 不直接做 SQL 执行 / POI 写入 / 字典查询，全部委托上述三模块；本模块只负责「报表定义 ↔ 列定义 ↔ 查询表单 ↔ 导出」的编排。

## 目录约定

- `src/main/java/com/rick/report/config/`：Spring Boot 自动配置（`ReportServiceAutoConfiguration`）。
- `src/main/java/com/rick/report/core/{controller,service,dao,entity,model,support}/`：核心代码。新增类放对应子包，**不要**在 `core` 根直接加类。
- `src/main/resources/META-INF/spring.factories`：唯一自动配置注册点。新增 AutoConfiguration 必须登记于此。
- `src/main/resources/templates/`：Thymeleaf 视图（默认 `list.html`，宿主可在 `tplName` 指定其他模板覆盖）。
- `src/main/resources/static/`：前端 jQuery 插件与第三方资源。
- `sql/report.sql`：`sys_report` 建表脚本（**与实体不完全对齐，参考用**）。

## 编码约定

- 实体类用 `@Table` + `@Column` 注解驱动建表/映射，列定义以 `Report` 实体为准。
- 列定义优先用 `ReportColumn` 链式构造；隐藏主键用 `HiddenReportColumn("id")`。
- 命名参数 SQL：`querySql` 用 `:name` 占位符，与 `queryFieldList` 的 `name` 对齐；HTTP 参数同名即绑定。
- `ValueConverter` 链按 `valueConverterNameList` 顺序串行调用，前一个输出为后一个输入；`context` 字段传字典 type 等上下文。
- `ReportAdvice` 实现类必须 `@Component` 且类名小驼峰作为 `reportAdviceName`；优先继承既有 `OperatorReportAdvice` 而非重写。
- 公共 key 放 `ReportConstants`，不要在 `additionalInfo` 里硬编码字符串。
- Controller 全部走 `@RequestMapping("reports")`，新增端点路径挂在 `/reports/...` 下；`ReportLayoutController`（宿主侧）展示如何子类化复用 `/reports/layout/...`。

## 常见改动清单

### 加一个 ReportAdvice

1. 宿主工程 `@Component` 实现 `ReportAdvice`（或 `extends OperatorReportAdvice`）。
2. 覆写需要的钩子（最常用 `beforeSetRow` 回填关联字段、`init` 注 `additionalInfo`）。
3. 报表元数据 `reportAdviceName = "<beanName>"`。无需改本模块。

### 加一种 ValueConverter

1. 在 `sharp-meta`（或宿主）`@Component class XxxConverter implements ValueConverter<C,T>`，Bean 名 = 小驼峰类名。
2. `new ReportColumn(name, label, false, context, Arrays.asList("xxxConverter"))`。
3. 无需改本模块；`ReportService` 通过 `Map<String, ValueConverter>` 自动发现。

### 加一列到既有报表

- 通过 `ReportService.saveOrUpdate` 整体覆盖 `reportColumnList`（无单列 PATCH 接口）；保持 `id` 字段不变以更新而非新增。
- 注意 `columnWidth` 单位是「格」，导出按 `*50` 转 POI 宽度。

### 加一个 HTTP 端点

- 在 `ReportController` 加 `@GetMapping`/`@PostMapping`；视图返回遵循 `StringUtils.defaultString(report.getTplName(), "list")`。
- 子类化场景（如 `ReportLayoutController`）通过 `super.index(...)` 复用。

## 构建测试命令

```bash
# 在 sharp-project 根目录
mvn -pl sharp-report -am clean install -DskipTests
mvn -pl sharp-report test
# 单测
mvn -pl sharp-report test -Dtest=ReportTest
```

宿主侧示例测试见 `sharp-demo` 的 `ReportTest`、`sharp-admin` 的 `HtmxTest`/`StudentTest`/`FormTagTest`（Spring Boot Test，通过 `ReportService.saveOrUpdate` 写元数据后浏览器访问 `/reports/{id}`）。

## 陷阱

### SQL 注入面
- `validateNonDeleteSql` 只拦 `delete from`，**不防** `update`/`truncate`/`drop`/`insert`，且对子查询伪装的 `delete from` 边界模糊。可写报表的人应视为可信角色；若开放给终端用户自行配 SQL，必须外加强校验。
- `querySql` 命名参数由 `GridService` 绑定（参数化），但列名拼到 `summarySQL` 用 `substringBetween(sql,"SELECT "," FROM")` + 字符串拼接——若列名/别名含恶意片段会被原样拼入汇总 SQL。
- 排序字段 `sidx` 直接进 ORDER BY（`SQLUtils.setOrderParams`），属典型 ORDER BY 注入面，需配合 `sortableColumns` 白名单（`GridUtils.list(..., String... sortableColumns)`）。

### 大数据量内存
- `export` 强制 `size=-1` 全量加载，再写 POI xlsx；`list` 内部 `convert` 会把 `Grid<Map>` 整体转成 `List<Object[]>`，内存 = 数据量 × 列数 × 对象开销。10 万行+ 报表导出易 OOM，无流式/分批写出。
- `beforeSetRow` 中常见「按 id 批量回填」（如 `OperatorReportAdvice` 一次查 sys_user IN(:ids)），行数极大时 IN 列表可能超限。
- 静态缓存 `reportCacheMap` 是 `HashMap`，多线程下结构不安全（虽然 `findById` 用 JSON 深拷贝返回，但缓存写入无锁）。

### 进程缓存无失效
- `ReportService.reportCacheMap` 是 `public static final HashMap`，`findById` 命中即返回 JSON 深拷贝。`saveOrUpdate`/`delete` **不清理**该缓存——更新报表元数据后必须重启进程或手动 `reportCacheMap.remove(id)`，否则旧定义持续生效。
- `init`（走 `ReportAdvice.init`）每次请求都跑，但 `findById` 命中后不会再读 DB，`ReportAdvice.init` 对 `Report` 的修改**不会**回写缓存（因为返回的是深拷贝）。

### 并发导出
- 同一报表多用户并发导出 → 多个 `size=-1` 全量查询同时压 DB；POI `XSSFWorkbook` 全内存对象并发更易 OOM。无导出并发限流。
- 文件名时间戳 `yyyyMMddHHmmSSS`，秒级 + 毫秒，同毫秒并发仍可能同名（attachment 头重复）。

### 模板路径
- `tplName` 为空时回退 `list`，要求宿主必须能解析 `list` 视图（本模块自带 `templates/list.html`，但宿主若覆盖视图目录可能找不到）。
- `tplName` 含 `ajax` 时 `index` 走 `ajaxIndex` 分支，返回 `list` 视图但 `summaryIndex=emptyList`，依赖前端 JS 走 `/json` 取数。
- `getUrl()` 拼的路径是 `/report/{id}/`（单数），与 Controller 的 `/reports/{id}`（复数）**不一致**——疑似历史遗留，外部调用以 Controller 路径为准（**待确认** `getUrl` 是否仍在使用）。

### 其它
- `export` 文件名时间戳格式 `yyyyMMddHHmmSSS`（`mm` 分 + `SSS` 毫秒，非 `ss` 秒），与一般 `yyyyMMddHHmmss` 不同，按源码字面理解。
- `sql/report.sql` 与 `Report` 实体字段不同步（实体多 `summaryColumnNames`/`tplName`/`additionalInfo`/`reportAdviceName`/`code` 列，SQL 多 `summary` bit 列）——以实体为准，建表用实体驱动而非该 SQL。
