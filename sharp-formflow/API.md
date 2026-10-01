# sharp-formflow API

## 模块定位

`sharp-formflow` 是一个**配置驱动的表单引擎**（不是工作流/审批引擎）。它将"表单定义 → 控件配置 → 数据采集 → 校验 → 存储"打通：通过 `Form` + `CpnConfigurer`（控件配置）+ `Cpn`（控件处理器）三件抽象，让一个表单的字段、控件类型、校验器、数据源、存储策略都可以在数据库/接口中配置，提交时由引擎统一做类型转换、校验、入库。

> 命名中的 "flow" 指表单数据流转链路（定义→渲染→提交→存储），不含任何 BPMN/审批/流程节点引擎。pom.xml 中无 Flowable/Camunda 等依赖。

核心能力：
- 20 种内置控件类型（`CpnTypeEnum`），可通过实现 `Cpn<T>` 扩展。
- 12 种内置校验器（`ValidatorTypeEnum`），可通过实现 `Validator<T>` 扩展。
- 两种存储策略：`INNER_TABLE`（统一存 `sys_form_cpn_value`）与 `CREATE_TABLE`（存到业务表，由 `repositoryName` 指定的 DAO）。
- Thymeleaf 表单页面渲染 + Ajax JSON 提交两条入口。
- `FormAdvice` SPI：表单实例加载/保存/删除前后、渲染前 的业务钩子。

## 核心 public 入口

### 1. `com.rick.formflow.form.service.FormService`

表单核心服务。Spring Bean，注入 `FormDAO`/`FormCpnDAO`/`CpnConfigurerDAO`/`FormCpnValueDAO`/`Map<String, FormAdvice> formAdviceMap`/`Map<String, CpnValueConverter> cpnValueConverterMap`/`ApplicationContext`。

| 方法签名 | 行为 |
|---|---|
| `Form saveOrUpdate(@Valid Form form)` | 新增/更新表单定义（`sys_form`）。仅做入库，不处理控件绑定。 |
| `FormBO getFormBO(Long formId, Long instanceId)` | 主入口：根据表单 ID（+可选实例 ID）返回渲染/校验用的 `FormBO`。`instanceId==null` 返回空值表单（带 `defaultValue`），否则按存储策略从 `sys_form_cpn_value` 或业务表装载实例值。内部走 `FormCache` 缓存（`FormUtils.formCacheMap`，进程内 HashMap）。 |
| `FormBO getFormBOById(Long formId)` | `getFormBO(formId, null)` 的便捷方法。 |
| `FormBO getFormBOByIdAndInstanceId(Long formId, Long instanceId)` | 同上，显式带实例 ID。 |
| `void post(Long formId, Map<String, Object> values) throws BindException` | 提交一个新实例。`instanceId` 由参数 `values` 中的 `id` 决定，若无则在 `INNER_TABLE` 策略下用 `IdGenerator.getSequenceId()` 生成。 |
| `void post(Long formId, Long instanceId, Map<String, Object> values) throws BindException` | 提交指定实例（更新）。 |
| `int delete(Long formId, Long[] instanceIds)` | 批量删除实例。 |
| `int delete(Long formId, Long instanceId)` | 删除单个实例。`INNER_TABLE` 删 `sys_form_cpn_value`；`CREATE_TABLE` 调 `repositoryName` 对应 `EntityDAO.deleteById`。前后触发 `FormAdvice.beforeDeleteInstance/afterDeleteInstance`。 |
| `FormAdvice getFormAdviceByName(String name)` | 按 `formAdviceName` 取对应 SPI 实例。 |

