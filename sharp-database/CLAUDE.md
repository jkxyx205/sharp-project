# CLAUDE.md — sharp-database 改动指南

> 本文件给修改本模块的 LLM/工程师看。对外使用面见 [API.md](./API.md)，内部结构见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 1. 模块定位与边界

- `sharp-database`（`com.rick.db:sharp-database`，version=`${sharp.version}`=`2.0-SNAPSHOT`）是整个 sharp 项目的数据库核心模块：轻量 ORM + 分页 + DAO/Service 分层，基于 Spring JDBC `NamedParameterJdbcTemplate`，支持 MySQL/PostgreSQL/Oracle。
- **新业务一律用 `sharp-database`**。仓库里的 `sharp-database2` 已废弃（无 `src`、无 `pom.xml`，只剩 `sharp-database2.iml` 与 `target/`），不要引用、不要恢复、不要混淆。如果看到 `sharp-database2` 字样，当作历史包袱忽略。
- 本模块定位为"基础设施"，改动需谨慎评估对 `sharp-admin`/`sharp-demo`/`sharp-formflow`/`sharp-report` 等下游的连锁影响。

## 2. 目录约定

```
src/main/java/com/rick/db/
├── config/         配置类、DB 类型常量(Constants)
├── constant/       列名/分隔符常量(SharpDbConstants)
├── dto/            实体基类 + PageModel/QueryModel/Grid
│   └── type/       按主键类型分的具体实体子类
├── exception/      DBException
├── formatter/      SQL 格式化器(方言)
├── middleware/     可选：jpa / mybatis 兼容层
├── plugin/
│   ├── (顶层)      工具类：SQLUtils/DbUtils/QueryUtils/GridUtils/DbScriptUtils/IdDescriptionUtils/DatabaseMetaData/GridHttpServletRequestUtils
│   ├── model/      查询模型 IdValue/IdCodeValue
│   ├── table/      列表/报表 Grid 服务基类
│   └── dao/
│       ├── annotation/  实体注解 @Table/@Column/@Id/@OneToMany/...
│       ├── core/       DAO 引擎核心(EntityDAOImpl 等)
│       └── support/   可替换钩子(ColumnAutoFill/ConditionAdvice/...) 与辅助
├── service/        BaseServiceImpl/GridService/SharpService/SharpServiceHandler
│   └── support/   Params(@Deprecated)
└── util/           OptionalUtils/PaginationHelper

src/main/java/org/springframework/jdbc/core/namedparam/
└── ParsedSqlHelper  拷贝自 Spring 内部，勿动(见陷阱)
```

约定：
- 实体放 `dto` 或业务的 `entity` 包，必须继承 `SimpleEntity` 系且标 `@Table`。
- DAO 放业务的 `dao` 包，继承 `EntityDAOImpl`/`EntityCodeDAOImpl`，标 `@Repository`。
- Service 放业务的 `service` 包，继承 `BaseServiceImpl`，标 `@Service`。

## 3. 编码约定（从现有代码归纳）

### 3.1 命名
- 实体类名 → 表名：默认 `camelToSnake(类名)`，可用 `@Table(value=...)` 覆盖。
- 属性名 → 列名：默认 `camelToSnake(属性名)`；`@ManyToOne` 字段默认 `属性类型SimpleName_id`；可用 `@Column(value=...)` 覆盖。
- 子表外键约定：父表的 `类名_id`（即 `subTableRefColumnName`），可用 `@OneToMany(joinValue=...)` 覆盖。
- 列名/属性名列表统一用逗号分隔字符串 `name, age, user_name`，分隔正则 `\\s*,\\s*`（见 `SharpDbConstants.COLUMN_NAME_SEPARATOR_REGEX`）。

### 3.2 SQL 写法
- **命名参数用 `:name`**（`NamedParameterJdbcTemplate` 风格），不用 `?`、不用 `#{}`、不用 `${}`。
  - `:name` 走参数绑定，安全；会被 `AbstractSqlFormatter` 处理（null 移除、IN 展开、LIKE 改写）。
  - `${name}` 是**字面替换**（由 `handleHolderSQL` 处理），仅用于可信常量，**不要用于用户输入**。
