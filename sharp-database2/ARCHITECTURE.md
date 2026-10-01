# ARCHITECTURE.md

> sharp-database2 的内部架构、关键抽象、数据流、依赖与扩展点。

---

## 1. 包结构树

```
com.rick.db
├── config/                # Spring Boot 自动配置 + 全局属性 + Dialect 持有器
│   ├── SharpDatabaseAutoConfiguration   # @Configuration 入口，注册核心 Bean
│   ├── SharpDatabaseProperties          # @ConfigurationProperties(prefix="sharp.database")
│   └── Context                          # @UtilityClass 静态持有 AbstractDialect
├── repository/            # ORM 核心：DAO 接口与实现、注解、TableMeta、级联、SQL 清理
│   ├── TableDAO / TableDAOImpl          # 表级 DAO（无实体映射）
│   ├── EntityDAO / EntityDAOImpl        # 实体级 DAO（CRUD + 级联 + 乐观锁）
│   ├── EntityCodeDAO / EntityCodeDAOImpl# 带 code 的实体 DAO
│   ├── EntityDAOSupport                 # 实体扫描 + DAO 注册中心
│   ├── EntityDAOManager                 # 静态 Map<Class, EntityDAO> + ThreadLocal 级联去重
│   ├── JdbcTemplateCallback             # 函数式回调接口
│   ├── *.java 注解                       # @Table/@Id/@Column/@Embedded/@Version/@ManyToOne/@OneToMany/@ManyToMany/@Select/@Transient/@ToStringValue
│   ├── model/                           # 实体基类：EntityId/EntityIdCode/BaseEntity/BaseCodeEntity/BaseCodeDescriptionEntity/BaseEntityInfo/IdValue/IdCodeValue/DatabaseType/...
│   └── support/                         # 内部支持类
│       ├── TableMeta / TableMetaResolver# 实体→表元数据解析
│       ├── SqlHelper / Constants        # SQL 拼装 + 列名常量
│       ├── SQLParamCleaner              # 动态 SQL 清理（空参数剔除、IN 展开、LIKE 转义）
│       ├── InsertUpdateCallback         # 写入后回调
│       ├── EntityUtils / CodeHelper    # 工具
│       ├── EntityCodeIdFillService      # code→id 反查
│       ├── IdToEntityConverterFactory  # Spring 转换器
│       ├── DatabaseMetaData             # JDBC 元数据缓存
│       ├── TriConsumer                 # 三参函数式接口
│       ├── dialect/                    # 数据库方言：AbstractDialect + MySQL5/8/Postgres/SQLite/SQLServer2012/Oracle10g/Oracle11c
│       ├── baseinfo/                   # 审计 + 逻辑删除增强：ExtendTableDAOImpl / ExtendInsertUpdateCallback / SqlSingleTableChecker
│       ├── category/                   # category 维度：CategoryEntityCodeDAOImpl / RowCategory
│       └── (ConditionEntityCodeDAOImpl)# 抽象子类：给 update/delete 注入额外条件
├── plugin/                # 上层插件：Service 模板、分页、DDL 生成、原生 JDBC
│   ├── BaseServiceImpl / BaseCodeServiceImpl
│   ├── DbUtils / DbScriptUtils
│   ├── page/                            # GridService/GridUtils/PageModel/QueryModel/Grid/GridHttpServletRequestUtils
│   ├── generator/                       # TableGenerator + MySQL5/MySQL8/PostgresSQL/SQLiteTableGenerator
│   └── table/                           # AbstractTableGridService / DefaultTableGridService
└── util/
    ├── OperatorUtils                     # expectedAsOptional / map / groupMap
    └── PaginationHelper                 # limitPages（UI 分页窗口）

org.springframework.jdbc.core.namedparam
└── ParsedSqlHelper                      # 越权放包内以访问 NamedParameterUtils.parseSqlStatement
```

---

## 2. 关键抽象与关系

### 2.1 DAO 两层体系

