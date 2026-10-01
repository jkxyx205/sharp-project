# sharp-meta API

> 一句话定位：`sharp-meta` 为应用提供「字典元数据」与「键值属性」两类基础数据能力——把 code↔label 的映射（字典）和 name↔value 的配置（属性）从数据库 / yml / 编程式三种来源统一加载进内存缓存，供查询、校验、自动填充 label、报表转换等使用。

依赖：`sharp-database2`（提供 `TableDAO`）。被 `sharp-admin`、`sharp-formflow`、`sharp-report`、`sharp-generator`、`sharp-demo` 引用。

---

## 一、字典（Dict）核心入口

### 1. `DictService`（接口）
全名 `com.rick.meta.dict.service.DictService`，Spring Bean，由 `MetaServiceAutoConfiguration` 注册（`DictServiceImpl`）。字典查询的总入口，所有数据来自 `DictUtils.dictMap` 内存缓存。

```java
DictProperties.Item getDictPropertyItemByType(String type);
// type：字典类型 code（如 "sex"、"MATERIAL_TYPE"）
// 返回：yml dict.items 中配置的该项；不存在抛 NoSuchElementException

Map<String, List<Dict>> getDictsByCodes(Collection<String> codes);
// codes：多个字典 type
// 返回：type -> 该类型下全部字典项列表

Map<String, List<Dict>> getDictsByCodes(String... codes);
// 变长参数版本，内部委托给上面的 Collection 版本

Optional<Dict> getDictByTypeAndName(String type, String name);
// type：字典类型；name：字典项 code（注意是 name 字段，即 code）
// 返回：匹配的 Dict；type/name 空抛 IllegalArgumentException

List<Dict> getDictByType(String type);
// type：字典类型
// 返回：该类型下全部字典项（按 sort 排序后的不可变副本）

void rebuild(String type);
// 重新加载指定 type 的字典：先查 yml，再查 sys_dict 表，最后查 DictDOSupplier

void rebuild();
// 全量重建：sys_dict 表 + DictDOSupplier + yml items，重新分组排序写入 DictUtils.dictMap
```

行为：`DictServiceImpl` 实现 `InitializingBean`，Bean 启动时自动 `rebuild()` 全量加载。`rebuild()` 对 sys_dict 查询失败仅 `log.warn`（表未建不阻断启动）。

### 2. `DictUtils`（工具类）
全名 `com.rick.meta.dict.service.DictUtils`，`final`，公开静态方法。`DictServiceImpl` 在 `@PostConstruct` 时把 `tableDAO` 注入到静态字段，使本类可独立使用。

```java
static Map<String, List<Dict>> dictMap;
// 字典内存缓存：type -> List<Dict>。外部可读；rebuild 时整体替换

static List<Dict> getDict(String key);
// key：字典 type；返回不可变空安全列表

static Optional<Dict> getDictLabel(String key, String name);
// key：字典 type；name：字典项 code；返回匹配的 Dict

static void fillDictLabel(Object obj);
// 递归对任意对象（entity / Map / Iterable / DictValue 字段）填充 label
// 扫描字段上的 @DictType 注解，按 type 查字典并把 label 回写到 DictValue.label
// 含 @DictType 的 List<DictValue> / List 元素 / 嵌套对象都会被处理
// 用 IdentityHashMap 防循环引用
```

### 3. `DictType`（注解）
全名 `com.rick.meta.dict.model.DictType`，`@Target(FIELD)`、`@Retention(RUNTIME)`，JSR-303 校验注解。标注在 `DictValue` / `String` / `List<DictValue>` / `List<String>` 字段上。

```java
String message() default "code %s 不存在";  // 校验失败消息，%s 替换为 code
String type() default "";   // 字典类型 code；非空时按字典校验/填充 label
String sql() default "";    // 当 type 未命中时，用此 SQL 兜底查 label（参数为 code）
Class<?>[] groups() default {};
Class<? extends Payload>[] payload() default {};
```

