# sharp-formflow API

> 模块定位：**动态表单引擎**。提供"表单定义 → 字段控件配置 → 实例数据存取 → 校验"全链路能力，支持两种存储策略（内部表 `sys_form_cpn_value` KV 存储 / 外部业务表自动建表读写）。模块名中的 "flow" 指**表单数据流转链路**，不是工作流/审批引擎——无 Flowable/Camunda/Activiti 依赖，无流程节点、审批人、流转状态等概念。

## 模块坐标

```xml
<dependency>
  <groupId>com.rick.formflow</groupId>
  <artifactId>sharp-formflow</artifactId>
  <version>${sharp.version}</version> <!-- 2.0-SNAPSHOT -->
</dependency>
```

依赖：`sharp-database`（必选）、`sharp-meta`（必选，字典 DictService）、`sharp-fileupload`（provided，附件/图片控件需要）、`spring-boot-starter-web` + `validation-api`（provided）。

自动装配：`META-INF/spring.factories` 注册 `FormFlowServiceAutoConfiguration`，在 `GridServiceAutoConfiguration` 之后装配，条件是容器中存在单例 `GridService`。内部 `@ComponentScan("com.rick.formflow.form")` 扫描所有 Service/DAO/Cpn/Validator。引入即生效，无需额外 `@Enable`。

## 核心入口

### FormService（表单实例主服务）

`com.rick.formflow.form.service.FormService`，`@Service`。表单元数据增删改 + 实例数据 GET/POST/DELETE 的统一入口。

| 方法 | 签名 | 行为 |
|---|---|---|
| `saveOrUpdate` | `Form saveOrUpdate(@Valid Form form)` | 新增/更新表单元数据（`sys_form`）。`form.id` 为空走 insert，否则 update。返回带 id 的 Form |
| `getFormBO` | `FormBO getFormBO(Long formId, Long instanceId)` | 获取表单+属性+值的聚合对象。`instanceId` 为 null 返回空白表单（带默认值），非 null 返回回填实例。**核心读取入口** |
| `getFormBOById` | `FormBO getFormBOById(Long formId)` | 等价 `getFormBO(formId, null)`，空白表单 |
| `getFormBOByIdAndInstanceId` | `FormBO getFormBOByIdAndInstanceId(Long formId, Long instanceId)` | 回填实例。内部走 `FormCache`（内存缓存，见陷阱） |
| `post`（新增） | `void post(Long formId, Map<String,Object> values) throws BindException` | 提交实例数据。`values` key 为控件 `name`。校验失败抛 `BindException`。事务 |
| `post`（更新） | `void post(Long formId, Long instanceId, Map<String,Object> values) throws BindException` | 更新指定实例。若 `values` 含非空 `id` 字段，会覆盖传入的 `instanceId`。事务 |
| `delete`（批量） | `int delete(Long formId, Long[] instanceIds)` | 批量删除实例，逐个调用单删 |
| `delete`（单个） | `int delete(Long formId, Long instanceId)` | 删除单个实例。INNER_TABLE 策略删 `sys_form_cpn_value`；CREATE_TABLE 走 `EntityDAO`/`MapDAO` 逻辑删/物理删。事务 |
| `getFormAdviceByName` | `FormAdvice getFormAdviceByName(String formAdviceName)` | 按 Form.formAdviceName 取扩展钩子 Bean |

`post` 内部流程：取 FormBO → 逐属性构造 `CpnInstanceProcessor` 做 `httpConverter` + `valid`（选项校验 + Validator 链）→ 校验失败收集 `BindingResult`/抛 `BindException` → `FormAdvice.beforeInstanceHandle` → 按存储策略落库 → `FormAdvice.afterInstanceHandle`。

### FormCpnService（表单-控件绑定）

`com.rick.formflow.form.service.FormCpnService`，`@Service`。维护"表单 ↔ 控件配置"的有序关联（`sys_form_cpn_configurer`）。

