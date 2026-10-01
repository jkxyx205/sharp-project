# API.md

> sharp-database2 是 sharp 项目的数据访问核心模块（groupId `com.rick.db`，包根 `com.rick.db`），基于 Spring JDBC（`NamedParameterJdbcTemplate`）封装出一套"实体注解 + DAO 抽象 + SQL 清理器 + 分页/方言"的 ORM-lite 体系。本文档面向调用方，列出对外稳定的 public 入口、配置项与最小用法示例。

---

## 1. 模块定位

一句话：**让业务模块用注解声明实体、用 `EntityDAO` 子类做 CRUD、用 `GridUtils` 做分页查询，无需手写 Mapper/Repository 模板。**

它不是 JPA/MyBatis：没有 Session、没有 XML；所有 SQL 由 `TableMeta` + `SqlHelper` + `SQLParamCleaner` 拼装，最终落到 Spring `NamedParameterJdbcTemplate`。

---

## 2. 依赖坐标

```xml
<dependency>
    <groupId>com.rick.db</groupId>
    <artifactId>sharp-database2</artifactId>
    <version>${sharp.version}</version> <!-- 3.0-SNAPSHOT -->
</dependency>
```

模块自身依赖（来自 `sharp-dependencies` BOM）：
- `com.rick.common:sharp-common`（`IdGenerator`、`JsonUtils`、`EnumUtils`、`ObjectUtils`、`ClassUtils`、`Maps`、`StringUtils`、`HttpServletRequestUtils`、`BizException` 等）
- `org.springframework.boot:spring-boot-starter-jdbc`（HikariCP、`NamedParameterJdbcTemplate`）
- `javax.validation:validation-api` + `org.hibernate.validator:hibernate-validator`
- `javax.servlet:javax.servlet-api`（provided，仅 `GridHttpServletRequestUtils` 用）
- `org.postgresql:postgresql`（provided，PG `json`/`jsonb` 才需要）

---

## 3. 配置项

`application.yml` 前缀 `sharp.database`，对应 `com.rick.db.config.SharpDatabaseProperties`：

| 属性 | 类型 | 默认 | 含义 |
|---|---|---|---|
| `type` | `DatabaseType` 枚举 | `MySQL5` | 数据库方言。可选 `MySQL5` / `MySQL8` / `PostgreSQL` / `Oracle10g` / `Oracle11c` / `SQLServer2012` / `SQLite` |
| `entity-base-package` | String | （无） | 逗号分隔的实体扫描包（支持 `**` 通配）。`EntityDAOSupport` 启动时扫描 `@Table` 注解类并自动注册 `EntityDAO` |
| `init-database-meta-data` | boolean | `false` | 启动时是否读取 JDBC 元数据填充 `DatabaseMetaData.tableColumnMap` / `tablePrimaryKeyMap` |
| `track-if-has-update` | boolean | `false` | `TableDAO.update` 前先 SELECT 比对，无变化则跳过（仅对 Map/args 形参的 update 生效） |
| `database-product-version` | String | 自动 | 由自动配置从 JDBC `DatabaseMetaData` 自动写入，外部一般不设 |

示例：

```yaml
sharp:
  database:
    type: PostgreSQL
    entity-base-package: com.rick.admin.module.**.entity
    init-database-meta-data: false
    track-if-has-update: false
```

### 3.1 多数据源

模块本身在 `SharpDatabaseAutoConfiguration` 上标注 `@ConditionalOnSingleCandidate(DataSource.class)`，**只支持单数据源**。如需多数据源，业务侧需自行声明多个 `NamedParameterJdbcTemplate` / `TableDAOImpl` 并构造 `EntityDAOImpl` 子类，绕开自动配置的默认 `TableDAO` Bean。

### 3.2 覆盖默认 Bean

自动配置注册的关键 Bean（均可在业务侧 `@Configuration` 中用同名 `@Bean` 覆盖）：
- `TableDAO tableDAO` → 默认 `TableDAOImpl`。sharp-admin 覆盖为 `ExtendTableDAOImpl`（自动注入审计字段 + 逻辑删除）。
- `AbstractDialect getDialect` → 按 `type` 选择。
- `GridService gridService`、`EntityCodeIdFillService`、`EntityDAOSupport`、`dbConversionService`、`TableGenerator`。

---

## 4. 核心 public 入口

### 4.1 `TableDAO`（底层表级 DAO）

全名：`com.rick.db.repository.TableDAO`，实现 `com.rick.db.repository.TableDAOImpl`。
职责：直接对"表名 + 列名 + 条件"做 JDBC 操作，不涉及实体映射。Spring `@Repository` Bean。`NamedParameterJdbcTemplate` 为其 final 字段（构造注入）。

