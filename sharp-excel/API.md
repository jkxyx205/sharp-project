# sharp-excel API

> 一句话定位：基于 Apache POI（XSSF）封装的轻量级 Excel 读写库，提供"单元格坐标式写入""表格式导出""SQL 查询结果直出""HTML 表格转 Excel"四类能力，仅支持 `.xlsx`。

## 1. 包结构速览

| 包 | 作用 |
|---|---|
| `com.rick.excel.core` | 写入器 `ExcelWriter`、读取器 `ExcelReader`、回调接口 `ExcelResultSet` / `ExcelWriterHook` / `CellValueExtractor` |
| `com.rick.excel.core.model` | 单元格/行/列模型：`BaseExcelCell`、`ExcelCell`、`ExcelRow`、`ExcelColumn`、`ExcelWriteSupport` |
| `com.rick.excel.core.support` | 工具类 `ExcelUtils` |
| `com.rick.excel.table` | 表格导出抽象 `AbstractExportTable` 及子类 `GeneralExportTable` / `MapExcelTable` / `QueryResultExportTable` / `QueryRollingExportTable` / `HtmlExcelTable` |
| `com.rick.excel.table.model` | `TableColumn` / `MapTableColumn` / `AlignEnum` |
| `com.rick.excel.plugin` | 与 `sharp-database` 集成的导出插件：`ExportUtils` / `ExportHttpServletRequestUtils` / `AbstractExportTableGridService` |
| `com.rick.excel.config` | Spring Boot 自动配置 `ExportGridServiceAutoConfiguration` |

## 2. 核心 public 入口

### 2.1 `ExcelWriter` —— 单元格坐标式写入器

类全名：`com.rick.excel.core.ExcelWriter`

职责：持有一个 `XSSFWorkbook`，按"列号 x、行号 y（均从 1 开始）"坐标系写入单元格、行、列，并支持合并、图片、插入行、模板克隆。

注意：方法名 `toFile(OutputStream)` 实际是"写到流并关闭 book"，并不落盘到文件。

关键方法：

```java
public ExcelWriter()                                  // 默认构造，初始化一个空 Sheet
public ExcelWriter(boolean initFirstSheet)            // initFirstSheet=false 时不创建初始 Sheet，需自行 createSheetAndActive
public ExcelWriter(String sheetName)                  // 用指定名称创建首个 Sheet
public ExcelWriter(XSSFWorkbook book)                 // 包装已有 workbook（常用于模板填充），取第 0 个 Sheet 为 active

public XSSFWorkbook getBook();                        // 暴露底层 POI workbook
public XSSFSheet getActiveSheet();                    // 当前活动 Sheet

public void setActiveSheet(int index);                // 切换活动 Sheet（0 基）
public void createSheetAndActive(String sheetName);   // 新建 Sheet 并切为活动
public void cloneSheetAndActive(int sheetNum);                                       // 克隆第 sheetNum 个 Sheet 并切为活动
public void cloneSheetAndActive(int sheetNum, String sheetName);                    // 同上，并重命名
public XSSFSheet getSheetAndActive(int index);                                       // 取第 index 个 Sheet 并切为活动

// —— 单元格写入（坐标 1 基）——
public void writeCell(ExcelCell ecell);                                  // 在 (x,y) 写值，自动合并/样式
public void writeCell(ExcelCell ecell, ExcelWriterHook hook);            // 带 hook（见 §2.4）
public void insertAndWriteCell(ExcelCell ecell);                         // 先在第 y 行插入一空行，再写

// —— 行/列写入 ——
public void writeRow(ExcelRow row);                                      // 从 row.x 起横向写一整行
public void writeRow(ExcelRow row, ExcelWriterHook hook);
public void writeColumn(ExcelColumn column);                             // 从 column.y 起纵向写一整列
public void writeColumn(ExcelColumn column, ExcelWriterHook hook);

// —— 批量插入行（带样式复用，适合模板填充明细行）——
public void insertAndWriteRow(int x, int y, List<Object[]> dataList, float heightInPoints, XSSFCellStyle cellStyle);          // 统一单元格样式
public void insertAndWriteRow(int x, int y, List<Object[]> dataList, float heightInPoints, XSSFCellStyle cellStyle, ExcelWriterHook hook);
public void insertAndWriteRow(int x, int y, List<Object[]> dataList, float heightInPoints, XSSFCellStyle[] cellStyles);       // 每列不同样式
public void insertAndWriteRow(int x, int y, List<Object[]> dataList, float heightInPoints, XSSFCellStyle[] cellStyles, ExcelWriterHook hook);

// 复用插入点下方那一行的样式（含公式/格式化），常用于模板明细行：
public void insertAndWriteRowWithAfterRowStyle(int y, List<Object[]> dataList, ExcelWriterHook hook);   // 仅复制样式
public void insertAndWriteRowWithAfterRowStyle2(int y, List<Object[]> dataList, ExcelWriterHook hook);  // 复制样式+公式+格式（copyRows）

// —— 行操作 ——
public void insertRows(int y, int n);     // 在第 y 行处下移 n 行（1 基）
public void removeRow(int y);             // 删除第 y 行
public void removeRows(int y, int n);     // 删除自第 y 行起的 n 行

// —— 图片 ——
public void createPicture(PathType pathType, String path, String extension,
                          int col1, int row1, int col2, int row2) throws IOException;   // 在 col1/row1 ~ col2/row2 锚定插入图片（0 基 POI 坐标）

public void toFile(OutputStream os) throws IOException;   // 将 workbook 写入 os 并 close workbook

public static enum PathType { URL, CLASS_PATH, ABSOLUTE_PATH;   // 图片路径来源
    public InputStream read(String path) throws IOException; }
```

