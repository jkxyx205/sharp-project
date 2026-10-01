# CLAUDE.md — sharp-excel 模块工作指南

> 给改这个模块的 LLM 的工作指南。读完再动手，避免破坏既有约定。

## 1. 模块边界

- 这是一层薄封装：在 Apache POI **XSSF**（`.xlsx`）之上提供"坐标驱动写入 + 表格/HTML/SQL 导出 + 行回调读取"。**不**支持 HSSF / `.xls`，**不**支持 SAX 流式读取（读用 DOM `XSSFWorkbook`）。
- 写入收敛到 `ExcelWriter.writeCell` 一个出口；`writeRow` / `writeColumn` / `insertAndWriteRow*` 都是它的包装。
- SQL 导出（`QueryResultExportTable` / `QueryRollingExportTable` / `plugin` 包 / 自动配置）依赖 `sharp-database2`，属可选能力，不要在通用写/读代码里硬绑 database2。
- 不读任何 `application.yml` / `application.properties`。所有行为通过构造器/setter/列模型/回调配置。

## 2. 目录约定

```
src/main/java/com/rick/excel/
  core/        # 写/读核心 + 模型 + 工具。改动 ExcelWriter/ExcelReader 在这里。
  table/       # 表格导出抽象。新增导出形态（如 ExcelExportTable 子类）放这里。
  plugin/      # Servlet/SQL 一行式导出。依赖 servlet-api + database2（provided）。
  config/      # Spring Boot 自动配置。
src/main/resources/META-INF/
  spring.factories                                  # Spring Boot 2.x 自动配置注册（保留）
  spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports  # Spring Boot 3.x
src/test/java/com/excel/   # 测试（写绝对路径输出到 /Users/rick/...，CI 不可移植）
```

约定：新 public 类放对应包；模型类放 `core/model` 或 `table/model`；工具类用 `@UtilityClass`（参考 `ExcelUtils`、`ExcelWriteSupport`、`ExportHttpServletRequestUtils`）。

## 3. 编码约定

- 坐标系：`x` = 列号、`y` = 行号，**均从 1 开始**。POI 内部 0-based 的转换在 `ExcelWriter.writeCell` 内完成（`x-1` / `y-1`）。新增写入方法务必沿用此约定，不要在调用方做 0-based。
- 地址字符串（`A1`、`AB123`）大小写不敏感，统一走 `ExcelUtils.getCoordinateByLocation`。
- 类型分发改在 `ExcelWriter.setCellValue(XSSFCell, Object)`。新增值类型处理加在这里，不要散落到各 table 子类。
- `null` 值：`setCellValue` 直接 return，不写。空单元格保留原 cell type。
- 日期：cell 已是 STRING 类型 → 走 `Time2StringUtils.format` 写字符串；否则写 POI 日期数值。改日期行为时两条分支都要看。
- 大整数（Long/Integer，abs > 999_999_999_999_999L）自动写成 String 防 ID 精度丢失。BigDecimal 走 double（已知精度风险，业务自行转字符串）。
- 合并单元格：`rowSpan != 1 || colSpan != 1` 时 `addMergedRegion` + `setRegionStyle` 把区域内 cell 样式统一。**样式必须先于合并设置**（当前实现：先 setStyle 再合并）。
- Lombok：模型类用 `@Getter/@Setter`，工具类用 `@UtilityClass`，`HtmlExcelTable` 用 `@Data`。匹配既有风格，不要混用。
- 所有 Excel 类都不是线程安全的；每次导出新建实例。
- 中文 javadoc / 注释保留原样。

## 4. 常见改动清单

- **新增值类型支持**（如枚举、Duration）：改 `ExcelWriter.setCellValue` 的 if-else 链 + `convertCellType`。验证：写一个 `@Test` 把该类型塞进 `writeCell`，检查输出 cell 类型与显示。
- **新增列属性**（如背景色、格式化 pattern）：扩 `TableColumn` 字段 + `AbstractExportTable.writeColumns/writeRows` 中读取并应用到 `XSSFCellStyle`。
- **新增导出形态**：继承 `AbstractExportTable<T>`，实现 `resolve(T row)` 返回 `Object[]`；若数据源是异步/流式，重写 `writeRows()`（参考 `QueryRollingExportTable`）。
- **自定义单元格写后处理**：实现 `ExcelWriterHook`，传给 `writeCell`/`writeRow`/`insertAndWriteRow*`。不要为它新增参数化字段，hook 已是统一出口。
- **整表级后处理**（合计行、sheet 重命名、图片）：用 `AbstractExportTable.write(os, Consumer<AbstractExportTable>)` 的第二参。
- **新增 Servlet 一行式导出变体**：扩 `ExportHttpServletRequestUtils` 静态方法；若需要业务 SQL，继承 `AbstractExportTableGridService`。

## 5. 构建与测试

