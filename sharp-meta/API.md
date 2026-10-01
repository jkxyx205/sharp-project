# sharp-meta API

> 一句话定位：`sharp-meta` 提供两类"元数据"能力——**字典元数据**（`type`↔`code`↔`label` 的下拉/枚举字典，三来源：`sys_dict` 表 / `dict` yml 配置 / 编程式 `DictDOSupplier`，启动时全量灌入内存缓存供读）和**键值属性元数据**（`name`↔`value` 的全局属性，两来源：`sys_property` 表 / `props` yml 配置）。同时提供字典校验注解 `@DictType` 与一套值转换器 `ValueConverter`，用于 Bean Validation 与报表渲染。

## 包概览

| 包 | 职责 |
|---|---|
| `com.rick.meta.config` | Spring Boot 自动配置 `MetaServiceAutoConfiguration` |
| `com.rick.meta.config.validator` | `@DictType` 注解的 5 个 ConstraintValidator 实现 |
| `com.rick.meta.dict.entity` | 字典实体 `Dict`（对应 `sys_dict` 表） |
| `com.rick.meta.dict.model` | 字典相关模型：`DictProperties`(yml 绑定)、`DictType`(注解)、`DictValue`(值对象) |
| `com.rick.meta.dict.dao` | `DictDAO`（继承 `EntityDAOImpl`，无自定义方法） |
| `com.rick.meta.dict.service` | `DictService` / `DictServiceImpl` / `DictUtils` / `DictDOSupplier` |
| `com.rick.meta.dict.convert` | `ValueConverter` 接口及 6 个转换器实现 |
| `com.rick.meta.props.model` | `KeyValueProperties`（`props` yml 绑定） |
| `com.rick.meta.props.service` | `PropertyService` / `PropertyServiceImpl` / `PropertyUtils` |
| `com.rick.meta.props.dao.dataobject` | `PropertyDO`（仅作 DO，当前未被 SQL 直接映射） |

---

## 一、字典元数据

### 1.1 实体 `Dict`

`com.rick.meta.dict.entity.Dict` —— 对应数据库表 `sys_dict`，继承 `BaseEntity<Long>`。

字段（全部 private，Lombok 生成 getter/setter）：

| 字段 | 类型 | 说明 |
|---|---|---|
| `type` | `String` | 字典分类编码，如 `sex`、`UNIT` |
| `name` | `String` | 字典项 code（程序内部值），如 `M` |
| `label` | `String` | 字典项显示文案，如 `男` |
| `sort` | `Integer` | 排序号（可空） |
| `remark` | `String` | 备注 |

构造器：
- `Dict()` 无参
- `Dict(String type, String name, String label, Integer sort)`
- `Dict(String type, String name, String label, Integer sort, String remark)`
- `@SuperBuilder` 链式构造：`Dict.builder().type(..).name(..).label(..).sort(..).build()`

> `@Table(value = "sys_dict", comment = "字典表")`，主键 `id` 来自 `BaseEntity`。

### 1.2 值对象 `DictValue`

`com.rick.meta.dict.model.DictValue` —— 业务实体中表达"指向某字典项的引用"的字段类型，配合 `@DictType` 注解使用。

字段：
| 字段 | 类型 | 注解 | 说明 |
|---|---|---|---|
| `code` | `String` | — | 对应 `Dict.name`（即字典 code），持久化入库的就是它 |
| `label` | `String` | `@Transient` | 字典标签，运行时由 `DictUtils.fillDictLabel` 或校验器回填，不落库 |
| `type` | `String` | `@Transient` | 字典 type，运行时回填，不落库 |

构造器：`DictValue(String code)`、`DictValue(String code, String type)`、无参。

`toString()` 返回 `code`。`equals/hashCode` 仅基于 `code`。

> 用法约定：实体字段类型声明为 `DictValue` 或 `List<DictValue>`，加 `@DictType(type="xxx")`，反序列化时只传 code，运行时回填 label。

### 1.3 注解 `@DictType`

`com.rick.meta.dict.model.DictType` —— JSR-303 字典校验注解，标注在字段上。

```java
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {
    DictDictValueValidator.class,        // 校验 DictValue
    DictDictValueListValidator.class,    // 校验 List<DictValue>
    DictStringValidator.class,           // 校验 String（纯 code）
    DictStringListValidator.class,       // 校验 List<String>
    DefaultDictValidator.class           // 兜底：Object 永远返回 true
})
```

