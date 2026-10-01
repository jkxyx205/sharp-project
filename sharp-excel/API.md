# sharp-excel API

> 一句话定位：基于 Apache POI（XSSF）封装的轻量级 Excel 读写与导出库，提供单元格级 / 行列级 / 表格级 / HTML 表格转换 / SQL 查询结果导出五种粒度的 `.xlsx` 读写能力，并内置 Spring Boot 自动装配与 Servlet 导出工具。

所有 public 入口均在 `com.rick.excel.*` 包下。坐标系约定：`x` 为列号（从 1 开始），`y` 为行号（从 1 开始）；模板/单元格定位字符串（如 `A1`、`AB123`）大小写不敏感，由 `ExcelUtils.getCoordinateByLocation` 解析。

---

## 1. 核心 public 入口清单

### 1.1 `com.rick.excel.core.ExcelWriter` —— 写入器（核心）

类全名：`com.rick.excel.core.ExcelWriter`
职责：封装 `XSSFWorkbook`，按坐标写入单元格 / 行 / 列，处理合并单元格、行插入、图片插入，最终输出到 `OutputStream`。**线程不安全**，每次导出需新建实例。

构造器：

| 构造器 | 行为 |
|---|---|
| `ExcelWriter()` | 新建 workbook，并创建一个默认 sheet（无名称）。 |
| `ExcelWriter(boolean initFirstSheet)` | 新建 workbook；`initFirstSheet=false` 时不创建默认 sheet，调用方需自行 `createSheetAndActive`。 |
| `ExcelWriter(String sheetName)` | 新建 workbook 并创建一个命名为 `sheetName` 的 sheet。 |
| `ExcelWriter(XSSFWorkbook book)` | 包装一个已存在的 workbook（如基于模板）；首个 sheet 设为 active。 |

关键 public 方法（逐参数中文注释）：

