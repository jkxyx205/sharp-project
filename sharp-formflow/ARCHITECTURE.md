# sharp-formflow 架构

## 真实职责（澄清）

模块名为 `sharp-formflow`，但 **不是工作流/审批引擎**。全模块代码中无 Flowable/Camunda/Activiti 依赖，无流程定义（BPMN）、流程实例、任务节点、审批人、流转状态机等任何概念。

`flow` 在本模块指**表单数据流转链路**：从"表单元数据定义 → 字段控件配置（含校验规则） → HTTP 提交值校验 → 实例数据存储/回填 → 页面渲染"的完整数据流。本质上是一个**配置化动态表单引擎**：表单结构由数据库配置驱动，控件类型可插拔，校验器可组合，存储策略可选，渲染走 Thymeleaf 模板。

## 包结构

```
com.rick.formflow
├── config
│   └── FormFlowServiceAutoConfiguration      自动装配入口（spring.factories）
└── form
    ├── cpn
    │   ├── core                              核心抽象
    │   │   ├── Cpn / AbstractCpn             控件接口/基类
    │   │   ├── CpnTypeEnum                   21 种控件类型枚举
    │   │   ├── CpnManager                    控件注册表（静态 Map）
    │   │   ├── CpnInstanceProcessor          单字段提交处理（转换+校验）
    │   │   ├── CpnConfigurer                 控件配置实体（@Table sys_form_configurer）
    │   │   ├── Form / FormCpn / FormCpnValue 表单/关联/值实体
    │   └── *.java                            21 个具体控件实现（@Component）
    ├── controller
    │   ├── FormController                    表单+绑定 REST
    │   ├── CpnConfigurerController          控件配置 REST
    │   └── instance
    │       ├── PageInstanceController       页面渲染/表单提交（Thymeleaf）
    │       └── AjaxInstanceController       AJAX 实例 CRUD
    ├── dao                                  4 个 EntityDAO（Form/FormCpn/CpnConfigurer/FormCpnValue）
    ├── service
    │   ├── FormService                      主服务（实例 GET/POST/DELETE + 元数据）
    │   ├── FormCpnService                   表单-控件绑定
    │   ├── CpnConfigurerService             控件配置 CRUD
    │   ├── FormAdvice                       生命周期钩子接口（扩展点）
    │   ├── CpnValueConverter                值转换接口（扩展点）
    │   ├── FormUtils                        进程内 FormCache 静态缓存
    │   ├── FormConstants                    additionalInfo key 常量
    │   ├── bo/FormBO                        聚合业务对象
    │   ├── model/FormCache                  可序列化缓存对象
    │   └── convert/DateTimeToStringConverter 内置值转换
    └── valid
        ├── core
        │   ├── Validator / AbstractValidator 验证器接口/基类
        │   ├── ValidatorTypeEnum             12 种验证类型枚举
        │   └── ValidatorManager             验证器注册表（静态 Map）
        └── *.java                           12 个验证器实现
```

## 核心抽象关系

### 表单模型 ↔ 控件配置 ↔ 控件实现

```
Form（sys_form，表单元数据）
  │ 1:N
  ▼
FormCpn（sys_form_cpn_configurer，表单上某序位绑定的 configurer）
  │ N:1
  ▼
CpnConfigurer（sys_form_configurer，字段配置：label/type/options/validators/...）
  │ type 字段 → CpnTypeEnum
  ▼
CpnManager.getCpnByType(CpnTypeEnum) → Cpn<T> 实现（@Component）
```

- **Form** 是表单的"壳"：name + 存储策略 + 模板 + Advice 关联。
- **CpnConfigurer** 是字段级配置，**与 Form 无直接外键**——通过 `FormCpn`（关联表，含 `orderNum`）建立"Form 在第 N 位使用 configurer X"的关系。一个 CpnConfigurer 可被多个 Form 复用。
- **Cpn<T>** 是无状态单例（`@Component`），按 `CpnTypeEnum` 在 `CpnManager` 注册。所有同类型字段共享同一 Cpn 实例，Cpn 不持有任何字段值。

### 提交数据流（FormService.handle）

