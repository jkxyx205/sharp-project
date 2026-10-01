# CLAUDE.md

> 给修改 sharp-database2 模块的 LLM 的工作指南。按此规范工作，避免破坏既有行为。

---

## 1. 模块定位与边界

- **职责**：基于 Spring JDBC 的 ORM-lite 数据访问核心。提供实体注解映射、实体级 CRUD + 级联 + 乐观锁、表级 DAO、动态 SQL 清理、分页、方言、DDL 生成、审计 + 逻辑删除扩展。
- **不做什么**：不做 JPA/MyBatis；不维护 Session；不提供 XML Mapper；不内建多数据源；不实现连接池（用 Spring Boot 自带 HikariCP）。
- **groupId / 包根**：`com.rick.db`。
- **旧版 sharp-database**：仓库中只留 `sharp-database/target/*.class`，无源码，已废弃。包是 `com.rick.db.dto`。**新业务一律用 sharp-database2**；不要试图"修复"或"迁移"旧 sharp-database 目录。
- **版本**：`3.0-SNAPSHOT`（由 `sharp-dependencies/pom.xml` 的 `<sharp.version>` 统一管理）。

---

## 2. 目录约定

```
sharp-database2/
├── pom.xml
├── src/main/java/com/rick/db/
│   ├── config/                 # 自动配置、属性、Context（不要把业务配置放这）
│   ├── repository/             # 核心：DAO、注解、TableMeta、SQL 工具
│   │   ├── model/              # 实体基类（EntityId/BaseEntity/...）——业务实体不放在这
│   │   └── support/            # 内部实现（dialect/baseinfo/category/support 类）
│   ├── plugin/                 # 上层插件：Service 模板、分页、TableGenerator、DbUtils
│   │   ├── page/
│   │   ├── generator/
│   │   └── table/
│   └── util/
└── src/main/resources/META-INF/
    ├── spring.factories        # Boot 2 兼容
    └── spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

**新加代码归位原则**：
- 注解 → `com.rick.db.repository`
- 实体基类 → `com.rick.db.repository.model`
- 内部辅助类 → `com.rick.db.repository.support`（必要时分子包，参考 `dialect/`、`baseinfo/`、`category/`）
- 方言 → `com.rick.db.repository.support.dialect`
- Service 模板 / 分页 / DDL → `com.rick.db.plugin` 对应子包
- 通用工具 → `com.rick.db.util`
- 自动配置 / 全局属性 → `com.rick.db.config`

**不要**：在 `com.rick.db.repository` 放业务实体的子类；在 `plugin` 放底层 DAO；在 `support` 暴露 public API（外部应优先依赖 `repository` 包的接口）。

---

## 3. 编码约定

### 3.1 命名
- DAO 接口大驼峰：`XxxDAO`；实现 `XxxDAOImpl`。
- Service 模板：`BaseServiceImpl` / `BaseCodeServiceImpl`；业务侧 Service 命名 `XxxService extends BaseServiceImpl`。
- 实体基类：`BaseEntity` / `BaseCodeEntity` / `BaseCodeDescriptionEntity`；扩展实体放 `model`。
- 列名 `snake_case`，属性名 `camelCase`。`TableMetaResolver` 用 `com.rick.common.util.StringUtils.camelToSnake` 自动转换。
- 注解与 JPA 同名的（`@Table`/`@Id`/`@Column`/`@Transient`/`@Version`/`@OneToMany`/`@ManyToOne`/`@ManyToMany`）不要重命名——业务侧 import 时是 `com.rick.db.repository.*`。

### 3.2 SQL 写法
- **`?` 占位**用于 `Object... args` 与 `batchUpdate(sql, Object[][])`。
- **`:name` 命名参数**用于 `Map<String,Object> paramMap`。
- **`IN (:name)`** 用命名参数 + 集合，让 `SQLParamCleaner` 自动展开。
- **`@Select(params="key@property.path")`** 用 `@` 分隔 SQL 参数名与实体属性路径。
- **`@Embedded(columnPrefix="x_")`** 内嵌对象字段拼到当前表，前缀加到列名与属性路径前段。
- **`columnDefinition` 优先级最高**：设了它 DDL 生成与 `nullable`/`comment` 失效（PG json 也靠它走特殊路径）。
- **LIKE**：写 `col LIKE :name`，`SQLParamCleaner` 自动转 `UPPER(col) LIKE CONCAT(...) escape`，值里 `%`/`_` 自动转义。
- **`@ManyToOne` 列名**默认 `属性名_id`；`@OneToMany` 必须给 `joinColumnId` 或让父类 `referenceColumnId` 默认（`类名_id`）。

### 3.3 事务
- 本模块**不开事务**。级联写入、`insertOrUpdateTable`（多次 SQL）必须由调用方包 `@Transactional`。
- `TableDAOImpl.execute(ConnectionCallback)` **新开连接**（`dataSource.getConnection()`），不参与 Spring 事务同步；如需事务内执行，用 `getNamedParameterJdbcTemplate()`。

### 3.4 异常处理
- 底层抛 `org.springframework.dao.DataAccessException` 子类（`IncorrectResultSizeDataAccessException`、`DataRetrievalFailureException` 等）。
- 业务校验失败抛 `com.rick.common.http.exception.BizException`（参考 `EntityCodeDAOImpl.insert` 重复 code）。
- 状态错误抛 `IllegalArgumentException`（参考 `@Version` 过期）。
- **不要 catch 后吞**：`EntityDAOImpl.setPropertyValue`/`getPropertyValue` catch `BeansException` 返回 null 是有意的（属性不存在时静默），其他地方异常应向上传。

### 3.5 Lombok
- 实体基类用 `@SuperBuilder`（支持继承）+ `@Getter`/`@Setter`/`@NoArgsConstructor`。
- 工具类用 `@UtilityClass`（`SqlHelper`、`OperatorUtils`、`PaginationHelper`、`ParsedSqlHelper`、`EntityUtils`、`CodeHelper`、`GridHttpServletRequestUtils`、`Context`）。
- `@FieldDefaults(level = AccessLevel.PRIVATE)` 在需要显式 private 时加（如 `EntityId`）。
- 不可变值对象用 `@Value`（`Grid`、`TableMeta.Reference`、`IdMeta`、`FormatParam`）。
- `@RequiredArgsConstructor` 用于 `TableDAOImpl`、`BaseServiceImpl`、`TableGenerator`（final 字段构造注入）。

### 3.6 Spring
- `@Repository` 标在 `TableDAOImpl`；`@Validated` 标在 `EntityDAOImpl`/`EntityCodeDAOImpl`（让 `@NotBlank` 等校验生效）。
- `@Resource` 优先用于字段注入（与现有代码一致）；构造注入用 `@RequiredArgsConstructor` + `final`。
- 自动配置类用 `@Configuration` + 内部 `static class` 子配置，按 `@ConditionalOn*` 拆分。

### 3.7 SQL 注入面
- `SQLParamCleaner.replaceVars` 的 `${var}` 是**字面替换**，禁止把用户输入拼进去；仅用于表名、固定片段。
- `GridUtils.setOrderParams` 用 `sortableColumns` 白名单防止 `sidx` 注入；**任何接受用户排序列的入口都要传白名单**。
- `dialect.getOrderBy` 实现里 MySQL5 多塞了一个 `id ASC` 兜底，避免排序字段非唯一导致分页抖动。

---

## 4. 常见改动清单

### 4.1 加一个 DAO 方法（实体级）
1. 在 `EntityDAO` 接口加方法签名。
2. 在 `EntityDAOImpl` 实现（必要时也在 `EntityCodeDAOImpl` 重写）。
3. `BaseServiceImpl` 是委托类——若方法是 `EntityDAO` 接口的，必须加委托方法（否则编译失败）。
4. 若方法涉及级联，注意 `threadLocalEntity`/`localStack` 的栈管理：用 `watchSelect(() -> ...)` 包装。

### 4.2 加一种 SQL 方言
1. 在 `DatabaseType` 枚举加常量。
2. 新建 `com.rick.db.repository.support.dialect.XxxDialect extends AbstractDialect`，实现 `pageSql`/`contactString`/`escapeString`/`getOrderBy`/`getType`/`summaryFun`。
3. 在 `SharpDatabaseAutoConfiguration.getDialect(...)` 加 `else if`。
4. 若要支持 DDL 生成，在 `TableGeneratorConfiguration` 加分支 + 新建 `XxxTableGenerator extends TableGenerator`。
5. 在 `application-*.yml` 示例加 `type: Xxx`。

### 4.3 加一个实体字段映射
1. 实体类加字段 + `@Column`（或 `@ManyToOne`/`@Embedded`/`@OneToMany` 等）。
2. `TableMetaResolver.loopAllFields` 已通用化，通常无需改。
3. 若是新类型（如 `Duration`），可能需要：
   - `MySQL5TableGenerator.determineSqlType` 加分支（DDL 推导）。
   - `dbConversionService` 注册 `Converter`（读取时 String→类型）。
   - `EntityDAOImpl.parsingColumnValue` 加序列化分支（写入时类型→DB 值）。
4. 若是 PG json/jsonb，加 `@Column(columnDefinition="json"|"jsonb")`，`TableMeta.appendColumnVar` 与 `EntityDAOImpl.insertOrUpdate0`/`postgresParamsHandler` 已自动处理。

### 4.4 加一个 InsertUpdateCallback
1. 实现 `InsertUpdateCallback<T extends EntityId<ID>, ID>`。
2. 业务侧 `@Bean` 暴露，自动被 `EntityDAOImpl.insertUpdateCallback`（`@Autowired(required=false)`）注入。
3. 参考已有 `ExtendInsertUpdateCallback`：从 `args` Map 取扁平列名（insert 用 `create_by`，update 用 `baseEntityInfo.updateBy`）。

### 4.5 改造 select 自动追加过滤条件（如多租户）
- 推荐做法：继承 `ExtendTableDAOImpl`，重写 `select(...)` 在 `super.select` 前对 SQL 做改写（参考 `addIsDeletedCondition`）。
- 或继承 `ConditionEntityCodeDAOImpl`，给所有 update/delete 注入额外条件（适合固定维度隔离）。
- 不要直接改 `EntityDAOImpl`——它被所有实体共用。

### 4.6 加一个 Grid 自定义映射
- 实现 `JdbcTemplateCallback<T>`，传给 `GridService.query(sql, model, params, callback, countSQL)`。
- 或直接用 `GridUtils.list(sql, params)` 返回 `Grid<Map<String,Object>>`，业务侧自行 flatten（参考 `BaseApi.flattenKeys`）。

### 4.7 加一个 Converter
1. 实现 `org.springframework.core.convert.converter.Converter` 或 `ConverterFactory`。
2. 业务侧 `@Bean` 暴露 → `SharpDatabaseAutoConfiguration.EntityDAOConfiguration` 用 `@Autowired(required=false) List<ConverterFactory>` 收集并注册到 `dbConversionService`。
3. 普通 `Converter` 需在 `dbConversionService()` 方法体里手动 `addConverter(new ...)`（参考 `JsonStringToCollectionConverter`）。

---

## 5. 构建与测试

### 5.1 构建
```bash
# 在仓库根目录
mvn -pl sharp-database2 -am clean install -DskipTests