```bash
# 在仓库根目录
mvn -pl sharp-excel -am clean install -DskipTests      # 编译并装到本地仓库

mvn -pl sharp-excel test                                # 跑测试（注意：现有测试写死 /Users/rick/... 路径，多在 CI 失败）

mvn -pl sharp-excel dependency:tree                     # 看依赖树
```

测试现状：`src/test/java/com/excel/*Test.java` 多为本地手写用例，输出到 `/Users/rick/Documents/*.xlsx` 等绝对路径，**不可移植**。新增测试请用 `@TempDir` 或 `Files.createTempFile`，输出路径不要写死。改 `ExcelWriter` / `ExcelReader` 时优先补一个可移植的单测再改。

## 6. 陷阱

### 6.1 内存 / 大表
- 读：`ExcelReader` 用 `XSSFWorkbook` DOM，整文件常驻内存。**不要**用它读超大表；如需流式，需引入 POI SAX（当前模块未提供）。
- 写：`XSSFWorkbook` 同样是 DOM，导出几十万行会吃内存。`QueryRollingExportTable` 是唯一做了"边查边写"的导出形态，但写入端仍是 DOM；真正大表导出需要切换到 SXSSF（当前模块未支持）。
- `insertAndWriteRow*` 每次 `shiftRows` 是 O(lastRowNum) 操作，大批量逐行插入会变慢；模板填充场景尽量一次性 `insertAndWriteRow(x,y,dataList,...)` 批量插入。

### 6.2 日期 / 数字格式
- 日期写入时，若目标 cell 已是 STRING（如模板里是文本格式的日期单元格），会被 `Time2StringUtils.format` 转成字符串；否则写 POI 数值。两种结果差异大，模板设计时要明确该 cell 是文本还是日期格式。
- BigDecimal → double 会丢精度；金额列建议在 `MapTableColumn.converter` 里先转 `String`。
- Long ID（>15 位）会自动转字符串；15 位以内的 Long 走 double，通常没问题但 ID 接近 15 位时需留意。

### 6.3 合并单元格
- 合并后区域内的 cell 样式由 `setRegionStyle` 统一为左上角样式；**先设左上角 style 再触发合并**，否则样式可能为空。
- 调用方重复对同一区域调用 `writeCell` 且带 rowSpan/colSpan 会重复 `addMergedRegion` → POI 抛 `IllegalStateException`。

### 6.4 模板占位符约定
- 本模块**没有**"占位符变量替换"机制（不是 EasyExcel 模板、不是 `${var}`）。模板填充靠调用方按坐标 `writeCell(ExcelCell)`。模板的"占位符"就是坐标约定（哪个 cell 填什么），由业务代码维护。
- 基于模板时务必先 `IOUtils.toByteArray` 读字节再 `new XSSFWorkbook(ByteArrayInputStream)`，避免 POI 持有原文件句柄、改写覆盖原模板。参考 `ExcelTest.testWriteFromTemplate`。
- `insertAndWriteRowWithAfterRowStyle` 只复制样式；要复制公式/格式化用 `insertAndWriteRowWithAfterRowStyle2`（`copyRows`）。

### 6.5 静态状态
- `ExportUtils` 持有**静态** `GridService` 字段，由自动配置注入。单元测试中若未启 Spring，`ExportUtils.export(...)` 会 NPE。测试 `ExportUtils` 需手动 `new ExportUtils().setGridService(...)`。
- `ExportGridServiceAutoConfiguration` 只在 `@ConditionalOnSingleCandidate(GridService.class)` 时生效。容器内有多个 `GridService` 候选时自动配置静默失效，`ExportUtils.GRID_SERVICE` 保持 null。

### 6.6 已知疑似 bug（改动前先核对）
- `ExcelWriteSupport.excelCell(String location, Object value, int rowSpan, int colSpan)`：实现为 `new ExcelCell(coordinate[0], coordinate[1], rowSpan, colSpan)`，**未传 value**，会调到 `ExcelCell(int x, int y, Object value, int rowSpan, int colSpan)` 把 rowSpan（装箱为 Integer）当 value。使用前确认是否仍存在；修复时同步检查其它带 rowSpan/colSpan 的重载是否也有此问题。
- `ExcelWriteSupport.excelCell(String location, float heightInPoints, Object value, int rowSpan, int colSpan)` 同样看起来 OK（传了 value），但仍建议对照源码确认。

### 6.7 其它
- `ExcelReader` 行列数取 `max(row.getLastCellNum(), row.getPhysicalNumberOfCells())`，空行返回空数组；不处理合并单元格的"主值扩散"——合并区域非左上角 cell 取值为 null/空。
- `HtmlExcelTable` 用 jsoup `select("tr")` + `select("td, th")`，要求 HTML 是标准 `<table>` 结构；嵌套 table 会被扁平化处理，不要依赖嵌套结构。
- 自动配置同时在 `spring.factories` 和 `AutoConfiguration.imports` 注册，改类名/包名时**两处都要改**。