写入值类型自动映射（`setCellValue` 内部）：
- `null` → 不写
- `RichTextString` → 富文本
- `Boolean` → 布尔
- `Date` / `LocalDate` / `LocalDateTime` / `Calendar` → 当目标单元格已是 STRING 类型时按 `Time2StringUtils` 格式化成字符串；否则按 POI 原生日期写入
- `BigDecimal` → `doubleValue`（注意精度丢失）
- `Long` / `Integer`：绝对值超过 `999_999_999_999_999L`（超出 double 安全整数范围）写为字符串，避免科学计数法；否则写 double
- 其它 `Number` → double
- 其它对象 → `String.valueOf`

### 2.2 `ExcelReader` —— 读取入口（静态）

类全名：`com.rick.excel.core.ExcelReader`

职责：把 `.xlsx` 流式读入，逐行回调 `ExcelResultSet`，整本工作簿所有 Sheet 顺序读完。

```java
public static void readExcelContent(InputStream is, ExcelResultSet ers) throws Exception;
public static void readExcelContent(InputStream is, ExcelResultSet ers, CellValueExtractor cellValueExtractor) throws Exception;
```

- `is`：`.xlsx` 输入流，方法内部会 `close()`。
- `ers`：行回调，返回 `false` 提前终止当前及后续读取。
- `cellValueExtractor`：自定义单元格值提取策略，默认实现见 §2.3。
- 注意：内部 `new XSSFWorkbook(is)` 一次性载入内存，**不适合超大文件**（百万行级请改用 POI SAX 或 EasyExcel）。

### 2.3 `ExcelResultSet` / `CellValueExtractor` —— 读取回调接口

```java
public interface ExcelResultSet {
    default void beforeReader() {}
    boolean rowMapper(int index, Object[] data, int sheetIndex, String sheetName) throws Exception;  // 返回 false 终止读取
    default void afterReader() {}
}

public interface CellValueExtractor {
    default Object getCellValue(XSSFCell cell);   // NUMERIC+日期→Date；NUMERIC→Double；BOOLEAN→Boolean；FORMULA/_NONE→rawValue；ERROR→null；其它→String
}
```

实现 `ExcelResultSet` 即可拿到每行的 `Object[]`（按列顺序，空单元格为 `null`）。

### 2.4 `ExcelWriterHook` —— 写入钩子

```java
@FunctionalInterface
public interface ExcelWriterHook {
    void afterCellWrite(ExcelCell ecell, XSSFCell cell);                  // 写完每个单元格后回调，可改样式
    default boolean setCellValue(XSSFCell cell, Object object) {          // 返回 true 则跳过默认 setCellValue
        return false;
    }
}
```

`ExcelWriter` 的所有带 `hook` 重载都会在写入前尝试 `hook.setCellValue`，写入后调用 `hook.afterCellWrite`。常见用法：对特定列值做条件染色（见测试 `ExcelTest#testWriteFromTemplate`）。

### 2.5 表格导出：`AbstractExportTable` 及子类

类全名：`com.rick.excel.table.AbstractExportTable<T>`

职责：给定列定义 `List<TableColumn>` 和数据行 `List<T>`，自动渲染表头（第 1 行，灰底加粗居中）+ 数据行（第 2 行起）。