```java
public void setActiveSheet(int index)
// index：sheet 在 workbook 中的下标（从 0 开始）。切换当前活跃 sheet。

public void cloneSheetAndActive(int sheetNum)
// sheetNum：被克隆 sheet 的下标。克隆后设为活跃，sheet 名称沿用 POI 默认。

public void cloneSheetAndActive(int sheetNum, String sheetName)
// sheetNum：被克隆 sheet 下标；sheetName：新 sheet 名称（可为 null）。

public void createSheetAndActive(String sheetName)
// sheetName：新 sheet 名称；为空则创建匿名 sheet。设为活跃。

public XSSFSheet getSheetAndActive(int index)
// index：sheet 下标。返回该 sheet 并设为活跃。

public void writeCell(ExcelCell ecell)
// ecell：单元格模型（含 x/y/value/style/heightInPoints/rowSpan/colSpan）。
// x、y 从 1 开始。值为 null 写入空单元格。当 rowSpan/colSpan != 1 自动调用
// sheet.addMergedRegion 合并区域，并把区域内所有单元格样式置为左上角样式。

public void writeCell(ExcelCell ecell, ExcelWriterHook hook)
// hook：单元格写入后的回调（见 1.5），可用于覆盖样式或值。

public void writeRow(ExcelRow row)
// row：行模型，其 value 是 Object[]，从 row.x 列起逐个写入。

public void writeRow(ExcelRow row, ExcelWriterHook hook)
// 同上，每个单元格写入后回调 hook。

public void writeColumn(ExcelColumn column)
// column：列模型，其 value 是 Object[]，从 column.y 行起向下逐个写入。

public void insertAndWriteCell(ExcelCell ecell)
// 在 ecell.y 行位置先插入 1 行（shiftRows），再写单元格。用于模板插入。

public void insertRows(int y, int n)
// y：起始行（1-based）；n：要插入的行数。底层调用 activeSheet.shiftRows(y-1, lastRowNum, n, true, false)。

public void removeRow(int y)        // 删除第 y 行（1-based）
public void removeRows(int y, int n) // 删除自第 y 行起共 n 行

public void insertAndWriteRow(int x, int y, List<Object[]> dataList,
                              float heightInPoints, XSSFCellStyle cellStyle)
public void insertAndWriteRow(int x, int y, List<Object[]> dataList,
                              float heightInPoints, XSSFCellStyle cellStyle,
                              ExcelWriterHook hook)
// x：起始列（1-based）；y：起始行（1-based）；dataList：每行一个 Object[]；
// heightInPoints：行高（≤0 表示使用默认）；cellStyle：统一单元格样式（每行复用同一 style 对象）；
// hook：每单元格写入后回调。先 shiftRows(y, dataList.size()) 插入空行再写入。

public void insertAndWriteRow(int x, int y, List<Object[]> dataList,
                              float heightInPoints, XSSFCellStyle[] cellStyles)
public void insertAndWriteRow(int x, int y, List<Object[]> dataList,
                              float heightInPoints, XSSFCellStyle[] cellStyles,
                              ExcelWriterHook hook)
// cellStyles：按列下标提供一组样式（每行复用同一组）。

public void insertAndWriteRowWithAfterRowStyle(int y, List<Object[]> dataList, ExcelWriterHook hook)
// 数据从第 1 列起写；自动复制模板中第 y 行的各单元格样式作为新插入行的样式。
// 仅复制样式，不复制公式/格式化。底层走 insertAndWriteRow。

public void insertAndWriteRowWithAfterRowStyle2(int y, List<Object[]> dataList, ExcelWriterHook hook)
// 与上类似，但用 activeSheet.copyRows 复制整行（样式、公式、格式化都复制），
// 再写值覆盖。需要更完整的行级复用时用此方法。

public void createPicture(PathType pathType, String path, String extension,
                          int col1, int row1, int col2, int row2) throws IOException
// pathType：图片来源类型（URL / CLASS_PATH / ABSOLUTE_PATH，见内部枚举 PathType）；
// path：路径或 URL；extension：图片扩展名（"png" / "jpeg"，其它降级为 PNG）；
// col1/row1/col2/row2：图片锚定的起止单元格（0-based，POI 约定），类型 MOVE_AND_RESIZE。

public void toFile(OutputStream os) throws IOException
// 把 workbook 写入 os 并 close workbook。调用一次后不可再写。
```

值类型映射（`setCellValue` 内部行为，外部只传 `Object`）：

| 入参类型 | 写入行为 |
|---|---|
| `null` | 不写入 |
| `RichTextString` | 直接 `setCellValue(RichTextString)` |
| `Boolean` | 数值类型 BOOLEAN |
| `java.util.Date` / `LocalDate` / `LocalDateTime` | 当目标单元格已是 STRING 类型 → 调 `Time2StringUtils.format` 写成字符串；否则写为 POI 日期数值 |
| `Calendar` | POI 日期数值 |
| `BigDecimal` | `doubleValue()`（注意精度丢失） |
| `Long` / `Integer` | abs(value) > 999_999_999_999_999L 时写成 `String`（避免 long ID 精度丢失）；否则写 double |
| 其它 `Number` | `doubleValue()` |
| 其它 | `String.valueOf(object)` |

> 注意：`BigDecimal` 与 `Long`（>15 位）的精度行为是内置保护，但仍可能不符合某些业务需求，必要时自行通过 `ExcelWriterHook` 覆盖写入或写成字符串。

---

### 1.2 `com.rick.excel.core.ExcelReader` —— 读取器

类全名：`com.rick.excel.core.ExcelReader`（全部静态方法）
职责：把 `.xlsx` 输入流逐行解析为 `Object[]`，回调 `ExcelResultSet` 处理。

