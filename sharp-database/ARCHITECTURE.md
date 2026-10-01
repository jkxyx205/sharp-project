# sharp-database 架构说明

> 本文面向需要深入理解模块内部结构、改动模块本身或扩展方言/插件的开发者。对外使用面见 [API.md](./API.md)。

## 1. 包结构

```
com.rick.db/
├── config/             配置：SharpDatabaseProperties(prefix=sharp.database)、GridServiceAutoConfiguration、Constants(DB类型常量)
├── constant/           SharpDbConstants：列名常量(id/code/description/create_by/create_time/update_by/update_time/is_deleted)与分隔符正则
├── dto/                实体基类与查询模型
│   ├── BaseEntity / BaseCodeEntity / BaseCodeDescriptionEntity / SimpleEntity / SimpleBaseEntity
│   ├── PageModel / QueryModel / Grid
│   └── type/          按主键类型分的具体子类(简化泛型解析)
├── exception/         DBException
├── formatter/         SQL 格式化器(AbstractSqlFormatter + Mysql/Postgres/Oracle)
├── middleware/        可选中间件兼容层
│   ├── jpa/           BaseJpaEntity + AttributeConverter(AbstractPojoObjectAttributeConverter/BaseListConverter/MapObjectListConverter)
│   └── mybatis/       BaseMybatisEntity + MappedSharpService(从 mapper.xml 取 SQL 交给 SharpService 执行)
├── plugin/            工具与 DAO 引擎
│   ├── DatabaseMetaData / DbScriptUtils / DbUtils / GridHttpServletRequestUtils / GridUtils / IdDescriptionUtils / QueryUtils / SQLUtils
│   ├── model/         IdValue / IdCodeValue(查询模型)
│   ├── table/         AbstractTableGridService / DefaultTableGridService(列表/报表 Grid 服务基类)
│   └── dao/
│       ├── annotation/  @Table @Id @Column @Embedded @Version @Transient @OneToMany @ManyToOne @ManyToMany @Select @Sql @ToStringValue
│       ├── core/        EntityDAO 体系核心
│       │   ├── CoreDAO(根接口) / EntityDAO / EntityCodeDAO / MapDAO(接口)
│       │   ├── AbstractCoreDAO(基于 tableName/columnNames 的通用实现)
│       │   ├── EntityDAOImpl(基于实体的实现，含级联/版本/@Sql)
│       │   ├── EntityCodeDAOImpl(基于 code 的扩展)
│       │   ├── MapDAOImpl(无实体实现)
│       │   ├── EntityDAOSupport(Spring 容器桥，扫描注册 DAO)
│       │   ├── EntityDAOManager(静态注册表：tableName/entityClass → EntityDAO)
│       │   ├── EntityDAOHelper(fillEntityIdsByUniqueColumnName + getComputedProperty)
│       │   ├── TableMeta / TableMetaResolver(实体元数据解析)
│       │   ├── TableGenerator / MySQLTableGenerator / PostgresSQLTableGenerator(自动建表)
│       │   ├── ColumnFillType(枚举 INSERT/UPDATE)
│       │   ├── CascadeSelectThreadLocalValue / EntityDAOThreadLocalValue(级联查询/删除的线程内去重)
│       └── support/    ColumnAutoFill+DefaultColumnAutoFill / ConditionAdvice+DefaultConditionAdvice / EntityCodeIdFillService / IdToEntityConverterFactory / BaseEntityUtils
├── service/           Service 层
│   ├── BaseServiceImpl(模板 Service)
│   ├── GridService(分页引擎)
│   ├── SharpService(SQL 执行引擎，含 NestedRowMapper)
│   ├── SharpServiceHandler(回调接口)
│   └── support/Params(@Deprecated 参数构造器)
└── util/              OptionalUtils / PaginationHelper

org.springframework.jdbc.core.namedparam/
└── ParsedSqlHelper    拷贝自 Spring 内部，用于反射获取 NamedParameterUtils 解析出的参数名列表
```

## 2. 关键类与抽象关系

### 2.1 DAO/Service 角色关系