```
HTTP Map<String,Object> values
  │ key = CpnConfigurer.name
  ▼
对每个 Property 构造 CpnInstanceProcessor(property, paramValue, bindingResult)
  ├─ cpn.httpConverter(paramValue)  → cpnValue (业务类型 T)
  ├─ cpn.getStringValue(cpnValue)   → stringValue (DB 存储串)
  ├─ cpn.valid(cpnValue, options)   选项校验 → BindingResult
  └─ 遍历 configurer.getValidatorList()
       └─ cpn.hasValidator(v) ? v.valid(cpnValue) → BindingResult
  │
  ├─ bindingResult.hasErrors() → 抛 BindException
  ▼
FormAdvice.beforeInstanceHandle(form, instanceId, values)  改写 values
  │
  ├─ INNER_TABLE: formCpnValueDAO.deleteByInstanceId + insert(FormCpnValue 列表)
  ├─ CREATE_TABLE + repositoryName: EntityDAO.insertOrUpdate(values)
  ├─ CREATE_TABLE + tableName:     MapDAO.insertOrUpdate(values)
  └─ FormAdvice.insertOrUpdate(values)==true → 跳过上述默认落库
  │
  ▼
FormAdvice.afterInstanceHandle(form, instanceId, values)
```

### 读取数据流（FormService.getFormBOByIdAndInstanceId）

```
FormCache（进程缓存 FormUtils.formCacheMap）命中？
  ├─ 否: formDAO.selectById + formCpnDAO.listByFormId + cpnConfigurerDAO.selectByIdsAsMap → 构建 FormCache → 缓存
  └─ 是: SerializationUtils.clone(FormCache)  深拷贝避免污染缓存
  ▼
按 storageStrategy 取实例值:
  ├─ INNER_TABLE: formCpnValueDAO.selectByInstanceIdAsMap → 按 formCpnId 取 value
  └─ CREATE_TABLE: EntityDAO/MapDAO.selectById → valueMap（propertyName↔columnName 双向）
  ▼
FormAdvice.init / beforeGetInstance / afterGetInstance / beforeReturn 链
  ▼
每个 FormCpn: cpn.parseValue(value) + 可选 CpnValueConverter → Property.value
  ▼
FormBO(form, instanceId, propertyList, data, formAdvice)
```

### 渲染数据流

`PageInstanceController.gotoFormPage` → `formService.getFormBO` → `FormAdvice.beforeRender` → 填充 `model`（formBO + 数据 Map + query 参数）→ Thymeleaf 渲染 `tplName`（默认 `tpl/form/form`）。模板内按 `p.configurer.cpnType` 用 `th:if` 分支渲染各控件 HTML，提交目标 `th:action="'/forms/page/' + formBO.actionUrl`。

## 存储策略

| 策略 | 读 | 写 | 删 | 适用 |
|---|---|---|---|---|
| NONE | 无值 | 不落库（仅 FormAdvice） | — | 纯 Advice 驱动 |
| INNER_TABLE | `sys_form_cpn_value` 按 instanceId 取 | 先按 instanceId 删，再批量 insert | 删 `sys_form_cpn_value` | 通用，无独立业务表 |
| CREATE_TABLE + repositoryName | `EntityDAO.selectById` | `EntityDAO.insertOrUpdate(Map)` | `EntityDAO.deleteLogicallyById`（实体则逻辑删） | 有业务实体+级联 |
| CREATE_TABLE + tableName | `MapDAO.selectById` | `MapDAO.insertOrUpdate(Map)` | （仅走 Advice 链） | 仅有动态表 |

## 对外依赖

- **sharp-database**：`EntityDAO`/`EntityCodeDAO`/`MapDAO`/`GridService`、`@Table`/`@Column` 注解、`BaseEntityUtils`。强依赖。
- **sharp-meta**：`DictService`/`Dict`/`DictValue`。`datasource` 字段非空时按字典 type 加载选项。
- **sharp-fileupload**（provided）：附件/图片控件的文件存储。`SingleImage`/`Attachment` 的 value 是文件元信息 JSON（含 url/path/name/size 等），由前端上传后回填。
- Spring Web + Validation（provided）：Controller、`BindingResult`/`BindException`、`@Validated`。
- Thymeleaf：模板渲染（provided 传递，实际由宿主 sharp-admin 提供）。
- 无 ORM 中间件，DAO 全部继承 `sharp-database` 的 `EntityDAOImpl`/`EntityCodeDAOImpl`。