```java
public static void readExcelContent(InputStream is, ExcelResultSet ers) throws Exception
// is：xlsx 输入流（方法内部会 close）；ers：行结果集回调。使用默认 CellValueExtractor。

public static void readExcelContent(InputStream is, ExcelResultSet ers,
                                    CellValueExtractor cellValueExtractor) throws Exception
// cellValueExtractor：自定义单元格值提取器（见 1.6）。
// 流程：ers.beforeReader() → 遍历所有 sheet → 每个 sheet 内逐行 i∈[0, getLastRowNum()]
//   调 ers.rowMapper(i, Object[], sheetIndex, sheetName)，
//   返回 false 终止遍历 → ers.afterReader() → close wb 与 is。
```

行为说明：每行列数取 `max(row.getLastCellNum(), row.getPhysicalNumberOfCells())`；空行返回空数组；不识别合并单元格的"主值扩散"，只按 POI 原生 cell 取值。

### 1.3 `com.rick.excel.core.ExcelResultSet` —— 行回调接口

```java
public interface ExcelResultSet {
    default void beforeReader() {}
    boolean rowMapper(int index, Object[] data, int sheetIndex, String sheetName) throws Exception;
    // index：当前行号（0-based）；data：该行单元格值数组；sheetIndex：sheet 下标；sheetName：sheet 名。
    // 返回 false 终止整个读取流程。
    default void afterReader() {}
}
```

可使用 lambda 实现（`boolean rowMapper(...)` 是唯一抽象方法，但接口非 `@FunctionalInterface` 标注；如需 lambda，需自行保证仅覆盖该方法）。

### 1.4 `com.rick.excel.core.ExcelWriterHook` —— 单元格写后钩子

```java
@FunctionalInterface
public interface ExcelWriterHook {
    void afterCellWrite(ExcelCell ecell, XSSFCell cell);  // 写值与样式之后调用，可改样式/改值
    default boolean setCellValue(XSSFCell cell, Object object) { return false; } // 保留扩展，当前未被 ExcelWriter 内部调用
}
```

实现为 lambda 即可：`(ecell, cell) -> { ... }`。

### 1.5 `com.rick.excel.core.CellValueExtractor` —— 单元格值提取器

```java
public interface CellValueExtractor {
    default Object getCellValue(XSSFCell cell) {
        // null → null；NUMERIC + DateUtil.isCellDateFormatted → java.util.Date；NUMERIC → double；
        // BOOLEAN → boolean；FORMULA/_NONE → getRawValue()；ERROR → null；STRING/默认 → String。
    }
}
```

自定义提取时实现该接口并覆盖 `getCellValue`，传给 `ExcelReader.readExcelContent` 第三参数（或用 `new CellValueExtractor(){}` 匿名类）。

---

### 1.6 `com.rick.excel.core.model.*` —— 单元格 / 行 / 列模型

`BaseExcelCell<T>`（基类，字段：`int x`、`int y`、`T value`、`float heightInPoints=12.75f`、`XSSFCellStyle style`）：

- `ExcelCell extends BaseExcelCell<Object>`：增加 `int rowSpan=1`、`int colSpan=1`。构造器：
  - `(int x, int y, Object value)`
  - `(int x, int y, float heightInPoints, Object value)`
  - `(int x, int y, float heightInPoints, XSSFCellStyle style, Object value)`
  - `(int x, int y, Object value, int rowSpan, int colSpan)`
  - `(int x, int y, float heightInPoints, Object value, int rowSpan, int colSpan)`
  - `(int x, int y, float heightInPoints, XSSFCellStyle style, Object value, int rowSpan, int colSpan)`
- `ExcelRow extends BaseExcelCell<Object[]>`：增加 `XSSFCellStyle[] cellStyles`（按列下标给出每格样式）。构造器：`(x,y)` / `(x,y,Object[] values)` / `(x,y,float heightInPoints,Object... values)` / `(x,y,float heightInPoints,XSSFCellStyle style,Object... values)`。
- `ExcelColumn extends BaseExcelCell<Object[]>`：构造器 `(int x, int y, Object[] values)`。

