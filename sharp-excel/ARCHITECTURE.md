# sharp-excel 架构

## 1. 分层与包结构

```
com.rick.excel
├── core                         # 核心读写引擎
│   ├── ExcelWriter              # 写入门面（封装 XSSFWorkbook/XSSFSheet）
│   ├── ExcelReader              # 读入门面（静态，整本扫描）
│   ├── ExcelResultSet           # 读取行回调接口（before/rowMapper/after）
│   ├── CellValueExtractor       # 单元格值提取策略
│   ├── ExcelWriterHook          # 写入钩子（写前/写后干预）
│   ├── model                    # 写入数据模型
│   │   ├── BaseExcelCell<T>     # x, y, value, heightInPoints, style
│   │   ├── ExcelCell            # 单元格（含 rowSpan/colSpan）
│   │   ├── ExcelRow             # 行（含逐列 cellStyles）
│   │   ├── ExcelColumn          # 列
│   │   └── ExcelWriteSupport    # "A1" 地址 → ExcelCell/Row 工厂
│   └── support
│       └── ExcelUtils            # 样式拷贝、地址解析
├── table                        # 表格导出抽象
│   ├── AbstractExportTable<T>   # 表头+数据行渲染骨架
│   ├── GeneralExportTable       # Object[] 行
│   ├── MapExcelTable            # Map 行 + converter + sum
│   ├── QueryResultExportTable   # GridService 分页全量查询后导出
│   ├── QueryRollingExportTable  # SharpService+JdbcTemplate 滚动流式导出
│   └── HtmlExcelTable           # jsoup 解析 HTML <table> → xlsx
│       └── model
│           ├── TableColumn      # label/columnWidth/align
│           ├── MapTableColumn   # + name + converter
│           └── AlignEnum        # LEFT/CENTER/RIGHT
├── plugin                       # 与 sharp-database 集成
│   ├── ExportUtils              # 静态门面：sql→OutputStream
│   ├── ExportHttpServletRequestUtils  # Servlet 友好的导出
│   └── AbstractExportTableGridService # 继承 AbstractTableGridService，业务子类只填 SQL
└── config
    └── ExportGridServiceAutoConfiguration  # 把 GridService 注入 ExportUtils
```

## 2. 关键抽象与数据流

### 2.1 坐标式写入流（ExcelWriter）

```
调用方构造 ExcelCell/ExcelRow/ExcelColumn (1 基 x,y)
   │
   ▼
ExcelWriter.writeCell(ecell, hook?)
   ├── 1. CellUtil.getRow(y-1) / createRow  → XSSFRow
   ├── 2. row.getCell(x-1) / createCell(convertCellType(value))   ← 按值类型选 CellType
   ├── 3. 行高取 max(current, ecell.heightInPoints)
   ├── 4. 若 ecell.style != null → cell.setCellStyle
   ├── 5. hook?.setCellValue(cell, value) 返回 true 则跳过；否则 setCellValue(cell, value)
   │      └─ setCellValue 按 Java 类型分支：Date/LocalDate/LocalDateTime 看 cell 类型决定写原生还是字符串
   │                                                   BigDecimal→double
   │                                                   Long/Integer 超 999_999_999_999_999L→字符串
   ├── 6. rowSpan/colSpan != 1 → addMergedRegion + setRegionStyle(把区域内所有 cell 套同一样式)
   └── 7. hook?.afterCellWrite(ecell, cell)
   │
   ▼
toFile(OutputStream) → book.write(os) + book.close()
```

行/列写入 (`writeRow`/`writeColumn`) 是 `writeCell` 的循环包装；`insertAndWriteRow*` 额外调用 `Sheet.shiftRows` 腾出空行，可选地复制下方行样式或整行 `copyRows`（含公式/格式）。

### 2.2 表格导出流（AbstractExportTable）

```
AbstractExportTable(cols, rows)
   │
   ▼ write(OutputStream, beforeToFileConsumer?)
   ├── writeColumns()
   │     · 遍历 cols：getActiveSheet().setColumnWidth(i, col.columnWidth)
   │     · 构造表头 ExcelRow(1,1, labels)，逐列 cellStyles 按 align 设置
   │     · columnStyle 优先，否则 defaultColumnStyle（灰底加粗居中）
   │     · excelWriter.writeRow(row)
   ├── writeRows()                                   ← 子类可 override
   │     · 默认实现：遍历 rows，调 resolve(row) → Object[]
   │       构造 ExcelRow(1, i+2, data)，套 rowStyle 或 defaultRowStyle
   │     · MapExcelTable.resolve：按 MapTableColumn.name 从 Map 取值，过 converter
   │     · QueryResultExportTable.writeRows：先 gridService.query(sql,pageModel,params) 拿 rows 再 super.writeRows
   │     · QueryRollingExportTable.writeRows：直接 SharpService.query + JdbcTemplate 回调，逐行 writeRow（不缓存全量）
   ├── beforeToFileConsumer?.accept(this)            ← 业务侧补合计行/图片/重命名 Sheet
   └── excelWriter.toFile(os)
```

### 2.3 HTML 表格导出流（HtmlExcelTable）

```
html String
   │ jsoup.parse → select("tr") → 每个 tr 选 td,th
   ▼
逐 td：text() + rowspan/colspan → ExcelCell(index+1, i+1, text, rowSpan, colSpan)
   · cellStyle 优先用户设置，否则 getDefaultStyle（黑边框+居中）
   · excelWriter.writeCell(cell)  ← 复用 ExcelWriter 的合并逻辑
   · index += colSpan
   ▼
设列宽（默认 4096）→ toFile
```