## 扩展点

1. **自定义控件**：`AbstractCpn<T>` + `@Component` + `getCpnType()` 返回 `CpnTypeEnum`。实现 `httpConverter`（HTTP→T）、`parseValue`（DB→T）、`getStringValue`（T→DB串）、`valid`（选项校验）、`cpnValidators`（自带验证）、`internalValidatorSupports`（可配验证类型）。`CpnManager` 自动收集。
2. **自定义校验器**：`AbstractValidator<T>` + `@Component` + `getValidatorType()`。`ValidatorManager` 自动收集。`CpnConfigurer.validators` JSON 中填 `{validatorType, ...}` 即可启用。
3. **FormAdvice**：实现接口 + `@Component`，`Form.formAdviceName=Bean名` 关联。覆盖 9 个生命周期回调。
4. **CpnValueConverter**：实现接口 + `@Component`，`CpnConfigurer.cpnValueConverterName=Bean名` 关联。读取时对原始 value 做转换再 `parseValue`。
5. **Thymeleaf 标签**（外部模块）：`SharpFormProcessor` 模式，`<sharp:form>` 标签内调 `FormService.getFormBO` + `beforeRender` 渲染指定模板。

## 配置与启动

- 引入依赖即自动装配，无需 `@EnableXxx`。条件：容器存在单例 `GridService`（来自 sharp-database）。
- 默认注册空 `FormAdvice`（`@ConditionalOnMissingBean`），无业务 Advice 也能启动。
- 无数据库迁移脚本——`sql/form.sql` 为示例数据（含 4 张表 DDL + 演示数据）。生产环境需自行建表，表结构见下节。
- 无独立 `application.yml` 配置项。

## 数据库表（4 张）

| 表 | 实体 | 职责 | 关键列 |
|---|---|---|---|
| `sys_form` | Form | 表单元数据 | id, code, name, form_advice_name, table_name, repository_name, storage_strategy, tpl_name, additional_info |
| `sys_form_configurer` | CpnConfigurer | 字段配置（可复用） | id, name, label, type, validators(JSON), options(JSON), data_source, default_value, placeholder, is_disabled, cpn_value_converter_name, additional_info(JSON) |
| `sys_form_cpn_configurer` | FormCpn | 表单↔configurer 有序关联 | id, form_id, config_id, order_num, additional_info |
| `sys_form_cpn_value` | FormCpnValue | INNER_TABLE 实例值 | id, value(text), form_cpn_id, form_id, config_id, instance_id |

通用列（BaseEntity/BaseEntityWithLongId 提供）：`create_id`/`create_time`/`update_id`/`update_time`/`is_deleted`。`Form` 继承 `BaseCodeEntity` 额外有 `code` 列。

CREATE_TABLE 策略下的外部业务表由使用方自行维护（`tableName` 或 `repositoryName` 指向）。

## 设计要点

- **Cpn 无状态**：所有 `@Component` 单例，泛型类型 `afterPropertiesSet` 反射获取并缓存。线程安全靠不持有可变状态。
- **FormCache 深拷贝**：`getFormBO` 命中缓存后 `SerializationUtils.clone`，避免调用方修改污染缓存。要求 FormCache 及其内部对象（Form/FormCpn/CpnConfigurer）均可序列化。
- **Validator 双源合并**：`getValidatorList()` = 控件自带 `cpnValidators()` ∪ 配置的 `validators` JSON 反序列化。前者不可关闭（如 EmailRegex），后者按需。
- **option 动态化**：`datasource` 非空时 `getFormBO` 内用 `DictService` 实时覆盖 options，实现选项走字典维护。
- **提交 instanceId 推断**：`post` 优先用 `values.id`，其次参数 instanceId，INNER_TABLE 新建时自动 `IdGenerator.getSequenceId()`。