### 1.7 `com.rick.excel.core.model.ExcelWriteSupport` —— 工厂（`@UtilityClass`）

用 Excel 地址字符串（如 `"A1"`、`"AB123"`）构造 cell/row：

```java
public ExcelCell excelCell(String location, Object value)
public ExcelCell excelCell(String location, float heightInPoints, Object value)
public ExcelCell excelCell(String location, float heightInPoints, XSSFCellStyle style, Object value)
public ExcelCell excelCell(String location, Object value, int rowSpan, int colSpan)
public ExcelCell excelCell(String location, float heightInPoints, Object value, int rowSpan, int colSpan)
public ExcelCell excelCell(String location, float heightInPoints, XSSFCellStyle style, Object value, int rowSpan, int colSpan)
public ExcelRow excelRow(String location)
public ExcelRow excelRow(String location, Object[] values)
public ExcelRow excelRow(String location, float heightInPoints, Object... values)
public ExcelRow excelRow(String location, float heightInPoints, XSSFCellStyle style, Object... values)
```

> 待确认 / 疑似 bug：`excelCell(String location, Object value, int rowSpan, int colSpan)` 当前实现为 `new ExcelCell(coordinate[0], coordinate[1], rowSpan, colSpan)`，**未把 `value` 传入**，会调到 `ExcelCell(int x, int y, Object value, int rowSpan, int colSpan)` 而把 `rowSpan`（int 自动装箱）当成 value。使用带 rowSpan/colSpan 的重载前请先核对源码，或直接用 `new ExcelCell(x, y, value, rowSpan, colSpan)`。

### 1.8 `com.rick.excel.core.support.ExcelUtils` —— 工具（`@UtilityClass`）

```java
public XSSFCellStyle[] getSheetXSSFCellStyle(XSSFSheet sheet, int y)               // 取第 y 行所有单元格样式
public XSSFCellStyle[] getSheetXSSFCellStyle(XSSFSheet sheet, int x, int y)         // x>0 时只返回 [x-1] 单元格样式
public CellStyle copyXSSFCellStyle(Workbook workbook, XSSFSheet sheet, int x, int y) // 复制 (x,y) 单元格样式
public CellStyle copyXSSFCellStyle(Workbook workbook, CellStyle source)             // 复制任意 CellStyle
public int[] getCoordinateByLocation(String location)                               // "A1" → [1,1]；"AB123" → [28,123]
```

---

### 1.9 `com.rick.excel.table.*` —— 表格导出抽象

所有表格类最终都通过持有一个 `ExcelWriter` 写出。

#### `AbstractExportTable<T>`（抽象基类）

字段（getter/setter）：
- `List<? extends TableColumn> tableColumnList`
- `List<T> rows`
- `ExcelWriter excelWriter`（默认 `new ExcelWriter()`）
- `Float columnHeight = 24f`（表头行高）
- `Float rowHeight = 24f`（数据行高）
- `XSSFCellStyle columnStyle` / `rowStyle`（自定义表头/数据样式，覆盖默认）
- `XSSFCellStyle defaultColumnStyle` / `defaultRowStyle`（默认样式）

```java
public void write(OutputStream os) throws IOException
// 写表头（第 1 行，每个 TableColumn 一列）+ 数据行（从第 2 行起），最后 excelWriter.toFile(os)。

public void write(OutputStream os, Consumer<AbstractExportTable> beforeToFileConsumer) throws IOException
// beforeToFileConsumer：在写入数据之后、toFile 之前调用，可对 excelWriter/sheet 做最后调整
// （如重命名 sheet、补写合计行、插入图片）。Sharp-report 的 ReportAdvice.beforeExport... 即用此。
```

表头默认样式：水平/垂直居中、粗体、浅灰背景。数据行默认样式：空 `XSSFCellStyle`（无格式）。`TableColumn.align != null` 时按对齐方式覆盖每列样式。