```
                 ┌───────────────────────────────────────────┐
                 │            EntityDAOImpl<T, ID>            │  ← 实体级（带 TableMeta、级联、乐观锁）
                 │  insert/update/patch/selectById/...       │
                 │  委托                                     │
                 └───────────────┬───────────────────────────┘
                                 │ uses
                                ▼
                 ┌───────────────────────────────────────────┐
                 │              TableDAOImpl                  │  ← 表级（无实体映射）
                 │  select/update/delete/insert/batchInsert   │
                 │  持有 NamedParameterJdbcTemplate           │
                 └───────────────┬───────────────────────────┘
                                 │ delegates to
                                ▼
                 ┌───────────────────────────────────────────┐
                 │   Spring NamedParameterJdbcTemplate        │
                 │   + SimpleJdbcInsert + KeyHolder            │
                 └───────────────────────────────────────────┘

                 ┌───────────────────────────────────────────┐
                 │  EntityCodeDAOImpl<T extends EntityIdCode> │  ← 在 EntityDAO 基础上加 code 唯一校验、code→id 回填
                 └───────────────────────────────────────────┘
                 ┌───────────────────────────────────────────┐
                 │  ExtendTableDAOImpl (extends TableDAOImpl) │  ← 业务侧覆盖：审计 + 逻辑删除
                 └───────────────────────────────────────────┘
                 ┌───────────────────────────────────────────┐
                 │  CategoryEntityCodeDAOImpl                 │  ← category 维度
                 └───────────────────────────────────────────┘
```

业务侧典型路径：
```
Entity (@Table) ──extends──> BaseEntity / BaseCodeEntity
   │
   ├── DAO ──extends──> EntityCodeDAOImpl / EntityDAOImpl   (@Repository 子类，或由 EntityDAOSupport 自动 new)
   │
   ├── Service ──extends──> BaseServiceImpl / BaseCodeServiceImpl  (委托 baseDAO)
   │
   └── Controller ──extends──> BaseApi  (sharp-admin 中的基类，用 GridUtils 分页 + EntityDAO CRUD)
```

### 2.2 TableMeta 与解析

`TableMetaResolver.resolve(Class)` 递归扫描字段（含 `@Embedded` 嵌套），生成：
- `tableName` / `referenceColumnId`（来自 `@Table`）
- `fieldColumnNameMap` / `fieldPropertyNameMap` / `columnPropertyNameMap` / `columnNameMap`
- `idMeta`（`IdMeta<ID>`：idClass、`@Id`、列名、属性名、Field）
- `versionField`（`@Version` 标注的字段）
- `referenceMap`：`Field -> Reference{ field, referenceClass, manyToOne, manyToMany, oneToMany, select }`
- 缓存的 SQL 片段：`selectColumn`（带 `AS "property"` 别名）、`updateColumn`（不含 id 与 `updatable=false`）、`columnNames`

`TableMeta` 还提供：
- `getSelectSQL(columns)` / `getSelectConditionSQL()`（`SELECT ... WHERE col1=:col1 AND col2=:col2`，配合 `SQLParamCleaner`）
- `appendColumnVar(columns, namedVar, delimiter, columnVar)` — 把列名数组转成 `col1=:prop1, col2=:prop2` 或 `?,?,?`；PostgreSQL 下 `columnDefinition=json/jsonb` 自动追加 `::json`/`::jsonb`

### 2.3 级联机制

`EntityDAOImpl.selectReference(list)` 处理四种关联：

- **`@ManyToOne`**：取主表外键集合 → `referenceDAO.selectByIds` → 按主键 Map 回填到主实体字段。也支持从 `threadLocalEntity` 中复用同事务内已查过的对象。
- **`@OneToMany`**：按子表外键 `IN (:ids)` 查子实体，按 `mappedBy` 属性值分组回填。`oneToOne=true` 时取首元素。
- **`@ManyToMany`**：先查中间表 `(joinColumnId, inverseJoinColumnId)`，再查对方实体集合，按主 id 分组回填。也复用 `threadLocalEntity`。
- **`@Select`**：按 `params` 表达式从主实体取参，执行自定义 SQL，结果集为 Collection 取整列，否则取首行。参数任一为 null 直接置空（不查库）。