| 方法 | 参数 | 返回 | 行为 |
|---|---|---|---|
| `select(String sql, Object... args)` | `sql` 原生 SQL；`args` `?` 占位符顺序参数 | `List<Map<String,Object>>` | 用 `ColumnMapRowMapper` 映射 |
| `select(String sql, Map<String,Object> paramMap)` | `sql` 用 `:name` 占位；`paramMap` 命名参数 | 同上 | 走 `NamedParameterJdbcTemplate.query` |
| `select(Class<E> clazz, String sql, Object... args)` | `clazz` 目标类型；`sql`/`args` 同上 | `List<E>` | 类型决定 RowMapper：Map→ColumnMapRowMapper；纯值对象→`NestedRowMapper`；其他→`SingleColumnRowMapper` |
| `select(Class<E> clazz, String sql, Map<String,Object> paramMap)` | 同上命名参数版 | `List<E>` | 委托下面的回调版 |
| `select(String sql, Map paramMap, JdbcTemplateCallback<E> cb)` | `cb` 自定义回调，拿到 `NamedParameterJdbcTemplate` 自行 query | `List<E>` | 透传 |
| `selectForKeyValue(String sql, Object... args)` / `(String sql, Map paramMap)` | SQL 必须选 2 列 | `Map<K,V>` LinkedHashMap | 第一列作 key，第二列作 value |
| `selectForObject(String sql, Object... args)` / `(String sql, Map)` / `(Class<E>, String, ...)` | 同 select | `Optional<...>` | 结果 0 条→empty；>1 条抛 `IncorrectResultSizeDataAccessException` |
| `exists(String sql, Object... args)` / `(String sql, Map)` | `sql` 一段 SQL 片段（不带 WHERE 也可） | `boolean` | 实现：`sql + " LIMIT 1"` 命中即存在 |
| `update(String table, String columnsCondition, String condition, Object... args)` | `table` 表名；`columnsCondition` 形如 `name = ?, age = ?`；`condition` 如 `id = ?`（可空） | `int` 受影响行数 | 若 `track-if-has-update=true` 且检测无变化返回 0 |
| `update(String table, String columnsCondition, String condition, Map paramMap)` | 命名参数版，`columnsCondition` 形如 `name = :name` | `int` | 同上 |
| `batchUpdate(String table, String columnsCondition, String condition, List<Object[]> paramsList)` | `?` 占位的批量 | `int[]` | 走 `JdbcTemplate.batchUpdate` |
| `deleteIn(String table, String deleteColumn, Collection<?> values)` | | `int` | `>1000` 条自动分批；`<=1000` 用 `IN(:params)` 命名参数 |
| `deleteNotIn(String table, String deleteColumn, Collection<?> values)` | 同上 | `int` | `>1000` 抛 `RuntimeException`（NOT IN 不能超过 1000） |
| `delete(String table, String condition, Object... args)` / `(String table, String condition, Map)` | `condition` 可空 | `int` | 拼 `DELETE FROM table WHERE condition` |
| `insert(String table, String columnNames, Object... args)` | `columnNames` 形如 `id, name` | `int` | 校验列数==参数数；转 Map 走 `SimpleJdbcInsert` |
| `insert(String table, String columnNames, Map paramMap)` | 命名参数版 | `int` | `SimpleJdbcInsert.usingColumns(...).execute(paramMap)` |
| `batchInsert(String table, String columnNames, String columnsCondition, List<Object[]> paramsList)` | `columnsCondition` 形如 `?,?,?`；返回主键列表 | `List<Object>` | 逐条 `PreparedStatement` + `RETURN_GENERATED_KEYS`，收集 keyHolder |
| `insertAndReturnKey(String table, String columnNames, Map params, String... idColumnName)` | `idColumnName` 主键列名 | `Number` | `SimpleJdbcInsert.usingGeneratedKeyColumns(...).executeAndReturnKey` |
| `updateRefTable(String refTable, String keyColumn, String guestColumn, Object keyValue, Collection<?> guestIds)` | 多对多关联表同步 | `void` | 先删除 `key=keyValue AND guest NOT IN(guestIds)`，再插入差集 |
| `execute(String sql)` | DDL/DCL | `void` | `JdbcTemplate.execute` |
| `execute(ConnectionCallback<T>)` | | `<T> T` | **新开连接**（不走 Spring 事务同步），finally 释放 |
| `getNamedParameterJdbcTemplate()` | | `NamedParameterJdbcTemplate` | 暴露底层模板，供高级用法 |

> 注：`TableDAOImpl.execute(ConnectionCallback)` 使用 `dataSource.getConnection()` 而非 `DataSourceUtils`，**不参与 Spring 事务**；如需事务内执行请直接用 `getNamedParameterJdbcTemplate()`。