#### `GeneralExportTable extends AbstractExportTable<Object[]>`

```java
public GeneralExportTable(List<TableColumn> tableColumnList, List<Object[]> rows)
```
最简单的列表导出：行直接是 `Object[]`，`resolve` 原样返回。

#### `MapExcelTable extends AbstractExportTable<Map<String, Object>>`

```java
public MapExcelTable(List<MapTableColumn> tableColumnList, List<Map<String, Object>> rows)
public BigDecimal sum(String columnName)   // 对 rows 中指定列求和（支持 BigDecimal/String/Number）
```
`resolve` 按 `MapTableColumn.name` 从 row map 取值；若 `MapTableColumn.converter != null`，先 `converter.apply(value)` 再写入。

#### `QueryResultExportTable extends MapExcelTable`

```java
public QueryResultExportTable(GridService gridService, String sql,
                              PageModel pageModel, Map<String, Object> params,
                              List<MapTableColumn> tableColumnList)
```
构造时不传 rows（传 null），`writeRows()` 时调 `gridService.query(sql, pageModel, params).getRows()` 拉数据再写。**依赖 `sharp-database2`**（provided scope）。

#### `QueryRollingExportTable extends AbstractExportTable`（无分页流式导出）

```java
public QueryRollingExportTable(TableDAO tableDAO, String sql,
                               Map<String, Object> params,
                               List<MapTableColumn> tableColumnList)
```
不带分页、不在内存中累积全部数据：通过 `tableDAO.select(sql, params, JdbcTemplateCallback)` 在 `ResultSet` 行级回调中边查边写（每行一个 `ExcelRow`）。**适合大表导出**，但行号依赖 `rs.getRow()`，需确保 JDBC 驱动正确返回行号。

#### `HtmlExcelTable`（HTML 表格 → Excel，`final`）

```java
public final class HtmlExcelTable {
    private ExcelWriter excelWriter = new ExcelWriter();   // 可 getter 暴露
    private Float rowHeight = 24f;
    private XSSFCellStyle cellStyle;                       // 自定义样式；为 null 时使用默认边框样式

    public void write(String html, OutputStream os) throws IOException
    // html：含 <table><tr><td/th> 的 HTML 片段，jsoup 解析；
    //   支持 td/th 的 rowspan/colspan 属性 → 合并单元格；
    //   每个 cell 取 td.text()；自动把空列宽设为 4096；
    //   html 为空时直接 toFile(os)。
}
```
默认样式：黑色细边框、水平垂直居中。

### 1.10 `com.rick.excel.table.model.*` —— 列定义

| 类 | 字段 / 构造器 |
|---|---|
| `TableColumn` | `String label`（表头文案）、`int columnWidth=2400`（POI 列宽单位）、`AlignEnum align`。构造：`(label)`、`(label, columnWidth)`。`setAlign` 链式返回 this。 |
| `MapTableColumn extends TableColumn` | 增加 `String name`（Map key / SQL 列名）、`Function converter`（值转换）。构造：`(name,label)`、`(name,label,converter)`、`(name,label,columnWidth)`、`(name,label,columnWidth,converter)`。 |
| `AlignEnum` | 枚举 `LEFT` / `CENTER` / `RIGHT`；`@JsonValue` 与 `toString()` 返回小写。 |

---

### 1.11 `com.rick.excel.plugin.*` —— Servlet/报表导出插件

> 全部依赖 `javax.servlet-api` 与 `sharp-database2`（均 provided scope），仅在 Web/有 `GridService` 的环境下生效。

#### `ExportUtils`（`final`，内部持静态 `GridService`）

```java
public static void export(String sql, Map<String, Object> params,
                          OutputStream outputStream, List<MapTableColumn> columnList) throws IOException
// 用 QueryModel.of(params) 构造查询、page.size=-1（全量），
// 通过 QueryResultExportTable 查询并写出。依赖 GridService 已被注入。
```