`EntityDAOManager.threadLocalEntity` + `localStack` 是 ThreadLocal 栈：级联查询时把当前批次实体入栈，避免循环引用导致的重复查询；栈归零时 `remove`。

级联写入（`insertOrUpdate0`，`cascade=true`）：
- `@ManyToOne(cascadeSave=true)`：先保存对方实体，再保存自己
- `@OneToMany(cascadeSave=true)`：保存自己后，遍历子集合，给每个子设 `mappedBy` 字段（实体或 id），再 `insertOrUpdate`；可选先删不在集合中的子行（`cascadeSaveItemDelete`），删除前可选回调 `itemDeletedCheckCallback` / `cascadeSaveItemDeleteCheck`
- `@ManyToMany(cascadeSave=true)`：先保存对方实体集合，删除中间表当前主方行，再插入新中间表行

### 2.4 SQL 清理器（SQLParamCleaner）

这是模块的"动态 SQL"实现，绕过 MyBatis-XML 的写法。流程：

```
入参 (srcSql, params)
   │
   ▼ replaceVars: ${var} 字面替换
   │
   ▼ normalizeSqlParams: :obj.field → :objField
   │
   ▼ ParsedSqlHelper.get(srcSql): 取所有 :name（访问 Spring 包私有 API）
   │
   ▼ 遍历 fullPattern (col op :name) 片段：
   │     param 为 null/空 → 从 SQL 删除该片段（处理 WHERE/SET/AND/OR/逗号/括号）
   │     IN (:name) → 展开 :name0,:name1,...
   │     LIKE → UPPER(col) LIKE CONCAT(...) escape '\\'
   │     其他 → 写入 formatMap
   ▼ 未匹配的 :paramName → 空值替换为 ''，非空写入 formatMap
   ▼ rightParentheses: 递归删除空 () 与孤儿 AND/OR
   ▼ changeInSQL: IN 超过 1000 个值时拆分为 OR 子句
返回 (formatSql, formatMap)
```

### 2.5 方言（Dialect）

```
AbstractDialect（abstract）
   ├── pageSql(sql, PageModel)        # 各库分页 SQL
   ├── formatSqlCount(sql)            # SELECT COUNT(*) FROM (removeOrders(sql)) temp
   ├── wrapSordString(sql, sidx, sord)# 不分页时仅加排序
   ├── removeOrders(sql)             # 去 ORDER BY
   ├── contactString(name)           # LIKE 拼接
   ├── escapeString()                # LIKE 转义
   ├── getOrderBy(prefix, col, asc, sortableCols)
   ├── summaryFun(column)            # sum 聚合表达式
   └── getType() → DatabaseType

实现：
   MySQL5Dialect/MySQL8Dialect  : LIMIT offset, size
   SQLiteDialect                : LIMIT offset, size
   PostgresDialect              : LIMIT size OFFSET offset
   SQLServer2012Dialect         : OFFSET ... ROWS FETCH NEXT ... ROWS ONLY
   Oracle10gDialect/Oracle11cDialect : 占位（待确认实现进度）
```

`Context.getDialect()` 全局静态持有；`SQLParamCleaner` 也由 `SharpDatabaseAutoConfiguration.gridService(...)` 注入 dialect。

### 2.6 分页数据流（GridService）

```
GridUtils.list(sql, params, countSQL, sortableCols)
   │
   ▼ QueryModel.of(params) → PageModel + params
   ▼ GridUtils.setOrderParams: 校验 sidx ∈ sortableCols，否则清空
   ▼ GridService.query:
        SQLParamCleaner.formatSql(sql, params) → (sql, params)
        if 分页模式 (size != -1):
            countSQL = countSQL ?: dialect.formatSqlCount(sql)
            records = tableDAO.select(Integer.class, countSQL, params).get(0)
            if records == 0: return Grid.emptyInstance
            校正 page/size（size 上限 1000）
        dialect.pageSql(sql, model) 或 dialect.wrapSordString(...)
        rows = tableDAO.select(callback, sql, params)
        if 全量模式 (size == -1):
            records = rows.size(); totalPages = records > 0 ? 1 : 0
   ▼ Grid.builder() 组装返回
```

