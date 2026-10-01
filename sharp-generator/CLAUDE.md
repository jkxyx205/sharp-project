# sharp-generator 开发约定

## 模块边界

`com.rick:sharp-generator`，Java 8，parent 为 `sharp-dependencies`。职责：给定实体类，产出建表 DDL + DAO/Service/Controller java + 报表测试类 + 前端控件 HTML。**只生成、不参与运行时**；产物需人工迁移到正式位置或调整后使用。不提供 HTTP 接口，仅以 `Generator` Bean 编程式调用。

## 目录约定

- 源码：`src/main/java/com/rick/generator/`，下分 `config`（自动配置）、`control`（控件生成子系统）。
- 资源：仅 `META-INF/spring.factories` + `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，无模板文件。
- **生成物默认输出位置由调用方 `execute` 的参数决定**，模块自身不固定输出目录：
  - java 代码 → `rootPackagePath`（实体模块的 module 目录）下的 `dao/` `service/` `controller/`。
  - 报表测试 → `REPORT_TEST_PATH`。
  - 控件 HTML → `CONTROL_PATH`，文件名 `control-{renderType}.html`（如 `control-thymeleaf.html`、`control-vue.html`）。
- 模板 = Java 字符串字面量（`Generator` 内），不是文件。改模板即改源码。

## 编码约定

- 包名推导：`rootPackageName = entityClass.getPackage().getName()` 去掉最后一段（即 entity 的上一级包）。
- 类名推导：`entityName = entityClass.getSimpleName()`；camel 首字母小写；`@RequestMapping` 路径用 `{camel}s`。
- DAO 基类按 `BaseCodeEntity` 判定：继承则 `EntityCodeDAOImpl`，否则 `EntityDAOImpl`。
- 文件编码统一 `UTF-8`（`FileUtils.writeStringToFile(..., "UTF-8")`）。
- 日期注释 `@date` 用 `Time2StringUtils.format(LocalDateTime.now())`。
- 作者注释统一 `@author Rick.Xu`。

## 常见改动清单

### 加一种生成模板（如生成 Mapper/DTO）

1. 在 `Generator` 加私有 `xxxCodeTemplate(...)` 字符串方法。
2. 加私有 `generatorXxx(overwrite, ...)` 调 `writeCodeTemplate(overwrite, rootPackagePath, "xxx", entityName+"Xxx.java", tpl)`。
3. 在 `execute` 里按需调用，必要时加一个 `config` 开关常量。

### 改命名规则（如 `XxxDAO` → `XxxMapper`）

- 改 `generatorDAO` 里传给 `writeCodeTemplate` 的文件名 `entityName + "DAO.java"` 与模板中的类名即可。Service/Controller 同理。

### 加一种渲染类型（如新增 Angular）

1. `RenderTypeEnum` 加枚举值。
2. 新增 `control/generator/AngularControlGenerator extends AbstractControlGenerator`，实现 `generate(...无label...)` 与 `renderType()`。
3. `ControlGeneratorManager` 静态块 `registerMap.put(RenderTypeEnum.ANGULAR, new AngularControlGenerator())`。

### 改字段→控件映射

- 改 `Generator.generatorHtml` 里的类型判断分支；同时确保三个 `*ControlGenerator.generate` 都覆盖新增的 `CpnTypeEnum`。

## 构建测试命令

```bash
# 构建
mvn -pl sharp-generator -am clean install -DskipTests

# 仅编译
mvn -pl sharp-generator compile

# 该模块无单元测试；验证靠宿主模块（如 sharp-admin）的 GeneratorTest
mvn -pl sharp-admin -am test -Dtest=GeneratorTest#testGeneratorThymeleaf
```

## 陷阱

1. **覆盖策略易误**：`writeCodeTemplate` 仅在 `!exists || overwrite` 时写。`overwrite` 来自 `GENERATOR_CODE == true`。因此默认（`null`）是"不存在才写"，已存在文件不会被更新；想刷新代码必须显式 `Generator.GENERATOR_CODE, true`。报表测试类 `XxxTest.java` 与控件 HTML 则**总是覆盖**（直接 `writeStringToFile`，不判断 `overwrite`）。

2. **`CONTROL_RENDER_TYPE` 实际不生效（待确认/疑似 Bug）**：`Generator.execute` 第 124 行写的是
   ```java
   if (config.get(CONTROL_RENDER_TYPE == null) == null) {
   ```
   `CONTROL_RENDER_TYPE` 是 String 常量，`== null` 恒为 `false`；`config.get(false)` 恒为 `null`；故条件恒真，**永远走"生成全部三种渲染类型"分支**，`else`（按指定 `CONTROL_RENDER_TYPE` 单生成）是死代码。若需单一生成，需先修此行（应为 `config.get(CONTROL_RENDER_TYPE) == null`）。当前 React 实现也是 TODO 占位。

3. **报表会异步触发 `mvn test`**：`REPORT == true` 时新起线程执行 `mvn test -Dtest={Name}Test#testReport`，依赖宿主模块的 maven 环境；在无 `mvn` 的环境会静默失败（异常被 `RuntimeException` 包住，仅在子线程打印）。

4. **包名推导依赖实体包层级**：`rootPackageName` 取 `entityClass.getPackage().getName()` 去掉最后一段。实体若直接放在顶层包（无上一级），推导结果为空，生成代码的 `package` 语句会异常。

5. **`PROJECT` 必填且需匹配宿主包结构**：Controller 模板硬编码 `com.rick.{project}.common.api.BaseFormController` 与 `com.rick.{project}.module.{camel}.entity.{Name}`，项目短名错误则产物无法编译。

6. **表名→类名映射**：本模块不负责表名到类名的反推——它是**从实体类正向出发**的。`TableMetaResolver` 读 `@Table(value=...)` 得到表名，建表与查询 SQL 均用此名。改表名映射规则要去 `sharp-database2` 改，不在此模块。

7. **HTML 控件为多候选**：同一字段会生成多个候选控件（如 String 同时出 TEXT 和 TEXTAREA，字典同时出 SELECT/SEARCH_SELECT/RADIO），需人工挑选保留其一，不是开箱即用的最终页面。

8. **`@Column(columnDefinition=...)` 中 comment 解析脆弱**：`tableResolver` 用 `StringUtils.substringAfterLast(columnDefinition, " ")` 取列定义末段作 comment，要求 `comment 'xxx'` 在末尾且带引号；定义写法不符时 comment 取错或回落到属性名。
