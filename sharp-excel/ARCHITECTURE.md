# sharp-excel 架构

> 一句话：在 Apache POI XSSF 之上做了一层"坐标驱动 + 表格抽象"的封装，把单元格写入、表格导出、HTML 表格转换、SQL 查询结果导出统一收敛到 `ExcelWriter` 这一个出口。

## 1. 包结构

```
com.rick.excel
├── core
│   ├── ExcelWriter              # 写入器（核心，持有 XSSFWorkbook/XSSFSheet）
│   ├── ExcelReader              # 读取器（静态方法）
│   ├── ExcelResultSet           # 行回调接口（读）
│   ├── CellValueExtractor       # 单元格值提取接口（读）
│   ├── ExcelWriterHook          # 单元格写后钩子（写扩展点）
│   ├── model
│   │   ├── BaseExcelCell<T>     # x/y/value/heightInPoints/style 基类
│   │   ├── ExcelCell            # 单元格（含 rowSpan/colSpan）
│   │   ├── ExcelRow             # 行（Object[] + cellStyles[]）
│   │   ├── ExcelColumn          # 列（Object[]）
│   │   └── ExcelWriteSupport    # @UtilityClass：用 "A1" 地址构造 cell/row
│   └── support
│       └── ExcelUtils           # @UtilityClass：样式复制、地址解析
├── table
│   ├── AbstractExportTable<T>   # 表格导出抽象基类（列+数据行 → ExcelWriter）
│   ├── GeneralExportTable       # Object[] 行
│   ├── MapExcelTable            # Map<String,Object> 行（带 converter）
│   ├── QueryResultExportTable   # GridService 分页查询 → MapExcelTable
│   ├── QueryRollingExportTable  # TableDAO ResultSet 行级流式写入（大表）
│   ├── HtmlExcelTable           # HTML <table> → Excel（jsoup 解析）
│   └── model
│       ├── TableColumn          # label/columnWidth/align
│       ├── MapTableColumn       # 增加 name/converter
│       └── AlignEnum            # LEFT/CENTER/RIGHT
├── plugin
│   ├── ExportUtils              # 静态 export(sql, params, os, columns)，依赖 GridService
│   ├── ExportHttpServletRequestUtils  # Servlet 一行式导出
│   └── AbstractExportTableGridService # 继承 sharp-database2 的 AbstractTableGridService
└── config
    └── ExportGridServiceAutoConfiguration  # Spring Boot 自动装配 GridService → ExportUtils
```

资源：`META-INF/spring.factories` + `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 双注册（同时兼容 Spring Boot 2.x / 3.x）。

## 2. 关键抽象与数据流

### 2.1 写入主路径

```
调用方
  └─ new ExcelWriter() ── 创建 XSSFWorkbook + 首个 XSSFSheet
       ├─ writeCell(ExcelCell)
       │     ├─ 行列计算：x-1 / y-1（转 0-based）
       │     ├─ CellUtil.getRow / createRow → createCell(convertCellType(value))
       │     ├─ 应用 ExcelCell.style、行高
       │     ├─ setCellValue(cell, value)  ← 按类型分发：日期/BigDecimal/Long/Number/String
       │     ├─ 若 rowSpan/colSpan != 1 → sheet.addMergedRegion + setRegionStyle
       │     └─ ExcelWriterHook.afterCellWrite(ecell, cell)  ← 扩展点
       ├─ writeRow(ExcelRow)     → 逐格构造 ExcelCell 调 writeCell
       ├─ writeColumn(ExcelColumn) → 逐格构造 ExcelCell 调 writeCell
       ├─ insertAndWriteRow*(...) → shiftRows 插入空行 → writeRow
       └─ toFile(os)             → book.write(os) + book.close()
```

`writeCell` 是所有写路径的收敛点；`ExcelWriterHook` 是唯一的写后扩展点。

### 2.2 表格导出主路径

```
AbstractExportTable.write(os)
  ├─ writeColumns()                # 第 1 行写表头：每个 TableColumn.label 一列
  │     ├─ setColumnWidth(i, columnWidth)
  │     ├─ 按 align 生成各列 XSSFCellStyle
  │     └─ excelWriter.writeRow(表头 ExcelRow)  ← columnStyle 覆盖 defaultColumnStyle
  ├─ writeRows()                   # 第 2 行起写数据
  │     ├─ for each row: Object[] = resolve(row)   ← 子类实现
  │     │     ├─ GeneralExportTable:    原样返回 Object[]
  │     │     ├─ MapExcelTable:         按 MapTableColumn.name 取值 + 可选 converter
  │     │     ├─ QueryResultExportTable: 重写 writeRows()，先 gridService.query(...) 拉数据再 super.writeRows()
  │     │     └─ QueryRollingExportTable: 重写 writeRows()，在 ResultSet 回调里逐行 excelWriter.writeRow
  │     └─ excelWriter.writeRow(ExcelRow)  ← rowStyle 覆盖 defaultRowStyle
  ├─ beforeToFileConsumer.accept(this)   ← 扩展点（Sharp-report 用它做合计行/重命名 sheet）
  └─ excelWriter.toFile(os)
```

### 2.3 HTML 转换路径

```
HtmlExcelTable.write(html, os)
  ├─ Jsoup.parse(html) → doc.select("tr")
  ├─ for each tr: select("td, th")
  │     ├─ 解析 rowspan/colspan 属性
  │     ├─ ExcelCell(x=index+1, y=i+1, td.text(), rowSpan, colSpan)
  │     └─ excelWriter.writeCell(excelCell)   ← 走主写路径（合并由 writeCell 内部处理）
  ├─ 列宽补齐（空列设 4096）
  └─ excelWriter.toFile(os)