### 2.7 审计 + 逻辑删除（ExtendTableDAOImpl）

业务侧 `@Bean` 覆盖 `TableDAO` 时改用 `ExtendTableDAOImpl`。其拦截点：

| 方法 | 增强 |
|---|---|
| `update(table, cols, cond, args/map)` | 在 SET 子句前拼 `update_by=?, update_time=?`（值 = `getUserId()` + `LocalDateTime.now()`） |
| `insert(table, cols, map)` | 自动加 `create_by/create_time/update_by/update_time/is_deleted=false`（仅当该表名在 `tableNameDAOMap`，即对应一个注册实体） |
| `insertAndReturnKey` | 同 insert |
| `delete(table, cond, ...)` | 若表在 `tableNameDAOMap`：转为 `UPDATE is_deleted=true WHERE cond AND is_deleted=false`；否则按物理删除 |
| `select(clazz, sql, args)` / `select(sql, map, cb)` / `select(sql, args)` | 若 SQL 是单表查询（`SqlSingleTableChecker`）且未显式带 `is_deleted`：自动追加 `WHERE/AND is_deleted = false` |

`ApplicationReadyEvent` 时把 `EntityDAOManager.getAllEntityDAO()` 收集成 `tableNameDAOMap`，决定哪些表享受审计/逻辑删除。

`ExtendInsertUpdateCallback` 是配套的 `InsertUpdateCallback`，把 TableDAO 写入的扁平审计字段（`create_by` 等）回填到实体的 `baseEntityInfo` 嵌套对象上。

`SqlSingleTableChecker` 是一个正则/括号计数判定器：必须 `SELECT` 起始、无子查询、无 JOIN、无 UNION、FROM 子句只一张表。

---

## 3. 分层与数据流

### 3.1 一条查询从入口到 JDBC

```
HTTP 请求 (?page=2&size=15&sidx=name&sord=asc&name=张)
   │
   ▼ Controller: GridHttpServletRequestUtils.list(sql, req)
   │
   ▼ GridUtils.list(sql, params)  ←  静态代理，持有 GridService
   │
   ▼ GridService.query:
        SQLParamCleaner.formatSql → 清空参数片段 + IN 展开 + LIKE 转义
        dialect.formatSqlCount / dialect.pageSql → 拼 count + 分页 SQL
   │
   ▼ TableDAOImpl.select(Class<E>, sql, paramMap):
        determineRowMapper(clazz) → NestedRowMapper / ColumnMapRowMapper / SingleColumnRowMapper
        NamedParameterJdbcTemplate.query(sql, paramMap, rowMapper)
   │
   ▼ JdbcTemplate → PreparedStatement → ResultSet
   │
   ▼ NestedRowMapper.mapRow:
        遍历列，按 column→propertyName（snake→camel）找 PropertyDescriptor
        用 dbConversionService 转换（含 PGobject→String、JSON→对象、String→LocalDate 等）
        bw.setAutoGrowNestedPaths(true) + setConversionService → 嵌套属性自动实例化赋值
   │
   ▼ Grid 组装 → 返回
```

### 3.2 一条写入从入口到 JDBC（带级联）