### 2.4 SQL 直出流（plugin）

```
ExportHttpServletRequestUtils.export(sql, req, extendParams, resp, fileName, cols)
   ├── HttpServletRequestUtils.getParameterMap(req, extendParams) → params
   ├── QueryModel.of(params).getPageModel().setSize(-1)   ← 关闭分页，全量
   └── ExportUtils.export(sql, params, outputStream, cols)
         └── new QueryResultExportTable(gridService, sql, pageModel, params, cols)
               .write(outputStream)
         （outputStream 由 HttpServletResponseUtils.getOutputStreamAsAttachment 设置 attachment 头）
```

`AbstractExportTableGridService` 把上面流程再封装：子类只需实现 `getListSQL()`（来自 `AbstractTableGridService`），即可一行 `export(req, resp, fileName, cols)`。

## 3. 对外依赖（第三方库用途）

| 依赖 | 作用域 | 用途 |
|---|---|---|
| `org.apache.poi:poi` + `poi-ooxml` | compile | XSSF 实现 `.xlsx` 读写；`XSSFWorkbook`/`XSSFSheet`/`XSSFCellStyle`/`XSSFClientAnchor` 等 |
| `com.rick.common:sharp-common` | compile | `Time2StringUtils` 日期格式化、`HttpServletRequestUtils`/`HttpServletResponseUtils`（servlet 工具） |
| `com.rick.db:sharp-database` | provided | `GridService`/`SharpService`/`QueryModel`/`PageModel`/`AbstractTableGridService`，仅 plugin 与 Query*ExportTable 使用 |
| `org.jsoup:jsoup` | provided | 仅 `HtmlExcelTable` 解析 HTML |
| `javax.servlet:javax.servlet-api` | provided | 仅 `ExportHttpServletRequestUtils` / `AbstractExportTableGridService` |
| `junit:junit` | test | 单元测试（无 Spring 上下文） |

## 4. 扩展点

| 扩展点 | 形态 | 用途 |
|---|---|---|
| `ExcelWriterHook` | `@FunctionalInterface`，传入 `writeCell`/`writeRow`/`insertAndWriteRow*` | 写前替换值、写后改样式（条件染色、富文本） |
| `CellValueExtractor` | 接口，传入 `ExcelReader.readExcelContent` | 自定义单元格原始值→Java 对象的映射（如公式结果、自定义日期格式） |
| `ExcelResultSet` | 接口，`rowMapper` 返回 false 终止 | 行级监听：聚合、过滤、写入下游 |
| `AbstractExportTable.resolve` | protected 抽象 | 自定义行数据→Object[] 的映射；可继承 `AbstractExportTable` 实现任意行类型导出 |
| `AbstractExportTable.write(os, Consumer<AbstractExportTable>)` | 写入前回调 | 在表头+数据行之后、`toFile` 之前补写合计行、图片、Sheet 重命名 |
| `MapTableColumn.converter` | `Function` | 单列值转换（如 BigDecimal→"¥1,234.00"） |
| `AbstractExportTableGridService` | 抽象类，业务继承 | 只填 `getListSQL()`，复用导出链路 |
| `TableColumn.align` / `columnStyle` / `rowStyle` | setter | 表头/行对齐与样式覆盖 |

## 5. 配置与启动

- Spring Boot 自动配置：`META-INF/spring.factories` 注册 `ExportGridServiceAutoConfiguration`。
  - 触发条件：`@ConditionalOnSingleCandidate(GridService.class)`。
  - 顺序：`@AutoConfigureAfter(GridServiceAutoConfiguration.class)`。
  - 行为：通过 `@Autowired` 方法把唯一 `GridService` 注入 `ExportUtils` 的静态字段 `GRID_SERVICE`。
- 无 `GridService`（如纯 POI 使用场景）时：自动配置不生效，`ExportUtils`/`QueryResultExportTable`/`AbstractExportTableGridService` 不可用；`ExcelWriter`/`ExcelReader`/`AbstractExportTable`/`HtmlExcelTable` 仍可独立工作。

## 6. 设计要点与取舍

- **1 基坐标**：所有 `ExcelCell`/`ExcelRow`/`ExcelColumn` 的 `x`、`y` 从 1 开始，内部转 `y-1`/`x-1` 调 POI；这与 Excel 习惯一致但容易和 POI 的 0 基坐标混淆。
- **日期写法分叉**：`setCellValue` 会先看目标 cell 的 CellType——若是 STRING 则写格式化字符串，否则写原生日期。模板填充时需确保模板单元格已是文本格式才会得到可读日期，否则得到 Excel 序列号（待确认）。
- **大数保护**：`Long`/`Integer` 超 double 安全整数范围（`999_999_999_999_999L`）自动转字符串，避免 ID 被科学计数法截断。
- **流式 vs 全量**：`QueryResultExportTable` 一次性 `query` 全量到内存；`QueryRollingExportTable` 走 `JdbcTemplate.query` 回调逐行写，更适合大结果集，但仍依赖 POI XSSF 全内存模型，最终 xlsx 体积受堆大小限制。
- **模板填充约定**：基于已有 xlsx 字节流构造 `new XSSFWorkbook(new ByteArrayInputStream(bytes))`，避免直接读文件流导致原文件被锁。