### 4.2 `EntityDAO<T, ID>` / `EntityCodeDAO<T, ID>`

`com.rick.db.repository.EntityDAO`，实现 `com.rick.db.repository.EntityDAOImpl`。
`com.rick.db.repository.EntityCodeDAO`（继承 `EntityDAO`），实现 `com.rick.db.repository.EntityCodeDAOImpl`（实体须继承 `EntityIdCode<ID>`）。

职责：以实体类（`@Table` 注解）为中心，由 `TableMetaResolver` 解析出列映射后，提供完整 CRUD + 级联 + 乐观锁。所有方法最终委托 `TableDAO`。

#### 构造（业务侧一般不直接 new，由 `EntityDAOSupport` 或 `@Repository` 子类创建）

```java
public EntityDAOImpl(TableDAO tableDAO, Class<T> entityClass, InsertUpdateCallback insertUpdateCallback)
public EntityCodeDAOImpl(TableDAO tableDAO, Class<T extends EntityIdCode<ID>> entityClass, InsertUpdateCallback insertUpdateCallback)
public EntityDAOImpl()  // 无参构造，靠反射读子类泛型；构造时调用 EntityDAOManager.register
```

`EntityDAOSupport.getEntityDAO(Class)` 会按是否 `EntityIdCode` 子类自动选择 `EntityCodeDAOImpl` 或 `EntityDAOImpl`，并作为单例注册到 Spring `BeanFactory`（bean 名 = 实体类简单名小驼峰 + `DAO`）。

#### 关键方法（按用途分组，参数语义见下）

**查询：**
- `Optional<T> selectById(ID id)`
- `List<T> selectByIds(Collection<ID> ids)` — 内部转 `id IN (:ids)`
- `<S> Optional<S> selectById(ID id, String columnName, Class<S> clazz)` — 取单字段
- `<S> Map<ID,S> selectByIds(Collection<ID> ids, String columnName, Class<S> clazz)` — 取多 id 的单字段映射
- `<K,V> Map<K,V> selectForKeyValue(String columns, String condition, Object... args | Map | T example)` — `columns` 必须是 2 列
- `List<T> selectAll()`
- `List<T> select(String condition, Object... args | Map paramMap | T example)` — 默认列 + 级联
- `List<T> select(Map paramMap)` — 用 `TableMeta.getSelectConditionSQL()` 模板，由 `SQLParamCleaner` 清理空条件
- `List<T> select(String columns, String condition, Object...|Map|T)` — 指定列
- `<E> List<E> select(Class<E> clazz, String columns, String condition, Object...|Map|T)` — 投影到任意类型
- `List<T> selectWithoutCascade(String condition, Object... args)` / `(String columns, String condition, ...)` — 不触发 `@OneToMany`/`@ManyToOne`/`@Select` 级联
- `<E> List<E> selectWithoutCascade(Class<E>, String columns, String condition, ...)` — 同上
- `void cascadeSelect(List<T> list)` — 显式触发级联填充
- `boolean exists(ID id)` / `(String condition, ...)` / `(String condition, T example)`
- `long count(String condition, ...)` — `select count(*)` 取首行

**写入：**
- `T insert(T entity)` — 若 id 非空走 `INSERT`，否则按 `@Id.strategy`：`SEQUENCE` 调 `IdGenerator.getSequenceId()`；`IDENTITY` 用 `insertAndReturnKey`
- `T update(T entity)` — id 必须非空；如有 `@Version` 字段，先读 DB 版本号比对，过期抛 `IllegalArgumentException("version field is old")`
- `T patch(T entity)` — id 必须非空；只更新 entity 中非 null 字段（每个字段做 bean validation）
- `T insertOrUpdate(T entity)` — id 为空→insert，否则→update
- `T insertOrUpdate(Map<String,Object> paramMap)` — 把 Map 写回一个新 entity 再保存，并把 id 回写 paramMap
- `Collection<T> insertOrUpdate(Collection<T> list)` — 逐条
- `Collection<T> insertOrUpdateTable(Collection<T> list)` — **整表 upsert + 删除多余行**：先按 id 集合删除不在列表中的行，再逐条 upsert
- `Collection<T> insertOrUpdateTable(Collection<T> list, String refColumnName, Object refValue)` — 限定 `refColumnName=refValue` 范围
- `Collection<T> insertOrUpdateTable(Collection<T> list, boolean deleteItem, Consumer<Collection<ID>> deletedIdsConsumer)` — 可在被删 id 上回调
- `Collection<T> insertOrUpdateTable(Collection<T> list, String refColumnName, Object refValue, boolean deleteItem, Consumer<...>)` — 全参数版
- `T insertWithoutCascade(T)` / `T updateWithoutCascade(T)` / `Collection<T> insertWithoutCascade(Collection<T>)` / `updateWithoutCascade(Collection<T>)` / `insertOrUpdateWithoutCascade(Collection<T>)` — 跳过级联保存（批量更快）
- `Collection<T> insertOrUpdateTable(Collection<T>)` — 见上