校验器（`com.rick.meta.config.validator`）：
- `DictDictValueValidator`：校验 `DictValue`，命中后回写 `label`
- `DictDictValueListValidator`：校验 `List<DictValue>`，逐个校验并回写 label
- `DictStringValidator`：校验 `String` code
- `DictStringListValidator`：校验 `List<String>` code
- `DefaultDictValidator`：对 `Object` 直接放行（兜底，避免无匹配类型时报错）

校验逻辑（`AbstractDictValidator`）：先按 `type` 查 `DictService`；命中返回 true 并把 label 回写给 consumer；未命中再用 `sql` 查 `TableDAO`，结果恰好 1 行视为合法。

### 4. `DictValue`（值对象）
全名 `com.rick.meta.dict.model.DictValue`。实体字段持有字典 code 的载体，`label` / `type` 标注 `@Transient`（不落库）。`toString()` 返回 `code`，便于 Jackson 序列化、表单回传。

```java
DictValue(String code);
DictValue(String code, String type);
// 字段：String code; @Transient String label; @Transient String type;
// @EqualsAndHashCode(of = "code") —— 以 code 判等
```

约定：`code` 字段名不可改为 `name`，因为 `EntityWithCodePropertyDeserializer`（sharp-common）靠 `setCode` 反射注入 JSON 字符串值。

### 5. `Dict`（实体）
全名 `com.rick.meta.dict.entity.Dict`，`@Table("sys_dict", comment="字典表")`，继承 `BaseEntity<Long>`。

字段：`String type, String name, String label, Integer sort, String remark`。
构造：`Dict(String type, String name, String label, Integer sort)` 与带 remark 的全参版本。

### 6. `DictDOSupplier`（扩展点接口）
全名 `com.rick.meta.dict.service.DictDOSupplier`。应用可声明为 Spring Bean，`DictServiceImpl` 在 `rebuild()` 时调用 `get()` 把程序化字典（如枚举、`CodeDescription`）注入字典缓存。

```java
List<Dict> get();
```

---

## 二、字典配置项 `DictProperties`
全名 `com.rick.meta.dict.model.DictProperties`，`@ConfigurationProperties(prefix = "dict")`。yml 配置的字典源，三种形式任选其一：

```yaml
dict:
  items:
    - type: bol                 # 字典 type
      map: {"1": "是", "0": "否"}   # code -> label 直接映射
    - type: sys_user
      sql: "select id, name from sys_user order by id asc"   # 两列：key,value
    - type: priority
      list: ["high", "medium", "low"]   # code=label
```

`Item` 字段：`String type; String sql; Map<String,String> map; List<String> list;`。`getItemByType(type)` 取单项（不存在抛 `NoSuchElementException`）。

---

## 三、值转换器 `ValueConverter`
全名 `com.rick.meta.dict.convert.ValueConverter<C, T>`，`Serializable`。用于把原始值转成展示字符串（典型：报表列把 code 转 label）。所有实现注册为 Spring Bean，按 bean name 注入到使用方（如 `ReportService.valueConverterMap`）。

```java
String convert(C context, T value);
```

内置实现（均为 `@Component`，`MetaServiceAutoConfiguration` 显式注册 Bean）：

| 类 | C / T | 行为 |
|---|---|---|
| `DictConverter` | `String` / `Object` | context=字典 type；value 以 `[` 开头委托 `ArrayDictConverter`；否则查 `DictService` 取 label，未命中抛 `IllegalArgumentException` |
| `ArrayDictConverter` | `String` / `String` | context=字典 type；value 为 JSON 数组字符串（`["a","b"]` 或 `[{code,label}]`），逐个查 label 用逗号拼接 |
| `BoolConverter` | `Object` / `Object` | Boolean/Number/String → `是` / `否`（true/"true"/"1"/数值 1 视为 true） |
| `LocalDateTimeConverter` | `Object` / `LocalDateTime` | 用 `Time2StringUtils.format` 格式化 |
| `SqlDateConverter` | `Object` / `java.sql.Date` | `toString()` |
| `SqlTimestampConverter` | `Object` / `java.sql.Timestamp` | 转 `LocalDateTime` 后格式化 |