| 方法 | 签名 | 行为 |
|---|---|---|
| `saveOrUpdateByConfigurer(Form, Collection<CpnConfigurer>)` | 事务。先 `formDAO.insertOrUpdate(form)`，再保存 configurer，最后建立 FormCpn 关联（orderNum 自增） |
| `saveOrUpdateByConfigIds(Long formId, Long... configIds)` | 按已有 configurer id 列表绑定到 form |
| `saveOrUpdateByConfigIds(Long formId, Collection<Long> configIds)` | 同上，Collection 重载 |
| `saveOrUpdateByConfigurer(Long formId, Collection<CpnConfigurer> configurerList)` | 不含 Form 本身的版本，先 `cpnConfigurerDAO.insertOrUpdate(configurerList)` 再建关联 |
| `saveOrUpdateByConfigIds(Long formId, List<FormCpn> formCpnList)` | 底层方法：按 formId 删旧关联后全量 insert 新关联（保留已存在 configId 的原 id） |

### CpnConfigurerService（控件配置 CRUD）

`com.rick.formflow.form.service.CpnConfigurerService`，`@Service`。控件配置项（label/type/options/validators/...）的保存与查询。

| 方法 | 签名 | 行为 |
|---|---|---|
| `saveOrUpdate` | `int[] saveOrUpdate(List<CpnConfigurer> configurers)` | 批量保存。保存前对每个 configurer 调 `CpnManager.getCpnByType(type).check(configurer)`（选项去重等）。`disabled` 为 null 默认 false |
| `findById` | `CpnConfigurer findById(Long id)` | 按 id 查（`cpnConfigurerDAO.selectById`） |

## Controller API

### FormController — 表单与绑定

`@RestController @RequestMapping("forms")`

| 方法 | HTTP | 路径 | Body/参数 | 返回 |
|---|---|---|---|---|
| `save` | POST | `/forms` | `Form` JSON | `Result`（含 form.id） |
| `formCpnMapping` | POST | `/forms/configs` | `FormConfig{form, configs[]}` | `Result`（form.id） |
| `formIdCpnMapping` | POST | `/forms/{formId}` | `List<CpnConfigurer>` | `Result` |
| `formIdConfigIdsMapping` | POST | `/forms/{formId}/configs` | `Long[] configIds` | `Result` |

`FormController.FormConfig`：嵌套 DTO，`form: Form` + `configs: List<CpnConfigurer>`，均 `@Valid`。

### CpnConfigurerController — 控件配置

`@RestController @RequestMapping("forms/configurers")`

| 方法 | HTTP | 路径 | Body | 返回 |
|---|---|---|---|---|
| `save` | POST | `/forms/configurers` | `List<CpnConfigurer>` | `Result`（id 数组） |

### AjaxInstanceController — AJAX 实例数据

`@RestController @RequestMapping("forms/ajax")`

| 方法 | HTTP | 路径 | 参数 | 返回 |
|---|---|---|---|---|
| `get` | GET | `/forms/ajax/{formId}` | — | `FormBO`（空白表单） |
| `get` | GET | `/forms/ajax/{formId}/{instanceId}` | — | `FormBO`（回填实例） |
| `save` | POST | `/forms/ajax/{formId}` | `Map<String,Object>` JSON | `Result<String>`（实例 id） |
| `update` | PUT/POST | `/forms/ajax/{formId}/{instanceId}` | `Map<String,Object>` JSON | `Result<String>`（instanceId） |
| `delete` | DELETE | `/forms/ajax/{formId}?ids=...` | `Long[] ids` | `Result` |
| `delete` | DELETE | `/forms/ajax/{formId}/{instanceId}` | — | `Result` |

### PageInstanceController — 页面渲染/提交

`@Controller @RequestMapping("forms/page")`

| 方法 | HTTP | 路径 | 行为 |
|---|---|---|---|
| `gotoFormPage` | GET | `/forms/page/{formId}` 或 `/forms/page/{formId}/{instanceId}` | 取 FormBO → `FormAdvice.beforeRender` → 渲染模板（`form.tplName` 默认 `tpl/form/form`）。支持 `readonly` 等查询参数 |
| `saveOrUpdate` | POST | `/forms/page/{formId}` 或 `/forms/page/{formId}/{instanceId}` | 表单提交。校验失败回填数据+errors 重渲染表单模板，成功跳 `success` |

> `sharp-admin` 的 `PageInstanceLayoutController` 继承本类，路径前缀 `forms/page/layout`，支持 layout/Ajax 片段渲染。