`post` 内部流程（`handle` 方法）：
1. `getFormBOById(formId)` 取表单元数据（命中缓存则克隆 `FormCache`）。
2. 若 `values` 含非空 `id`，覆盖 `instanceId`。
3. 逐 `FormBO.Property`：`new CpnInstanceProcessor(property, paramValue, bindingResult)` → `valid()`（先做控件选项校验，再跑 `configurer.getValidatorList()` 中控件支持的校验器）。校验失败累计到 `BindingResult`，全部跑完后若 `hasErrors()` 抛 `BindException`。
4. 调 `FormAdvice.beforeInstanceHandle`（业务再处理）。
5. `INNER_TABLE`：先 `deleteByInstanceId` 再 `insertOrUpdate(formCpnValueList)`；`CREATE_TABLE`：若 `FormAdvice.insertOrUpdate` 返回 `true` 则交业务方处理，否则用 `repositoryName` 指定的 `EntityDAO.insertOrUpdate(values)`。
6. 调 `FormAdvice.afterInstanceHandle`。

### 2. `com.rick.formflow.form.service.FormCpnService`

表单与控件配置的绑定关系（`sys_form_cpn_configurer`）。

| 方法签名 | 行为 |
|---|---|
| `void saveOrUpdateByConfigurer(Form form, Collection<CpnConfigurer> configurerList)` | 保存 `Form` 后绑定控件（先 insertOrUpdate 控件，再写关联表）。 |
| `void saveOrUpdateByConfigurer(Long formId, Collection<CpnConfigurer> configurerList)` | 仅按 `formId` 绑定。先 `cpnConfigurerDAO.insertOrUpdate`，再删旧关联、写新关联（`orderNum` 顺序写入）。 |
| `void saveOrUpdateByConfigIds(Long formId, Long... configIds)` | 用已有控件 ID 数组绑定到表单。 |
| `void saveOrUpdateByConfigIds(Long formId, Collection<Long> configIds)` | 同上集合版，按 `configIds` 顺序建立关联。 |
| `void saveOrUpdateByConfigIds(Long formId, List<FormCpn> formCpnList)` | 底层实现：先查旧关联做 id 复用，再 `deleteByFormId` + `insertOrUpdate`。 |

### 3. `com.rick.formflow.form.service.CpnConfigurerService`

控件配置（`sys_form_configurer`）CRUD。

| 方法签名 | 行为 |
|---|---|
| `Collection<CpnConfigurer> saveOrUpdate(List<CpnConfigurer> configurers)` | 逐个调 `CpnManager.getCpnByType(type).check(configurer)`（如选项查重）+ 默认 `disabled=false`，再批量入库。返回带 ID 的集合。 |
| `CpnConfigurer findById(Long id)` | 按 ID 查控件配置。 |

> `CpnConfigurerService.checkIfAvailable` 为 private，但 `saveOrUpdate` 已调用 `Cpn.check` 与（隐式）`Cpn.hasValidator` 校验链路。

## Controller API

所有路径以 `forms` 为前缀。返回值统一为 `com.rick.common.http.model.Result`（`ResultUtils.success(...)`）。

### `FormController`  `@RequestMapping("forms")`
| 方法 | 路径 | Body | 说明 |
|---|---|---|---|
| POST | `/forms` | `Form` | 新增/更新表单定义。返回 `Form`。 |
| POST | `/forms/configs` | `FormConfig{form, configs}` | 一次性保存表单 + 控件列表（`FormCpnService.saveOrUpdateByConfigurer`）。返回 `form.id`。 |
| POST | `/forms/{formId}` | `List<CpnConfigurer>` | 用传入的控件列表覆盖绑定到 `formId`。 |
| POST | `/forms/{formId}/configs` | `Long[]` (configIds) | 用已有控件 ID 数组绑定。 |

`FormController.FormConfig` 内部静态类：`Form form; List<CpnConfigurer> configs;`（均带 `@Valid`）。

### `CpnConfigurerController`  `@RequestMapping("forms/configurers")`
| 方法 | 路径 | Body | 说明 |
|---|---|---|---|
| POST | `/forms/configurers` | `List<CpnConfigurer>` | 批量保存控件配置，返回 `Long[]` ID 数组。 |