```
Service.insertOrUpdate(entity)
   │
   ▼ EntityDAOImpl.insertOrUpdate0(entity, insert=true, cascade=true):
        1. threadLocalEntity.get().add(entity)        ← 入栈防循环
        2. 若有 @ManyToOne(cascadeSave=true)：先 save 对方
        3. getArgsFromEntity(entity, false)：字段→列值 Map
           - 枚举 → EnumUtils.getCode
           - Map/Collection/纯对象 → JsonUtils.toJson
           - @ToStringValue → toString()
           - @ManyToOne/@OneToMany 字段 → 取其 id
           - PostgreSQL json/jsonb → PGobject 包装
        4. @Version 字段 → 设 1
        5. id 为空 + SEQUENCE → IdGenerator.getSequenceId()
           id 为空 + IDENTITY → tableDAO.insertAndReturnKey(...) 回填
           id 非空 → tableDAO.insert(table, columnNames, args)
        6. InsertUpdateCallback.handler(...)（如 ExtendInsertUpdateCallback 回填 baseEntityInfo）
        7. @OneToMany(cascadeSave=true)：删多余子行 → 给子设 mappedBy → 逐条 insertOrUpdate
           @ManyToMany(cascadeSave=true)：删中间表当前主方行 → 插入新行
   │
   ▼ TableDAOImpl: SimpleJdbcInsert.execute(paramMap) 或 NamedParameterJdbcTemplate.update
   │
   ▼ JdbcTemplate → PreparedStatement.executeUpdate
```

---

## 4. 对外依赖

### 4.1 sharp-* 内部依赖
- **sharp-common**：`IdGenerator`（雪花序列）、`JsonUtils`、`EnumUtils`、`ObjectUtils.mayPureObject`、`ClassUtils`、`Maps`、`StringUtils`、`SFunction`、`HttpServletRequestUtils`、`BizException`、`ValidatorHelper`、Spring 转换器（`StringToLocalDateConverterFactory`、`CodeToEnumConverterFactory`、`JsonStringToObjectConverterFactory` 等）、`org.springframework.jdbc...` 兼容代码。

### 4.2 第三方依赖
| 库 | 用途 |
|---|---|
| `spring-boot-starter-jdbc` | `NamedParameterJdbcTemplate` / `JdbcTemplate` / `SimpleJdbcInsert` / `KeyHolder` |
| `spring-boot-autoconfigure` | `@AutoConfigureAfter` / `@ConditionalOnSingleCandidate` / `@ConfigurationProperties` |
| `hibernate-validator` + `validation-api` | 实体字段约束；`EntityDAOImpl` 是 `@Validated`，`patch/update` 时按属性做校验 |
| `lombok` | 实体基类的 `@SuperBuilder/@Getter/@Setter/@Value` 等；`@UtilityClass` 工具类 |
| `commons-lang3` / `commons-collections4` | `StringUtils`/`ObjectUtils`/`CollectionUtils`/`MapUtils`/`SetUtils` |
| `guava` | `Lists`/`Sets`/`Maps.newHashMapWithExpectedSize` |
| `jackson`（jsr310） | `@JsonSerialize(ToStringSerializer)` 等 |
| `postgresql`（provided） | `PGobject`，仅 PG json/jsonb 字段用 |

### 4.3 不依赖
不依赖 MyBatis、JPA/Hibernate、PageHelper、Druid。连接池默认走 spring-boot-starter-jdbc 自带的 HikariCP。

---

## 5. 数据库方言与多数据源

### 5.1 方言
- 由 `sharp.database.type` 选择，`SharpDatabaseAutoConfiguration.getDialect(...)` 实例化并 `Context.setDialect`。
- `DatabaseType` 枚举：`MySQL5` / `MySQL8` / `PostgreSQL` / `Oracle10g` / `Oracle11c` / `SQLServer2012` / `SQLite`。
- `Oracle10gDialect` / `Oracle11cDialect` 类已存在但 `pageSql` 等抽象方法是否完整实现待确认（自动配置分支已就位）。
- DDL 生成（`TableGenerator`）仅 MySQL5/MySQL8/PostgresSQL/SQLite 实现；Oracle/SQLServer 在 `TableGeneratorConfiguration` 中是 TODO。

### 5.2 多数据源
- 自动配置 `@ConditionalOnSingleCandidate(DataSource.class)`，**默认不支持多数据源**。
- 多源场景需业务侧自行声明 `DataSource` / `NamedParameterJdbcTemplate` / `TableDAOImpl`，并绕过 `SharpDatabaseAutoConfiguration` 的默认 `TableDAO` Bean。`EntityDAOSupport.getEntityDAO` 默认注入的是主 `TableDAO`；如需不同实体绑定不同源，需自行 new `EntityDAOImpl(tableDAO, entityClass, callback)` 并 `EntityDAOManager.register`。

