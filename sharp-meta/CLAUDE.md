# sharp-meta · CLAUDE.md

> 给 Claude / 开发者的工作指南。先读这份，再动代码。

## 模块边界

`sharp-meta` 只做两件事：

1. **字典元数据**：`code ↔ label` 映射的加载（sys_dict 表 / yml / `DictDOSupplier`）、内存缓存、查询、字段 `label` 反射填充、JSR-303 校验、值转换。
2. **键值属性**：`name ↔ value` 的 get/set + 启动加载。

**不做**：实体字段元信息描述、ORM、表单渲染、业务逻辑。它依赖 `sharp-database2`，但自身不含数据源；上层模块（admin/formflow/report）才是消费方。

## 目录约定

```
src/main/java/com/rick/meta
├── config/            自动配置 + JSR-303 校验器（validator 子包）
├── dict/              字典：entity / dao / model / service / convert
└── props/             键值属性：model / dao/dataobject / service
src/main/resources/META-INF/   spring.factories + AutoConfiguration.imports
sql/init.sql                   sys_dict / sys_property 建表 DDL
```

包名根 `com.rick.meta`，子包按职责（`dict` / `props` / `config`）切分，不再按层切。新增类放到对应子包。

## 编码约定

- 字典项 code 落库字段名是 **`name`**（不是 `code`）。`Dict.name` 即 code，`DictValue.code` 才是 code 字段。读写时别混。
- `DictValue.code` 字段名不可改名——`EntityWithCodePropertyDeserializer` 靠反射调 `setCode` 注入 JSON 字符串。
- 静态缓存：`DictUtils.dictMap`、`PropertyUtils.map`。`rebuild()` / `setProperty()` 直接整体替换或 put。**不要**在运行期并发写 `dictMap`（无锁）；写操作仅 `rebuild` 与 `setProperty`。
- 启动时 `sys_dict` / `sys_property` 表缺失只 `log.warn`，不抛异常——保持无表也能用 yml/Supplier。
- 校验器命中字典后会**副作用回写** `DictValue.label`。不要在校验器里做除校验/回写外的其他副作用。
- `ValueConverter` 实现要注册为 `@Component`，bean name 即注入 key；新增转换器时类名首字母小写即为 name。
- Lombok `@RequiredArgsConstructor` 构造注入；配置类用 `@ConfigurationProperties` + `@EnableConfigurationProperties`，不在 `@Component` 上直接标 prefix。
- 自动配置前置条件 `@ConditionalOnSingleCandidate(TableDAO.class)`——没有 sharp-database2 的 `TableDAO`，本模块不生效。

## 常见改动清单

| 改动 | 入口 | 注意 |
|---|---|---|
| 新增 yml 字典源 | `application.yml` `dict.items` | `map` / `sql` / `list` 三选一；`sql` 必须是两列 key,value |
| 新增程序化字典 | 实现 `DictDOSupplier` 注册为 Bean | 启动时合并进缓存；改完调 `dictService.rebuild()` 刷新 |
| 实体字段挂字典 | `@DictType(type="X")` + `DictValue` 字段 | 配 `@Embedded(columnPrefix=...)` 持久化 code；查后调 `DictUtils.fillDictLabel(obj)` |
| 字典未命中兜底 | `@DictType(sql="...")` | SQL 参数为 code，返回含 `label` 列 |
| 新增报表转换器 | 实现 `ValueConverter` 注册 `@Component` | bean name 即使用方 key；别覆盖内置 4 个 name |
| 新增键值属性 | yml `props.items` 或 `sys_property` 表 | `setProperty` 自动 upsert + 刷缓存 |
| 改字典查询字段 | `DictServiceImpl.SELECT_SQL` | 列顺序要与 `Dict` 字段映射一致 |

## 构建与测试

```bash
# 构建（在仓库根目录）
mvn -pl sharp-meta -am clean install

# 仅编译
mvn -pl sharp-meta compile

# 运行本模块无独立测试；集成测试在 sharp-demo（MetaTest）
mvn -pl sharp-demo -am test -Dtest=MetaTest
```

`MetaTest`（`sharp-demo/src/test/java/com/rick/demo/MetaTest.java`）覆盖：按 type 查列表、按 type+name 查单项、yml 字典（map/sql/user）、属性 get/set。

## 陷阱

- **`Dict.name` = code**，`DictValue.code` = code，`DictValue.label` 是 `@Transient`。搞反会写出错误映射。
- `DictProperties.getItemByType(type)` 用 `findFirst().get()`，type 不存在抛 `NoSuchElementException`——调用前确认或用 stream 兜底。
- `DictUtils.fillDictLabel` 反射扫**所有字段**（`FieldUtils.getAllFields`），深嵌套对象会递归。对纯值对象（String/Number/Date）用 `ObjectUtils.mayPureObject` 跳过。循环引用靠 `IdentityHashMap` 打破。
- `DictConverter.convert` 在 code 未命中时**抛 `IllegalArgumentException`**，不要在不能容忍异常的路径上直接用。
- `ArrayDictConverter` 解析 JSON 失败会回退到 `DictValue.class` 解析取 `code`；`dictService.getDictByTypeAndName(...).get()` 未命中会抛 `NoSuchElementException`（无 `isPresent` 判断）——调用方需保证字典完整。
- `DictDAO` 继承 `EntityDAOImpl<Dict, Long>` 但本模块内未注册使用（自动配置只注册 `DictDAO` Bean，CRUD 由上层模块用）。改 `DictDAO` 不影响 `DictServiceImpl`（后者直接用 `TableDAO.select(Dict.class, ...)`）。
- `spring.factories` 与 `AutoConfiguration.imports` 两份注册文件并存，改自动配置类名/包名时**两处都要改**。
- `PropertyServiceImpl.setProperty` 用 `tableDAO.getNamedParameterJdbcTemplate().getJdbcTemplate()` 直接执行 UPDATE/INSERT，不走 `TableDAO` 抽象——切换数据源时注意 JdbcTemplate 可用性。

## 待确认

- `sharp-generator` 引用本模块的具体使用面（仅 `Generator.java` 一处 import）。
- `valueConverterMap` 注入 key 的 bean name 约定在不同 Spring 版本下是否一致（默认类名首字母小写）。