## 表单/字段模型

### Form（表单元数据实体）

`com.rick.formflow.form.cpn.core.Form extends BaseCodeEntity<Long>`，`@Table("sys_form")`。

| 字段 | 类型 | 列 | 说明 |
|---|---|---|---|
| `id` | Long | id | BaseCodeEntity 提供，雪花 id |
| `code` | String | code | BaseCodeEntity 提供 |
| `name` | String | name | 表单名（`@NotBlank`） |
| `formAdviceName` | String | form_advice_name | 关联 `FormAdvice` Bean 名，可空 |
| `tableName` | String | table_name | CREATE_TABLE 策略下的外部表名 |
| `repositoryName` | String | repository_name | CREATE_TABLE 策略下指定的 `EntityDAO` Bean 名（优先于 tableName） |
| `storageStrategy` | `Form.StorageStrategyEnum` | storage_strategy | 存储策略枚举 |
| `tplName` | String | tpl_name | Thymeleaf 模板名 |
| `additionalInfo` | `Map<String,Object>` | additional_info | 扩展信息（`showSaveFormBtn`/`label-col`/`pane-list`/`css`/`js`，见 FormConstants） |

`Form.StorageStrategyEnum`：`NONE`（不落库，仅靠 FormAdvice）、`INNER_TABLE`（KV 存 `sys_form_cpn_value`）、`CREATE_TABLE`（外部业务表，走 EntityDAO/MapDAO）。`@JsonValue` 输出枚举名。

### CpnConfigurer（控件配置实体）

`com.rick.formflow.form.cpn.core.CpnConfigurer extends BaseEntityWithLongId`，`@Table("sys_form_configurer")`。一个表单字段的全部配置。

| 字段 | 类型 | 列 | 说明 |
|---|---|---|---|
| `id` | Long | id | |
| `name` | String | name | 字段名（表单 key，提交 Map 的 key），`@NotEmpty` |
| `label` | String | label | 显示标签，`@NotBlank` |
| `cpnType` | `CpnTypeEnum` | type | 控件类型枚举 |
| `validators` | `Set<Map<String,?>>` | validators | 验证器配置（JSON 文本存） |
| `options` | `List<CpnOption>` | options | 选项列表 |
| `datasource` | String | data_source | 字典 type，非空时加载 `DictService.getDictByType` 覆盖 options |
| `defaultValue` | String | default_value | 默认值 |
| `placeholder` | String | placeholder | |
| `disabled` | Boolean | is_disabled | |
| `cpnValueConverterName` | String | cpn_value_converter_name | 关联 `CpnValueConverter` Bean 名 |
| `additionalInfo` | `Map<String,Object>` | additional_info | 扩展（TABLE 控件用 `columns`/`labels`） |

`getValidatorList()`：惰性把 `validators` JSON 反序列化为 `Validator` 对象集合（合并控件自带的 `cpnValidators()`）。`getOptionMap()`：name→label。`CpnOption`：`name`+`label`，`name` 缺省取 label。

### FormCpn（表单-控件关联实体）

`@Table("sys_form_cpn_configurer")`。字段：`formId`、`configId`、`orderNum`、`additionalInfo`。一行 = 表单上某序位绑定的某个 configurer。

### FormCpnValue（实例值实体，INNER_TABLE 专用）

`@Table("sys_form_cpn_value")`。字段：`formCpnId`、`formId`、`configId`、`instanceId`、`value`(String)。一次实例提交 = 一组 FormCpnValue。

### FormBO（业务对象，读取/提交的聚合载体）

`com.rick.formflow.form.service.bo.FormBO`（`@Value` 不可变）。包含：`form`、`instanceId`、`propertyList`、`data`（Map）、`formAdvice`。
- `Property`：`id`、`name`、`configurer`、`value`（可 set）。
- `getActionUrl()`：`formId` 或 `formId/instanceId`。
- `getMethod()`：有 instanceId 返回 `PUT`，否则 `POST`。
- `getPropertyMap()`：name→Property。

### CpnTypeEnum（控件类型）