属性：
| 属性 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `message` | `String` | `"code %s 不存在"` | 校验失败消息，`%s` 被实际 code 替换 |
| `type` | `String` | `""` | 字典 type；非空时按 `DictService.getDictByTypeAndName` 校验并回填 label |
| `sql` | `String` | `""` | 当 `type` 未命中时的备用 SQL（`?` 占位符，返回单行含 `label` 列）；非空时用 `JdbcTemplate` 校验 |
| `groups` | `Class<?>[]` | `{}` | JSR-303 分组 |
| `payload` | `Class<? extends Payload>[]` | `{}` | JSR-303 载荷 |

> `type` 与 `sql` 二选一或并存：先查 type（命中字典缓存），未命中再查 sql。两者都空则由 `DefaultDictValidator` 直接放行。

### 1.4 SPI `DictDOSupplier`

`com.rick.meta.dict.service.DictDOSupplier` —— 编程式字典来源，由应用层实现。

```java
public interface DictDOSupplier {
    List<Dict> get();   // 返回全部字典项（多个 type 混在一起，按 type 分组入缓存）
}
```

实现方式：注册为 Spring Bean 即可（`@Component`），`MetaServiceAutoConfiguration` 在容器中按 `required=false` 注入。**最多一个 Bean**（注入为单候选）。

### 1.5 配置类 `DictProperties`

`com.rick.meta.dict.model.DictProperties` —— 绑定 yml 前缀 `dict`。

```java
@ConfigurationProperties(prefix = "dict")
public class DictProperties {
    private List<Item> items;          // dict.items 列表

    public static class Item {
        private String type;            // 字典 type
        private String sql;             // 来源 1：查库 SQL（需返回 key/value 两列）
        private Map<String, String> map;// 来源 2：静态 code→label 映射
        private List<String> list;      // 来源 3：纯 code 列表（label=code）
    }

    public Item getItemByType(String type); // 找不到时抛 NoSuchElementException
}
```

> 一个 `Item` 的 `sql`/`map`/`list` 三者按优先级取用（实现里依次判空，先 map → sql → list，见 `DictServiceImpl.initYml`）。**待确认**：`map` 与 `sql` 同时配置时实际只走 `map`。

### 1.6 服务接口 `DictService`

`com.rick.meta.dict.service.DictService` —— 字典读服务，唯一实现 `DictServiceImpl`。

```java
DictProperties.Item getDictPropertyItemByType(String type);
// 按 type 取 yml Item（用于拿到原始 sql 等），找不到抛异常

Map<String, List<Dict>> getDictsByCodes(Collection<String> codes);
// 批量取多个 type 的字典项，key=type，value=不可变 List

Map<String, List<Dict>> getDictsByCodes(String... codes);
// 可变参重载，内部委托上面的方法；空参返回 null

Optional<Dict> getDictByTypeAndName(String type, String name);
// type + code 精确取一条；type/name 空抛 IllegalArgumentException

List<Dict> getDictByType(String type);
// 取某 type 的全部字典项（不可变、emptyIfNull）；type 空抛 IllegalArgumentException

void rebuild(String type);
// 单 type 重建：先 yml（命中即返回），否则查 sys_dict，否则走 DictDOSupplier

void rebuild();
// 全量重建：sys_dict 全表 + DictDOSupplier + yml 全量；启动时由 afterPropertiesSet 调用
```

**行为要点**：
- 启动时 `DictServiceImpl`（`InitializingBean`）自动 `rebuild()`，把所有来源汇入 `DictUtils.dictMap`（内存 Map）。
- `sys_dict` 表不存在时 catch 异常并 `log.warn("sys_dict表没有创建成功！")`，不阻断启动。
- 全部读操作实际走的是 `DictUtils.dictMap` 静态缓存，而非每次查库。
- `rebuild()` 同一 type 的覆盖顺序：`sys_dict/Supplier`（合并后按 type 分组）→ yml（yml 覆盖前者，因 `initYml` 最后执行并直接 `put` 覆盖）。

### 1.7 工具类 `DictUtils`

`com.rick.meta.dict.service.DictUtils` —— 静态缓存与回填工具，`final` 类。