`GridService` 由 `ExportGridServiceAutoConfiguration` 在 Spring Boot 启动时注入（见 1.12）。

#### `ExportHttpServletRequestUtils`（`@UtilityClass final`）

```java
public static void export(String sql, HttpServletRequest request,
                          Map<String, Object> extendParams, HttpServletResponse response,
                          String fileName, List<MapTableColumn> columnList) throws IOException
// extendParams：除 request 参数外的额外参数（可 null）；
// fileName：导出文件名（不带扩展名），方法内追加 yyyyMMddHHmmss + ".xlsx"。

public static void export(String sql, HttpServletRequest request,
                          HttpServletResponse response, String fileName,
                          List<MapTableColumn> columnList) throws IOException
// 同上，extendParams=null。
```

#### `AbstractExportTableGridService extends AbstractTableGridService`（抽象）

```java
public abstract class AbstractExportTableGridService extends AbstractTableGridService {
    public void export(HttpServletRequest request, HttpServletResponse response,
                       String fileName, List<MapTableColumn> columnList) throws IOException
    public void export(HttpServletRequest request, Map<String, Object> extendParams,
                       HttpServletResponse response, String fileName,
                       List<MapTableColumn> columnList) throws IOException
}
// 子类需实现 AbstractTableGridService#getListSQL()，提供 SQL。
// 方法内：合并 request 参数与 extendParams → ExportUtils.export(...)
```

### 1.12 `com.rick.excel.config.ExportGridServiceAutoConfiguration`

Spring Boot 自动配置。条件：容器中存在且仅有一个 `com.rick.db.plugin.page.GridService` Bean，且在 `SharpDatabaseAutoConfiguration` 之后装配。装配时把 `GridService` 注入到 `ExportUtils` 静态字段，使 `ExportUtils.export(...)` 可用。

注册方式：`META-INF/spring.factories`（Spring Boot 2.x）与 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（Spring Boot 3.x）双注册。

---

## 2. 配置项

本模块自身**不读取任何 application.properties / application.yml 配置项**。行为配置通过代码（构造器参数、setter、列模型、回调）完成。

自动装配条件：容器内存在唯一 `GridService` Bean（来自 `sharp-database2`）。若无 `GridService`，`ExportUtils.export(...)` 将因静态字段为 null 抛 NPE；但 `ExcelWriter` / 各 `*ExportTable` / `HtmlExcelTable` / `ExcelReader` 仍可独立使用。

---

## 3. 常用使用示例

### 3.1 定义一个最简单的导出（数组行）

```java
List<TableColumn> columns = Arrays.asList(
        new TableColumn("姓名"),
        new TableColumn("年龄"));
List<Object[]> rows = Arrays.asList(
        new Object[]{"rick", 23},
        new Object[]{"jim",   88});

new GeneralExportTable(columns, rows)
        .write(new FileOutputStream("/tmp/people.xlsx"));
```

### 3.2 基于 Map 行 + 列值转换

```java
List<MapTableColumn> columns = Arrays.asList(
        new MapTableColumn("name", "姓名"),
        new MapTableColumn("age",  "年龄")
                .setAlign(AlignEnum.RIGHT),
        new MapTableColumn("status", "状态", v -> "1".equals(v) ? "启用" : "禁用"));

List<Map<String, Object>> rows = ...;  // 来自 SQL 查询
new MapExcelTable(columns, rows).write(response.getOutputStream());
```

### 3.3 单元格 / 行 / 合并 / 多 sheet（裸 ExcelWriter）