**更新（按列）：**
- `int update(String columns, String condition, Object...|Map|T)` — `columns` 形如 `name = :name, age = :age`
- `int updateWithPropertyNames(String propertyNames, String condition, T example)` — 用**属性名**而非列名（内部转列名）
- `int[] batchUpdate(String columns, String condition, List<Object[]> paramsList)` — 当前实现返回 `new int[0]`（待确认）
- `int updateById(String columns, ID id, Object...|Map|T)`
- `int updateByIdWithPropertyNames(String propertyNames, ID id, T example)`
- `int updateByIds(String columns, Collection<ID> ids, Map|T)` — `id IN (:ids)`
- `int updateByIdsWithPropertyNames(String propertyNames, Collection<ID> ids, T example)`

**删除：**
- `int deleteById(ID id)` / `deleteByIds(Collection<ID> ids)` / `deleteAll()` / `delete(String condition, ...)`
- 若实体声明了 `@OneToMany`/`@ManyToMany` 且 `cascadeDelete=true`，删除前会先按主表 id 删子表

**元信息：**
- `TableMeta getTableMeta()` — 实体的表元数据
- `TableDAO getTableDAO()`
- `Map<String,Object> entityToMap(T entity)` — 字段→列名映射后的 Map（级联对象取 id；枚举取 code；Map/Collection/纯对象 JSON 化）

#### `EntityCodeDAO` 扩展（针对有 `code` 的实体）

- `Optional<T> selectByCode(String code)` / `List<T> selectByCodes(Collection<String> codes)`
- `<S> Optional<S> selectByCode(String code, String columnName, Class<S> clazz)`
- `<S> Map<ID,S> selectByCodes(Collection<String> codes, String columnName, Class<S> clazz)`
- `Optional<ID> selectIdByCode(String code)` / `List<ID> selectIdsByCodes(Collection<String> codes)`
- `insert(T)` / `update(T)` 重写：插入前校验 `code` 唯一，更新前校验 `id != ? AND code = ?`；`insertOrUpdate` 自动按 code 回填 id

#### 行为约定

- **事务**：`EntityDAOImpl` 本身不开启事务；调用方需用 `@Transactional`。级联写入是多次 SQL，**务必包在事务里**。
- **线程安全**：单例 Bean，无实例可变状态。`EntityDAOManager` 用 `static Map` 注册；`threadLocalEntity` 与 `localStack` 是 `ThreadLocal`，用于级联查询去重，方法栈结束自动 `remove`。
- **异常**：底层抛 Spring `DataAccessException` 子类；`EntityCodeDAOImpl` 重复 code 抛 `com.rick.common.http.exception.BizException`；乐观锁失败抛 `IllegalArgumentException`。
- **PostgreSQL json/jsonb**：实体字段加 `@Column(columnDefinition = "json")` 或 `"jsonb"`，写入时包装为 `PGobject`，读取时反射取 `getValue`。
- **`@Version` 乐观锁**：仅 update 路径生效；DB 版本号 > 实体版本号即抛异常；insert 时版本初始化为 1。
- **`@Id.strategy`**：`ASSIGN`（外部赋值）/ `SEQUENCE`（默认，`IdGenerator.getSequenceId()` 雪花序列）/ `IDENTITY`（数据库自增，insert 时排除 id 列，事后回填）。

### 4.3 实体基类（`com.rick.db.repository.model`）

| 类 | 继承 | 用途 |
|---|---|---|
| `EntityId<ID>` | — | 仅 `@Id ID id`；重写 `equals/hashCode/toString` 基于 id |
| `EntityIdCode<ID>` | `EntityId<ID>` | 加 `@Column("code") String code`（`@NotBlank` + 正则 `^[0-9a-zA-Z_#/%-]+$`，max 32） |
| `BaseEntity<ID>` | `EntityId<ID>` | `@Embedded BaseEntityInfo baseEntityInfo`，自动带审计 + 逻辑删除字段 |
| `BaseCodeEntity<ID>` | `EntityIdCode<ID>` | 同上，但带 code |
| `BaseCodeDescriptionEntity<ID>` | `BaseCodeEntity<ID>` | 再加 `@NotBlank String description`（max 32） |
| `BaseEntityInfo` | — | `createBy/createTime/updateBy/updateTime/deleted`，列名分别 `create_by/create_time/update_by/update_time/is_deleted` |
| `IdValue` | — | DTO：`Long id` + `@Transient String description`，用于 `@ManyToOne`/`@Select` 关联返回 |
| `IdCodeValue` | — | DTO：`id` + `code` + `@Transient description` |
| `BaseEntityInfoGetter` | interface | 暴露 `getBaseEntityInfo()`，`EntityUtils` 用其判断是否回填审计字段 |
| `DatabaseType` | enum | 见配置项 |