```java
public static Map<String, List<Dict>> dictMap;
// 全局字典缓存，启动时由 DictServiceImpl.rebuild() 写入。外部不应直接改写。

public static List<Dict> getDict(String key);
// 取某 type 字典项，返回不可变空安全 List

public static Optional<Dict> getDictLabel(String key, String name);
// 按 type+code 取单条 Dict

public static void fillDictLabel(Object obj);
// 反射回填：遍历 obj（entity / Iterable / Map 递归），
// 对类型为 DictValue 且标注 @DictType 的字段，按 type+code 查字典并 setLabel/setType；
// 对 DictValue 类型的集合元素同样回填；
// 用 IdentityHashMap 防循环引用。
```

> `fillDictLabel` 是面向"前端展示前补 label"的核心入口，递归进入嵌套实体、`Iterable`、`Map`。仅对 `DictValue` 字段（带 `@DictType`）和 `Iterable<DictValue>` 生效。

### 1.8 DAO `DictDAO`

`com.rick.meta.dict.dao.DictDAO` —— `extends EntityDAOImpl<Dict, Long>`，无自定义方法。提供标准 CRUD，供字典管理页面使用。

---

## 二、值转换器 `ValueConverter`

`com.rick.meta.dict.convert.ValueConverter<C, T>` —— 报表/导出场景把字段值转成展示字符串的统一接口。`Serializable`。

```java
String convert(C context, T value);   // context 通常是字典 type；返回展示文本
```

实现（全部 `@Component`，由 `MetaServiceAutoConfiguration` 注册，bean 名即下表第一列）：

| Bean 名 | 类 | context | value 类型 | 行为 |
|---|---|---|---|---|
| `dictConverter` | `DictConverter` | `String`(dictType) | `Object` | value 以 `[` 开头委托 `arrayDictConverter`；否则 `String.valueOf` 后查 `DictService.getDictByTypeAndName`，命中返回 label，未命中抛 `IllegalArgumentException` |
| `arrayDictConverter` | `ArrayDictConverter` | `String`(dictType) | `String` | 把 JSON 数组字符串解析成 `List<String>`（或 `List<DictValue>` 取 code），逐个查字典，逗号拼接 label |
| `boolConverter` | `BoolConverter` | `Object` | `Object` | `Boolean/Number/String` → `是`/`否` |
| `sqlDateConverter` | `SqlDateConverter` | `Object` | `java.sql.Date` | `value.toString()` |
| `sqlTimestampConverter` | `SqlTimestampConverter` | `Object` | `java.sql.Timestamp` | `Time2StringUtils.format(value.toLocalDateTime())` |
| `localDateTimeConverter` | `LocalDateTimeConverter` | `Object` | `java.time.LocalDateTime` | `Time2StringUtils.format(value)` |

> 报表模块 `sharp-report` 通过 `Map<String, ValueConverter>` 注入全部转换器，按 `ReportColumn.valueConverterNameList` 中的 bean 名顺序链式调用。

---

## 三、键值属性元数据

### 3.1 配置类 `KeyValueProperties`

`com.rick.meta.props.model.KeyValueProperties` —— 绑定 yml 前缀 `props`。

```java
@ConfigurationProperties(prefix = "props")
public class KeyValueProperties {
    private Map<String, String> items;   // props.items 的 name→value 映射
}
```

### 3.2 服务接口 `PropertyService`

`com.rick.meta.props.service.PropertyService` —— 全局 name/value 属性读写服务。

```java
String getProperty(String name);
// 从 PropertyUtils.map（内存缓存）取值，name 空白返回 null。
// 注意：不查库，仅读缓存

void setProperty(String name, String value);
// name 必须非空白（否则抛 IllegalArgumentException）
// UPDATE sys_property；影响行数为 0 时 INSERT；
// 同步更新 PropertyUtils.map 缓存
```

**行为要点**：`PropertyServiceImpl` 实现 `InitializingBean`，启动时先灌入 yml `props.items`，再查 `sys_property` 全表覆盖（表不存在 `log.warn` 不阻断）。读全程走缓存，写会同时改库与缓存。`PropertyDO` 当前未在 SQL 中映射（SQL 直接用 `name`/`value` 列）。

### 3.3 工具类 `PropertyUtils`

`com.rick.meta.props.service.PropertyUtils` —— `final`，静态缓存。

```java
public static String getProperty(String name);   // 空白返回 null，否则 map.get(name)
```

`static Map<String,String> map` 包级可见，由 `PropertyServiceImpl` 维护。

---

## 四、自动配置 `MetaServiceAutoConfiguration`

`com.rick.meta.config.MetaServiceAutoConfiguration` —— 通过 `META-INF/spring.factories` 注册为 `EnableAutoConfiguration`。