---

## 6. 扩展点

### 6.1 可继承的抽象 DAO

| 父类 | 扩展点 |
|---|---|
| `EntityDAOImpl` | 子类化以覆盖 `select`/`insert`/`update` 行为；可重写 `protected itemDeletedCheckCallback(EntityDAO, Collection<ID>)` 做删除前业务校验；`protected handlerReferenceListBefore(EntityDAO, List, refCol, refVal)` 在级联保存子表前介入 |
| `EntityCodeDAOImpl` | 重写 `insert`/`update` 改变 code 唯一策略 |
| `ConditionEntityCodeDAOImpl`（abstract） | 实现 `getMergeArgsCondition/getMergeMapCondition/getMergeArgs/getMergeMap`，给所有 update/delete 注入额外条件 |
| `CategoryEntityCodeDAOImpl` | 已实现 `(category, code)` 维度的 CRUD，可直接用或再子类化 |
| `TableDAOImpl` / `ExtendTableDAOImpl` | 业务侧 `@Bean` 覆盖 `TableDAO`，重写 `getUserId()` 接真实用户上下文，或扩展 `addInsertInfo(Map)` |
| `AbstractTableGridService` | 实现 `getListSQL()`，可选 `getCountSQL()` / `getSummarySQL()`，复用分页框架 |

### 6.2 钩子与回调

- `InsertUpdateCallback<T, ID>`：每次 insert/update 完成后触发，可读 `args` Map（写入的字段值）。`ExtendInsertUpdateCallback` 是其实现。
- `Consumer<Collection<ID>> deletedIdsConsumer`：`insertOrUpdateTable` 删除多余行后回调。
- `itemDeletedCheckCallback` / `handlerReferenceListBefore`：见上。
- `JdbcTemplateCallback<T>`：自定义 `select` 的 ResultSet→对象映射。

### 6.3 Spring `ConversionService`
自动配置注册 `dbConversionService`（`DefaultFormattingConversionService`），注入到 `EntityDAOImpl` 用于实体回写时的类型转换（PGobject→String、JSON→POJO、String→LocalDate、Id→Entity 等）。业务侧可通过实现 `ConverterFactory` Bean 自动被收集（`@Autowired List<ConverterFactory>`）追加。

### 6.4 DDL 生成
`TableGenerator.createTable(Class)` 由实体注解生成 DDL 并 `jdbcTemplate.execute`。`@Column.columnDefinition` 优先；否则按 Java 类型推导（`Long`→`BIGINT`、`LocalDateTime`→`DATETIME`、枚举→`ENUM(...)` 或 `INT`（看 `getCode()` 返回类型）、`Map/List/JsonValue`→`TEXT`/`JSON`、纯对象→`JSON`）。`@OneToMany`/`@ManyToMany` 触发建中间表。

---

## 7. 配置与启动流程