```java
public AbstractExportTable(List<? extends TableColumn> tableColumnList, List<T> rows);
public void write(OutputStream os) throws IOException;
public void write(OutputStream os, Consumer<AbstractExportTable> beforeToFileConsumer) throws IOException;  // 写入前回调，可补写合计行/图片
public ExcelWriter getExcelWriter();                    // 暴露内部 writer，可调底层 POI 设 sheet 名/列宽等
public void setRowStyle(XSSFCellStyle rowStyle);        // 自定义数据行样式
public void setColumnStyle(XSSFCellStyle columnStyle);  // 自定义表头样式
public void setRowHeight(Float rowHeight);              // 数据行高，默认 24
public void setColumnHeight(Float columnHeight);        // 表头行高，默认 24
protected abstract Object[] resolve(Object row);        // 子类把一行数据转成 Object[]
```

子类清单：

| 子类 | 数据行类型 | 说明 |
|---|---|---|
| `GeneralExportTable` | `Object[]` | 最简单的二维数组导出，`resolve` 原样返回 |
| `MapExcelTable` | `Map<String,Object>` | 按 `MapTableColumn.name` 取值；支持 `converter`（`Function`）做单元格值转换；提供 `BigDecimal sum(String columnName)` 求和 |
| `QueryResultExportTable` | —— | 构造时不传 rows；`writeRows()` 时调用 `GridService.query(sql, pageModel, params)` 拉取数据（分页 `size=-1` 表示全量） |
| `QueryRollingExportTable` | —— | 不分页，通过 `SharpService.query` + `JdbcTemplate` 回调滚动写行，适合大结果集流式导出 |

`HtmlExcelTable`（`com.rick.excel.table.HtmlExcelTable`）是独立分支，不走 `AbstractExportTable`：

```java
public void write(String html, OutputStream os) throws IOException;   // 用 jsoup 解析 <table><tr><td/th rowspan/colspan>，转成合并单元格的 xlsx
```

### 2.6 列定义：`TableColumn` / `MapTableColumn` / `AlignEnum`

```java
public class TableColumn {
    public TableColumn(String label);
    public TableColumn(String label, int columnWidth);     // columnWidth 单位为 POI 列宽（1/256 字符宽），默认 2400
    public TableColumn setAlign(AlignEnum align);          // LEFT / CENTER / RIGHT，链式
}

public class MapTableColumn extends TableColumn {
    public MapTableColumn(String name, String label);                                  // name=Map 取值 key
    public MapTableColumn(String name, String label, Function converter);              // converter 对原始值做转换
    public MapTableColumn(String name, String label, int columnWidth);
    public MapTableColumn(String name, String label, int columnWidth, Function converter);
    public MapTableColumn setAlign(AlignEnum align);
}

public enum AlignEnum { LEFT, CENTER, RIGHT;   // toString()/JsonValue 返回小写 }
```

### 2.7 坐标式写入辅助：`ExcelCell` / `ExcelRow` / `ExcelColumn` / `ExcelWriteSupport`

```java
public class ExcelCell extends BaseExcelCell<Object> {     // BaseExcelCell 字段：int x, int y, Object value, float heightInPoints(默认12.75), XSSFCellStyle style
    public ExcelCell(int x, int y, Object value);
    public ExcelCell(int x, int y, float heightInPoints, Object value);
    public ExcelCell(int x, int y, float heightInPoints, XSSFCellStyle style, Object value);
    public ExcelCell(int x, int y, Object value, int rowSpan, int colSpan);                              // 合并：行/列跨度
    public ExcelCell(int x, int y, float heightInPoints, Object value, int rowSpan, int colSpan);
    public ExcelCell(int x, int y, float heightInPoints, XSSFCellStyle style, Object value, int rowSpan, int colSpan);
    public int rowSpan;   // 默认 1
    public int colSpan;   // 默认 1
}

public class ExcelRow extends BaseExcelCell<Object[]> {     // x=起始列, y=行号, value=各列值数组
    public ExcelRow(int x, int y);
    public ExcelRow(int x, int y, Object[] values);
    public ExcelRow(int x, int y, float heightInPoints, Object... values);
    public ExcelRow(int x, int y, float heightInPoints, XSSFCellStyle style, Object... values);
    public XSSFCellStyle[] cellStyles;                       // 可逐列指定样式
}

public class ExcelColumn extends BaseExcelCell<Object[]> {  // x=列号, y=起始行, value=各行值数组
    public ExcelColumn(int x, int y, Object[] values);
}

@UtilityClass
public class ExcelWriteSupport {                            // 用 Excel 形如 "AB12" 的地址字符串构造单元格/行
    public ExcelCell excelCell(String location, Object value);
    public ExcelCell excelCell(String location, float heightInPoints, Object value);
    public ExcelCell excelCell(String location, float heightInPoints, XSSFCellStyle style, Object value);
    public ExcelCell excelCell(String location, Object value, int rowSpan, int colSpan);
    // ... 行同理
    public ExcelRow excelRow(String location);
    public ExcelRow excelRow(String location, Object[] values);
    public ExcelRow excelRow(String location, float heightInPoints, Object... values);
    public ExcelRow excelRow(String location, float heightInPoints, XSSFCellStyle style, Object... values);
}
```

