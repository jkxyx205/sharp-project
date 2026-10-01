# sharp-meta 架构

## 模块定位

`sharp-meta` 是 sharp 体系的"元数据基础设施"，提供两类元数据：

1. **字典元数据**（dict）—— 下拉/枚举字典的 `type → code → label` 三元组，读多写少，启动时全量加载到内存缓存，运行时零 SQL。
2. **键值属性元数据**（props）—— 全局 `name → value` 配置属性，读走缓存、写穿透到库。

外加三个上层消费点：
- `@DictType` 注解 + 5 个 `ConstraintValidator`：把字典校验嵌入 JSR-303 Bean Validation，并在校验通过时副作用回填 `label`。
- `DictUtils.fillDictLabel`：反射递归回填实体图里所有 `DictValue` 字段的 label。
- `ValueConverter` 体系：报表/导出场景把任意字段值转成展示字符串（字典、布尔、日期时间）。

## 包结构

```
com.rick.meta
├── config
│   ├── MetaServiceAutoConfiguration        # Spring Boot 自动配置入口
│   └── validator
│       ├── AbstractDictValidator           # 校验基类：type 走 DictService、sql 走 JdbcTemplate
│       ├── DictDictValueValidator           # 校验 DictValue
│       ├── DictDictValueListValidator       # 校验 List<DictValue>
│       ├── DictStringValidator              # 校验 String
│       ├── DictStringListValidator          # 校验 List<String>
│       └── DefaultDictValidator             # 兜底 Object -> true
├── dict
│   ├── entity
│   │   └── Dict                            # @Table(sys_dict)，extends BaseEntity<Long>
│   ├── model
│   │   ├── DictProperties                   # @ConfigurationProperties(prefix="dict")
│   │   ├── DictType                         # @DictType 校验注解
│   │   └── DictValue                        # code/label/type 值对象
│   ├── dao
│   │   └── DictDAO                          # extends EntityDAOImpl<Dict, Long>
│   ├── service
│   │   ├── DictService                      # 读接口
│   │   ├── DictServiceImpl                  # 实现 + InitializingBean
│   │   ├── DictUtils                        # 静态缓存 + 反射回填
│   │   └── DictDOSupplier                   # 编程式字典来源 SPI
│   └── convert
│       ├── ValueConverter<C,T>             # 转换器接口
│       ├── DictConverter                    # 单值字典转换
│       ├── ArrayDictConverter               # JSON 数组字典转换
│       ├── BoolConverter                    # 布尔 -> 是/否
│       ├── SqlDateConverter
│       ├── SqlTimestampConverter
│       └── LocalDateTimeConverter
└── props
    ├── model
    │   └── KeyValueProperties               # @ConfigurationProperties(prefix="props")
    ├── dao
    │   └── dataobject
    │       └── PropertyDO                  # name/value DO（当前 SQL 直接用列，DO 未映射）
    └── service
        ├── PropertyService                  # name/value 读写接口
        ├── PropertyServiceImpl              # 实现 + InitializingBean
        └── PropertyUtils                    # 静态缓存
```

## 关键抽象

### 字典的描述方式

字典有三种"形状"统一收敛到 `Dict(type, name, label, sort)`：

| 来源 | 配置/提供方式 | 收敛逻辑（`DictServiceImpl`） |
|---|---|---|
| `sys_dict` 表 | `SELECT id,type,name,label,sort,remark FROM sys_dict` 全表 | 启动 `rebuild()` 全量查，按 `type` 分组、按 `sort` 排序 |
| yml `dict.items` | `Item.type` + `Item.map` / `Item.sql` / `Item.list` | `initYml(item)`：map→`Dict(name=key,label=value)`；sql→`queryForKeyValue` 转 `Map<String,String>` 后同 map；list→`Dict(name=key,label=key)` |
| 编程式 `DictDOSupplier` | 实现返回 `List<Dict>`（多 type 混合） | `rebuild()` 把 supplier 结果并入按 type 分组 |

合并优先级：`sys_dict` + `Supplier` 先合并入 `dictMap`，随后遍历 yml items 调 `initYml`，**yml 命中的 type 会直接 `put` 覆盖前者**。`rebuild(type)` 单 type 重建则按 yml→sys_dict→Supplier 顺序，命中即返回。

### 属性的描述方式

属性只有 `name → value` 一种形状，来源两路：
1. yml `props.items`（Map）
2. `sys_property` 表（`SELECT name, value FROM sys_property`）

启动 `PropertyServiceImpl.afterPropertiesSet()`：先 put yml，再 put 全表覆盖。读全程走 `PropertyUtils.map` 缓存；写走 `UPDATE`，影响 0 行则 `INSERT`，并同步刷缓存。

## 数据流

### 启动期（写缓存）

