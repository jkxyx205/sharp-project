# sharp-database API 使用手册

> 数据库核心模块。基于 Spring JDBC（`NamedParameterJdbcTemplate`）实现的轻量 ORM + 分页 + DAO/Service 分层框架，支持 MySQL / PostgreSQL / Oracle 三种方言，提供实体基类、注解驱动的表/列映射、一对多/多对一/多对多级联查询与级联写入、逻辑删除、乐观锁、自动建表等能力。

> **与 `sharp-database2` 的关系**：仓库里同时存在 `sharp-database`（`artifactId=sharp-database`，有完整 `src/main/java` 与 `pom.xml`）和 `sharp-database2`（仅剩 `sharp-database2.iml` 与 `target/`，无 `src`、无 `pom.xml`）。`sharp-database2` 是早期尝试重写的 v2，**已废弃/未完成**，不要引用它。**所有业务一律使用 `sharp-database`（v1，当前在用）**。本文档只描述 `sharp-database`。

## 目录

- [1. 模块定位](#1-模块定位)
- [2. 配置项](#2-配置项)
- [3. 实体基类体系](#3-实体基类体系)
- [4. 注解](#4-注解)
- [5. DAO 体系（EntityDAO / EntityCodeDAO / MapDAO）](#5-dao-体系)
- [6. Service 层（BaseServiceImpl / SharpService / SharpServiceHandler）](#6-service-层)
- [7. GridService / GridUtils 分页查询](#7-gridservice--gridutils-分页查询)
- [8. 工具类（SQLUtils / DbUtils / QueryUtils / DbScriptUtils / IdDescriptionUtils）](#8-工具类)
- [9. DTO（Grid / PageModel / QueryModel）](#9-dto)
- [10. 异常](#10-异常)
- [11. 与其它 sharp-* 模块的关系](#11-与其它-sharp--模块的关系)
- [12. 使用示例](#12-使用示例)

---

## 1. 模块定位

| 项 | 值 |
|---|---|
| groupId / artifactId | `com.rick.db` / `sharp-database` |
| 版本 | `${sharp.version}` = `2.0-SNAPSHOT` |
| 父模块 | `com.rick:sharp-dependencies:2.0-SNAPSHOT` |
| 自动配置入口 | `META-INF/spring.factories` → `com.rick.db.config.GridServiceAutoConfiguration` |
| 核心依赖 | `sharp-common`、`spring-boot-starter-jdbc`、`hibernate-validator`、`jackson-datatype-jsr310`；`mybatis`/`mybatis-plus`/`javax.persistence`/`spring-data-jpa`/`javax.servlet-api` 为 `provided`（按需引入） |

模块定位一句话：**给上层业务提供"实体定义 → DAO → Service → 分页查询 → SQL 工具"的全栈数据库访问能力**，不依赖 JPA/MyBatis 即可独立工作，但保留了对 MyBatis SQL 片段（`MappedSharpService`）和 JPA 实体（`BaseJpaEntity`）的兼容钩子。

---

## 2. 配置项

### 2.1 `SharpDatabaseProperties`（prefix = `sharp.database`）

类：`com.rick.db.config.SharpDatabaseProperties`

| 属性 | 类型 | 默认值 | 含义 |
|---|---|---|---|
| `type` | `String` | `"MySQL"` | 数据库类型。取值为 `Constants.DB_MYSQL`="MySQL" / `Constants.DB_POSTGRESQL`="PostgreSQL" / `Constants.DB_ORACLE`="Oracle"。**启动时会被实际连接的 `DatabaseMetaData.databaseProductName` 覆盖**，因此通常不需要手动配置 |
| `databaseProductVersion` | `String` | null | 数据库产品版本，启动时自动填充。建表时用于区分 MySQL 5.x（用 `TEXT`）与 8.x（用 `JSON`） |
| `selectCache` | `boolean` | `false` | 查询是否缓存（代码中留有 `@Import` 缓存拦截器，当前未启用，**待确认**实际效果） |
| `initDatabaseMetaData` | `boolean` | `false` | 是否在启动时初始化 `DatabaseMetaData` 表/列映射（供 `MapDAOImpl` 按 `tableName` 反查列名使用） |
| `entityBasePackage` | `String` | null | 实体类扫描根包，多个用 `,` 分隔。设置后 `EntityDAOSupport` 会在 `@PostConstruct` 时扫描该包下所有 `@Table` 注解类并自动注册 `EntityDAO`/`EntityCodeDAOImpl` |

### 2.2 `jdbc.properties`（src/main/resources）

```
jdbc.driverClass=com.mysql.jdbc.Driver
jdbc.connectionURL=jdbc:mysql://127.0.0.1:3306/platform?useUnicode=true&characterEncoding=utf-8&useSSL=false
jdbc.username=rootx
jdbc.password=jkxyx205x
```

这是模块自带的**示例配置文件**，实际数据源由应用层（`DataSourceAutoConfiguration`）提供；本模块在 `GridServiceAutoConfiguration` 上声明 `@ConditionalOnSingleCandidate(DataSource.class)`，即只要 Spring 上下文里存在唯一 `DataSource` 即可激活。

### 2.3 数据库类型/方言切换

`GridServiceAutoConfiguration#sqlFormatter(...)` 根据 `sharp.database.type` 选择 `AbstractSqlFormatter` 实现：

| type | Formatter | TableGenerator |
|---|---|---|
| `MySQL`（默认） | `MysqlSqlFormatter` | `MySQLTableGenerator` |
| `PostgreSQL` | `PostgresSqlFormatter` | `PostgresSQLTableGenerator` |
| `Oracle` | `OracleSqlFormatter` | TODO（`TableGeneratorConfiguration` 中 Oracle 分支注释掉了，**Oracle 自动建表未实现**） |

---

## 3. 实体基类体系

所有基类位于 `com.rick.db.dto` 与 `com.rick.db.dto.type`。实体必须继承自 `SimpleEntity`（或其子类）并标注 `@Table` 才能被 DAO 识别。

### 3.1 主继承链

```
SimpleEntity<ID>                         // 仅 id（@Id 默认 strategy=SEQUENCE）
├── SimpleBaseEntity<ID>                 // + createTime/updateTime/deleted(逻辑删除)
│   └── (无更下层)
└── BaseEntity<ID>                       // + createBy/createTime/updateBy/updateTime/deleted
    └── BaseCodeEntity<ID>               // + code（外部可见唯一编号，正则 ^[0-9a-zA-Z_/%-]{1,}$，max 32）
        └── BaseCodeDescriptionEntity<ID>// + description
```

| 类 | 全名 | 职责 |
|---|---|---|
| `SimpleEntity<ID>` | `com.rick.db.dto.SimpleEntity` | 最基础实体，仅 `@Id id`。`equals/hashCode` 基于 id；`toString` 返回 id 字符串 |
| `SimpleBaseEntity<ID>` | `com.rick.db.dto.SimpleBaseEntity` | 在 `SimpleEntity` 上加 `createTime/updateTime/deleted`（无 `createBy/updateBy`） |
| `BaseEntity<ID>` | `com.rick.db.dto.BaseEntity` | 在 `SimpleEntity` 上加审计四件套 `createBy/createTime/updateBy/updateTime` 与逻辑删除 `deleted`（列名 `is_deleted`）。审计字段 `@JsonProperty(access=READ_ONLY)`，前端不可写 |
| `BaseCodeEntity<ID>` | `com.rick.db.dto.BaseCodeEntity` | 在 `BaseEntity` 上加 `code`（`@Column(value="code", updatable=false)`）。`equals` 同时认 id 或 code |
| `BaseCodeDescriptionEntity<ID>` | `com.rick.db.dto.BaseCodeDescriptionEntity` | 在 `BaseCodeEntity` 上加 `description`（`@NotBlank`, max 32） |

### 3.2 按主键类型分的具体子类（`dto/type`）

这些类是为了让 `TableMetaResolver` 在解析泛型 `ID` 时无需反射父类泛型参数，**约定优于配置**。新业务直接继承这些子类即可。

| 子类 | 父类 | 主键策略 |
|---|---|---|
| `BaseEntityWithLongId` | `BaseEntity<Long>` | SEQUENCE（雪花 id，由 `IdGenerator.getSequenceId()` 生成） |
| `BaseEntityWithIdentity` | `BaseEntity<Long>` | `@Id(strategy=IDENTITY)`（数据库自增，insert 时用 `SimpleJdbcInsert.executeAndReturnKey`） |
| `BaseEntityWithStringId` | `BaseEntity<Long>`（注意：泛型实参写的是 `Long`，**待确认**是否历史遗留，使用 String id 需自行校验） |
| `BaseCodeEntityWithLongId` | `BaseCodeEntity<Long>` | SEQUENCE |
| `BaseCodeEntityWithIdentity` | `BaseCodeEntity<Long>` | IDENTITY |
| `BaseCodeEntityWithStringId` | `BaseCodeEntity<Long>`（同上待确认） |
| `BaseCodeDescriptionEntityWithLongId` | `BaseCodeDescriptionEntity<Long>` | SEQUENCE |

> `Id.GenerationType` 三种：`ASSIGN`（外部赋值，id 为 null 时报错）、`SEQUENCE`（默认，雪花 id）、`IDENTITY`（数据库自增）。

### 3.3 中间件兼容实体（可选）

| 类 | 用途 |
|---|---|
| `com.rick.db.middleware.mybatis.BaseMybatisEntity` | 使用 MyBatis-Plus 注解（`@TableId(type=ASSIGN_ID)`、`@TableField`、`@TableLogic`）的实体基类，字段与 `BaseEntity` 类似 |
| `com.rick.db.middleware.jpa.BaseJpaEntity` | 使用 JPA 注解（`@Id`、`@CreatedBy`、`@MappedSuperclass`、`@EntityListeners(AuditingEntityListener.class)`）的实体基类，`@PrePersist` 自动生成雪花 id |

这两个类**只在同时引入 MyBatis-Plus / JPA 时使用**，sharp-database 自身 DAO（`EntityDAOImpl`）不识别它们。

---

## 4. 注解

所有注解在 `com.rick.db.plugin.dao.annotation`。

| 注解 | 目标 | 关键属性 | 说明 |
|---|---|---|---|
| `@Table` | TYPE | `value`（表名，空则类名转 snake_case）、`comment`（表注释） | 标识实体类，**必须有** |
| `@Id` | FIELD | `value`（列名）、`strategy`（`ASSIGN`/`SEQUENCE`/`IDENTITY`，默认 `SEQUENCE`） | 主键。未标注时 `TableMetaResolver` 默认用 `id` 列 |
| `@Column` | FIELD | `value`（列名，空则属性名转 snake_case）、`updatable`(true)、`nullable`(true)、`comment`、`columnDefinition`（指定后 `nullable/comment` 失效，直接拼 DDL） | 列映射。`ManyToOne` 上通过 `@AliasFor` 复用 |
| `@Embedded` | FIELD/METHOD | `columnPrefix`、`comment` | 嵌入对象，字段会被平铺到主表的列（带前缀） |
| `@Version` | FIELD | `value` | 乐观锁版本号。更新时自动 `version = version + 1` 并校验影响行数，0 行抛 `BizException("版本不一致")` |
| `@Transient` | FIELD/ANNOTATION | 无 | 标记字段不映射到列。`OneToMany`/`ManyToMany`/`Select`/`Sql`/`Embedded` 上已自带 `@Transient` |
| `@OneToMany` | FIELD | `subTable`、`subEntityClass`、`joinValue`（子表外键列名）、`reversePropertyName`、`cascadeInsertOrUpdate`(true)、`cascadeDelete`(true)、`cascadeQuery`(true)、`cascadeDeleteLogically`(true)、`oneToOne`(false)、`cascadeInsert`(false) | 一对多级联。`subTable` 必填 |
| `@ManyToOne` | FIELD | `value`（外键列名，默认 `属性类型SimpleName_id`）、`parentTable`（必填）、`cascadeInsertOrUpdate`(false)、`cascadeQuery`(true)、`cascadeInsert`(false)、`comment`、`updatable` | 多对一。组合了 `@Column` |
| `@ManyToMany` | FIELD | `thirdPartyTable`（中间表，必填）、`referenceTable`（引用表，必填）、`referenceColumnName`（中间表中引用端的列名，必填）、`columnDefinition`（中间表中本端的列名，必填）、`sortColumnName`、`cascadeQuery`(true) | 多对多，通过中间表维护 |
| `@Select` | FIELD | `table`（必填）、`subEntityClass`、`joinValue`（默认 `类名_id`）、`referencePropertyName`（默认 id 属性）、`oneToOne` | 仅级联查询（不级联写入），用于跨表关联但不属于标准 OneToMany 的场景 |
| `@Sql` | FIELD | `value`（SQL，必填）、`params`（形如 `k1@prop1,k2@prop2`，从实体属性取参数）、`nullWhenParamsIsNull`（指定参数为 null 时跳过查询直接返回 null） | 在实体上声明派生属性，查询后回填。支持 `Collection` 字段返回列表 |
| `@ToStringValue` | FIELD | 无 | 标记该字段入库时调用 `toString()` 而非 `JsonUtils.toJson()`（用于值对象如 `PhoneNumber`） |

---

## 5. DAO 体系

### 5.1 接口层次

```
CoreDAO<ID>                                   (根接口：通用 CRUD/count/exists/checkId/meta)
├── EntityDAO<T, ID>      extends CoreDAO<ID> (实体 DAO，按实体对象操作)
│   └── EntityCodeDAO<T extends BaseCodeEntity, ID> extends EntityDAO  (带 code 的实体 DAO)
└── MapDAO<ID>            extends CoreDAO<ID> (无实体，按 Map 操作；实现类大量方法返回 null，**待确认**完整度)
```

实现类：

| 实现类 | 全名 | 说明 |
|---|---|---|
| `AbstractCoreDAO<ID>` | `com.rick.db.plugin.dao.core.AbstractCoreDAO` | 抽象基类，封装基于 `tableName/columnNames` 的通用 SQL 拼装、自动填充、条件注入、事务。子类必须实现 `generatorId(Object)` |
| `EntityDAOImpl<T, ID>` | `com.rick.db.plugin.dao.core.EntityDAOImpl` | 默认实体 DAO 实现。通过 `TableMetaResolver` 解析实体类元数据，支持级联查询/写入/删除、乐观锁、`@Sql` 派生属性 |
| `EntityCodeDAOImpl<T extends BaseCodeEntity, ID>` | `com.rick.db.plugin.dao.core.EntityCodeDAOImpl` | 在 `EntityDAOImpl` 上增加 `code` 维度操作：`selectByCode/selectIdByCode/assertCodeNotExists/assertCodesExists` 等 |
| `MapDAOImpl<ID>` | `com.rick.db.plugin.dao.core.MapDAOImpl` | 无实体 DAO，通过 `MapDAOImpl.of(applicationContext, tableName, idClass)` 创建；依赖 `DatabaseMetaData` 已初始化 |

### 5.2 `EntityDAO<T, ID>` 关键方法

> 完整签名见 `com.rick.db.plugin.dao.core.EntityDAO`。下列为高频方法。

**写入**

| 方法 | 返回 | 说明 |
|---|---|---|
| `int insert(T entity)` | 影响行数 | 单条插入。IDENTITY 策略走 `SimpleJdbcInsert.executeAndReturnKey` 并回写 id；其他走 `SQLUtils.insert`。触发级联插入。`@Transactional(rollbackFor=Exception.class)` |
| `int insertOrUpdate(T entity)` | 影响行数 | id 为 null 则 insert，否则 update；ASSIGN 策略下 update 0 行则 fallback insert |
| `int[] insertOrUpdate(Collection<T> entities)` | 各行影响行数 | 批量：先 update 有 id 的，再 insert 无 id 的 |
| `int update(T entity)` | 影响行数（0 表示 id 不存在或版本滞后） | 更新所有可更新列。带版本管理时 0 行抛 `BizException("版本不一致")` |
| `int update(T entity, String updateColumnNames)` | 影响行数 | 指定更新列 |
| `int[] update(Collection<T> entities)` | 各行影响行数 | 批量更新，**忽略版本管理** |

**子表全量维护**（用于父表不级联时手动维护子表）

| 方法 | 说明 |
|---|---|
| `int[] insertOrUpdate(Collection<T> entities, String refColumnName, Object refValue)` | 以 `refColumnName` 分组，全量更新（删除库中多余的、更新匹配的、插入新的） |
| `int[] insertOrUpdate(Collection<T> entities, String refColumnName, Object refValue, Consumer<Collection<ID>> deletedIdsConsumer)` | 同上，回调被删除的 id 集合 |
| `int[] insertOrUpdateTable(Collection<T> entities)` | 同步整张表：删除 entities 中没有的 id，再 insertOrUpdate 全部 |
| `int[] insertOrUpdateTable(Collection<T> entities, Consumer<Collection<ID>> deletedIdsConsumer)` | 同上，回调被删除的 id |

**查询**

| 方法 | 返回 | 说明 |
|---|---|---|
| `Optional<T> selectById(ID id)` | `Optional<T>` | 按主键查（带级联） |
| `List<T> selectByIds(String ids)` / `(ID... ids)` / `(Collection<?> ids)` | `List<T>` | 按多个主键查 |
| `Map<ID, T> selectByIdsAsMap(...)` | `Map<ID,T>` | 同上，返回 id→entity |
| `List<T> selectByParams(Map<String,?> params)` / `(T example)` / `(String queryString)` | `List<T>` | 按参数动态查询。Map 的 key 既可以是列名也可以是属性名；值非集 合走 `=`，集合/数组走 `IN`。**默认带级联查询** |
| `List<T> selectByParamsWithoutCascade(...)` | `List<T>` | 同上但**不带级联** |
| `List<T> selectAll()` | `List<T>` | 全表（带级联） |
| `Optional<ID> selectIdByParams(T example, String conditionSQL)` | `Optional<ID>` | 按条件查 id，结果 >1 抛 `IncorrectResultSizeDataAccessException` |
| `<S> Optional<S> selectSingleValueById(ID id, String columnName, Class<S> clazz)` | `Optional<S>` | 查单列单值 |
| `Map<ID, List<T>> groupByColumnName(String refColumnName, Collection<?> refValues)` | `Map<ID,List<T>>` | 按外键分组查子表，用于级联装配 |

**删除**

| 方法 | 说明 |
|---|---|
| `int deleteById(ID id)` | 物理删除 |
| `int deleteByIds(Collection<?> ids)` | 批量物理删除 |
| `int deleteLogicallyById(ID id)` | 逻辑删除（`is_deleted=true`） |
| `int deleteLogicallyByIds(Collection<?> ids)` | 批量逻辑删除 |
| `long deleteAll()` | `TRUNCATE TABLE`（**性能警告：先 count 再 truncate，慎用**） |

**校验**

| 方法 | 说明 |
|---|---|
| `void checkId(ID id)` | id 不存在抛 `ExceptionCode.notExists` |
| `void checkIds(Collection<ID> ids, Map<String,Object> params, String condition)` | 批量校验，差集抛异常 |
| `boolean existsByParams(Map<String,?> params, String conditionSQL)` | MySQL 走 `LIMIT 1`，其他走 `count(*)` |
| `long countByParams(Map<String,?> params, String conditionSQL)` | count |

**元数据**

| 方法 | 说明 |
|---|---|
| `Class<T> getEntityClass()` / `Class<ID> getIdClass()` | 实体/id 类型 |
| `TableMeta getTableMeta()` | 表元数据（列名、属性名、级联注解列表等） |
| `Map<String,Object> entityToMap(T entity)` / `<E> E mapToEntity(Map)` | 实体↔Map 互转 |
| `String getSelectSQL()` | `SELECT 列别名 FROM 表名`（列已带 `AS` 别名，可直接喂给 `SharpService`） |
| `String getSelectConditionSQL(Map<String,?> params)` | `SELECT ... FROM ... WHERE 动态条件`（条件由 params 的列名生成） |
| `String getInsertSQL(T t)` | 拼出可执行的 `INSERT INTO ... VALUES(...)` 文本（参数已字面化，用于导出/建表脚本） |
| `Map<String,String> getPropertyNameToColumnNameMap()` / `getColumnNameToPropertyNameMap()` | 属性↔列名映射 |

### 5.3 `EntityCodeDAO<T, ID>` 额外方法

| 方法 | 说明 |
|---|---|
| `Optional<T> selectByCode(String code)` | 按 code 查 |
| `List<T> selectByCodes(Collection<String> codes)` | 按 codes 查 |
| `ID selectIdByCodeOrThrowException(String code)` | 按 code 查 id，不存在抛 `BizException(4040)` |
| `Optional<ID> selectIdByCode(String code)` | 按 code 查 id |
| `Optional<String> selectCodeById(Long id)` | 反查 code |
| `Map<String,ID> selectCodeIdMap(Collection<String> codes)` | code→id 映射 |
| `void assertCodeNotExists(String code)` | code 已存在抛 `BizException(400)` |
| `void assertCodeExists(String code)` | code 不存在抛 `BizException(404)` |
| `void assertCodesExistsAndUnDuplicate(List<String> codes)` | 校验存在且不重复 |

### 5.4 DAO 注册方式

1. **声明为 Spring Bean**（推荐）：`@Repository public class RoleDAO extends EntityCodeDAOImpl<Role, Long> {}`，无参构造会通过泛型反射自动解析实体类型，由 `EntityDAOManager` 收集。
2. **包扫描自动注册**：配置 `sharp.database.entity-base-package`，`EntityDAOSupport` 在 `@PostConstruct` 扫描该包下所有 `@Table` 类，自动创建 `EntityDAOImpl` 或 `EntityCodeDAOImpl`（根据是否继承 `BaseCodeEntity`）并注册为单例 bean。
3. **运行时按需创建**：`EntityDAOSupport#getEntityDAO(Class<?>)`，未注册时即时 new 一个并注册。

> `EntityDAOManager` 维护 `tableName → EntityDAO` 与 `entityClass → EntityDAO` 两个静态映射，级联查询时按表名反查子表 DAO；若子表未注册会 `log.warn` 并自动生成。

---

## 6. Service 层

### 6.1 `BaseServiceImpl<D extends EntityDAO<T,ID>, T extends SimpleEntity<ID>, ID>`

`com.rick.db.service.BaseServiceImpl`。**业务 Service 不强制继承**，但继承可获得标准 CRUD 模板。

| 字段 | 说明 |
|---|---|
| `protected final D baseDAO` | 构造注入的 DAO（`@Getter` 暴露） |
| `@Autowired protected SharpService sharpService` | 用于 `findByConditionWithoutCascade` 等直接 SQL 查询 |

关键方法（均委托给 `baseDAO`）：

| 方法 | 签名 | 说明 |
|---|---|---|
| `save` | `T save(@Valid T e)` / `Collection<T> save(@Valid Collection<T>)` | insert |
| `saveOrUpdate` | `T saveOrUpdate(@Valid T e)` | id 为 null 且为 `BaseCodeEntity` 时先 `assertCodeNotExists`，再 `insertOrUpdate` |
| `update` | `boolean update(@Valid T e)` | update，0 行 `log.warn` 并返回 false |
| `deleteById` | `boolean deleteById(ID id)` | 物理删除 |
| `deleteLogicallyById` | `boolean deleteLogicallyById(ID id)` | 逻辑删除 |
| `findById` | `Optional<T> findById(ID id)` | 按主键（带级联） |
| `findByIdWithoutCascade` | `Optional<T> findByIdWithoutCascade(ID id)` | 按主键（不带级联） |
| `findAll` / `findAllWithoutCascade` | `List<T>` | 全表（带/不带级联） |
| `findByConditionWithoutCascade` | `List<T> findByConditionWithoutCascade(Map<String,?> params, String condition)` | 直接拼 `baseDAO.getSelectSQL() + " WHERE " + condition`，走 `sharpService.query`，**不带级联** |
| `exists` | `boolean exists(T e, String conditionSQL)` / `(Map, String)` | 委托 `baseDAO.existsByParams` |

### 6.2 `SharpService`

`com.rick.db.service.SharpService`。**底层 SQL 执行引擎**，封装 `NamedParameterJdbcTemplate`，所有 DAO 最终都走它。

| 字段 | 说明 |
|---|---|
| `@Autowired @Getter NamedParameterJdbcTemplate namedJdbcTemplate` | Spring 命名参数 JDBC |
| `@Autowired @Getter AbstractSqlFormatter sqlFormatter` | SQL 格式化器（方言相关） |
| `@Autowired @Qualifier("dbConversionService") ConversionService conversionService` | DB 转换服务（注册了 String→LocalDate、code→Enum、JSON→Object/Collection/Map、id→Entity 等 ConverterFactory） |

关键方法：

| 方法 | 签名 | 行为 |
|---|---|---|
| `query` | `List<Map<String,Object>> query(String sql, Map<String,?> params)` | 查询为 `List<Map>` |
| `query` | `<T> List<T> query(String sql, Map<String,?> params, Class<T> clazz)` | 查询为 `List<T>`。`String/Number/Boolean/Enum/Temporal` 走 `queryForList`；`Map` 走 `queryForList`；POJO 走 `NestedRowMapper`（支持下划线列名→驼峰、嵌套属性自动生长、JSON 列反序列化） |
| `query` | `<T> List<T> query(String sql, Map<String,?> params, JdbcTemplateCallback<T>)` | 自定义回调 |
| `queryForObject` | `Optional<Map<String,Object>> queryForObject(String sql, Map<String,?> params)` / `<T> Optional<T> queryForObject(String sql, Map<String,?> params, Class<T> clazz)` | 单行；结果 >1 抛 `IncorrectResultSizeDataAccessException` |
| `queryForKeyValue` | `<K,V> Map<K,V> queryForKeyValue(String sql, Map<String,?> params)` | 取前两列做 key/value |
| `queryCountFromQuerySql` | `long queryCountFromQuerySql(String querySql, Map<String,?> params)` | 用 `sqlFormatter.formatSqlCount` 包一层 count |
| `update` | `int update(String sql, Map<String,?> params)` | 执行 INSERT/UPDATE/DELETE |

**SQL 格式化行为**（由 `AbstractSqlFormatter#formatSql` 完成，每次 query/update 前调用）：

1. 先解析 `${name}` 占位符（注意是 `${}`，不是 `:`），用 params 直接替换为字面量。
2. 解析 `:name` 命名参数，按 `column OP :name` 模式匹配。值为 null/空串时**自动移除该条件**（含前导 `WHERE/AND/OR` 与尾随 `,`），除非调用方传 `isSetIsNull=true` 改为 `IS NULL`。
3. `IN (:name)` 当值为集合/数组时展开为 `IN (:name0,:name1,...)`，写入 `formatMap`。
4. `LIKE :name` 改写为 `UPPER(column) LIKE CONCAT('%',UPPER(:name),'%') escape '...'`，并对 `% _` 转义。
5. 超过 1000 个元素的 `IN` 自动拆成多个 `IN (...) OR IN (...)`（Oracle 限制）。
6. 清理遗留的空括号 `()`、悬挂的 `AND/OR`。
7. 最终返回处理后的 SQL 与 `paramMap`，交给 `NamedParameterJdbcTemplate`。

> **SQL 注入面**：`:name` 走 `NamedParameterJdbcTemplate` 参数绑定，安全；`${name}` 是字面拼接，**调用方必须保证值可信**。`SQLUtils` 中按 `tableName/columnNames` 拼 SQL 的方法（`insert/update/delete`）也是字符串拼接，表名/列名来自代码常量或 `TableMeta`，不接受外部输入。

### 6.3 `SharpServiceHandler<T>`

`com.rick.db.service.SharpServiceHandler`。回调接口，配合 `MappedSharpService` 使用：

```java
T handle(SharpService sharpService, String sql, Map<String, Object> params);
```

典型用法见 `MappedSharpService`：从 MyBatis mapper.xml 取出动态 SQL 后，交给 handler 用 `SharpService` 执行。

### 6.4 `Params`（`com.rick.db.service.support.Params`，**已 `@Deprecated`**）

参数 Map 构造器，链式 put：

```java
Map<String,Object> params = Params.builder(2)
    .pv("name", "rick")
    .pv("ids", Arrays.asList(1L,2L))
    .pvAll(otherMap)
    .build();
```

> Javadoc 提示 JDK 9+ 可用 `Maps.of`、Guava `ImmutableMap.of` 替代。当前代码大量在用，废弃标注**待确认**是否真要移除。

---

## 7. GridService / GridUtils 分页查询

### 7.1 `GridService`

`com.rick.db.service.GridService`（构造 `new GridService(sharpService)`，由自动配置注册）。

核心方法：

| 方法 | 说明 |
|---|---|
| `Grid<Map<String,Object>> query(String sql, PageModel model, Map<String,?> params)` | 分页查询，返回 `List<Map>` |
| `Grid<Map<String,Object>> query(String sql, PageModel model, Map<String,?> params, String countSQL)` | 同上，可指定优化后的 countSQL |
| `<T> Grid<T> query(String sql, PageModel model, Map<String,?> params, Class<T> clazz)` | 分页查询，返回 `List<T>` |
| `<T> Grid<T> query(String sql, PageModel model, Map<String,?> params, Class<T> clazz, String countSQL)` | 同上 |
| `<T> Grid<T> query(String sql, PageModel model, Map<String,?> params, JdbcTemplateCallback<T> callback, String countSQL)` | 最底层重载 |

行为：
- `PageModel.isPageQueryModel()`（`size != -1`）时执行 count + 分页：先用 `sqlFormatter.formatSqlCount` 生成 count，再 `pageSql` 包装分页。
- `PageModel.isAllQueryModel()`（`size == -1`）时不分页，仅排序，records 取 `rows.size()`。
- 分页参数校验：`size < 1` → 15；`size > 1000` → 1000；`page > totalPages` → 修正为 totalPages；`page < 1` → 1。

### 7.2 `GridUtils`

`com.rick.db.plugin.GridUtils`（静态工具，由自动配置注入 `GridService`）。**业务最常用的入口**。

| 方法 | 说明 |
|---|---|
| `Grid<Map<String,Object>> list(String sql, Map<String,Object> params)` | 默认排序字段取 `params.get("sidx")` |
| `Grid<Map<String,Object>> list(String sql, Map<String,Object> params, String countSQL)` | 指定 countSQL |
| `Grid<Map<String,Object>> list(String sql, Map<String,Object> params, String countSQL, String... sortableColumns)` | 指定可排序列白名单。MySQL 下会自动追加 `id ASC` 做稳定排序（解决 `ORDER BY limit` 数据重复） |
| `List<BigDecimal> numericObject(String sql, Map<String,Object> params)` | 单行数字合计（如 `SUM`/`AVG`），返回各列 `BigDecimal` 列表 |

### 7.3 `GridHttpServletRequestUtils`

`com.rick.db.plugin.GridHttpServletRequestUtils`。从 `HttpServletRequest` 取参数后委托 `GridUtils`。

### 7.4 `AbstractTableGridService` / `DefaultTableGridService`

`com.rick.db.plugin.table`。报表/列表服务的基类，子类实现 `getListSQL()`，可选实现 `getCountSQL()`/`getSummarySQL()`。

```java
public class MyGridService extends AbstractTableGridService {
    @Override public String getListSQL() { return "SELECT ... FROM t_xxx WHERE ..."; }
    @Override public String getCountSQL() { return "SELECT COUNT(*) FROM t_xxx WHERE ..."; }
    @Override public String getSummarySQL() { return "SELECT SUM(amount) FROM t_xxx WHERE ..."; }
}
```

`DefaultTableGridService` 是直接传 SQL 字符串的现成实现。

---

## 8. 工具类

### 8.1 `SQLUtils`（`com.rick.db.plugin.SQLUtils`）

`final` 类，静态方法。由自动配置注入 `NamedParameterJdbcTemplate` 与 `SharpDatabaseProperties`。**面向表名+列名的低层 CRUD，不依赖实体类**。

| 方法 | 签名 | 说明 |
|---|---|---|
| `execute` | `void execute(String sql)` | 执行任意 DDL/DML（`JdbcTemplate.execute`） |
| `insert` | `int insert(String tableName, Map<String,Object> params)` | 按 Map 列名插入（`SimpleJdbcInsert`） |
| `insertAndReturnKey` | `Number insertAndReturnKey(String tableName, Map<String,Object> params, String... idColumnName)` | 插入并返回自增主键 |
| `insert` | `int insert(String tableName, String columnNames, Object[] params)` | 按列名+`?` 占位插入 |
| `insert` | `int[] insert(String tableName, String columnNames, List<Object[]> paramsList)` | 批量插入（相同列） |
| `insert` | `int[] insert(String tableName, Map<String,?>... batch)` | 批量插入（每行不同 Map） |
| `update` | `int update(String tableName, Map<String,Object> params, String idColumnName)` | 按 id 更新 |
| `update` | `int update(String tableName, String updateColumnNames, Object[] params, Serializable id)` | 按 id 更新（idColumnName 默认 `id`） |
| `update` | `int update(String tableName, String updateColumnNames, Object[] params, String conditionSQL)` | 按条件更新，`conditionSQL` 用 `?` |
| `update` | `int[] update(String tableName, String updateColumnNames, List<Object[]> paramsList, String conditionSQL)` | 批量条件更新 |
| `delete` | `int delete(String tableName)` | 删全表 |
| `delete` | `int delete(String tableName, String deleteColumn, String deleteValues)` / `(String, String, Collection<?>)` | 按 IN 删除 |
| `delete` | `int delete(String tableName, Object[] params, String conditionSQL)` | 按条件删除，`?` 占位 |
| `deleteNotIn` | `int deleteNotIn(String tableName, String deleteColumn, Collection<?> deleteValues)` | 删除 NOT IN 的行 |
| `deleteCascade` | `int deleteCascade(String masterTable, String refColumnName, Collection<?> deleteValues, String... subTables)` | 级联删除：先删子表 `refColumnName IN(values)`，再删主表 |
| `updateRefTable` | `void updateRefTable(String refTableName, String keyColumn, String guestColumn, Object keyInstance, Collection<?> guestInstanceIds)` | 全量维护中间表：删除 `keyInstance` 下不在 `guestInstanceIds` 的记录，插入新增的 |
| `setOrderParams` | `void setOrderParams(PageModel pageModel, String[] sortableColumns)` | 校验排序字段白名单；MySQL 自动追加 `id ASC` |
| `getOrderBy` | `String getOrderBy(String tablePrefix, String column, Boolean asc, String[] sortableColumns)` | 生成 `ORDER BY` 片段 |
| `formatInSQLPlaceHolder` | `String formatInSQLPlaceHolder(int paramSize)` | 生成 `(?,?,?)`，超过 1000 抛异常 |
| `extractWhereCondition` | `String extractWhereCondition(String sql)` | 截取 `WHERE` 之后的条件 |
| `paramsHolderToQuestionHolder` | `String paramsHolderToQuestionHolder(String condition)` | `:name` → `?` |
| `convertToArray` | `Object[] convertToArray(Map<String,?> map)` / `(Map, Collection<String> columnNames)` | Map → 参数数组 |
| `resolveValue` | `Object resolveValue(Object value)` / `(Object value, ResolveValueFunction fn)` | 将 Java 值转 JDBC 值：Enum→code、Instant→Timestamp、Collection/Map/pure object→JSON 字符串、JsonValue→JSON、空集合→`"[]"`、空 Map→`"{}"` |
| `execute` | `<T> T execute(ConnectionCallback<T> action)` | 直接拿原生 `Connection` 操作（不走 Spring 事务管理，**新连接**） |

> `IN_SIZE = 1000`：单个 `IN` 最多 1000 个元素，超出自动分批。

### 8.2 `DbUtils`（`com.rick.db.plugin.DbUtils`）

实例类，构造 `new DbUtils(url, username, password)` 或 `new DbUtils(dataSource)`。**脱离 Spring 的原生 JDBC 工具**，用于初始化脚本、数据迁移等场景。

| 方法 | 说明 |
|---|---|
| `int executeUpdate(String sql)` / `(String sql, Object[] params)` / `(String sql, Object[] params, Consumer<PreparedStatement>)` | 执行更新，可拿到 `PreparedStatement`（如取 generated keys） |
| `List<Object[]> executeQuery(String sql)` / `(String sql, Object[] params)` | 查询为行数组 |
| `<T> T execute(Function<Connection,T> consumer)` | 直接操作 `Connection` |

### 8.3 `QueryUtils`（`com.rick.db.plugin.QueryUtils`）

`final`，静态方法，由自动配置注入 `SharpService`。**子表批量查询装配工具**。

| 方法 | 说明 |
|---|---|
| `<T> Map<?,List<T>> subTableValueMap(String selectColumnNames, String queryObject, String refColumnName, Collection<?> refValue, Class<T> tClass)` | 查子表并按 `refColumnName` 分组为 Map |
| `<T> List<T> subTableValueList(String selectColumnNames, String queryObject, String refColumnName, Serializable/Collection<?> refValue, Class<T> tClass)` | 查子表为 List |

### 8.4 `DbScriptUtils`（`com.rick.db.plugin.DbScriptUtils`）

`@UtilityClass`。导入 SQL 脚本文件/文本到指定 `Connection`：按行读，跳过 `--` 注释和空行，遇 `;` 执行一条语句。

### 8.5 `IdDescriptionUtils`（`com.rick.db.plugin.IdDescriptionUtils`）

`final`，静态方法，由自动配置注入 `SharpService`。按 id 批量查 `id/code/description`。

| 方法 | 说明 |
|---|---|
| `List<IdCodeValue> queryIdCodeValue(List<Long> ids, String table, String idColumnName, String codeColumnName, String descriptionColumnName)` | 返回 `IdCodeValue` 列表 |
| `Map<Long,IdCodeValue> queryIdCodeValueAsMap(...)` | 同上为 Map |
| `List<IdValue> queryIdValue(List<Long> ids, String table, String idColumnName, String descriptionColumnName)` | 只查 id+description |
| `Optional<IdCodeValue> queryIdCodeValue(Long id, ...)` / `Optional<IdValue> queryIdValue(Long id, ...)` | 单个 |

### 8.6 `DatabaseMetaData`（`com.rick.db.plugin.DatabaseMetaData`）

启动时（`sharp.database.init-database-meta-data=true`）扫描库下所有表与列、主键，存入静态 `tableColumnMap`/`tablePrimaryKeyMap`，供 `MapDAOImpl` 使用。

### 8.7 `PaginationHelper`（`com.rick.db.util.PaginationHelper`）

`limitPages(total, displayPage, activePage)` → `{startPage, endPage}`，用于 UI 分页栏显示范围计算。

### 8.8 `OptionalUtils`（`com.rick.db.util.OptionalUtils`）

`expectedAsOptional(List<E>)`：空→`Optional.empty()`；size==1→`Optional.of`；size>1→抛 `IncorrectResultSizeDataAccessException`。

---

## 9. DTO

### 9.1 `PageModel`（`com.rick.db.dto.PageModel`）

| 字段 | 默认 | 含义 |
|---|---|---|
| `page` | 1 | 当前页 |
| `size` | 15 | 每页条数；`-1` 表示一次性全部加载（不分页） |
| `sidx` | null | 排序字段 |
| `sord` | null | 排序方向 |

请求参数键名常量：`PARAM_PAGE="page"`、`PARAM_SIZE="size"`、`PARAM_SIDX="sidx"`、`PARAM_SORD="sord"`。

`isPageQueryModel()` = `size != -1`；`isAllQueryModel()` = `size == -1`。

### 9.2 `QueryModel`（`com.rick.db.dto.QueryModel`）

`QueryModel.of(Map<String,Object> requestMap)` 从请求 Map 中抽出 `page/size/sidx/sord` 构造 `PageModel`，其余作为 `params`。`QueryModel.of(PageModel)` 用 PageModel 自身属性做参数。

### 9.3 `Grid<T>`（`com.rick.db.dto.Grid`）

`@Value @Builder` 不可变分页结果：

| 字段 | 含义 |
|---|---|
| `page` | 当前页 |
| `pageSize` | 每页条数 |
| `records` | 总记录数 |
| `totalPages` | 总页数 |
| `rows` | 数据行 `List<T>` |
| `additionalInfo` | 附加信息 `Map<String,Object>` |

`Grid.emptyInstance(pageSize)` 返回空 Grid。

### 9.4 模型对象

| 类 | 字段 |
|---|---|
| `com.rick.db.plugin.model.IdValue` | `Long id`、`@Transient String description` |
| `com.rick.db.plugin.model.IdCodeValue` | `Long id`、`String code`、`@Transient String description`。`@EqualsAndHashCode(of={"code"})` |

---

## 10. 异常

`com.rick.db.exception.DBException extends RuntimeException`：仅一个 `DBException(String message)` 构造。模块内部更多使用 `com.rick.common.http.exception.BizException`（来自 sharp-common）抛业务异常（如 code 重复、版本不一致），`DBException` 使用面较小，**待确认**是否有调用方捕获。

---

## 11. 与其它 sharp-* 模块的关系

| 方向 | 模块 | 关系 |
|---|---|---|
| 依赖 | `sharp-common` | 提供 `JsonUtils`、`ObjectUtils`、`StringUtils`、`ClassUtils`、`IdGenerator`、`EnumUtils`、`HttpServletRequestUtils`、`ResultUtils`/`BizException`/`ExceptionCode`、`ValidatorHelper`、各种 `ConverterFactory` |
| 被依赖 | `sharp-admin` | 业务实体继承基类、DAO 继承 `EntityCodeDAOImpl`、Service 继承 `BaseServiceImpl`、Controller 用 `GridUtils.list`、`BaseApi` 泛型基类 |
| 被依赖 | `sharp-demo` | 同上，含大量测试用例（`SharpServiceTest`、`GridUtilsTest`、`ManyToManyTest` 等） |
| 被依赖 | `sharp-formflow` | 实体/DAO 复用 |
| 被依赖 | `sharp-report` | `ReportService` 使用 `SharpService` 执行报表 SQL |
| 可选 | `sharp-excel` | `ExportUtils.export` 可与 `GridUtils` 查询结果配合导出 |

---

## 12. 使用示例

### 12.1 定义实体

```java
@Getter @Setter @NoArgsConstructor @SuperBuilder
@Table(value = "sys_role", comment = "角色")
public class Role extends BaseCodeEntityWithLongId {   // 有 code、有审计字段、Long 主键(雪花)
    private String name;

    @ManyToMany(thirdPartyTable = "sys_role_permission",
                referenceTable = "sys_permission",
                referenceColumnName = "permission_id",
                columnDefinition = "role_id")
    private List<Permission> permissionList;
}
```

### 12.2 定义 DAO

```java
@Repository
public class RoleDAO extends EntityCodeDAOImpl<Role, Long> { }
```

无实体 DAO（运行时）：

```java
MapDAO<Long> mapDAO = MapDAOImpl.of(applicationContext, "t_xxx", Long.class);
```

### 12.3 定义 Service

```java
@Service
@Validated
public class RoleService extends BaseServiceImpl<RoleDAO, Role, Long> {
    public RoleService(RoleDAO roleDAO) { super(roleDAO); }
}
```

### 12.4 CRUD

```java
Role role = Role.builder().code("ADMIN").name("管理员").build();
roleService.save(role);                 // insert，id 自动回写
Optional<Role> opt = roleService.findById(role.getId());
role.setName("超管");
roleService.update(role);               // update
roleService.deleteLogicallyById(role.getId());   // 逻辑删除
```

按 code 操作（`RoleDAO` 是 `EntityCodeDAO`）：

```java
Optional<Role> byCode = roleDAO.selectByCode("ADMIN");
Long id = roleDAO.selectIdByCodeOrThrowException("ADMIN");
roleDAO.assertCodeNotExists("ADMIN");   // 已存在抛 BizException(400)
```

### 12.5 分页 Grid 查询

```java
// Controller 中从 HttpServletRequest 取参
@GetMapping
public Grid<Map<String, Object>> list(HttpServletRequest request) {
    Map<String, Object> params = HttpServletRequestUtils.getParameterMap(request);
    return GridUtils.list(
        "SELECT id, name, code FROM sys_role WHERE name like :name",
        params,
        null,                  // 不指定 countSQL，自动 formatSqlCount
        "name"                 // 可排序列白名单
    );
}
```

参数中传 `page/size/sidx/sord` 即可分页排序；传 `size=-1` 查全部。

### 12.6 直接用 `SharpService` 写动态 SQL

```java
@Resource SharpService sharpService;

// 命名参数自动忽略空值
List<User> users = sharpService.query(
    "SELECT id, name FROM sys_user WHERE name = :name AND status = :status",
    Params.builder(2).pv("name", "rick").pv("status", null).build(),   // status 条件会被移除
    User.class);

Optional<Long> cnt = sharpService.queryForObject(
    "SELECT COUNT(*) FROM sys_user WHERE create_time > :start",
    Params.builder(1).pv("start", LocalDateTime.now().minusDays(1)).build(),
    Long.class);

sharpService.update(
    "UPDATE sys_user SET status = :status WHERE id = :id",
    Params.builder(2).pv("status", 1).pv("id", 100L).build());
```

### 12.7 用 `SQLUtils` 做批量/级联操作

```java
// 批量插入相同列
SQLUtils.insert("t_log", "user_id, action, create_time",
    Arrays.asList(
        new Object[]{1L, "login", Timestamp.from(Instant.now())},
        new Object[]{2L, "logout", Timestamp.from(Instant.now())}));

// 维护中间表（角色-权限全量覆盖）
SQLUtils.updateRefTable("sys_role_permission", "role_id", "permission_id",
    roleId, permissionIdList);
```

### 12.8 用 `MappedSharpService` 复用 MyBatis 动态 SQL

mapper.xml（注意参数用 `:name` 而非 `#{}`/`${}`）：

```xml
<select id="findById">
    SELECT id, name FROM t_project WHERE id = :id
    <if test="title != null and title != ''">
        AND title like ${title}
    </if>
</select>
```

```java
mappedSharpService.handle("findById", params, (sharpService, sql, p) ->
    sharpService.queryForObject(sql, p, Project.class).orElse(null));
```