```
                    业务调用方
                        |
            +-----------+-----------+
            |                       |
    BaseServiceImpl<S,D,T,ID>   GridUtils.list(...)        SharpService.update/query(...)
            |                       |                              ^
            | baseDAO               |                              |
            v                       v                              |
    EntityDAO<T,ID>            GridService --query-----> SharpService --formatSql--> AbstractSqlFormatter(方言)
            |                                                    |
            | extends                                             |
    AbstractCoreDAO<ID> <-----------------------------------------+
            |                                                 NamedParameterJdbcTemplate
            | implements                                        |
    CoreDAO<ID> (根接口)                                       JDBC

    EntityDAOImpl<T,ID>   --基于实体, TableMetaResolver 解析元数据, 支持级联/版本/@Sql
        └── EntityCodeDAOImpl<T,ID>  --增加 code 维度

    MapDAOImpl<ID>        --基于 tableName/columnNames, 依赖 DatabaseMetaData
```

- **`SharpService`** 是底层 SQL 执行器，所有 DAO/Service/GridService 最终都通过它访问 `NamedParameterJdbcTemplate`。它持有一个 `NestedRowMapper`，支持结果集列名 → 嵌套 POJO 属性（含 `a.b.c` 深层路径自动生长），并对无法直接映射的列做 JSON 反序列化。
- **`AbstractCoreDAO`** 是通用 SQL 拼装层：基于 `tableName` + `columnNames`（逗号分隔）提供通用 insert/update/delete/count/select/exists/checkId，封装 `ColumnAutoFill`（自动填充 create_time/update_time/is_deleted/id）与 `ConditionAdvice`（自动注入逻辑删除条件）两个可替换的钩子。
- **`EntityDAOImpl`** 在 `AbstractCoreDAO` 之上引入 `TableMeta`（来自 `TableMetaResolver`），按实体类的 `@Column/@Id/@OneToMany/@ManyToOne/@ManyToMany/@Select/@Sql/@Version/@Embedded` 注解驱动 SQL 生成，并实现级联查询（`cascadeSelect`）与级联写入（`cascadeInsertOrUpdate`）。
- **`GridService`** 是分页引擎，组合 `SharpService` 与 `AbstractSqlFormatter`，负责 count + 分页 SQL 包装 + PageModel 校验。
- **`BaseServiceImpl`** 是业务 Service 的可选模板，组合 `EntityDAO` 与 `SharpService`，提供标准 CRUD 委托。
- **`SharpServiceHandler`** 是回调接口，配合 `MappedSharpService` 让上层用 MyBatis 写动态 SQL、用 `SharpService` 执行（统一命名参数与格式化）。

### 2.2 实体基类继承体系

```
SimpleEntity<ID>                  (@Id, equals/hashCode/toString 基于 id)
├── SimpleBaseEntity<ID>          (createTime/updateTime/deleted)
└── BaseEntity<ID>                (createBy/createTime/updateBy/updateTime/deleted)
    └── BaseCodeEntity<ID>        (code, @Column(updatable=false))
        └── BaseCodeDescriptionEntity<ID>  (description)

dto/type/  (按主键类型分，简化 TableMetaResolver 泛型解析)
├── BaseEntityWithLongId          (BaseEntity<Long>, SEQUENCE)
├── BaseEntityWithIdentity        (BaseEntity<Long>, @Id(IDENTITY))
├── BaseEntityWithStringId        (BaseEntity<Long> 待确认)
├── BaseCodeEntityWithLongId / WithIdentity / WithStringId
└── BaseCodeDescriptionEntityWithLongId
```

`TableMetaResolver#getIdClass` 优先匹配上述具体子类直接返回 `Long`/`String`，否则反射父类泛型参数。

### 2.3 SQL 格式化器与分页关系

```
SharpService.query/update
      |
      v
getSQLFormatter(sql, params)
      |
      v
AbstractSqlFormatter.formatSql(sql, params, paramMap)   // 同一个 formatter 处理所有 SQL
      |
      +-- handleHolderSQL          // ${name} 字面替换
      +-- splitParam                // 解析 "column OP :name" 占位
      +-- IN 展开 / LIKE 改写 / null 条件移除
      +-- changeInSQL               // IN>1000 拆分
      +-- rightParentheses          // 清理空括号与悬挂 AND/OR
      |
      v
NamedParameterJdbcTemplate.query/update(formatSql, paramMap)

GridService.query
      |
      +-- formatSqlCount(sql)      // SELECT COUNT(*) FROM (原SQL去ORDER BY) temp
      +-- pageSql(sql, model)       // 方言相关：MySQL LIMIT offset,size / PG LIMIT size OFFSET offset / Oracle ROWNUM
```

## 3. 分层与数据流

### 3.1 一条分页查询的流动

