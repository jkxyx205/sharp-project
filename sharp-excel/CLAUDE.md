# sharp-excel 工作指南

> 给改这个模块的 LLM 看。读完即可在不读源码的前提下做小改动不踩坑。

## 1. 模块边界

- 职责：基于 Apache POI XSSF 的 `.xlsx` 读写。**只支持 xlsx**，不支持 xls/csv。
- 不引入 EasyExcel；不使用 SAX 流式读取。
- 与 `sharp-database` 的耦合全部走 `provided` 作用域 + `plugin`/`table/Query*ExportTable` 包，**核心 `core`/`table/AbstractExportTable` 不依赖 sharp-database**。改 core 时不要新增对 sharp-database 的引用。
- 顶层包 `com.rick.excel`。

## 2. 目录约定

```
sharp-excel/
├── pom.xml
├── src/main/java/com/rick/excel/
│   ├── core/           # 不依赖 sharp-database 的核心
│   ├── table/          # 表格导出（部分子类依赖 sharp-database）
│   ├── plugin/         # 与 sharp-database + Servlet 集成的导出门面
│   └── config/         # Spring Boot 自动配置
├── src/main/resources/META-INF/spring.factories
└── src/test/java/com/excel/   # 单测，可直接跑（写到 /Users/rick/... 本地路径，需按需改）
```

新增类时按上述归属放置：纯 POI 工具放 `core` 或 `core.support`；表格导出走 `table`；与 SQL/Servlet 相关走 `plugin`。

## 3. 编码约定

- 坐标统一 1 基（`ExcelCell.x`/`y`、`ExcelRow.x`/`y`、`insertRows(y,n)` 等都从 1 开始）。内部转 0 基调 POI。**新增 API 必须沿用 1 基**，否则破坏一致性。
- 模型类用 Lombok `@Getter @Setter`；工具类用 `@UtilityClass`（`ExcelUtils`、`ExcelWriteSupport`）。
- 单元格值类型分支集中在 `ExcelWriter.setCellValue`，不要在调用方手工判断类型再 set；新增值类型在这里统一加分支。
- 颜色用 `XSSFColor + DefaultIndexedColorMap`，不要用老式 `HSSFColor.INDEX`。
- 中文注释保留作者风格，方法注释写清"输入格式、输出格式、异常"。
- 不写无意义的错误处理（POI 抛 `IOException`/`IllegalStateException` 直接上抛）。

## 4. 常见改动清单

| 改动 | 入口 | 注意 |
|---|---|---|
| 新增一种单元格值类型支持 | `ExcelWriter.setCellValue` + `convertCellType` | 两条分支都要改 |
| 自定义表头/行样式 | `AbstractExportTable.createDefaultColumnStyle`/`createDefaultRowStyle` 或调用方 `setColumnStyle`/`setRowStyle` | 不要破坏默认灰底加粗 |
| 新增列对齐方式 | `AlignEnum` + `AbstractExportTable.writeColumns`/`writeRows` 里 `HorizontalAlignment.valueOf` 转换处 | 注意 Jackson `@JsonValue` |
| 新增导出列转换器 | `MapTableColumn(name, label, converter)` 的 `Function` | converter 入参是原始 Object，出参写进单元格 |
| 新增表格导出子类 | 继承 `AbstractExportTable<T>`，实现 `resolve`；如需流式 override `writeRows` | `writeRows` 默认从第 2 行起写（startY=2） |
| 新增读取值提取策略 | 实现 `CellValueExtractor`，传给 `ExcelReader.readExcelContent` 第三参 | 默认实现把日期单元格返回 `Date`，数字返回 `Double` |
| 新增写入钩子 | 实现 `ExcelWriterHook`（lambda 即可），传入 `writeCell`/`writeRow`/`insertAndWriteRow*` 的 hook 重载 | `setCellValue` 返回 true 才会跳过默认值写入 |
| 新增 SQL 直出门面 | 一般直接用 `ExportUtils.export`；如需自定义参数解析再在 `plugin` 包加类 | `ExportUtils.GRID_SERVICE` 是静态字段，由 `ExportGridServiceAutoConfiguration` 注入，单元测试需手动 set |
| 改 Spring 自动配置 | `ExportGridServiceAutoConfiguration` + `spring.factories` | 别忘了 `@AutoConfigureAfter(GridServiceAutoConfiguration.class)` |