```java
@Configuration
@ConditionalOnSingleCandidate(SharpService.class)   // 依赖 sharp-database 的 SharpService 单例
@AutoConfigureAfter(GridServiceAutoConfiguration.class)
```

内部 `MetaGridServiceConfiguration` 启用 `DictProperties`、`KeyValueProperties`，并注册以下 Bean：
- `DictDAO`
- `DictService`（注入 `SharpService`、`DictProperties`、可选 `DictDOSupplier`）
- `PropertyService`（注入 `SharpService`、`KeyValueProperties`）
- `DictConverter`、`ArrayDictConverter`、`BoolConverter`、`SqlDateConverter`、`SqlTimestampConverter`、`LocalDateTimeConverter`

> 引入 `sharp-meta` 后只要容器中存在 `SharpService`，上述 Bean 自动装配，无需额外 `@ComponentScan`。校验器（`AbstractDictValidator` 子类）由 Hibernate Validator 通过注解 `validatedBy` 反射构造（构造器注入 `DictService`、`JdbcTemplate`），**不在本配置里声明 Bean**。

---

## 五、配置项

| yml 前缀 | 类 | 结构 | 说明 |
|---|---|---|---|
| `dict` | `DictProperties` | `dict.items: [{type, sql|map|list}]` | 静态/SQL 字典来源 |
| `props` | `KeyValueProperties` | `props.items: {name: value, ...}` | 全局键值属性 |

yml 示例（取自 `sharp-admin`）：

```yaml
dict:
  items:
    - type: bol
      map: { "1": "是", "0": "否" }
    - type: sys_user
      sql: "select id, name from sys_user order by id asc"
    - type: sys_dict_type
      sql: "select distinct type, type from sys_dict order by type asc"

props:
  items: {"group_select_sql": "select 1 id, 'Rick' name from dual"}
```

---

## 六、数据库表

DDL 见模块内 `sql/init.sql`：

```sql
create table sys_dict (
    type   varchar(32) not null,
    name   varchar(32) not null,
    label  varchar(32) not null,
    sort   int null,
    primary key (type, name)
);

create table sys_property (
    name   varchar(32) not null primary key,
    value  varchar(255) not null
);
```

> 实际生产表还会带 `BaseEntity` 的审计列（`id`/`create_by`/`create_time`/`update_by`/`update_time`/`is_deleted`），见 `sharp-admin/sql` 下的 init 脚本。模块自带 DDL 是精简版。

---

## 七、使用示例

### 7.1 取字典（Service 注入）

```java
@Autowired
private DictService dictService;

// 取某 type 全部
List<Dict> sexList = dictService.getDictByType("sex");
// "M" -> Dict(type=sex, name=M, label=男)

// code -> label
String label = dictService.getDictByTypeAndName("sex", "M").get().getLabel(); // "男"

// 批量
Map<String, List<Dict>> map = dictService.getDictsByCodes("sex", "UNIT");
```

### 7.2 实体字段用字典 + 校验 + 回填 label

```java
public class Student {
    @DictType(type = "UNIT")          // 校验 unit.code 必须存在于 UNIT 字典
    DictValue unit;                   // 前端只传 {"code":"kg"}，回填后 label="千克"

    @DictType(type = "MATERIAL")
    List<DictValue> materialTypeList; // 多选，前端传 [{"code":"A"},{"code":"B"}]
}

// 校验后（@Valid 触发），label 已由 validator 回填；
// 也可手动回填（适用于已持久化的数据查询出来后补 label）：
DictUtils.fillDictLabel(student);
```

### 7.3 编程式字典来源

```java
@Component
public class MyDictSupplier implements DictDOSupplier {
    @Override
    public List<Dict> get() {
        return List.of(
            new Dict("status", "LOCKED", "锁定", 0),
            new Dict("status", "NORMAL", "正常", 1)
        );
    }
}
```

### 7.4 报表列接值转换器

```java
// sharp-report 中 ReportColumn 指定 converter bean 名
new ReportColumn("unit.code", "单位", false, "UNIT", Arrays.asList("dictConverter"));
// 渲染时 ReportService 按 "dictConverter" 取 DictConverter，用 "UNIT" 作 context 查 label
```

### 7.5 全局属性读写

```java
@Autowired
private PropertyService propertyService;

String v = propertyService.getProperty("hello");      // 读（走缓存）
propertyService.setProperty("hello", "world");        // 写库 + 刷缓存
```