### `PageInstanceController`  `@Controller @RequestMapping("forms/page")`
Thymeleaf 页面入口。
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/forms/page/{formId}` 或 `/forms/page/{formId}/{instanceId}` | 渲染表单页面。`instanceId` 缺省为新增态。返回 `form.getTplName()`（缺省 `tpl/form/form`）。`FormAdvice.beforeRender` 可改写返回的 `FormBO`。Model 含 `formBO`、`model`（属性值 map）、`query`（请求参数）。 |
| POST | `/forms/page/{formId}/{instanceId}` 或 `/forms/page/{formId}` | 表单提交（form-encoded）。成功返回视图 `success`；校验失败（`BindException`）回填 `errors` 与 `model` 重新渲染表单页（`form.getTplName()` 缺省 `form`）。 |

### `AjaxInstanceController`  `@RestController @RequestMapping("forms/ajax")`
JSON 入口。
| 方法 | 路径 | Body/参数 | 说明 |
|---|---|---|---|
| GET | `/forms/ajax/{formId}` | — | 返回 `FormBO`（新增态）。 |
| GET | `/forms/ajax/{formId}/{instanceId}` | — | 返回 `FormBO`（实例态）。 |
| POST | `/forms/ajax/{formId}` | `Map<String,Object>` JSON | 保存实例，返回 `Result<String>`（值为 instanceId）。 |
| PUT/POST | `/forms/ajax/{formId}/{instanceId}` | `Map<String,Object>` JSON | 更新实例。 |
| DELETE | `/forms/ajax/{formId}?ids=...` | `Long[] ids` | 批量删除。 |
| DELETE | `/forms/ajax/{formId}/{instanceId}` | — | 删除单个实例。 |

## 表单/控件模型类

### `com.rick.formflow.form.cpn.core.Form`  实体 `@Table("sys_form")` 继承 `BaseCodeEntity<Long>`
表单定义。
- `String name` — 表单名称（必填）。
- `String formAdviceName` — 关联的 `FormAdvice` Bean 名称（对应 `formAdviceMap` 的 key）。
- `String tableName` — `CREATE_TABLE` 策略下的目标业务表名（当未提供 `repositoryName` 时使用，当前实现中以 `repositoryName` 为准）。
- `String repositoryName` — `EntityDAO` Bean 名称，`CREATE_TABLE` 策略下用它从容器取 DAO。
- `StorageStrategyEnum storageStrategy` — `NONE` / `INNER_TABLE` / `CREATE_TABLE`。
- `String tplName` — Thymeleaf 模板名（`PageInstanceController` 渲染用）。
- `Map<String,Object> additionalInfo` — 透传给模板的附加信息（`FormConstants` 定义了若干 key）。

### `com.rick.formflow.form.cpn.core.CpnConfigurer`  实体 `@Table("sys_form_configurer")` 继承 `BaseEntity<Long>`
单个控件配置（一个表单字段对应一条）。
- `String name` — 字段名（必填，对应业务表列名/`FormBO.Property.name`）。
- `String label` — 显示标签（必填）。
- `CpnTypeEnum cpnType` — 控件类型（必填）。
- `Set<Map<String,?>> validators` — 校验器配置（DB 列 `validators`，varchar(512)）。`getValidatorList()` 懒加载为 `List<Validator>`，并自动追加控件自带的 `cpnValidators()`。
- `List<CpnOption> options` — 选项列表（DB 列 `options`）。
- `String datasource` — 字典 type（`DictService.getDictByType` 会自动填充到 `options`）。
- `String defaultValue` / `String placeholder` / `Boolean disabled` / `String cpnValueConverterName`。
- `Map<String,Object> additionalInfo`。
- 内部类 `CpnOption{String name; String label;}` — `name` 缺省回退到 `label`。

`getValidatorProperties()` 将所有校验器的 getter 拍平为 `ClassName.fieldName` → value 的 map，供前端 `th:with` 取 `Length.max` 等。

### `com.rick.formflow.form.cpn.core.FormCpn`  实体 `@Table("sys_form_cpn_configurer")`
表单与控件配置的关联（多对多）。字段：`Long formId; Long configId; Integer orderNum; Map additionalInfo`。

### `com.rick.formflow.form.cpn.core.FormCpnValue`  实体 `@Table("sys_form_cpn_value")`
`INNER_TABLE` 策略下的实例值存储。字段：`Long formCpnId; Long formId; Long configId; Long instanceId; String value`。一个实例 = 一组 `formCpnId → value`。

### `com.rick.formflow.form.cpn.core.CpnTypeEnum`
20 种控件类型：`HIDDEN LABEL TEXT TEXTAREA SELECT GROUP_SELECT MULTIPLE_SELECT SEARCH_SELECT SWITCH RADIO NUMBER_TEXT INTEGER_NUMBER CURRENCY CHECKBOX SINGLE_CHECKBOX MOBILE SINGLE_IMAGE FILE EMAIL DATE TIME TABLE`。
（注意：模板/源码中 `SINGLE_CHECKBOX` 在 enum 有，但 `cpn` 包下未见对应 `@Component` 类——待确认是否仅作前端区分。）

### `com.rick.formflow.form.cpn.core.Cpn<T>` 接口 / `AbstractCpn<T>`
控件处理器 SPI。关键方法：
- `CpnTypeEnum getCpnType()`
- `T parseValue(Object value)` — DB → UI/Java 对象（按泛型 `T` 转换，支持 String/Integer/BigDecimal/JSON）。
- `String getStringValue(T value)` — UI → DB 字符串（非 String 走 JSON）。
- `T httpConverter(Object value)` — HTTP 参数 → Java 对象（String 走 `parseValue`，其它直接强转）。
- `void valid(T value, List<CpnOption> options)` — 选项合法性校验（默认实现：值必须在 options 的 name 集合内）。
- `void check(CpnConfigurer configurer)` — 配置期检查（默认：选项 label 不重复）。
- `Set<ValidatorTypeEnum> validatorSupports()` — 该控件支持的校验类型集合 = `{REQUIRED}` ∪ `cpnValidators().type` ∪ `internalValidatorSupports()`。
- `Set<Validator> cpnValidators()` — 控件自带校验器（如 `Date` 自带 `DateRegex`）。

`AbstractCpn<T>` 提供默认实现并实现 `InitializingBean`，通过 `ClassUtils.getClassGenericsTypes(getClass())` 解析泛型 `T` 的 Class 用于 `parseValue`。

### `com.rick.formflow.form.cpn.core.CpnManager`  `@Component`
静态注册中心。容器启动时收集所有 `Cpn` Bean，按 `getCpnType()` 建映射。`CpnManager.getCpnByType(type)` 是全局静态取控件处理器的入口。

### `com.rick.formflow.form.cpn.core.CpnInstanceProcessor`
单次提交的"字段处理器"实例（非 Bean，每次提交 new）。构造时完成 `httpConverter` → `parseValue` → `getStringValue`；`valid()` 执行选项 + 校验器链；`getParamValue()`/`getCpnValue()` 返回字符串值与 Java 对象值。

### `com.rick.formflow.form.service.bo.FormBO`  `@Value` 不可变
渲染/返回给前端的业务对象。
- `Form form` / `Long instanceId` / `List<Property> propertyList` / `Map<String,Object> data` / `FormAdvice formAdvice`（`@JsonIgnore`）。
- `Property{Long id; String name; CpnConfigurer configurer; Object value}` — 一个字段。
- `getPropertyMap()` — name → Property。
- `getActionUrl()` — `formId` 或 `formId/instanceId`（用于拼提交 URL）。
- `getMethod()` — 有 instanceId 返回 `PUT`，否则 `POST`。

### `com.rick.formflow.form.service.FormAdvice`  SPI 接口（全部 default）
业务钩子。一个表单通过 `Form.formAdviceName` 关联一个 Bean。容器中所有 `FormAdvice` 实例注入为 `Map<String, FormAdvice> formAdviceMap`（beanName → 实例）。
- `beforeInstanceHandle(FormBO, Long instanceId, Map values)` — 保存前。
- `afterInstanceHandle(FormBO, Long instanceId, Map values)` — 保存后。
- `init(Form, Long, Map)` / `beforeGetInstance(Form, Long, Map)` — `@Deprecated`。
- `afterGetInstance(Form, Long, List<Property>, Map)` — 取实例后。
- `beforeReturn(Form, Long, List<Property>, Map)` — 返回前。
- `beforeRender(Map parameterMap, FormBO)` — 渲染前（可返回新 `FormBO`，模板/`SharpFormProcessor` 调用）。
- `beforeDeleteInstance(Long)` / `afterDeleteInstance(Long)`。
- `boolean insertOrUpdate(Map values)` — `CREATE_TABLE` 策略下若返回 `true`，引擎跳过默认 `EntityDAO.insertOrUpdate`。

### `com.rick.formflow.form.service.CpnValueConverter<K,V>`  SPI 接口
值转换器，通过 `CpnConfigurer.cpnValueConverterName` 关联 Bean。`V convert(K k)`。内置 `DateTimeToStringConverter`（`LocalDateTime → String`）。

### 校验器 SPI：`com.rick.formflow.form.valid.core.Validator<T>` / `AbstractValidator<T>` / `ValidatorManager`
- `Validator<T>`：`void valid(T t); ValidatorTypeEnum getValidatorType(); String getMessage();`
- `AbstractValidator<T>`：重写 `equals/hashCode` 为按 class 比较（用于 `CpnConfigurer.validatorList` 去重）。
- `ValidatorManager` `@Component`：启动时收集所有 `Validator` Bean → `Map<ValidatorTypeEnum, Class>`，并手动注册 `REGEX → CustomizeRegex`。`getValidatorClassByType(type)` 静态取类。
- 内置实现（`com.rick.formflow.form.valid` 包）：`Required` `Length` `Size` `CustomizeRegex` `EmailRegex` `MobileRegex` `NumberRegex` `DecimalRegex` `PositiveInteger` `StringIntegerNumber` `TextNumberSize` `DateRegex` `TimeRegex`。

### `com.rick.formflow.config.FormFlowServiceAutoConfiguration`
Spring Boot 自动配置（`spring.factories` + `AutoConfiguration.imports`）。条件：`@ConditionalOnSingleCandidate(GridService.class)`（依赖 sharp-database2）。`@AutoConfigureAfter(SharpDatabaseAutoConfiguration)`。内部 `FormServiceConfiguration` `@ComponentScan("com.rick.formflow.form")`，注册 `DateTimeToStringConverter` Bean 与默认 `FormAdvice`（空实现，`@ConditionalOnMissingBean`）。

### `com.rick.formflow.form.service.FormConstants`
`additionalInfo` 中的 key 常量：`showSaveFormBtn`、`label-col`、`pane-list`、`css`、`js`。

## 配置项

模块自身无 `application.properties`/`@ConfigurationProperties`。运行期可配置点：
- `Form.tplName` — Thymeleaf 模板名（缺省 `tpl/form/form`）。
- `Form.formAdviceName` — `FormAdvice` Bean 名称。
- `Form.repositoryName` — `EntityDAO` Bean 名称（`CREATE_TABLE` 时使用）。
- `CpnConfigurer.cpnValueConverterName` — `CpnValueConverter` Bean 名称。
- `CpnConfigurer.datasource` — 字典 type，自动从 `DictService` 装载选项。
- 进程内缓存 `FormUtils.formCacheMap`：表单结构变更后需重启或调用 `FormUtils.update(id, null)` 失效（无自动失效逻辑——见陷阱）。

## 使用示例

### 1. 编程式定义一个表单（参考 `sharp-admin` 的 `FormTagTest`）

```java
// 1) 创建表单定义
Form form = formService.saveOrUpdate(Form.builder()
        .code("sys_user_form")
        .name("用户信息")
        .formAdviceName("userFormAdvice")   // 对应 @Component("userFormAdvice") 的 FormAdvice
        .tableName("sys_user")
        .repositoryName("userDAO")          // CREATE_TABLE 时由 ApplicationContext 取该 EntityDAO
        .storageStrategy(Form.StorageStrategyEnum.CREATE_TABLE)
        .tplName("demos/student/form-tag")
        .additionalInfo(Maps.of("label-col", 1))
        .build());