地址解析由 `ExcelUtils.getCoordinateByLocation(String)` 完成，大小写不敏感（"A1"/"ab123"/"Ab123" 等价）。

### 2.8 `ExcelUtils` —— 工具类

```java
@UtilityClass
public class ExcelUtils {
    public XSSFCellStyle[] getSheetXSSFCellStyle(XSSFSheet sheet, int y);                  // 取第 y 行各单元格样式（1 基）
    public XSSFCellStyle[] getSheetXSSFCellStyle(XSSFSheet sheet, int x, int y);           // 同上，若指定 x 则只返回该单元格样式（数组 [0]）
    public CellStyle copyXSSFCellStyle(Workbook workbook, XSSFSheet sheet, int x, int y);  // 复制某单元格样式
    public CellStyle copyXSSFCellStyle(Workbook workbook, CellStyle source);
    public int[] getCoordinateByLocation(String location);                                 // "AB12" → [28, 12]
}
```

### 2.9 SQL 直出插件：`ExportUtils` / `ExportHttpServletRequestUtils` / `AbstractExportTableGridService`

依赖 `sharp-database`（`provided` 作用域，运行时由使用方引入）。

```java
public final class ExportUtils {
    public void setGridService(GridService gridService);   // 由自动配置注入，静态字段
    public static final void export(String sql, Map<String,Object> params,
                                    OutputStream outputStream, List<MapTableColumn> columnList) throws IOException;
    // 内部：PageModel.size=-1（全量），new QueryResultExportTable(...).write(outputStream)
}

@UtilityClass
public final class ExportHttpServletRequestUtils {
    public static final void export(String sql, HttpServletRequest request, HttpServletResponse response,
                                    String fileName, List<MapTableColumn> columnList) throws IOException;
    public static final void export(String sql, HttpServletRequest request, Map<String,Object> extendParams,
                                    HttpServletResponse response, String fileName, List<MapTableColumn> columnList) throws IOException;
    // 自动从 request 取参数、设置 attachment 响应头、文件名拼接 yyyyMMddHHmmss + .xlsx
}

public abstract class AbstractExportTableGridService extends AbstractTableGridService {
    public void export(HttpServletRequest request, HttpServletResponse response,
                       String fileName, List<MapTableColumn> columnList) throws IOException;
    public void export(HttpServletRequest request, Map<String,Object> extendParams, HttpServletResponse response,
                       String fileName, List<MapTableColumn> columnList) throws IOException;
    // 子类实现 getListSQL() 提供 SQL，即可一行导出
}
```

## 3. 配置

| 项 | 默认 | 说明 |
|---|---|---|
| `spring.factories` 自动配置 | 启用 | `com.rick.excel.config.ExportGridServiceAutoConfiguration` 在容器存在唯一 `GridService` 时，把其注入 `ExportUtils`（静态字段）。无 `GridService` 时不生效，`ExportUtils.export` 会 NPE。 |
| 无任何 `application.properties` 配置项 | —— | 本模块不读取配置属性 |

## 4. 使用示例

### 4.1 坐标式写入（含合并、样式、多 Sheet）

```java
ExcelWriter writer = new ExcelWriter("订单");          // 首个 Sheet 名 "订单"
writer.writeCell(new ExcelCell(1, 1, "国内仓"));       // A1
ExcelCell merge = new ExcelCell(2, 3, "合计");
merge.setColSpan(3);                                   // 横向合并 3 列
writer.writeCell(merge);
writer.createSheetAndActive("明细");                   // 新 Sheet
writer.writeRow(new ExcelRow(1, 2, new Object[]{1, "SKU", 23.5}));
try (FileOutputStream fos = new FileOutputStream("/tmp/out.xlsx")) {
    writer.toFile(fos);                                // 写流并关闭
}
```

### 4.2 模板填充（基于已有 xlsx）