### 4.4 实体注解（`com.rick.db.repository`）

| 注解 | 目标 | 关键属性 |
|---|---|---|
| `@Table` | TYPE | `value` 表名（默认类名转 snake_case）；`comment` 表注释；`referenceColumnId` 默认 `类名_id` |
| `@Id` | FIELD | `strategy` 默认 `SEQUENCE`，可选 `ASSIGN`/`IDENTITY` |
| `@Column` | FIELD | `value` 列名（默认属性名转 snake_case，`@ManyToOne` 默认加 `_id`）；`updatable` 默认 true；`nullable` 默认 true；`comment`；`columnDefinition` 直接写 DDL 类型片段（设了之后 `nullable`/`comment` 失效，仅 DDL 生成时用） |
| `@Transient` | FIELD / 元注解 | 跳过列映射（`@Embedded`/`@OneToMany`/`@ManyToMany`/`@Select` 上都标了 `@Transient`，故这些字段本身不映射为列） |
| `@Embedded` | FIELD | `columnPrefix` 前缀；`comment`。把内嵌对象字段拆平到当前表，属性路径用 `field.subField` |
| `@Version` | FIELD | 乐观锁版本号；insert 设 1，update 时比对并自增 |
| `@ManyToOne` | FIELD | `value` 列名（默认 `xxx_id`）；`cascadeSelect` 默认 true；`cascadeSave` 默认 false。是真实列 |
| `@OneToMany` | FIELD | `mappedBy` 子实体属性名；`joinColumnId` 子表外键列；`cascadeDelete/cascadeSelect/cascadeSave` 默认 true；`cascadeSaveItemDelete` 默认 true（保存前先删子表不在列表中的）；`cascadeSaveItemDeleteCheck` 默认 false（true 时回调 `itemDeletedCheckCallback`）；`oneToOne` 默认 false（true 时取列表首元素） |
| `@ManyToMany` | FIELD | `tableName` 中间表；`joinColumnId` 本方列；`inverseJoinColumnId` 对方列；`cascadeSave` 默认 false（true 时级联保存对方实体，再插中间表）；`cascadeDelete`/`cascadeSelect` 默认 true |
| `@Select` | FIELD | `value` 自定义查询 SQL；`entityClass` 可选（指定后用其 `selectSQL` 做前缀）；`params` 格式 `key@property.path`，从实体属性取参；`cascadeSelect` 默认 true。返回值若是 Collection 取整列，否则取首行 |
| `@ToStringValue` | FIELD | 持久化时用 `value.toString()` 而非 JSON（用于值对象如 `PhoneNumber`） |

### 4.5 SQL 清理与构造工具

#### `com.rick.db.repository.support.SQLParamCleaner`（静态）

`formatSql(String srcSql, Map<String,Object> params)` → `FormatParam { String formatSql; Map<String,Object> formatMap; }`

行为：
1. `${var}` 模板替换：直接把 `params` 中 `var` 的 `toString()` 拼进 SQL（**有 SQL 注入风险，仅用于不可被用户控制的常量片段**）。
2. `:obj.field` → `:objField` 规范化（首字母大写），以便 `BeanWrapper` 嵌套取值。
3. `::type` 后缀清理。
4. 对每个 `column op :param` 片段：参数为 null/空串/空集合 → **整段条件从 SQL 中删除**（自动处理首尾的 `AND`/`OR`/`,`、`WHERE` 后唯一条件、`SET` 后赋值）。
5. `IN (:name)` 展开：集合元素拆成 `:name0,:name1,...`；超 1000 条自动改写为 `col IN(...) OR col IN(...)` 分批。
6. `LIKE` → `UPPER(col) LIKE dialect.contactString(name) dialect.escapeString()`，值里 `%`/`_` 转义。
7. 括号内被清空后递归移除多余的 `()`/`AND`/`OR`。

> 调用方应**只用返回的 `formatSql` 与 `formatMap`**，原 `params` 中的 `${var}` 不会被带回。