`com.rick.formflow.form.cpn.core.CpnTypeEnum`，21 种：`HIDDEN/LABEL/TEXT/TEXTAREA/SELECT/GROUP_SELECT/MULTIPLE_SELECT/SEARCH_SELECT/SWITCH/RADIO/NUMBER_TEXT/INTEGER_NUMBER/CURRENCY/CHECKBOX/SINGLE_CHECKBOX/MOBILE/SINGLE_IMAGE/FILE/EMAIL/DATE/TIME/TABLE`。`@JsonValue` 输出枚举名。`valueOfCode(String)` 反查。

> `SINGLE_CHECKBOX`、`SINGLE_IMAGE` 枚举已定义，但 `cpn/` 包下未见对应 `@Component` 实现类（仅 CHECKBOX/SINGLE_IMAGE 等已实现），自定义前需注意补齐，否则 `CpnManager.getCpnByType` 返回 null（待确认是否有外部模块注册）。

## 控件体系（Cpn）

接口 `Cpn<T>`，抽象基类 `AbstractCpn<T>`。泛型 `T` 为该控件经 `parseValue` 后的业务值类型（如 Text→String、CheckBox→`List<String>`、Currency→BigDecimal、Attachment→`List<Map>`）。

| 方法 | 说明 |
|---|---|
| `getCpnType()` | 返回对应 `CpnTypeEnum` |
| `parseValue(Object)` | DB 值 → 业务值（String/JSON → T） |
| `getStringValue(T)` | 业务值 → DB 存储字符串（String 直存，复杂对象 `JsonUtils.toJson`） |
| `httpConverter(Object)` | HTTP 提交值 → 业务值（CheckBox 处理 `[..]` 字符串，SingleImage 处理 JSON Map） |
| `valid(T, List<CpnOption>)` | 选项合法性校验（默认校验值在 options.name 集合内） |
| `check(CpnConfigurer)` | 配置期校验（选项去重等） |
| `validatorSupports()` | 该控件支持的 `ValidatorTypeEnum` 集合 = `REQUIRED` ∪ `cpnValidators()` 类型 ∪ `internalValidatorSupports()` |
| `hasValidator(Validator)` | 是否支持某验证器 |
| `cpnValidators()` | 控件**自带、不可关闭**的验证器（如 Mobile 自带 `MobileRegex`，Email 自带 `EmailRegex`+`Length(32)`） |

`AbstractCpn`：`afterPropertiesSet` 反射拿到泛型类型 `cpnClass`，供 `parseValue` 默认实现做 String/Integer/BigDecimal/JSON 转换。`check` 默认实现选项 label 去重。

### 控件实现一览（`com.rick.formflow.form.cpn`）

| 类 | 类型 | 泛型 | 特性 |
|---|---|---|---|
| `Hidden` | HIDDEN | String | 无验证 |
| `Label` | LABEL | String | 仅展示 |
| `Text` | TEXT | String | 支持 LENGTH |
| `TextArea` | TEXTAREA | String | 支持 LENGTH |
| `Select` | SELECT | String | Enum/Number 兼容 |
| `GroupSelect` | GROUP_SELECT | String | 委托 Select |
| `MultipleSelect` | MULTIPLE_SELECT | `List<String>` | 委托 CheckBox |
| `SearchSelect` | SEARCH_SELECT | String | 委托 Select |
| `Switch` | SWITCH | String | Boolean→"1"/"0" |
| `Radio` | RADIO | String | Enum 兼容 |
| `NumberText` | NUMBER_TEXT | String | 自带 NumberRegex，支持 TEXT_NUMBER_SIZE |
| `IntegerNumber` | INTEGER_NUMBER | Integer | 支持 SIZE |
| `Currency` | CURRENCY | BigDecimal | 自带 NumberRegex，支持 SIZE |
| `CheckBox` | CHECKBOX | `List<String>` | 多选，Boolean/Enum/DictValue 兼容 |
| `Mobile` | MOBILE | String | 自带 MobileRegex |
| `SingleImage` | SINGLE_IMAGE | `Map<String,Object>` | JSON 图片对象 |
| `Attachment` | FILE | `List<Map<String,Object>>` | 多文件 JSON |
| `Email` | EMAIL | String | 自带 EmailRegex + Length(32) |
| `Date` | DATE | String | 自带 DateRegex，兼容 LocalDate/Date |
| `Time` | TIME | String | 自带 TimeRegex |
| `Table` | TABLE | `List<List>` | 可编辑表格，过滤全空行 |