- 列名/表名拼 SQL（`SQLUtils.insert/update/delete` 系列）用 `?` 占位，由 `JdbcTemplate.update` 绑定。
- 动态条件优先用 `selectByParams(Map)` 让 DAO 自动生成（值非集合走 `=`，集合走 `IN`）；需要复杂条件时手写 `conditionSQL` 用 `:name`。
- 排序字段走白名单：`GridUtils.list(sql, params, countSQL, "col1", "col2")`，否则 `SQLUtils.setOrderParams` 会忽略非法字段。

### 3.3 事务
- 所有写操作（`insert/update/delete/deleteLogically`）在 DAO 层标 `@Transactional(rollbackFor = Exception.class)`。
- Service 层一般不再重复加 `@Transactional`（委托给 DAO），跨多 DAO 的复合操作才在 Service 加。
- `SQLUtils.execute(ConnectionCallback)` 与 `DbUtils` 是**新连接**，不参与 Spring 事务，仅用于初始化/迁移脚本。

### 3.4 异常处理
- 业务校验抛 `com.rick.common.http.exception.BizException`（来自 sharp-common）或 `ExceptionCode.xxx` 静态方法，带错误码与消息。
- 版本不一致抛 `BizException("版本不一致，更新失败！")`。
- 结果集 >1 时抛 Spring 的 `IncorrectResultSizeDataAccessException`（由 `OptionalUtils.expectedAsOptional`）。
- `DBException` 当前使用面很小，**不要新增使用**，统一用 `BizException`。

### 3.5 Lombok
- 实体基类用 `@SuperBuilder @Getter @Setter @NoArgsConstructor`（`@SuperBuilder` 要求链上所有父类都用）。
- 不可变 DTO（`Grid`/`TableMeta`/各 `*Property`）用 `@Value @Builder`。
- 工具类用 `@UtilityClass`（`DbScriptUtils`/`OptionalUtils`/`PaginationHelper`/`EntityDAOHelper`/`TableMetaResolver`/`CascadeSelectThreadLocalValue`/`EntityDAOThreadLocalValue`）或 `final class + private 构造`（`SQLUtils`/`QueryUtils`/`IdDescriptionUtils`/`GridUtils`——这几个是静态字段需注入的，不能用 `@UtilityClass`）。

## 4. 常见改动清单

### 4.1 加一个实体
1. 业务模块 `entity` 包下新建类，继承合适的基类（`BaseEntityWithLongId`/`BaseCodeEntityWithLongId`/`BaseEntityWithIdentity` 等）。
2. 标 `@Table(value="t_xxx", comment="...")`，字段标 `@Column(comment="...")`。
3. （可选）业务模块 `dao` 包下新建 `XxxDAO extends EntityDAOImpl<Xxx, Long>` 或 `extends EntityCodeDAOImpl<Xxx, Long>`，标 `@Repository`。
4. 配置 `sharp.database.entity-base-package` 后可省略第 3 步，`EntityDAOSupport` 自动注册。
5. （可选）业务模块 `service` 包下 `XxxService extends BaseServiceImpl<XxxDAO, Xxx, Long>`。

### 4.2 给 Service 加一个方法
- 优先用 `baseDAO` 现有能力（`selectByParams`/`selectIdsByParams`/`existsByParams`/`countByParams`）。
- 需要 SQL 时用注入的 `sharpService.query/queryForObject/update`，命名参数 `:name`，参数用 `Params.builder(n).pv(...).build()`（注意 `Params` 已 `@Deprecated`，新代码可用 `Map.of`/`Maps.newHashMap`，但模块内仍大量使用 `Params`）。
- 复杂动态 SQL 考虑写 mapper.xml 用 `MappedSharpService`。

### 4.3 加一种 SQL 方言 Formatter
1. `formatter/` 下新建 `XxxSqlFormatter extends AbstractSqlFormatter`，实现 `pageSql`/`contactString`/`escapeString`。
2. 在 `Constants` 加 `DB_XXX` 常量。
3. 在 `GridServiceAutoConfiguration#sqlFormatter(...)` 加分支。
4. （若需要自动建表）在 `dao/core/` 下新建 `XxxTableGenerator extends TableGenerator`，在 `TableGeneratorConfiguration` 加分支。
5. 测试：count SQL、分页 SQL、LIKE、IN>1000 拆分。

### 4.4 加一个实体基类
1. `dto/` 下新建，继承 `SimpleEntity` 系，用 `@SuperBuilder`。
2. 若是按主键类型分的子类，放 `dto/type/`，并在 `TableMetaResolver#getIdClass` 加匹配分支（否则走泛型反射）。
3. 更新 API.md 的基类表。