```
Controller.list(HttpServletRequest)
  -> HttpServletRequestUtils.getParameterMap(request)              // 合并 extendParams
  -> QueryModel.of(params)                                        // 抽出 page/size/sidx/sord
  -> SQLUtils.setOrderParams(pageModel, sortableColumns)         // 校验排序白名单, MySQL 追加 id ASC
  -> GridService.query(sql, pageModel, params, callback, countSQL)
        1. pageQueryModel?
           Y: countSQL = blank ? formatSqlCount(sql) : countSQL
              records = SharpService.query(countSQL, params, Integer).get(0)
              records==0 -> 返回 emptyInstance
              totalPages = records / size (向上取整)
              page 越界修正
           N(isAllQueryModel): 跳过 count
        2. SharpService.query(sql, params, jdbcTemplateCallback)
           a. getSQLFormatter -> formatSql 处理 :name 占位、null 移除、IN 展开
           b. callback: pageQueryModel? pageSql(sql2, model) : wrapSordString(sql2, sidx, sord)
           c. NamedParameterJdbcTemplate.queryForList / NestedRowMapper
        3. isAllQueryModel? records = rows.size(), totalPages = 1/0
        4. Grid.builder().page/totalPages/rows/records/pageSize/additionalInfo
```

### 3.2 一条写入的流动（实体 insert）

```
BaseServiceImpl.save(entity)
  -> EntityDAOImpl.insert(entity)
        1. validatorHelper.validate(entity)              // javax.validation
        2. handleAutoFill(entity, paramsArray, ...)      // ColumnAutoFill.insertFill: id/createTime/updateTime/is_deleted
        3. IDENTITY 策略?
           Y: SimpleJdbcInsert.usingGeneratedKeyColumns(idColumnName).executeAndReturnKey(mapParams) -> 回写 id
           N: SQLUtils.insert(tableName, columnNames, params)  // JdbcTemplate.update("INSERT INTO ... VALUES(?,?,...)")
        4. cascadeInsertOrUpdate(entity, insert=true)    // 遍历 OneToMany/ManyToOne/ManyToMany 注解
           - OneToMany: 子表 insertOrUpdate(t, refColumnName, refId, subDataList)
           - ManyToOne: 父表 insertOrUpdate(targetObject)，必要时 updateById 回填外键
           - ManyToMany: SQLUtils.updateRefTable(...) 维护中间表 + 排序列
        5. EntityDAOThreadLocalValue.removeAll()        // 清理级联去重标记
```

### 3.3 SQL 格式器如何参与命名参数解析与方言适配

`AbstractSqlFormatter` 是**所有 SQL**（不只是分页）的统一预处理层。`SharpService.query/update` 在每次执行前调用 `getSQLFormatter`，得到处理后的 `formatSql` 与 `paramMap`，再交给 `NamedParameterJdbcTemplate`。方言差异通过 `pageSql`/`contactString`(LIKE 拼接)/`escapeString`(转义符)/`removeOrders`(Oracle 对 `LISTAGG` 特殊处理)四个抽象方法隔离。

`ParsedSqlHelper`（位于 `org.springframework.jdbc.core.namedparam` 包，因为 `ParsedSql.getParameterNames()` 是包级可见）是 Spring `NamedParameterUtils.parseSqlStatement` 的薄封装，用于在格式化前拿到 SQL 中所有命名参数名，以便格式化后对"未被处理的参数"做兜底（null 替换为 `''`，非 null 写入 paramMap）。

## 4. 对外依赖

### 4.1 sharp-* 模块

| 模块 | 用途 |
|---|---|
| `sharp-common` | `JsonUtils`/`ObjectUtils`/`StringUtils`/`ClassUtils`/`IdGenerator`/`EnumUtils`/`HttpServletRequestUtils`/`ResultUtils`+`BizException`+`ExceptionCode`/`ValidatorHelper`/`HttpServletRequestUtils`/各种 `ConverterFactory`(`StringToLocalDate`/`CodeToEnum`/`JsonStringToObject`/`JsonStringToMap`/`JsonStringToCollection`/`JsonStringToSetMap`/`StringToPhoneNumber`/`LocalDateTimeToInstant`) |

### 4.2 关键第三方库