// 2) 设计控件
List<Validator> textValidators = Lists.newArrayList(new Required(true), new Length(16));
CpnConfigurer username = CpnConfigurer.builder()
        .cpnType(CpnTypeEnum.TEXT).name("code").label("用户名")
        .placeholder("请输入用户名").validatorList(textValidators)
        .build();
CpnConfigurer roleIds = CpnConfigurer.builder()
        .cpnType(CpnTypeEnum.MULTIPLE_SELECT).name("roleIds").label("角色")
        .datasource("sys_role")            // 自动从字典装载选项
        .build();

// 3) 保存控件配置 + 绑定到表单（FormCpnService 内部先 insertOrUpdate 控件，再写关联表）
formCpnService.saveOrUpdateByConfigurer(form, Lists.newArrayList(username, roleIds));
```

业务侧实现 `FormAdvice`：

```java
@Component
public class UserFormAdvice implements FormAdvice {
    private final PasswordEncoder passwordEncoder;
    @Override
    public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        values.put("password", passwordEncoder.encode("DEFAULT_PASSWORD"));
    }
    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        // 保存后副作用，如重建字典缓存
    }
}
```

### 2. HTTP 提交一个表单实例

新增（POST `/forms/ajax/{formId}`）：
```http
POST /forms/ajax/859875429241106432 HTTP/1.1
Content-Type: application/json

