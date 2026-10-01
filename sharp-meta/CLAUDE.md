# sharp-meta — Agent 工作指南

> 本文件面向在本模块内做改动的 AI/人类工程师，给出边界与约定。配合 `API.md`（接口签名）与 `ARCHITECTURE.md`（数据流）阅读。

## 模块边界

`sharp-meta` 只负责"元数据"两件事：

- **字典元数据**：`type → code → label`，启动灌内存缓存，运行时只读。
- **键值属性元数据**：`name → value`，读走缓存、写穿透。

**不在本模块做的事**：
- 不做字典管理 CRUD 控制器（在 `sharp-admin` 的 `DictApi`/`DictFormService`）。
- 不做 Thymeleaf 下拉渲染（在 `sharp-admin`/`sharp-demo` 的 dialect processor）。
- 不做报表调度（在 `sharp-report`，本模块只提供 `ValueConverter`）。
- 不做表单组件（在 `sharp-formflow`，本模块只提供 `DictValue` 值对象）。

改字典/属性"能力"看这里；改字典"管理界面/渲染"去对应模块。

## 目录约定

```
sharp-meta/
├── pom.xml                       # 依赖 sharp-database
├── sql/init.sql                  # sys_dict / sys_property 精简 DDL
└── src/main/
    ├── java/com/rick/meta/
    │   ├── config/               # 自动配置 + validator
    │   ├── dict/                 # 字典：entity/model/dao/service/convert
    │   └── props/                # 属性：model/dao/service
    └── resources/META-INF/spring.factories
```

- `dict/` 与 `props/` 两个子域互相不引用，独立演进。
- `convert/` 物理放在 `dict/` 下，但语义是通用值转换器，也被报表消费。
- 模块**没有** `src/test`。测试在 `sharp-demo`/`sharp-admin` 的测试模块里（`MetaTest`、`DictTest`）。

## 编码约定

- **Lombok 优先**：实体/模型用 `@Data`/`@Getter`/`@Setter`/`@SuperBuilder`/`@RequiredArgsConstructor`，构造器走 Lombok。
- **字段可见性**：实体用 `@FieldDefaults(level = AccessLevel.PRIVATE)`。
- **静态缓存**：`DictUtils.dictMap` 与 `PropertyUtils.map` 是 `static` 包级字段，外部不得直接写。所有写都经 `DictServiceImpl.rebuild()` / `PropertyServiceImpl.setProperty()`。
- **不可变返回**：`DictUtils.getDict` 返回 `ListUtils.unmodifiableList(emptyIfNull(...))`，调用方不可改。
- **Service 实现**：实现类 `final` 字段 + 构造注入（`@RequiredArgsConstructor`），实现 `InitializingBean` 完成启动初始化。
- **SQL 写在实现类常量**：`SELECT_SQL`/`INSERT_SQL`/`UPDATE_SQL` 为 `private static final String`。
- **异常策略**：表不存在等"基础设施未就绪"异常 catch + `log.warn` 不阻断；`Assert.hasText` 用于参数校验直接抛。
- **JSR-303**：校验器统一继承 `AbstractDictValidator`，构造器注入 `DictService` + `JdbcTemplate`，不在配置类里声明 Bean（由 Hibernate Validator 反射实例化）。
- **包名**：`com.rick.meta.*`，artifactId `sharp-meta`，groupId `com.rick.meta`，version 走 `${sharp.version}`。

## 常见改动清单

| 改动 | 入口 | 注意 |
|---|---|---|
| 新增一个字典 type | yml `dict.items` 加一项 | `map`/`sql`/`list` 三选一；`sql` 须返回两列（key,value） |
| 新增编程式字典来源 | 实现 `DictDOSupplier` 并 `@Component` | 单候选注入，最多一个 Bean |
| 新增字段值转换器 | 实现 `ValueConverter<C,T>` + `@Component` | bean 名即报表引用名；不要加进 `MetaServiceAutoConfiguration` |
| 新增校验器 | 写 `ConstraintValidator<DictType, X>` 并加入 `@DictType.validatedBy` | 构造器注入 `DictService`+`JdbcTemplate`；改注解影响所有使用方 |
| 改字典加载顺序 | `DictServiceImpl.rebuild()` / `initYml()` | 注意 yml 会覆盖 sys_dict/Supplier |
| 改属性表结构 | `PropertyServiceImpl` 三个 SQL 常量 + DDL | 缓存与库需同步 |
| 新增配置前缀 | 新 `@ConfigurationProperties` 类 + `MetaServiceAutoConfiguration` 启用 | yml 前缀勿与 `dict`/`props` 冲突 |