#### `com.rick.db.repository.support.SqlHelper`（静态工具）
- `buildSelect(table, columns)` → `SELECT cols FROM table`
- `buildWhere(condition)` → 空条件返回 `""`，否则 `" WHERE condition"`
- `buildSelectWhere(table, columns, condition)`
- `getInsertSQL(table, columnNames)` / `(table, columnNames, columnsCondition)` → 拼 `INSERT INTO ...`
- `buildNullColumnCondition(column, value, holder)` → null 时 `IS NULL`

#### `com.rick.db.repository.support.Constants`
列名常量：`id` / `code` / `description` / `create_by` / `create_time` / `update_by` / `update_time` / `is_deleted`（逻辑删除列名）；`COLUMN_NAME_SEPARATOR_REGEX = "\\s*,\\s*"`；`PARAM_IN_SEPARATOR = ","`。

#### `com.rick.db.repository.JdbcTemplateCallback<T>`
函数式接口：`List<T> select(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String,Object> paramMap)`。用于 `TableDAO.select` 与 `GridService.query` 的自定义映射回调。

### 4.6 分页（`com.rick.db.plugin.page`）

#### `GridUtils`（静态入口，由自动配置注入 `GridService`）
- `Grid<Map<String,Object>> list(String sql, Map<String,Object> params)` — 默认 `params` 中的 `sidx` 为可排序列
- `Grid<Map<String,Object>> list(String sql, Map params, String countSQL)` — 自定义 count SQL
- `Grid<Map<String,Object>> list(String sql, Map params, String countSQL, String... sortableColumns)` — 限定可排序列（防 SQL 注入）
- `List<BigDecimal> numericObject(String sql, Map params)` — 单行数值聚合（`select sum/avg ...`）

#### `GridHttpServletRequestUtils`（静态）
- `list(String sql, HttpServletRequest request)` / `(sql, request, countSQL)` / `(sql, request, Map extendParams, countSQL)`
- `numericObject(String sql, HttpServletRequest request)` / `(sql, request, Map extendParams)`

#### `PageModel`
- `page`（默认 1）、`size`（默认 15，`-1` 表示不分页一次性加载）、`sidx`、`sord`
- `isPageQueryModel()` = `size != -1`；`isAllQueryModel()` = `size == -1`
- 常量：`PARAM_PAGE/PARAM_SIZE/PARAM_SIDX/PARAM_SORD`（即 `"page"/"size"/"sidx"/"sord"`）

#### `QueryModel`
- `QueryModel.of(Map requestMap)` — 从 Map 抽 PageModel + params
- `QueryModel.of(PageModel pageModel)`

#### `Grid<T>`（不可变，`@Builder`）
字段：`page` / `pageSize` / `records`（总条数）/ `totalPages` / `rows` / `additionalInfo`。`Grid.emptyInstance(int pageSize)` 静态工厂。

#### `GridService`（一般不直接调，用 `GridUtils`）
`query(String sql, PageModel model, Map params, [Class<T>|JdbcTemplateCallback], [String countSQL])` → `Grid<T>`。
内部：先 `SQLParamCleaner.formatSql`；分页模式先 count（无 countSQL 用 `dialect.formatSqlCount`）；若 0 条返回空 Grid；分页 SQL 由 `dialect.pageSql` 包装；`size=-1` 模式只加 `ORDER BY`。`size` 上限 1000，`page` 越界自动校正。

### 4.7 Service / DAO 模板（`com.rick.db.plugin`）

- `BaseServiceImpl<D extends EntityDAO<T,ID>, T extends EntityId<ID>, ID> implements EntityDAO<T,ID>` — 把所有方法委托给构造注入的 `baseDAO`；业务 Service 继承它即可复用所有 DAO 方法。
- `BaseCodeServiceImpl<D extends EntityCodeDAO<T,ID>, T extends EntityIdCode<ID>, ID>` — 额外委托 `selectByCode(s)` / `selectIdByCode(s)`。
- `AbstractTableGridService` — 抽象类，子类实现 `getListSQL()`，可选覆盖 `getCountSQL()`/`getSummarySQL()`；提供 `list(Map|HttpServletRequest)` 与 `summary(...)`。
- `DefaultTableGridService` — 用构造传入 listSQL/countSQL/summarySQL 的简单实现。

### 4.8 其他工具