```

### 2.4 读取路径

```
ExcelReader.readExcelContent(is, ers, extractor)
  ├─ ers.beforeReader()
  ├─ XSSFWorkbook wb = new XSSFWorkbook(is)
  ├─ for sheet in wb:
  │     for i in [0, sheet.getLastRowNum()]:
  │       data[j] = extractor.getCellValue(row.getCell(j))   ← 默认 CellValueExtractor
  │       if (!ers.rowMapper(i, data, sheetIndex, sheetName)) break;
  ├─ ers.afterReader()
  └─ wb.close(); is.close();
```

读取是"一次性全量"模型，**无 SAX 流式**（POI `XSSFWorkbook` DOM 模式），大文件读取会占内存。

### 2.5 Servlet / SQL 导出路径

```
AbstractExportTableGridService.export(req, resp, fileName, columns)
  └─ ExportUtils.export(getListSQL(), 参数Map, OutputStream, columns)
       └─ new QueryResultExportTable(gridService, sql, pageModel, params, columns)
            └─ .write(os)   ← 走 2.2，writeRows 时拉 SQL 数据

ExportHttpServletRequestUtils.export(sql, req, extendParams, resp, fileName, columns)
  └─ QueryModel.of(params).pageModel.size = -1  → ExportUtils.export(...)
```

`GridService` 通过 `ExportGridServiceAutoConfiguration` 在启动时被注入到 `ExportUtils` 的**静态字段**。

## 3. 对外依赖

| 依赖 | scope | 用途 |
|---|---|---|
| `org.apache.poi:poi` / `poi-ooxml` | compile | 核心底层。仅用 XSSF（`XSSFWorkbook` / `XSSFSheet` / `XSSFRow` / `XSSFCell` / `XSSFCellStyle` / `XSSFColor` / `XSSFDrawing` / `XSSFClientAnchor`）。`XSSFWorkbook.PICTURE_TYPE_PNG/JPEG` 用于图片。 |
| `com.rick.common:sharp-common` | compile | `Time2StringUtils`（日期格式化为字符串）、`HttpServletRequestUtils` / `HttpServletResponseUtils`。 |
| `com.rick.db:sharp-database2` | provided | `GridService` / `QueryModel` / `PageModel` / `TableDAO` / `JdbcTemplateCallback` / `AbstractTableGridService` / `SharpDatabaseAutoConfiguration`。仅在 SQL 导出场景需要。 |
| `org.jsoup:jsoup` | provided | `HtmlExcelTable` 解析 HTML。 |
| `javax.servlet:javax.servlet-api` | provided | `plugin` 包的 Servlet 入参。 |
| `org.apache.commons:commons-collections4` / `commons-lang3` | （经父 BOM `sharp-dependencies`） | `CollectionUtils` / `ArrayUtils` / `StringUtils` / `ObjectUtils`。 |
| `lombok` | （经父 BOM） | `@Getter/@Setter/@Data/@UtilityClass`。 |
| `junit:junit` | test | 单元测试。 |

> 父 POM 是 `com.rick:sharp-dependencies:3.0-SNAPSHOT`，统一管理版本。

## 4. 扩展点

| 扩展点 | 类型 | 用法 |
|---|---|---|
| `ExcelWriterHook` | 函数式接口（`afterCellWrite`） | 传给 `writeCell` / `writeRow` / `insertAndWriteRow*`，在单元格写值+样式之后修改样式或值。`AbstractExportTable.write(os, consumer)` 内部不传 hook，需通过裸 `ExcelWriter` 使用。 |
| `Consumer<AbstractExportTable>` | `AbstractExportTable.write(os, consumer)` 第二参 | 在表头与数据行都写完、`toFile` 之前做整表级后处理（重命名 sheet、追加合计行、插入图片等）。Sharp-report 的 `ReportAdvice.beforeExportAndReturnBeforeToFileConsumer` 即返回此 consumer。 |
| `CellValueExtractor` | 接口（`getCellValue` default 实现） | 传给 `ExcelReader.readExcelContent` 第三参，自定义单元格值解析（如公式求值、特殊格式）。 |
| `MapTableColumn.converter` | `java.util.function.Function` | 列级值转换：Map 行的某列在写入前先 `converter.apply(value)`。 |
| `AbstractExportTable` 子类化 | 继承 + 实现 `resolve(Object row)` | 自定义数据源到 `Object[]` 的映射。`QueryResultExportTable` / `QueryRollingExportTable` 通过重写 `writeRows()` 接入不同数据源。 |
| `AbstractExportTableGridService` 继承 | 抽象类 | 业务 Service 继承它、实现 `getListSQL()`，即可用 `export(req,resp,fileName,columns)` 一行导出。 |
| `columnStyle` / `rowStyle` setter | 属性注入 | 覆盖 `AbstractExportTable` 默认表头/数据样式。 |

## 5. 配置与启动

- 无配置文件，无配置项。
- Spring Boot 自动装配：`ExportGridServiceAutoConfiguration`，条件 `@ConditionalOnSingleCandidate(GridService.class)` + `@AutoConfigureAfter(SharpDatabaseAutoConfiguration.class)`。装配时通过内部 `ExportGridServiceConfiguration` 把容器中的 `GridService` 注入 `ExportUtils` 的静态字段。
- 不引入 database2 / 无 `GridService` Bean 时，自动配置不生效；`ExcelWriter` / 各 `*ExportTable`（除 `QueryResultExportTable` / `QueryRollingExportTable`）/ `HtmlExcelTable` / `ExcelReader` 仍可在纯 POI 环境使用。