```java
ExcelWriter writer = new ExcelWriter("汇总");
writer.writeCell(new ExcelCell(1, 1, "标题"));
writer.writeCell(new ExcelCell(2, 3, "合并值"));  // 然后手动设 rowSpan/colSpan
ExcelCell merged = new ExcelCell(2, 3, "合并值");
merged.setRowSpan(3); merged.setColSpan(2);
writer.writeCell(merged);

writer.createSheetAndActive("明细");
writer.writeRow(new ExcelRow(1, 1, new Object[]{1, "a", LocalDate.now()}));

try (FileOutputStream os = new FileOutputStream("/tmp/x.xlsx")) {
    writer.toFile(os);
}
```

### 3.4 基于模板填充

```java
byte[] bytes = Files.readAllBytes(Paths.get("/tmp/template.xlsx")); // 读字节避免污染原模板
ExcelWriter writer = new ExcelWriter(new XSSFWorkbook(new ByteArrayInputStream(bytes)));

writer.writeCell(ExcelWriteSupport.excelCell("A1", "PO NO: 20260930-001"));
writer.writeCell(ExcelWriteSupport.excelCell("B5", "供方：示例公司"));

// 复用模板第 12 行样式插入多行
List<Object[]> data = ...;
writer.insertAndWriteRowWithAfterRowStyle(12, data, (ecell, cell) -> {
    // 写完后可改样式
});

writer.toFile(new FileOutputStream("/tmp/filled.xlsx"));
```

### 3.5 读取 Excel（行回调）

```java
ExcelReader.readExcelContent(new FileInputStream("/tmp/in.xlsx"),
        (index, data, sheetIndex, sheetName) -> {
            System.out.println(sheetName + " row " + index + ": " + Arrays.toString(data));
            return true;  // 返回 false 终止
        });
```

> 注意：`ExcelResultSet` 上 lambda 写法仅当接口被识别为函数式接口时编译通过；当前接口有三个 default 方法但只有一个抽象方法 `rowMapper`，可作为 functional interface 使用。

### 3.6 HTML 表格直接转 Excel

```java
String html = "<table><tr><th colspan='2'>国内仓</th></tr>"
            + "<tr><td>苏州</td><td>10</td></tr></table>";
new HtmlExcelTable().write(html, new FileOutputStream("/tmp/h.xlsx"));
```

### 3.7 Servlet 一行式 SQL 导出

```java
// 注入 GridService 后（自动装配），直接：
ExportUtils.export("select id, name, age from t_user",
        Collections.emptyMap(),
        HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, "users.xlsx"),
        Arrays.asList(new MapTableColumn("id", "ID"),
                      new MapTableColumn("name", "姓名"),
                      new MapTableColumn("age", "年龄")));
```

---

## 4. 与其它 sharp-* 模块的关系

| 关系 | 说明 |
|---|---|
| `sharp-common` | 编译期依赖。提供 `Time2StringUtils`（日期→字符串）、`HttpServletRequestUtils` / `HttpServletResponseUtils`（HTTP 参数与下载流）。 |
| `sharp-database2` | provided 依赖。`QueryResultExportTable` / `ExportUtils` / `QueryRollingExportTable` / `AbstractExportTableGridService` / 自动配置类用到 `GridService`、`PageModel`、`QueryModel`、`TableDAO`、`JdbcTemplateCallback`、`AbstractTableGridService`、`SharpDatabaseAutoConfiguration`。**没有 database2 时这些类不可用，其它写/读能力仍可用。** |
| `jsoup` | provided 依赖，仅 `HtmlExcelTable` 使用。 |
| `javax.servlet-api` | provided 依赖，仅 `plugin` 包使用。 |
| `Apache POI`（poi / poi-ooxml） | 编译期依赖，核心底层。仅使用 XSSF（`.xlsx`），不支持 HSSF / `.xls`。 |
| `sharp-report` | 上游使用方。`ReportService` 用 `MapExcelTable` + `Consumer<AbstractExportTable>` 钩子导出，`ReportController` 用 `HtmlExcelTable` 直接把 HTML 转 Excel。 |
| `sharp-admin` / `sharp-demo` | 引入依赖（pom），具体使用见各模块自身。 |
