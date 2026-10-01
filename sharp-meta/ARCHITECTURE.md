# sharp-meta 架构

## 1. 模块职责

`sharp-meta` 是「字典元数据 + 键值属性」的基础设施层，对外提供两类轻量数据服务：

- **字典（Dict）**：统一管理 `code → label` 映射，来源三选一或并存——`sys_dict` 表、yml `dict.items`（map / sql / list 三种形式）、编程式 `DictDOSupplier`（枚举、外部数据）。提供查询、字段 `label` 自动填充、JSR-303 校验、报表值转换。
- **键值属性（Property）**：`name → value` 简单存储，来源 `sys_property` 表 + yml `props.items`，提供 get / set 与启动时全量缓存。

两者都把数据在启动时一次性灌入 `static` 内存缓存（`DictUtils.dictMap` / `PropertyUtils.map`），运行期只读缓存、写操作同步刷缓存。

## 2. 包结构

```
com.rick.meta
├── config
│   ├── MetaServiceAutoConfiguration      自动配置 + Bean 注册
│   └── validator                         @DictType 注解的 JSR-303 校验器
│       ├── AbstractDictValidator         校验公共逻辑（type 查字典 / sql 兜底）
│       ├── DefaultDictValidator          Object 兜底放行
│       ├── DictDictValueValidator        校验 DictValue
│       ├── DictDictValueListValidator    校验 List<DictValue>
│       ├── DictStringValidator          校验 String
│       └── DictStringListValidator       校验 List<String>
├── dict
│   ├── entity/Dict                       字典实体 @Table("sys_dict")
│   ├── dao/DictDAO                       继承 EntityDAOImpl<Dict, Long>
│   ├── model
│   │   ├── DictType                      @DictType 注解
│   │   ├── DictValue                     code 持有者值对象
│   │   └── DictProperties                dict.* 配置
│   ├── service
│   │   ├── DictService / DictServiceImpl 查询 + rebuild
│   │   ├── DictUtils                     静态缓存 + 反射填充 label
│   │   └── DictDOSupplier                编程式字典源扩展点
│   └── convert
│       ├── ValueConverter<C,T>           转换器接口
│       ├── DictConverter / ArrayDictConverter   code→label
│       ├── BoolConverter                 布尔→是/否
│       ├── LocalDateTimeConverter
│       ├── SqlDateConverter
│       └── SqlTimestampConverter
└── props
    ├── model/KeyValueProperties          props.* 配置
    ├── dao/dataobject/PropertyDO         name/value 数据对象（仅作 DTO）
    └── service
        ├── PropertyService / PropertyServiceImpl  get/set + 启动加载
        └── PropertyUtils                 静态缓存
```

资源：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 与 `META-INF/spring.factories` 双注册（兼容新旧版 Spring Boot）。建表 DDL 在 `sharp-meta/sql/init.sql`。

## 3. 关键抽象

### 字典模型三要素
- `Dict`：字典项记录（`type` + `name`(=code) + `label` + `sort` + `remark`），主键 `(type, name)`。
- `DictValue`：实体字段持有字典 code 的载体。`code` 是真实持久化值；`label` / `type` 标 `@Transient` 不落库。`toString()` 返回 `code`，配合 Jackson 序列化只回传 code。
- `@DictType`：字段注解，既是「字段→字典 type」的元信息，又是 JSR-303 校验器入口。校验命中时顺带把 `label` 回写到 `DictValue`。

### 元数据如何描述字段
没有显式的「实体元信息 / 字段元数据」描述对象。模块对「字段语义」的建模是**注解驱动 + 反射填充**：在任意实体的 `DictValue` / `List<DictValue>` 字段标 `@DictType(type=...)`，`DictUtils.fillDictLabel` 反射扫描全字段，按注解查字典缓存并回写 `label`。可遍历 `Iterable` / `Map` / 嵌套对象，用 `IdentityHashMap` 防循环引用。

## 4. 数据流

### 字典加载（启动期）
```
MetaServiceAutoConfiguration  ──> DictServiceImpl.afterPropertiesSet()
                                       │
                                       ├── rebuild()
                                       │     ├── getDbDictList(null)  ──> SELECT sys_dict 全表
       rick     │     ├── dictDOSupplier.get() ──> 应用提供的枚举/CodeDescription
       rick     │     └── yml dict.items  ──> map / sql / list 三分支
                                       │
                                       └── 按 type 分组、按 sort 排序 ──> DictUtils.dictMap
                                       │
                                       └── @PostConstruct 注入 DictUtils.tableDAO
```

### 字典运行期查询
```
调用方 ──> DictService.getDictByType/getDictByTypeAndName
        ──> DictUtils.dictMap（内存只读）
```

### 字段 label 回填
```
查出的实体 ──> DictUtils.fillDictLabel(obj)
        ──> 反射扫字段 @DictType(type) + DictValue.code
        ──> DictUtils.getDictLabel(type, code) ──> DictValue.setLabel(...)
        ──> 若注解有 sql 且字典未命中：tableDAO.select(sql, code) 兜底
```

### JSR-303 校验
```
@DictType 标注字段 ──> 匹配 ConstraintValidator
        ──> AbstractDictValidator.isValid(context, code, labelConsumer)
        ──> type 查 DictService；未命中用 sql 查 TableDAO
        ──> 命中回写 label，返回 true；否则 buildConstraintViolation
```