```
JVM 启动
  └─ spring.factories -> MetaServiceAutoConfiguration
       ├─ DictServiceImpl.afterPropertiesSet() -> rebuild()
       │    ├─ SELECT sys_dict 全表  ──(失败 log.warn，不阻断)──┐
       │    ├─ DictDOSupplier.get()  (可选)                    │
       │    └─ 遍历 dict.items: initYml(map|sql|list)          │
       │           └── 写入 DictUtils.dictMap (按 type 分组) ◄┘
       └─ PropertyServiceImpl.afterPropertiesSet()
            ├─ putAll(props.items)
            └─ SELECT sys_property 全表 putAll (失败 log.warn)
```

### 运行期（读 / 回填 / 写）

```
读字典:
  DictService.getDictByType / getDictByTypeAndName / getDictsByCodes
    -> 委托 DictUtils.dictMap (内存，不可变视图)

回填 label:
  DictUtils.fillDictLabel(obj)
    -> 反射遍历 entity/Iterable/Map
    -> 命中 DictValue + @DictType -> DictUtils.getDictLabel(type, code)
    -> setLabel / setType

校验:
  @DictType on DictValue/String/List
    -> AbstractDictValidator.isValid
    -> 先 dictService.getDictByTypeAndName(type, code) [命中回填 label]
    -> 再 jdbcTemplate.query(sql, [code]) (取 label 列)
    -> 失败 -> 违反约束

报表值转换:
  ReportService 按 ReportColumn.valueConverterNameList
    -> Map<String,ValueConverter>.get(beanName)
    -> DictConverter/ArrayDictConverter/BoolConverter/...
```

### 写属性

```
PropertyService.setProperty(name, value)
  -> UPDATE sys_property ... ; 影响行数 0 -> INSERT sys_property
  -> PropertyUtils.map.put(name, value)
```

## 对外依赖

| 依赖 | 用途 |
|---|---|
| `sharp-database`（`com.rick.db`） | `SharpService`（命名参数查询、`queryForKeyValue`）、`EntityDAOImpl`、`BaseEntity`、`@Table`/`@Transient`、`GridServiceAutoConfiguration`（自动配置顺序约束） |
| `com.rick.common`（间接） | `JsonUtils`、`ObjectUtils`、`Time2StringUtils`（来自 sharp 通用包） |
| Hibernate Validator（间接） | `@Constraint`/`ConstraintValidator`/`ConstraintValidatorContextImpl` |
| Spring Boot | `@ConfigurationProperties`、`InitializingBean`、`JdbcTemplate` |
| Guava / Commons Lang3 / Commons Collections4 | 集合与字符串工具 |

被下游模块依赖（pom 已确认）：`sharp-admin`、`sharp-formflow`、`sharp-report`、`sharp-generator`、`sharp-demo`。`sharp-database` 自身也引用了 `DictValue`（`IdCodeValue`）—— **存在模块间反向引用待确认**（见陷阱）。

## 扩展点

1. **`DictDOSupplier`**：注册一个 `@Component` 实现即可追加编程式字典来源（枚举类、第三方接口等）。注意单候选注入，多于一个 Bean 会报错。
2. **`ValueConverter`**：实现 `ValueConverter<C,T>` 并 `@Component`，bean 名即报表 `ReportColumn.valueConverterNameList` 中引用的 key。无需改 `MetaServiceAutoConfiguration`（仅内置 6 个转换器声明为 Bean；自定义 converter 由 `@Component` 自动加入 `Map<String,ValueConverter>` 注入）。
3. **`@DictType.sql`**：字段级动态 SQL 校验，适合字典 type 不能预先枚举、需联表校验的场景。
4. **yml `dict.items`**：新增 type 只改 yml，不动代码；启动自动入缓存。

## 配置与启动

- 自动装配入口：`META-INF/spring.factories` → `MetaServiceAutoConfiguration`。
- 装配条件：容器中有单候选 `SharpService`（来自 `sharp-database`），且在 `GridServiceAutoConfiguration` 之后。
- 配置前缀：`dict`（`DictProperties`）、`props`（`KeyValueProperties`）。
- 启动副作用：`DictUtils.dictMap` 与 `PropertyUtils.map` 两块静态缓存被填充。这两块是 `static`，**全 JVM 共享**，重启应用前不会清空；测试时需注意跨用例污染。

## 数据库表

模块自带 DDL（`sharp-meta/sql/init.sql`）：

```sql
-- 字典表
create table sys_dict (
    type   varchar(32) not null,
    name   varchar(32) not null,
    label  varchar(32) not null,
    sort   int null,
    primary key (type, name)
);

-- 全局属性表
create table sys_property (
    name   varchar(32) not null primary key,
    value  varchar(255) not null
);
```

生产环境（见 `sharp-admin/sql`）会附带 `BaseEntity` 审计列：`id`、`create_by`、`create_time`、`update_by`、`update_time`、`is_deleted`。`Dict` 实体继承 `BaseEntity<Long>`，主键 `id` 由基类承载；模块自带 DDL 用 `(type,name)` 复合主键属精简版，生产以 `id` 为主键——**待确认**：两套 DDL 在主键定义上不一致，需以应用部署的 init 脚本为准。

`DictServiceImpl.SELECT_SQL` 实际查询列：`id, type, name, label, sort, remark`，与生产表结构一致；`sys_property` 查询 `name, value`。