## 校验器（Validator）

接口 `Validator<T>`（`valid(T)` / `getValidatorType()` / `getMessage()`），抽象 `AbstractValidator<T>`（重写 equals/hashCode 按类去重）。`ValidatorTypeEnum`：LENGTH/POSITIVE_INTEGER/NUMBER/REQUIRED/SIZE/REGEX/TEXT_NUMBER_SIZE/DATE/TIME/EMAIL/MOBILE/DECIMAL。

`ValidatorManager`（`@Component`，`InitializingBean`）：收集容器内所有 `Validator` Bean 建立 `ValidatorTypeEnum → Class` 映射；手动追加 `REGEX → CustomizeRegex`（CustomizeRegex 未加 `@Component`）。`getValidatorClassByType(type)` 静态查询。

| 验证器类 | type | 触发条件 | 备注 |
|---|---|---|---|
| `Required` | REQUIRED | `required=true` 且值为 null/空集合/空白串 | `@Component`，可配 `required` 布尔 |
| `Length` | LENGTH | 非空且长度越界 | `min/max`，构造 `Length(max)` |
| `Size` | SIZE | Number 越界 | `min/max` |
| `TextNumberSize` | TEXT_NUMBER_SIZE | 字符串数字 + 范围 | 复合 |
| `EmailRegex` | EMAIL | 邮箱正则 | `@Component` |
| `MobileRegex` | MOBILE | 手机号正则 | `@Component` |
| `DecimalRegex` | DECIMAL | 小数正则 | `@Component` |
| `DateRegex` | DATE | 日期格式 | `@Component` |
| `TimeRegex` | TIME | 时间格式 | `@Component` |
| `NumberRegex` | NUMBER | 数字格式 | `@Component` |
| `PositiveInteger` | POSITIVE_INTEGER | 正整数 | `@Component` |
| `CustomizeRegex` | REGEX | 自定义 `regex`+`message` | **非 @Component**，通过 validators JSON 配置构造 |

> 控件自带的 `cpnValidators()` 会无条件附加（如 Mobile 总带 MobileRegex），与用户在 `validators` 中显式配置的是两套，`getValidatorList()` 合并返回。

## 扩展点

### FormAdvice（表单生命周期钩子）

`com.rick.formflow.form.service.FormAdvice`，接口，全 default 方法。注册为 `@Component` 后，通过 `Form.formAdviceName` 指定 Bean 名关联。`FormService` 注入 `Map<String,FormAdvice>`。

| 回调 | 时机 |
|---|---|
| `init(form, instanceId, valueMap)` | （已废弃）回填前 |
| `beforeGetInstance(form, instanceId, valueMap)` | （已废弃）CREATE_TABLE 取实例前 |
| `afterGetInstance(form, instanceId, propertyList, valueMap)` | CREATE_TABLE 取实例后 |
| `beforeReturn(form, instanceId, propertyList, valueMap)` | 返回 FormBO 前（所有策略） |
| `beforeRender(parameterMap, formBO)` | Thymeleaf 渲染前，可改 FormBO（PageInstanceController/SharpFormProcessor 调用） |
| `beforeInstanceHandle(form, instanceId, values)` | 提交校验后、落库前，可改写 values（如加密密码、转 roleId→Role 对象） |
| `insertOrUpdate(values)` | CREATE_TABLE 落库前，返回 true 则**跳过框架默认落库**（自行处理） |
| `afterInstanceHandle(form, instanceId, values)` | 落库后（如重建字典缓存） |
| `beforeDeleteInstance(instanceId)` / `afterDeleteInstance(instanceId)` | 删除前后 |

### CpnValueConverter（值转换器）

`com.rick.formflow.form.service.CpnValueConverter<K,V>`，`convert(K): V`。注册 `@Component`，通过 `CpnConfigurer.cpnValueConverterName` 关联。`FormService` 注入 `Map<String,CpnValueConverter>`。内置 `DateTimeToStringConverter`（LocalDateTime→字符串，自动配置类注册）。