### 值转换（报表）
```
ReportService 注入 Map<String, ValueConverter> valueConverterMap
        ──> row[i] = valueConverter.convert(context, row[i])
        ──> DictConverter 查 DictService；ArrayDictConverter 解析 JSON 数组
```

### 属性加载 / 读写
```
PropertyServiceImpl.afterPropertiesSet()
        ├── PropertyUtils.map.putAll(props.items)        yml
        └── PropertyUtils.map.putAll(SELECT sys_property)  表
运行期：getProperty 读 map；setProperty 先 UPDATE/INSERT sys_property 再 put map
```

## 5. 对外依赖

- `sharp-database2`：`TableDAO`（核心查询）、`EntityDAOImpl`（`DictDAO` 基类）、`SQLParamCleaner`、`SharpDatabaseAutoConfiguration`、`Transient` 注解。
- `sharp-common`（传递）：`ObjectUtils.mayPureObject`、`JsonUtils`、`Time2StringUtils`、`EntityWithCodePropertyDeserializer`、`OperatorUtils`。
- 三方：Spring Boot（自动配置、`ConfigurationProperties`）、Hibernate Validator（JSR-303 `ConstraintValidator`）、Lombok、Apache Commons（lang3 / collections4）、Guava。

## 6. 扩展点

| 扩展点 | 机制 | 用法 |
|---|---|---|
| `DictDOSupplier` | 实现 `get(): List<Dict>` 注册为 Bean | 把应用内枚举 / 外部数据源注入字典缓存（见 `sharp-admin` `DictDOSupplierImpl` 注册 `CategoryEnum` 等） |
| `ValueConverter<C,T>` | 实现接口注册为 `@Component` | 新增报表列转换器；按 bean name 注入到 `valueConverterMap` |
| yml `dict.items` | `map` / `sql` / `list` 三选一 | 不写代码即增加字典源；`sql` 形式可从任意表查两列映射 |
| `@DictType(type=, sql=)` | 注解 `sql` 字段 | 字典未配置时用自定义 SQL 兜底查 label |

## 7. 配置与启动

### 配置项
| 前缀 | 类 | 字段 |
|---|---|---|
| `dict` | `DictProperties` | `items: List<Item>`，`Item{type, sql, map, list}` |
| `props` | `KeyValueProperties` | `items: Map<String,String>` |

### 启动顺序
1. Spring Boot 加载 `AutoConfiguration.imports` → `MetaServiceAutoConfiguration`
2. 前置条件：容器存在唯一 `TableDAO`（`SharpDatabaseAutoConfiguration` 已就绪）
3. 注册 `DictProperties` / `KeyValueProperties` 与各 Bean
4. `DictServiceImpl.afterPropertiesSet()` 触发 `rebuild()` 全量加载；`PropertyServiceImpl.afterPropertiesSet()` 加载属性
5. `sys_dict` / `sys_property` 表不存在时仅 `log.warn`，不阻断启动（便于无表场景下仅用 yml/Supplier）

### 自动注册的 Bean
`DictDAO`、`DictService`、`PropertyService`、`DictConverter`、`ArrayDictConverter`、`BoolConverter`、`SqlDateConverter`、`SqlTimestampConverter`、`LocalDateTimeConverter`。`DictDOSupplier` 可选。

## 8. 数据库表

### `sys_dict`（字典表）
```sql
create table sys_dict (
    id     bigint,                 -- 来自 BaseEntity，部分 DDL 未设主键
    type   varchar(32) not null,   -- 字典类型
    name   varchar(32) not null,   -- 字典项 code（注：字段名是 name）
    label  varchar(32) not null,   -- 展示文本
    sort   int,                   -- 排序
    remark varchar(64),           -- 备注
    primary key (type, name)
);
```
> `DictServiceImpl` 实际查询 SQL：`SELECT id, type, name, label, sort, remark FROM sys_dict WHERE type = :type ORDER BY sort`。

### `sys_property`（键值属性表）
```sql
create table sys_property (
    name  varchar(32) not null primary key,
    value varchar(255) not null
);
```
> `PropertyServiceImpl` 用 `JdbcTemplate` 直接 `UPDATE`/`INSERT`（非 `TableDAO`），SELECT 走 `tableDAO.selectForKeyValue`。

参考 DDL：`sharp-meta/sql/init.sql`（最简版）、`sharp-admin/init/*.sql`、`sharp-admin/deploy/docker/init/sharp-admin.sql`（含 id 列与完整索引）。

## 9. 与其他模块的协作

- **sharp-admin**：`DictApi`（`GET /dicts?codes=`）暴露字典给前端；Thymeleaf `SelectProcessor` / `SpanProcessor` 用 `DictService` 渲染下拉；`DictDOSupplierImpl` 注入枚举字典；实体（`ComplexModel`、`Student`）用 `@DictType` + `DictValue`。
- **sharp-formflow**：表单组件 `CheckBox` 等用 `DictValue.getCode()` 解析勾选项。
- **sharp-report**：`ReportService` 用 `ValueConverter`（`DictConverter`、`BoolConverter`、`LocalDateTimeConverter` 等）转换报表列。
- **sharp-generator**：代码生成器引用本模块（具体用法待确认）。