---

## 四、键值属性（Property）入口

### `PropertyService`（接口）
全名 `com.rick.meta.props.service.PropertyService`，Spring Bean（`PropertyServiceImpl`）。

```java
String getProperty(String name);
// name：属性名；返回缓存中的值，不存在返回 null

void setProperty(String name, String value);
// name 必须非空（否则抛 IllegalArgumentException）
// 先 UPDATE sys_property；影响行数为 0 则 INSERT；最后同步写入 PropertyUtils.map 缓存
```

### `PropertyUtils`（工具类）
全名 `com.rick.meta.props.service.PropertyUtils`，`final`，静态方法。`PropertyServiceImpl` 启动时把 yml `props.items` 与 sys_property 表全量加载到 `static Map<String,String> map`。

```java
static String getProperty(String name);
// 直接读静态 map，name 空返回 null
```

### `KeyValueProperties`
全名 `com.rick.meta.props.model.KeyValueProperties`，`@ConfigurationProperties(prefix = "props")`。

```yaml
props:
  items: {"hello": "world", "name": "Ashley"}
```

`PropertyDO`（`com.rick.meta.props.dao.dataobject.PropertyDO`）：仅 `String name; String value;`，未作为实体使用（仅作 DTO/示意）。

---

## 五、自动配置 `MetaServiceAutoConfiguration`
全名 `com.rick.meta.config.MetaServiceAutoConfiguration`。

- `@Configuration`，注册于 `META-INF/spring/...AutoConfiguration.imports` 与 `spring.factories`
- `@ConditionalOnSingleCandidate(TableDAO.class)`：容器中存在唯一 `TableDAO`（来自 sharp-database2）才生效
- `@AutoConfigureAfter(SharpDatabaseAutoConfiguration.class)`
- 内部静态类 `MetaGridServiceConfiguration` 通过 `@EnableConfigurationProperties` 启用 `DictProperties`、`KeyValueProperties`，并注册：`DictDAO`、`DictService`、`PropertyService`、`DictConverter`、`ArrayDictConverter`、`BoolConverter`、`SqlDateConverter`、`SqlTimestampConverter`、`LocalDateTimeConverter`
- `DictDOSupplier` 为可选注入（`@Autowired(required = false)`），应用按需提供

---

## 六、使用示例

### 1) 查询某类型的字典项

```java
@Autowired DictService dictService;

List<Dict> sexList = dictService.getDictByType("sex");          // [{M,男},{F,女}]
String label = dictService.getDictByTypeAndName("sex", "F")    // "女"
        .map(Dict::getLabel).orElse(null);
```

### 2) 实体字段持有字典 code 并自动填充 label

```java
@Table(value = "t_model", comment = "测试")
public class MyModel extends BaseEntity<Long> {
    @Embedded(columnPrefix = "material_type_")
    @JsonDeserialize(using = EntityWithCodePropertyDeserializer.class) // JSON 字符串自动注入 code
    @DictType(type = "MATERIAL_TYPE")   // 校验 + DictUtils.fillDictLabel 填充 label
    DictValue materialType;
}
// 查出实体后：
DictUtils.fillDictLabel(entity);   // materialType.label 被回写
```

### 3) yml 配置字典 / 属性

```yaml
dict:
  items:
    - type: bol
      map: {"1": "是", "0": "否"}
props:
  items: {"hello": "world"}
```

```java
@Autowired PropertyService propertyService;
propertyService.getProperty("hello");   // "world"
propertyService.setProperty("name", "Rick");  // 写库 + 刷缓存
```

### 4) 报表中用 ValueConverter 把 code 转 label

```java
// ReportService 中：valueConverterMap 按 bean name 注入
ValueConverter vc = valueConverterMap.get("dictConverter");   // bean name
String label = vc.convert("sex", "F");   // "女"
```

> 待确认：`valueConverterMap` 的 key 在不同上下文中的具体 bean name 约定（默认为类名首字母小写：`dictConverter`、`boolConverter` 等）。使用方应以其注入约定为准。