| 类 | 用途 |
|---|---|
| `com.rick.db.plugin.DbUtils` | 原生 JDBC 工具：`executeUpdate(sql, params, Consumer<PreparedStatement>)`、`executeQuery(sql, params)`、`execute(Function<Connection,T>)`。可由 url/username/password 或 DataSource 构造 |
| `com.rick.db.plugin.DbScriptUtils` | `importSQL(Connection, String|File|Reader)` — 按行读 SQL 脚本，遇 `;` 执行一句，跳过 `--` 注释 |
| `com.rick.db.plugin.generator.TableGenerator` | 由 `@Table` 实体类生成 DDL 并执行。按 `DatabaseType` 选 `MySQL5/MySQL8/PostgresSQL/SQLiteTableGenerator`（Oracle/SQLServer 未实现）。可被业务调 `createTable(Class)` |
| `com.rick.db.util.OperatorUtils` | `expectedAsOptional(List)`（>1 抛异常）、`map/groupMap` 列表→Map |
| `com.rick.db.util.PaginationHelper` | `limitPages(total, displayPage, activePage)` → UI 分页窗口 `{startPage, endPage}` |
| `com.rick.db.repository.support.EntityUtils` | `copyPropertiesAndResetAdditionalFields`（拷贝并清空 id/审计/code）、`isEntityClass` |
| `com.rick.db.repository.support.EntityCodeIdFillService` | 用 code 反查 id 并回填到 `BaseCodeEntity`，找不到 code 抛 `BizException` |
| `com.rick.db.repository.support.IdToEntityConverterFactory` | Spring `ConverterFactory<Object, EntityId>`，把 Long/String 转 EntityId（注册到 `dbConversionService`） |
| `com.rick.db.repository.support.DatabaseMetaData` | 静态 `tableColumnMap` / `tablePrimaryKeyMap`，需 `init-database-meta-data=true` |
| `com.rick.db.repository.support.InsertUpdateCallback<T extends EntityId<ID>, ID>` | 函数式：`handler(boolean insert, T entity, Map args)`，每次 insert/update 后回调 |
| `com.rick.db.repository.support.baseinfo.ExtendTableDAOImpl` | **审计 + 逻辑删除增强的 TableDAO**（覆盖 `TableDAOImpl`）。`update` 自动拼 `update_by/update_time`；`insert` 自动拼四审计字段 + `is_deleted=false`；`delete` 转为 `UPDATE is_deleted=true`（仅对已注册实体的表）；`select` 自动追加 `AND is_deleted=false`（仅单表查询，由 `SqlSingleTableChecker` 判定）。`getUserId()` 默认返回 1L，子类可覆盖取真实用户。 |
| `com.rick.db.repository.support.baseinfo.ExtendInsertUpdateCallback` | 把 ExtendTableDAOImpl 写入的审计字段回填到 `BaseEntity`/`BaseCodeEntity` 的 `baseEntityInfo` 对象上 |
| `com.rick.db.repository.support.category.CategoryEntityCodeDAOImpl<T extends EntityIdCode & RowCategory, ID, E extends Enum<E>>` | 带 `category` 维度的 Code DAO：`selectByCategoryAndCode(E, code)`、`selectAll(E)`、`insertOrUpdate(E, list)`；插入/更新按 `(code, category)` 唯一 |
| `com.rick.db.repository.support.category.RowCategory<E>` | 接口：`getCategory()` / `setCategory(E)` |
| `com.rick.db.repository.support.ConditionEntityCodeDAOImpl` | 抽象子类，让子类覆盖 `getMergeArgsCondition/getMergeMapCondition` 给所有 update/delete 注入额外条件 |
| `com.rick.db.config.Context` | 静态持有 `AbstractDialect`（`Context.getDialect()`） |

---

## 5. 使用示例

### 5.1 定义实体

```java
@Table(value = "t_student", comment = "学生表")
@Getter @Setter @NoArgsConstructor @SuperBuilder
public class Student extends BaseCodeEntity<Long> {   // 带 code + 审计字段

    @NotBlank
    @Column(columnDefinition = "varchar(16) not null comment '姓名'") // columnDefinition 优先级最高
    String name;

    @NotNull
    GenderEnum gender;                 // 枚举默认存 code（EnumUtils.getCode），列名 gender

    @Column(value = "birthday", comment = "出生日期")
    LocalDate birthday;

    @Embedded(columnPrefix = "unit_")  // 内嵌 DictValue，字段拆到 unit_code/unit_description...
    DictValue unit;

    @Column(columnDefinition = "json")  // PostgreSQL json；其他库当 TEXT/JSON 存
    List<Document> attachments;

    @OneToMany(mappedBy = "student", joinColumnId = "student_id")  // 子表外键 student_id
    List<Score> scores;
}
```

### 5.2 写一个 DAO（二选一）

```java
// 方式 A：直接继承，让 EntityDAOSupport 自动注册（需 entity-base-package 扫描到实体包）
@Repository
public class StudentDAO extends EntityCodeDAOImpl<Student, Long> { }

// 方式 B：自定义 TableDAO / InsertUpdateCallback（一般不需要）
public class StudentDAO extends EntityCodeDAOImpl<Student, Long> {
    public StudentDAO(TableDAO tableDAO, InsertUpdateCallback callback) {
        super(tableDAO, Student.class, callback);
    }
}
```