| 库 | 用途 |
|---|---|
| `spring-boot-starter-jdbc` | `DataSource`、`JdbcTemplate`、`NamedParameterJdbcTemplate`、`SimpleJdbcInsert` |
| `hibernate-validator` + `validation-api` | `@Valid`/`@NotBlank`/`@NotNull`/`@Length`/`@Pattern` 实体校验 |
| `jackson-datatype-jsr310` | `LocalDateTime`/`Instant` JSON 序列化 |
| `commons-lang3`/`commons-collections4` | `StringUtils`/`ArrayUtils`/`CollectionUtils`/`SetUtils` |
| `guava` | `Maps`/`Lists`/`Sets`/`Splitter` |
| `mybatis` + `mybatis-plus`（provided） | `MappedSharpService`/`BaseMybatisEntity`，按需引入 |
| `javax.persistence-api` + `spring-data-jpa`（provided） | `BaseJpaEntity`/JPA AttributeConverter，按需引入 |
| `javax.servlet-api`（provided） | `GridHttpServletRequestUtils`/`AbstractTableGridService.list(HttpServletRequest)` |

## 5. 数据库方言支持

| 方言 | Formatter | 建表 Generator | 分页语法 | LIKE 拼接 | 转义 | 备注 |
|---|---|---|---|---|---|---|
| MySQL | `MysqlSqlFormatter` | `MySQLTableGenerator` | `SELECT * FROM (...) temp_ ORDER BY ... LIMIT offset, size` | `CONCAT('%',UPPER(:name),'%')` | `escape '\\\\'` | 排序自动追加 `id ASC` 防分页数据丢失/重复；MySQL5.x 用 `TEXT`，8.x 用 `JSON`；enum 用 `ENUM(...)` 列类型 |
| PostgreSQL | `PostgresSqlFormatter` | `PostgresSQLTableGenerator` | `SELECT * FROM (...) temp_ ORDER BY ... LIMIT size OFFSET offset` | `CONCAT('%',UPPER(:name),'%')` | `escape '\\'` | enum 用 `VARCHAR(32)` + `CHECK IN(...)`；IDENTITY 用 `GENERATED ALWAYS AS IDENTITY`；建表后用 `comment on table/column` |
| Oracle | `OracleSqlFormatter` | **TODO 未实现**（`TableGeneratorConfiguration` 中 Oracle 分支注释） | `SELECT * FROM (SELECT A.*, ROWNUM RN FROM (...) temp ORDER BY ...) A WHERE ROWNUM <= endIndex) WHERE RN > startIndex` | `'%'\|\|UPPER(:name)\|\|'%'` | `escape '\\'` | `removeOrders` 对含 `LISTAGG` 的 SQL 跳过（避免误删） |

启动时 `GridServiceAutoConfiguration#sharpService(...)` 会用实际 `DatabaseMetaData.databaseProductName` 覆盖 `SharpDatabaseProperties.type`，因此即便配置写错也会按真实库类型选 formatter（前提是连接成功）。

## 6. 扩展点

| 扩展点 | 怎么做 |
|---|---|
| 自定义 Service | 继承 `BaseServiceImpl<D,T,ID>`，构造注入 DAO；可直接用 `baseDAO`/`sharpService` |
| 自定义 DAO | 继承 `EntityDAOImpl`/`EntityCodeDAOImpl`，无参构造即可（泛型自动解析）。需要外部注入时用全参构造 |
| 自定义 `ColumnAutoFill` | 实现 `ColumnAutoFill<ID>`，注册为 Bean 覆盖 `DefaultColumnAutoFill`（默认填充 create_time/update_time/is_deleted/id） |
| 自定义 `ConditionAdvice` | 实现 `ConditionAdvice`，注册为 Bean 覆盖 `DefaultConditionAdvice`（默认注入 `is_deleted=false`）。所有 `selectByParams`/`delete(Map,...)`/`update(Map,...)` 会自动追加条件 |
| 自定义 SQL 方言 Formatter | 继承 `AbstractSqlFormatter`，实现 `pageSql`/`contactString`/`escapeString`；在 `GridServiceAutoConfiguration#sqlFormatter` 加分支。注意 `formatSql`/`formatSqlCount` 是 final-like 的核心逻辑，改动风险高 |
| 自定义建表 Generator | 继承 `TableGenerator`，实现 `idColumnHandler`/`versionColumnHandler`/`columnHandler`/`createManyToManyThirdPartyTable`/`afterCreateTableHandler`/`determineSqlType`；在 `TableGeneratorConfiguration` 加分支 |
| 自定义值转换 | 实现 `ConverterFactory`，作为 Spring Bean 注册，`GridServiceAutoConfiguration#dbConversionService` 会自动收集注入到 `dbConversionService` |
| `@Sql` 派生属性 | 在实体字段上标注 `@Sql(value="SELECT ...", params="k1@prop1,k2@prop2", nullWhenParamsIsNull={"k1"})`，查询后自动回填 |
| `MappedSharpService` | 用 MyBatis mapper.xml 写动态 SQL（注意参数用 `:name`），通过 `handle(selectId, params, SharpServiceHandler)` 走 `SharpService` 执行 |