# 仅编译本模块（依赖已 install）
mvn -pl sharp-database2 clean compile
```

### 5.2 测试
本模块**没有 src/test**，测试在 `sharp-admin` / `sharp-demo` 模块。改动后建议：
```bash
mvn -pl sharp-admin -am test
# 或运行具体测试
mvn -pl sharp-admin test -Dtest=TableGeneratorTest,ComplexModelTest
```

### 5.3 安装到本地仓库
```bash
mvn -pl sharp-database2 -am clean install -DskipTests
```

---

## 6. 陷阱与注意点

### 6.1 SQL 注入面
- `SQLParamCleaner.replaceVars` 的 `${var}` 是字面拼接，**禁止**把任何用户可控值放进去。
- `GridUtils.list(sql, params, countSQL, sortableColumns...)`：`sortableColumns` 白名单是必须的，不传等于允许所有列排序——若 `sidx` 来自用户输入，会被拼进 `ORDER BY`。
- `@Select(value = "...")` 的 SQL 字符串是写死在注解里的，安全；但 `params` 取的属性值会作为命名参数，安全。
- `EntityDAOImpl.update` / `delete` 的 `condition` 字符串由调用方拼接——调用方要保证条件里不出现字符串拼接的用户输入，统一用 `?`/`:name` 占位。

### 6.2 分页性能
- `dialect.formatSqlCount` 默认是 `SELECT COUNT(*) FROM (removeOrders(sql)) temp`——子查询包装。复杂多表 join 的 count 可能慢，应通过 `GridUtils.list(sql, params, countSQL)` 或 `AbstractTableGridService.getCountSQL()` 显式提供优化版。
- `MySQL5Dialect.getOrderBy` 强制在排序列后追加 `id ASC` 兜底（避免分页结果抖动），其他方言没做——给 MySQL 用务必保证 select 字段含 id。
- `size=-1`（全量）会一次性拉取所有行，**不要在数据量大时使用**。
- `TableDAOImpl.batchInsert` 不是真批量（逐条 + KeyHolder）；大批量无主键回填需求时改用 `getNamedParameterJdbcTemplate().getJdbcTemplate().batchUpdate(sql, paramsList)`。

### 6.3 事务边界
- 级联写入（`@OneToMany cascadeSave=true`）会发出 N 条 SQL，**必须 `@Transactional`**。
- `ExtendTableDAOImpl` 的 `select` 自动加 `is_deleted=false` 只对**单表 SELECT** 生效（`SqlSingleTableChecker`）。JOIN/子查询不会自动加，业务侧自行处理。
- `TableDAOImpl.execute(ConnectionCallback)` 不走 Spring 事务同步，慎用。

### 6.4 ThreadLocal 与并发
- `EntityDAOManager.threadLocalEntity` / `localStack` 仅在 `EntityDAOImpl` 调用栈内有效。**不要在异步线程里复用 `EntityDAO`**，也不要在 `@Async` 方法里直接调 `EntityDAO.select`（级联查询会丢失 threadLocal 上下文，可能重复查库，但不会出错）。
- `EntityDAOManager.map` 是 `static HashMap`，**启动后不要再调 `register`**（除非你能保证单线程）；运行期 `getDAO(Class)` 是安全的。

### 6.5 `@Version` 乐观锁
- update 路径会先 `select version from t where id=?` 比对，DB 版本 > 实体版本 → 抛 `IllegalArgumentException`。
- insert 路径自动设版本 = 1。
- `patch` 路径**不**做乐观锁检查（只更非空字段）；如需 patch 也走乐观锁要自行扩展。

### 6.6 `@Id.strategy`
- `ASSIGN`：调用方必须给 id 赋值。
- `SEQUENCE`（默认）：`IdGenerator.getSequenceId()`（sharp-common 的雪花序列）。
- `IDENTITY`：DB 自增，insert 排除 id 列，事后回填。
- 改 `IdGenerator` 的实现会影响所有 `SEQUENCE` 实体，谨慎。

### 6.7 `columnDefinition` 的副作用
- 设了 `columnDefinition`，`Column.nullable`/`comment` 在 DDL 生成时失效（仅 `columnDefinition` 生效）。
- 但 `EntityDAOImpl` 写入路径仍依赖 `columnDefinition` 判断 PG json/jsonb（`"json"`/`"jsonb"` 字符串匹配）——其他值不会触发 `PGobject` 包装。

### 6.8 历史包袱
- 旧 `sharp-database`（`com.rick.db.dto` 包）只有 `.class`，**不要试图反编译或修改它**；新业务一律用 sharp-database2。
- `Oracle10gDialect` / `Oracle11cDialect` / `SQLServer2012TableGenerator` 等是否完整实现待确认；改前先跑 `mvn -pl sharp-admin -am test` 验证。
- `EntityDAOImpl.batchUpdate(...)` 当前返回 `new int[0]`（见 line 1131）；如需真批量，需自行实现。
- `EntityDAOImpl.postgresParamsHandler` 的 `jsonb` 分支用了 `tableMeta.getColumnPropertyNameMap().get(columnName)` 而非 `propertyValue`，疑似笔误（待确认）；改 PG jsonb 写入前先写测试验证。
- `EntityDAOSupport.getEntityDAO` 里 bean 命名规则较复杂（`entityClass.getName().replaceAll(package + ".", "").replace("$", "")` + "DAO"）；改实体包名/内部类时要小心 bean 名冲突。

### 6.9 Spring Boot 版本兼容
- 同时注册了 `spring.factories` 和 `AutoConfiguration.imports`，兼容 Boot 2.x 与 3.x。改自动配置类时两处都要更新（或保持类名稳定）。
- `ParsedSqlHelper` 故意放在 `org.springframework.jdbc.core.namedparam` 包下以访问包私有 `NamedParameterUtils.parseSqlStatement`——改包名会失效。

---

## 7. 提交规范

- 提交信息参考仓库历史：`refactor: 代码优化` / `feat: 优化 xxx` / `fix: xxx`。
- 修改涉及到行为变化时，跑一遍 `sharp-admin` 的测试：`mvn -pl sharp-admin -am test`。
- 提交前确认未引入对 `com.rick.db.dto.*`（旧包）的新依赖。