### 5.3 写 Service

```java
@Service
public class StudentService extends BaseCodeServiceImpl<StudentDAO, Student, Long> {
    public StudentService(StudentDAO dao) { super(dao); }
}
```

### 5.4 CRUD

```java
Student s = Student.builder().code("S001").name("张三").build();
studentService.insert(s);                 // 返回带 id 的实体
s = studentService.selectByCode("S001").orElseThrow();
studentService.patch(s);                   // 只更非空字段
studentService.insertOrUpdate(s);          // id 空则 insert 否则 update
studentService.deleteById(1L);
List<Student> all = studentService.selectAll();
List<Student> zhang = studentService.select("name LIKE :name",
        Maps.of("name", "张%"));
```

### 5.5 启用审计 + 逻辑删除（覆盖默认 TableDAO）

```java
@Configuration
public class SharpConfig {
    @Bean
    public ExtendTableDAOImpl tableDAO(NamedParameterJdbcTemplate npjt) {
        return new ExtendTableDAOImpl(npjt);
    }
}
// 此后所有 EntityDAO 自动走 ExtendTableDAOImpl；
// select 自动追加 is_deleted=false，delete 变为 UPDATE is_deleted=true，
// insert/update 自动写 create_by/create_time/update_by/update_time。
```

### 5.6 分页查询（HTTP）

```java
@GetMapping("/students")
public Grid<Map<String, Object>> page(HttpServletRequest req) {
    String sql = "SELECT id, name, gender FROM t_student WHERE 1=1 "
               + " AND name LIKE :name AND gender = :gender";
    return GridHttpServletRequestUtils.list(sql, req, null, "name", "gender");
    // 第 4+ 参数是允许排序的列白名单
}
```

请求参数：`?page=1&size=15&sidx=name&sord=asc&name=张&gender=M`。`size=-1` 不分页。

### 5.7 分页查询（程序内）

```java
PageModel pm = new PageModel(2, 15, "name", "asc");
Grid<Student> g = gridService.query(
    "SELECT * FROM t_student WHERE name LIKE :name",
    pm, Maps.of("name", "张%"), Student.class);
g.getRows();   // List<Student>
g.getRecords(); g.getTotalPages();
```

### 5.8 批量插入（不级联）

```java
List<Student> list = ...;
studentDAO.insertWithoutCascade(list);  // 走 JdbcTemplate.batchUpdate，比逐条快
```

### 5.9 整表 upsert（主子表同步）

```java
// 把传入的 scores 完全替换 student_id=1 的所有子记录
scoreDAO.insertOrUpdateTable(scores, "student_id", 1L, true, deletedIds -> {
    log.warn("被删除的 score id: {}", deletedIds);
});
```

### 5.10 自定义 `@Select` 关联

```java
@Embedded(columnPrefix = "user_")
@Select(
   value = "select id, code, name as description from sys_user where id = :id",
   params = "id@operator.id",   // 从本实体的 operator.id 属性取参
   nullWhenParamsIsNull = "id"  // 已弃用：当前实现遇到任一参数为 null 直接置空，无需配置
)
IdCodeValue operator;
```

---

## 6. 与其它 sharp-* 模块的关系

- **sharp-common**：基础工具（`IdGenerator`、`JsonUtils`、`EnumUtils`、`BizException`、`HttpServletRequestUtils`、Spring 转换器 `JsonStringToObjectConverterFactory` 等）。本模块 Bean 注册时把这些转换器一并塞进 `dbConversionService`。
- **sharp-admin / sharp-demo**：业务侧。典型用法：实体继承 `BaseEntity`/`BaseCodeEntity`，DAO 继承 `EntityDAOImpl`/`EntityCodeDAOImpl`，Service 继承 `BaseServiceImpl`/`BaseCodeServiceImpl`，Controller 继承 `BaseApi`，分页用 `GridUtils`/`GridHttpServletRequestUtils`。
- **sharp-meta**：字典（`DictValue`/`DictType`）等元数据，与 `IdCodeValue` 一起用于 `@Embedded` 关联。
- **sharp-fileupload**：`Document` 等值对象，存为 JSON 列时配合 `@Column(columnDefinition="json")`。
- **sharp-generator / sharp-excel / sharp-report**：通过 `EntityDAO` / `TableDAO` / `GridUtils` 读数据。
- **旧版 sharp-database**：已废弃（仓库仅存 target/.class）。本模块是它的重写，groupId 同为 `com.rick.db`，但包从 `com.rick.db.dto` 迁到 `com.rick.db.repository(.model/.plugin/.support)`。新业务一律用 sharp-database2。