### 可覆盖钩子

- `EntityDAOImpl#beforeInsertOrUpdate(...)`：子表级联前的钩子，子类可定制子表预处理（`EntityCodeDAOImpl` 用它做 code→id 回填）。
- `EntityDAOImpl#appendParamHolder0(...)`：扩展动态条件生成（默认会同时用列名与属性名生成条件）。
- `AbstractCoreDAO#decideParamHolder(...)`：决定值的占位形式（null→`= :name`，集合/数组/逗号串→`IN (:name)`，其他→`= :name`）。

## 7. 配置与启动流程

### 7.1 `GridServiceAutoConfiguration`（`META-INF/spring.factories` 唯一入口）

```
@ConditionalOnSingleCandidate(DataSource.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(SharpDatabaseProperties.class)
```

内含 4 个静态配置类：

1. **`GridServiceConfiguration`**
   - 注册 `ColumnAutoFill`(`DefaultColumnAutoFill`)、`ConditionAdvice`(`DefaultConditionAdvice`)（均 `@ConditionalOnMissingBean`，可被业务覆盖）
   - `sqlFormatter(...)`：按 `sharp.database.type` 选 formatter
   - `sharpService(...)`：若 `initDatabaseMetaData=true` 先 `DatabaseMetaData.initTableMapping`；从 `DataSource` 取连接读 `databaseProductName/databaseProductVersion` 覆盖配置；new `SharpService`
   - `gridService(...)`：`new GridService(sharpService)`
   - `EntityCodeIdFillService`：code→id 回填服务

2. **`EntityDAOConfiguration`**
   - `baseDAOManager`：收集所有 `List<EntityDAO>` Bean 注入 `EntityDAOManager` 静态表
   - `entityDAOSupport`(`@DependsOn("baseDAOManager")`)：`@PostConstruct` 扫描 `entityBasePackage` 下 `@Table` 类自动注册
   - `validatorHelper`(`@ConditionalOnBean(Validator.class)`)
   - `dbConversionService`：`DefaultFormattingConversionService` + 自动收集所有 `ConverterFactory` Bean + 一组内置 JSON/Enum/Date/Id→Entity Converter

3. **`UtilGridServiceConfiguration`**：向静态工具类 `GridUtils`/`SQLUtils`/`QueryUtils`/`IdDescriptionUtils` 注入依赖（让它们能被静态调用）

4. **`MappedSharpServiceConfiguration`**（`@ConditionalOnClass(SqlSessionFactory.class)`）：classpath 有 MyBatis 时注册 `MappedSharpService`

5. **`TableGeneratorConfiguration`**：按类型注册 `MySQLTableGenerator`/`PostgresSQLTableGenerator`，Oracle TODO

### 7.2 数据源初始化

数据源由 Spring Boot `DataSourceAutoConfiguration` 提供（应用层配置），本模块只消费唯一的 `DataSource`。`jdbc.properties` 是模块自带示例，实际不参与自动配置。

### 7.3 `ParsedSqlHelper` 的作用

`org.springframework.jdbc.core.namedparam.ParsedSqlHelper`（拷贝自 Spring 内部）只在 `AbstractSqlFormatter#formatSql` 开头调用一次：`ParsedSqlHelper.get(sql)` 返回 SQL 中所有命名参数名列表。格式化过程中对每个匹配到 `column OP :name` 的参数会从 `paramNames` 中移除；最后剩下的 `paramNames`（即未被条件表达式消费的参数，如 SELECT 列表里的 `:name` 或 ORDER BY 里的 `:name`）走兜底逻辑：null/空串替换为字面 `''`，非 null 直接放入 `paramMap`。

> 因为 `ParsedSql.getParameterNames()` 是包级可见，所以 `ParsedSqlHelper` 必须放在 `org.springframework.jdbc.core.namedparam` 包下。**这是从 Spring 内部拷贝的类，不要随意改动**——升级 Spring 时需关注 `NamedParameterUtils.parseSqlStatement` 的兼容性。