## 5. 构建与测试命令

```bash
# 在仓库根目录
mvn -pl sharp-excel -am compile            # 编译（含依赖模块）
mvn -pl sharp-excel test                   # 跑单测（写到本地路径，可能需要改 @Test 中的输出路径）
mvn -pl sharp-excel -am install -DskipTests   # 安装到本地仓库
```

单测不依赖 Spring 上下文，纯 JUnit；但部分 `@Test` 写死 `/Users/rick/...` 路径，CI 跑前需调整或忽略。

## 6. 陷阱

### 6.1 内存与大表
- `ExcelReader` 与所有 `ExportTable` 都基于 `XSSFWorkbook`，**全内存模型**。几十万行就可能 OOM。需要大表导出时优先用 `QueryRollingExportTable`（行级回调）但 workbook 仍在内存；真正的流式目前做不到（待确认）。
- `insertAndWriteRowWithAfterRowStyle2` 用 `Sheet.copyRows` 复制公式+格式，对万行以上插入明显变慢。

### 6.2 日期/数字格式
- `setCellValue` 对 `Date`/`LocalDate`/`LocalDateTime`：若目标 cell 的 CellType 是 STRING 就写 `Time2StringUtils.format(...)` 字符串；否则写 POI 原生日期（在 Excel 里显示为序列号或按 cell style 的日期格式显示）。模板填充时务必确认模板单元格格式。
- `BigDecimal` 直接 `doubleValue()`——**金额精度会丢失**，需要保留精度的场景请提前转 String 或改用 hook 自己写值。
- `Long` ID 超 `999_999_999_999_999L`（约 16 位）会自动转字符串避免科学计数法；15 位以内的 Long 仍写为数值——若有 18 位 ID 但 < 该阈值（不存在，该阈值已是 15 位 9 的上界）需注意。

### 6.3 合并单元格
- `writeCell` 在 `rowSpan`/`colSpan != 1` 时调 `addMergedRegion` 并把区域内所有 cell 套同一样式（`setRegionStyle`）。**重复合并同一区域抛 `IllegalStateException`**。
- 合并区域的边框：POI 已知 bug，合并后边框只在左上 cell 生效。`HtmlExcelTable.getDefaultStyle` 已对每个 cell 预设边框规避；自定义样式时需照做。

### 6.4 模板占位符约定
- 本模块**没有内置的 `${var}` 占位符替换**。模板填充就是直接用 `writeCell` 往指定坐标写值。约定：模板里留好固定坐标（如 G3 写 PO 号），调用方按业务拼好后用 `writeCell`/`ExcelWriteSupport.excelCell("G3", value)` 写入。
- 模板填充务必用 `new XSSFWorkbook(new ByteArrayInputStream(bytes))` 而非直接 `new XSSFWorkbook(file)`，否则原模板文件会被锁。

### 6.5 其它
- `ExcelWriter.toFile(OutputStream)` 名字误导——它**不落盘**，只是 `book.write(os) + book.close()`。之后该 writer 不可再用。
- `ExportUtils.GRID_SERVICE` 是静态字段，被 `ExportGridServiceAutoConfiguration` 注入一次。多 `GridService` Bean 时自动配置不生效，调用 `ExportUtils.export` 会 NPE。
- `AbstractExportTable.writeRows` 的 `startY = 2`（表头占第 1 行）；若 `tableColumnList` 为空则 `startY = 1`，但此时 `writeColumns` 也不会写表头。
- `ExcelUtils.getCoordinateByLocation` 大小写不敏感，但对混合大小写（如 "Ab12"）依赖从右向左扫描数字段；纯字母+纯数字的合法 Excel 地址都能解析，含特殊字符会算错但不报错。
