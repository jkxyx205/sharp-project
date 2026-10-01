# sharp-generator

> 给大模型的模块工作指南。本模块是脚手架/代码生成器，详见 `API.md`、`ARCHITECTURE.md`。

## 模块边界

- 仅在**开发期**运行：通过 `Generator.execute(...)` 在 `@SpringBootTest` 用例里触发，产出建表 DDL、Java CRUD 代码、Report 测试类、表单 HTML 控件。
- **不是**运行时服务，不暴露 HTTP 接口，不参与业务请求链路。
- 不依赖 FreeMarker/Velocity；模板是 Java 字符串拼接，改动模板即改源码。

## 目录约定

- 生成物**不由本模块输出到自身目录**，而是写入调用方传入的绝对路径：
  - Java 代码 → `{rootPackagePath}/{dao|service|controller}/{Entity}{DAO|Service|Controller}.java`
  - Report 测试类 → `{REPORT_TEST_PATH}/{Entity}Test.java`
  - HTML 控件 → `{CONTROL_PATH}/control-{thymeleaf|vue|react}.html`
- 调用方约定（参考 sharp-admin）：实体放在 `module/{entity}` 下，`rootPackagePath` 指向该目录；HTML 放 `resources/templates/{...}`；Report 测试放 `src/test/java/{...}/demo`。
- 本模块源码仅 `src/main/java/com/rick/generator` 一层包 + `META-INF/spring.factories`，无 `src/test`。

## 编码约定

- 模板用 `${NAME}`、`${PACKAGE_NAME}` 占位 + `String.replace`；新增模板沿用此风格，不要引入模板引擎。
- 字段→控件映射集中在 `Generator.generatorHtml` 的 `if/else if` 链；新增字段类型在此扩展，并在对应 `AbstractControlGenerator` 子类补 `cpnType` 分支。
- 控件渲染风格集中在 `control/generator/` 三个实现；`RenderTypeEnum`、`FormLayoutEnum`、`DictCategoryEnum` 用枚举而非字符串。
- 包名前缀约定：生成的 DAO/Service/Controller 包为 `{实体原包名去掉最后一段}.dao|service|controller`（由 `StringUtils.substringBeforeLast(entityClass.getPackage().getName(), ".")` 得到）。

## 常见改动清单

- **加一种生成模板**（如生成 DTO/Mapper）：在 `execute` 增步骤 + 写 `xxxCodeTemplate()` + `writeCodeTemplate`；不要新建模板文件。
- **改命名规则**：Controller 的 `@RequestMapping` 由 `camelToSpinal(entityName) + "s"` 生成（如 `Student`→`students`）；改命名改这一处。属性名走 `stringToCamel`。
- **加表单控件类型**：扩 `CpnTypeEnum`（在 sharp-formflow）→ 在 `Generator.generatorHtml` 加字段类型分支 → 在 `ThymeleafControlGenerator`/`VueControlGenerator` 加 `cpnType` 分支。
- **加渲染风格**：`RenderTypeEnum` 加值 + 新 `AbstractControlGenerator` 子类 + `ControlGeneratorManager` 静态块注册。
- **改覆盖策略**：`writeCodeTemplate` 的 `!codeFile.exists() || overwrite` 判断；HTML 在 `generatorHtml` 末尾 `FileUtils.writeStringToFile` 无条件覆盖。

## 构建测试命令

- 构建（在 sharp-generator 目录）：`mvn clean install`
- 全量构建（项目根 `mvn.sh`）：`./mvn.sh`（按依赖顺序 install 各模块）
- 触发生成：在引用方项目（如 sharp-admin）跑对应 `@SpringBootTest`，例如 `mvn test -f sharp-admin -Dtest=GeneratorTest#testGeneratorThymeleaf`。生成 Report 后会自动 `mvn test -Dtest={Entity}Test#testReport`。

## 陷阱

- **覆盖已有文件**：`GENERATOR_CODE` 为 `null` 时仅“不存在才创建”，看似安全；但设为 `true` 会**覆盖**已存在的 DAO/Service/Controller。HTML 控件文件 (`control-*.html`) **总是覆盖**，改完别指望保留。Report 测试类同样直接覆盖写入。
- **`CONTROL_RENDER_TYPE` 判定疑似 bug**：`Generator.execute` 第 120 行写的是 `config.get(CONTROL_RENDER_TYPE == null) == null`——`CONTROL_RENDER_TYPE` 是非空常量，`CONTROL_RENDER_TYPE == null` 恒为 `false`，于是 `config.get(false)` 恒为 `null`，条件恒真，导致**始终遍历全部三种渲染风格**而非按指定渲染。如需只生成单一风格，需先把此处改为 `config.get(CONTROL_RENDER_TYPE) == null`（待确认/疑似 bug，改动前与作者确认）。
- **编码**：所有写文件用 `UTF-8`；注意生成物里中文注释（来自 `@Column(comment=...)`）依赖实体的编码，源实体文件须存为 UTF-8。
- **表名→类名映射**：实体名 `Student`→驼峰 `student`→spinal `students`（Controller 路径）。属性名由列名 `snake_case` 经 `stringToCamel` 转驼峰。字典 `DICT_VALUE` 字段取值表达式走 `camelToDot(name)`（如 `unit`→`unit.code`），`ENUM` 走 `name + ".name"`。改命名规则须同步控件表达式。
- **React 渲染未实现**：`ReactControlGenerator` 全部返回 `<!-- {name} 没有找到模版-->`，生成 React 风格目前无意义。
- **异步 mvn**：`REPORT=true` 会在新线程里 `Runtime.exec("mvn test -Dtest={Entity}Test#testReport")`，依赖 `mvn` 在 PATH 中；测试进程退出可能不等其完成。
- **`Params` 已 @Deprecated**：来自 sharp-database 的 `Params` builder 标记弃用，但本模块仍依赖它构造 config map；新代码可考虑直接用 `Map`。
- **`rootPackagePath` 必须绝对路径**：传入相对路径会落到 JVM 工作目录，生成物位置不可预期。