### 自定义控件

实现 `Cpn<T>` 或继承 `AbstractCpn<T>`，标 `@Component`，`getCpnType()` 返回新 `CpnTypeEnum`。`CpnManager` 启动时收集所有 `Cpn` Bean 建映射，`getCpnByType` 静态查询。

### 自定义校验器

实现 `Validator<T>` 继承 `AbstractValidator<T>`，标 `@Component`，`getValidatorType()` 返回 `ValidatorTypeEnum`。`ValidatorManager` 自动收集。若用 REGEX 类型，复用 `CustomizeRegex`（通过 validators JSON 配 `regex`+`message`）。

## 配置

无独立 `@ConfigurationProperties`。唯一配置点是 `FormFlowServiceAutoConfiguration`：
- 注册 `DateTimeToStringConverter` Bean。
- `@ConditionalOnMissingBean` 注册空实现 `FormAdvice`（所有回调 no-op），保证 `FormService` 在无业务 Advice 时可启动。

`FormUtils`：进程级静态 `Map<Long,FormCache> formCacheMap`，无过期/无失效机制（见陷阱）。

## 使用示例

### 1. 定义表单 + 绑定字段（HTTP）

```http
POST /forms/configs
Content-Type: application/json

{
  "form": {
    "id": 487677232379494400,
    "name": "我的第一个表单",
    "storageStrategy": "INNER_TABLE",
    "formAdviceName": null
  },
  "configs": [
    {
      "id": 487671506907070464,
      "name": "name",
      "label": "姓名",
      "cpnType": "TEXT",
      "validators": [{"validatorType":"LENGTH","max":16},{"validatorType":"REQUIRED","required":true}],
      "defaultValue": "Rick",
      "placeholder": "请输入姓名"
    },
    {
      "name": "age",
      "label": "年龄",
      "cpnType": "NUMBER_TEXT",
      "validators": [{"validatorType":"SIZE","min":18,"max":100},{"validatorType":"REQUIRED","required":true}],
      "defaultValue": "18"
    },
    {
      "name": "hobby",
      "label": "兴趣爱好",
      "cpnType": "CHECKBOX",
      "options": [{"name":"football","label":"足球"},{"name":"basketball","label":"篮球"}],
      "validators": [{"validatorType":"REQUIRED","required":true}]
    }
  ]
}
```

`validators` 元素必须含 `validatorType` 字段（反序列化用），其余字段按对应 Validator 类属性。

### 2. 提交实例（AJAX）

```http
POST /forms/ajax/487677232379494400
Content-Type: application/json

{"name":"李峰","age":"39","hobby":["足球","篮球"]}
```

返回 `{"code":200,"data":"<instanceId>"}`。校验失败抛 `BindException`（AjaxInstanceController 不捕获，全局异常处理）。

### 3. 页面渲染 + 提交

- 渲染：`GET /forms/page/487677232379494400`（新建）或 `/forms/page/{formId}/{instanceId}`（编辑）。
- 提交：`POST /forms/page/{formId}` 或 `POST /forms/page/{formId}/{instanceId}`，表单字段直传。

### 4. 查询实例

```http
GET /forms/ajax/487677232379494400/487684156282011648
```

返回 `FormBO` JSON：`form` 元数据 + `propertyList`（每个含 configurer+value）+ `data`。

### 5. FormAdvice 扩展（Java）

```java
@Component
public class UserFormAdvice implements FormAdvice {
    @Override
    public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String,Object> values) {
        values.put("password", passwordEncoder.encode("DEFAULT"));
        // 把 roleIds 转成 Role 对象放进 values，供外部表 insertOrUpdate
    }
    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String,Object> values) {
        dictService.rebuild("sys_user"); // 重建字典缓存
    }
}
```

`Form.formAdviceName = "userFormAdvice"`（Bean 名）即关联。

### 6. Thymeleaf 自定义表单标签（外部模块示例）

`sharp-admin` 通过 `SharpFormProcessor` 注册 `<sharp:form id=".." value=".." form-page=".." readonly show-btn>` 标签，内部调 `formService.getFormBO` + `beforeRender` 后渲染指定模板。见 `ARCHITECTURE.md`。