### 4.5 加一个实体注解
1. `plugin/dao/annotation/` 下新建注解，必要时标 `@Transient`（不映射列）。
2. `TableMetaResolver#resolveFields` 解析注解，存入 `TableMeta` 对应列表。
3. `EntityDAOImpl` 在 `cascadeSelect`/`cascadeInsertOrUpdate` 中遍历新列表实现级联逻辑。
4. 更新 API.md 注解表与 ARCHITECTURE.md 数据流。

### 4.6 加一个 `ColumnAutoFill` 字段
1. 在自定义 `ColumnAutoFill` 实现的 `insertFill`/`updateFill` 加键值（key 是列名）。
2. 确保实体基类有对应列，否则 `handleAutoFill` 找不到 index 会跳过。
3. 注意 `isUpdateFillColumnName` 在 `EntityDAOImpl` 中只允许 `updateColumnNames` 内的列。

## 5. 构建与测试命令

```bash
# 在仓库根目录
mvn -pl sharp-database -am clean install -DskipTests     # 只构建本模块及依赖
mvn -pl sharp-database test                              # 跑本模块测试（当前无 src/test）
mvn -pl sharp-database -am package                       # 打包

# 跑下游模块的测试验证改动
mvn -pl sharp-demo test
mvn -pl sharp-admin test
```

> 本模块当前没有 `src/test`，测试主要在 `sharp-demo/src/test` 与 `sharp-admin/src/test` 中。改本模块后务必跑这两个模块的 db 相关测试：`com.rick.demo.db.*`、`com.rick.admin.core.*`。

## 6. 陷阱与注意点

### 6.1 SQL 注入面
- `:name` 命名参数 → 安全。
- `${name}` 字面替换 → **不安全**，仅用于可信常量（表名/列名片段），不要传用户输入。
- `SQLUtils` 按表名/列名拼 SQL → 表名/列名来自代码常量或 `TableMeta`，不接受外部输入；但若调用方传了用户输入的表名/列名，会有注入风险。
- `DbUtils.executeUpdate(String sql, Object[] params)` 用 `?` 占位，安全；`executeUpdate(String sql)` 直接执行，调用方负责。

### 6.2 分页 count 性能
- `formatSqlCount` 默认是 `SELECT COUNT(*) FROM (原SQL去ORDER BY) temp`，子查询 count 在大表上可能慢。
- 可通过 `GridUtils.list(sql, params, countSQL)` 传入手写优化的 countSQL。
- `removeOrders` 用正则去 ORDER BY，Oracle 下含 `LISTAGG` 的 SQL 会跳过（避免误删）。

### 6.3 命名参数解析边界
- `AbstractSqlFormatter#formatSql` 的正则较复杂，以下场景需注意：
  - 参数值为 null 或空字符串 → **整个条件被移除**（含 WHERE/AND/OR 与逗号），可能改变 SQL 语义。需要保留条件用 `isSetIsNull=true`（当前仅 `EntityDAOSupport` 内部路径会传 true，**待确认**是否有对外 API）。
  - `IN (:name)` 当值为空集合 → 条件被移除。
  - LIKE 永远改写为 `UPPER(column) LIKE CONCAT('%',UPPER(:name),'%')`，即默认**忽略大小写、两端模糊**。需要精确匹配不要用 `:name`，改写 `conditionSQL` 用 `=`。
  - `:name` 后跟字母数字（如 `:name2`）会被当作同一参数名的一部分；参数名必须匹配 `[a-zA-Z]+\w*`。
  - 超过 1000 元素的 `IN` 自动拆成多个 `IN (...) OR IN (...)`，但 `SQLUtils.delete` 的 `IN_SIZE` 也是 1000，两处独立常量，改时同步。

### 6.4 `ParsedSqlHelper` 拷贝自 Spring 内部
- 位于 `org.springframework.jdbc.core.namedparam` 包，因为 `ParsedSql.getParameterNames()` 包级可见。
- **不要随意改**。升级 Spring Boot 时需验证 `NamedParameterUtils.parseSqlStatement` 的行为是否变化（参数名解析、`${}` vs `:name` 处理）。若 Spring 改了 `ParsedSql` 的可见性或行为，这里需要同步。