## 构建与测试

```bash
# 构建（在仓库根或本模块目录）
mvn -pl sharp-meta -am clean install

# 仅编译
mvn -pl sharp-meta compile

# 运行 sharp-meta 自身（无单测，仅编译校验）
mvn -pl sharp-meta test

# 验证字典/属性能力（依赖 sharp-demo/sharp-admin 的测试用例）
mvn -pl sharp-demo test -Dtest=MetaTest
mvn -pl sharp-admin test -Dtest=DictTest
```

`sharp-meta` 自身没有测试源码，能力验证靠 `sharp-demo` 的 `MetaTest`（`getDictByType`、`getDictByTypeAndName`、`getProperty`、`setProperty`）和 `sharp-admin` 的 `DictTest`（`fillDictLabel` 对 entity/list/map 的回填）。改本模块后建议至少跑这两个。

## 陷阱

1. **`dictMap` / `map` 是 static**：跨测试用例共享，跑集成测试前若 yml 或库数据不同会污染结果。`rebuild()` 可手动重置字典缓存，属性缓存没有公开 reset 入口。
2. **`sys_dict` 表不存在不报错**：只 `log.warn("sys_dict表没有创建成功！")`。若你看到字典查询返回空，先确认表是否真的建了，而不是查空。
3. **yml 覆盖 sys_dict**：同一 type 在 yml 配了 map/sql/list，启动后会覆盖 sys_dict 的同 type 数据。排查"库里有但查不到"时检查 yml。
4. **`rebuild(type)` 与 `rebuild()` 覆盖逻辑不同**：单 type 重建是 yml 命中即返回（不再查库）；全量重建是先合并库+Supplier 再让 yml 覆盖。改 `DictServiceImpl` 时务必同步两条路径。
5. **`@DictType` 必须标在 `DictValue`/`String`/`List<DictValue>`/`List<String>` 字段上**：标在其他类型走 `DefaultDictValidator` 直接放行（等于没校验）。
6. **`DictConverter` 未命中抛异常**：`IllegalArgumentException(type + " doesn't contain " + value)`。报表渲染时若字典 code 查不到会直接抛错而不是显示空——排查报表异常先查字典数据完整性。
7. **`ArrayDictConverter` 内部 `get().getLabel()`**：列表里任一 code 查不到会 NPE（`Optional.get()` on empty）。多选字段数据脏会导致报表 NPE。
8. **`DictDOSupplier` 单候选**：注册多个 `@Component` 会让 `MetaServiceAutoConfiguration` 的 `@Autowired(required=false)` 注入失败（实际为单候选注入，多于一个会报 `NoUniqueBeanDefinitionException`）。
9. **`sharp-database` 反向引用 `DictValue`**：`sharp-database` 的 `IdCodeValue` 引用了 `com.rick.meta.dict.model.DictValue`，而 `sharp-meta` 又依赖 `sharp-database`，存在循环依赖（编译期靠先构建 `sharp-database` 解，逻辑上是反向耦合）。改 `DictValue` 字段/签名要同步检查 `sharp-database.IdCodeValue`。
10. **DDL 双版本不一致**：模块自带 `sql/init.sql` 用 `(type,name)` 复合主键且无审计列；生产 init 脚本用 `id` 主键且带审计列。以生产为准，模块 DDL 仅作最小参考。
11. **`PropertyDO` 当前未被 SQL 映射**：`PropertyServiceImpl` 用 `queryForKeyValue` 返回 `Map`，`PropertyDO` 类只是遗留/待用，改属性读写逻辑不要去改 `PropertyDO` 的映射。

## 待确认

- `DictProperties.Item` 的 `map` 与 `sql` 同时配置时的实际优先级（实现是 `map` 先判空命中，`sql` 实际被遮蔽）。
- `sharp-database` 对 `DictValue` 的反向引用是否应迁出到本模块以打破循环。