{"code":"u001","name":"张三","roleIds":["1","2"],"available":"1"}
```
返回 `{"success":true,"data":"<instanceId>"}`。

更新（PUT）：
```http
PUT /forms/ajax/859875429241106432/123
Content-Type: application/json

{"id":"123","code":"u001","name":"张三改"}
```

### 3. HTTP 渲染表单页面

```http
GET /forms/page/859875429241106432           # 新增态
GET /forms/page/859875429241106432/123        # 编辑态（带 instanceId）
```
返回 Thymeleaf 渲染的 `tpl/form/form`（或 `Form.tplName` 指定的模板），Model 含 `formBO`、`model`、`query`。

### 4. HTTP 配置表单（管理端）

```http
POST /forms/configs
Content-Type: application/json

{
  "form": {"id": 859875429241106432, "name": "用户信息", "storageStrategy": "CREATE_TABLE", ...},
  "configs": [
    {"cpnType":"TEXT","name":"code","label":"用户名","validatorList":[{"validatorType":"REQUIRED","required":true},{"validatorType":"LENGTH","min":0,"max":16}]}
  ]
}
```

### 5. 扩展：自定义控件类型

```java
@Component
public class MyCpn extends AbstractCpn<String> {
    @Override public CpnTypeEnum getCpnType() { return CpnTypeEnum.TEXT; } // 覆盖已有类型，或新增枚举
    @Override protected Set<ValidatorTypeEnum> internalValidatorSupports() {
        return Sets.newHashSet(ValidatorTypeEnum.LENGTH);
    }
}
```
注册为 `@Component` 后 `CpnManager` 自动收集。新增枚举值需同步修改 `CpnTypeEnum` 与前端模板（`tpl/form.html` 按 `cpnType` 分支渲染）。

### 6. 扩展：自定义校验器

```java
@Component
public class MyRegex extends AbstractValidator<String> {
    @Override public void valid(String v) { /* 正则校验 */ }
    @Override public ValidatorTypeEnum getValidatorType() { return ValidatorTypeEnum.REGEX; }
    @Override public String getMessage() { return "格式不正确"; }
}
```
注意 `REGEX` 类型由 `ValidatorManager` 强制映射到 `CustomizeRegex`，自定义 REGEX 校验器不会覆盖该映射——若需自定义正则，使用 `CustomizeRegex(regex, message)` 实例（非 Bean）配到 `CpnConfigurer.validatorList`。