### 6.5 事务边界
- DAO 写方法标 `@Transactional`，但 `SQLUtils` 的静态方法**不参与** Spring 事务（直接走 `JdbcTemplate`，绑定当前事务的 `Connection`；若在 `@Transactional` 上下文内调用则同事务，否则自动提交）。
- `SQLUtils.execute(ConnectionCallback)` 与 `DbUtils` 显式 `dataSource.getConnection()` 取**新连接**，不参与当前事务。
- 级联写入（`cascadeInsertOrUpdate`）在 `EntityDAOImpl.insert/update` 内完成，整个方法 `@Transactional`，子表写入同事务。

### 6.6 同步锁 / 静态状态
- `EntityDAOManager` 维护 `tableName → EntityDAO`/`entityClass → EntityDAO` 两个静态 Map，启动时由 `setBaseDAOList` 一次性填充（`hasAutowired` 守卫），运行时由 `register` 增量添加。**非线程安全**，多线程并发首次触发 `EntityDAOSupport#getEntityDAO` 同一未注册类时可能重复 new，但有 `entityClassEntityDAOMap` 的 `containsKey` 检查兜底。
- `GridUtils`/`SQLUtils`/`QueryUtils`/`IdDescriptionUtils` 的静态字段由 `UtilGridServiceConfiguration` 在启动时注入一次，`QueryUtils`/`IdDescriptionUtils` 有 `BeanCreationException("bean has init already!")` 防重入守卫。
- `CascadeSelectThreadLocalValue`/`EntityDAOThreadLocalValue` 是 `ThreadLocal`，用于单次级联查询/删除内的去重，用完必须 `removeAll()`（`EntityDAOImpl` 各路径末尾都调用了，但**改动时务必保留**，否则线程池复用会脏数据）。
- `TableGenerator#tableNameCreatedContainer` 是 `ThreadLocal<Set<String>>`，防同一次 `createTable` 递归重复建表。

### 6.7 历史包袱
- `Params` 已 `@Deprecated` 但全模块在用，不要为了一致性就大面积替换。
- `BaseEntityWithStringId`/`BaseCodeEntityWithStringId` 泛型实参写的是 `Long`（应为 `String`），**待确认**是否历史遗留；改动前先搜下游使用。
- `MapDAOImpl` 大量方法返回 `null`（`selectByIdsAsMap`/`selectByParams(String)` 等），**未完成实现**，使用前确认目标方法有实现。
- `SharpDatabaseProperties.selectCache` 与注释掉的 `GridServiceCacheConfiguration` 是未完成的查询缓存特性，**待确认**。
- `OracleSqlFormatter` 存在但 `OracleTableGenerator` 未实现（`TableGeneratorConfiguration` 中 Oracle 分支注释掉）。
- `BaseEntity.id` 默认 `@Id(strategy=SEQUENCE)`，`BaseEntityWithIdentity` 重新声明 `@Id(strategy=IDENTITY)` 覆盖父类——实体继承时注意 id 策略是否符合预期。
- `SharpDbConstants.LOGIC_DELETE_COLUMN_NAME = "is_deleted"`，逻辑删除走 `is_deleted=true`；`DefaultConditionAdvice` 默认注入 `is_deleted=false` 到所有查询，**未继承 `BaseEntity`/`SimpleBaseEntity` 的实体会被注入但表中无此列而报错**——自定义 `ConditionAdvice` 或不注册 `DefaultConditionAdvice` 即可绕过。
- `IdToEntityConverterFactory` 把 `Long`/`String` id 转成 `SimpleEntity` 时硬编码 `Long.parseLong`，String id 实体转换会失败（**待确认**是否仅用于 Long id 场景）。

## 7. 改完后的自检清单

- [ ] 跑 `mvn -pl sharp-database -am clean install` 确认编译通过。
- [ ] 跑 `mvn -pl sharp-demo test` 与 `mvn -pl sharp-admin test` 确认 db 测试通过。
- [ ] grep 下游模块是否使用了改动的类/方法签名。
- [ ] 若改了 `AbstractSqlFormatter#formatSql`，至少跑一次 `SharpServiceTest`/`GridUtilsTest`（在 sharp-demo）验证命名参数、null 移除、IN 展开、LIKE 改写。
- [ ] 若改了 `TableMetaResolver`，跑一次含 `@OneToMany`/`@ManyToMany`/`@Embedded`/`@Sql` 的实体测试。
- [ ] 若改了 `EntityDAOManager` 静态表，跑一次多实体级联查询测试。
- [ ] 更新 API.md / ARCHITECTURE.md 对应章节。