```java
byte[] bytes = IOUtils.toByteArray(new FileInputStream("template.xlsx"));  // 用字节数组，避免污染原模板
ExcelWriter writer = new ExcelWriter(new XSSFWorkbook(new ByteArrayInputStream(bytes)));
writer.writeCell(ExcelWriteSupport.excelCell("G3", "PO NO: PY20230726-100"));
writer.writeCell(ExcelWriteSupport.excelCell("A5", "供方：xxx 公司"));

// 在第 12 行处批量插入明细行，复用第 11 行的样式
List<Object[]> data = ...;
writer.insertAndWriteRowWithAfterRowStyle2(12, data, (ecell, cell) -> {
    // 偶数行加底色
});
writer.toFile(new FileOutputStream("dist.xlsx"));
```

### 4.3 表格式导出（二维数组 / Map）

```java
List<TableColumn> cols = Arrays.asList(new TableColumn("姓名"), new TableColumn("年龄"));
List<Object[]> rows = Arrays.asList(new Object[]{"rick", 23}, new Object[]{"jim", 88});
new GeneralExportTable(cols, rows).write(new FileOutputStream("/tmp/1.xlsx"));

// Map 数据
List<MapTableColumn> mcols = Arrays.asList(
        new MapTableColumn("name", "姓名"),
        new MapTableColumn("age", "年龄").setAlign(AlignEnum.RIGHT));
List<Map<String,Object>> mrows = ...;
new MapExcelTable(mcols, mrows).write(new FileOutputStream("/tmp/2.xlsx"));
```

### 4.4 SQL 直出（Web 接口）

```java
@GetMapping("/demo/export")
public void export(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    List<MapTableColumn> cols = Arrays.asList(
            new MapTableColumn("title", "标题"),
            new MapTableColumn("id", "ID"));
    ExportHttpServletRequestUtils.export(
            "SELECT id, title FROM t_demo WHERE title like :title",
            req, resp, "demo", cols);
}
```

### 4.5 读取

```java
ExcelReader.readExcelContent(new FileInputStream("/tmp/1.xlsx"),
    (index, data, sheetIndex, sheetName) -> {
        System.out.println(sheetName + ":" + index + " => " + Arrays.toString(data));
        return true;   // 返回 false 提前终止
    });
```

### 4.6 HTML 表格转 Excel

```java
HtmlExcelTable table = new HtmlExcelTable();
table.write(htmlString, new FileOutputStream("/tmp/a.xlsx"));   // 解析 <tr>/<td rowspan/colspan>
```

## 5. 与其它 sharp-* 模块的关系

| 模块 | 关系 |
|---|---|
| `sharp-common` | 编译依赖；`Time2StringUtils` 用于日期格式化 |
| `sharp-database` | `provided` 依赖；`ExportUtils`/`QueryResultExportTable`/`QueryRollingExportTable`/`AbstractExportTableGridService` 依赖其 `GridService` / `SharpService` / `QueryModel` / `PageModel` / `AbstractTableGridService`。无该模块时这些类不可用，但 `ExcelWriter`/`AbstractExportTable`/`ExcelReader` 仍可独立使用 |
| `sharp-report` | 上游消费者：`ReportService` 用 `MapExcelTable` + `AbstractExportTable.write(os, consumer)` 导出报表；`ReportController` 用 `HtmlExcelTable` 把前端 HTML 表格转 xlsx；`ReportAdvice` 提供 `getMapExcelTable` / `beforeExportAndReturnBeforeToFileConsumer` 扩展点 |
| `jsoup` | `provided`；仅 `HtmlExcelTable` 使用 |
| `javax.servlet-api` | `provided`；仅 `ExportHttpServletRequestUtils` / `AbstractExportTableGridService` 使用 |
| Apache POI（`poi` + `poi-ooxml`） | 核心依赖，XSSF 实现 |

## 6. 异常与边界

- `ExcelWriter.toFile` 会 `book.close()`，重复调用会抛 POI 异常。
- `writeCell` 合并：当 `rowSpan`/`colSpan` 任一 ≠ 1 时调用 `addMergedRegion`；重复合并同一区域会抛 `IllegalStateException`。
- `insertRows` / `insertAndWriteRow*` 调用 `Sheet.shiftRows`，对超大表（万行级以上）性能较差。
- `ExcelReader.readExcelContent` 把整本 workbook 载入内存，不适合百万行级文件（待确认：当前未提供 SAX 流式实现）。
- `ExportUtils.export` 未注入 `GridService` 时抛 NPE；自动配置要求容器中存在唯一 `GridService` Bean。