```
Spring Boot 启动
   │
   ▼ spring.factories / AutoConfiguration.imports:
   │   EnableAutoConfiguration = SharpDatabaseAutoConfiguration
   │
   ▼ SharpDatabaseAutoConfiguration (@ConditionalOnSingleCandidate(DataSource), @AutoConfigureAfter(DataSourceAutoConfiguration))
   │
   ├── GridServiceConfiguration:
   │     - tableDAO(NamedParameterJdbcTemplate, props):
   │         若 props.initDatabaseMetaData=true → DatabaseMetaData.initTableMapping
   │         读 JDBC metadata 设 props.databaseProductVersion
   │         new TableDAOImpl(npjt)
   │     - getDialect(props) → 按 type 实例化，Context.setDialect
   │     - gridService(tableDAO, dialect): SQLParamCleaner.setDialect; new GridService
   │     - getEntityCodeIdFillService()
   │
   ├── EntityDAOConfiguration:
   │     - entityDAOSupport(List<EntityDAO>): new EntityDAOSupport
   │     - validatorHelper(Validator) (@ConditionalOnMissingBean + @ConditionalOnBean Validator)
   │     - dbConversionService(): 收集所有 ConverterFactory + 默认转换器 + IdToEntityConverterFactory
   │
   ├── UtilGridServiceConfiguration: @Autowired GridService → GridUtils.setGridService（静态注入）
   │
   └── TableGeneratorConfiguration: 按 dialect.type 选 TableGenerator 子类

   ▼ EntityDAOSupport @PostConstruct init():
        扫描 sharp.database.entity-base-package 下的 @Table 类，
        每个调 getEntityDAO(clazz) → new EntityDAOImpl/EntityCodeDAOImpl + BeanFactory.registerSingleton
   ▼ ApplicationReadyEvent（若用 ExtendTableDAOImpl）:
        tableNameDAOMap = EntityDAOManager.getAllEntityDAO().toMap(tableName, dao)
```

业务侧典型覆盖：
- sharp-admin 在 `SharpConfig` 里 `@Bean ExtendTableDAOImpl tableDAO(npjt)` 替换默认 `TableDAOImpl`，让所有自动注册的 `EntityDAO` 走审计增强路径。

---

## 8. 线程安全与并发

- `TableDAOImpl` / `EntityDAOImpl` / `EntityDAOSupport` 均为单例 Bean，无可变实例字段（除 `EntityDAOSupport` 注入的不可变 `TableDAO`）。
- `EntityDAOManager.map` 是静态 `HashMap`（非并发容器），仅在启动 `@PostConstruct` 与 `init()` 写入；运行期只读，安全。
- `threadLocalEntity` / `localStack` 是 `ThreadLocal`，方法栈内 `watchSelect` 配对 inc/dec，归零 `remove`。**不要跨线程传递 EntityDAO 调用上下文**。
- `Context.dialect` 静态字段，启动后只读。
- `DatabaseMetaData.tableColumnMap` 静态 HashMap，仅启动期 `initTableMapping` 写入。
- `GridUtils.GRID_SERVICE` 静态字段由 `setGridService` 一次性注入。

---

## 9. 性能与陷阱

- **`track-if-has-update=true`** 会让每次 `update` 多一次 SELECT 比对；高并发写场景慎用。
- **`SQLParamCleaner` 的 `${var}` 字面替换** 直接拼 SQL，**有 SQL 注入风险**，仅用于不可被用户控制的常量（如表名）。`:name` 命名参数是安全的。
- **`deleteIn`** >1000 自动分批；**`deleteNotIn`** >1000 抛异常（NOT IN 超限）。
- **`IN` 子句超过 1000** 由 `SQLParamCleaner.changeInSQL` 自动改写为 `OR` 链。
- **`selectForObject` / `selectById`** 在结果 >1 行时抛 `IncorrectResultSizeDataAccessException`。
- **`ExtendTableDAOImpl.select` 只对单表 SELECT 自动追加 `is_deleted=false`**，多表/JOIN/子查询不追加，业务侧需自行处理逻辑删除过滤。
- **`batchInsert`（TableDAO）** 是逐条 `PreparedStatement + KeyHolder`，**不是真正的批量**；如不需要回填主键，用 `getNamedParameterJdbcTemplate().getJdbcTemplate().batchUpdate(sql, paramsList)` 更快（`EntityDAOImpl.insertWithoutCascade(Collection)` 走的是后者）。
- **`EntityCodeDAOImpl.batchUpdate`（继承自 EntityDAOImpl）当前返回 `new int[0]`**（待确认是否未实现）。
- **`@Version` 字段** 必须非 null，否则 update 抛 `IllegalArgumentException("version field cannot be null")`。
- **PG jsonb 列在 `postgresParamsHandler`** 用了 `tableMeta.getColumnPropertyNameMap().get(columnName)` 而非 `propertyValue`，疑似笔误（待确认）。
