# sharp-formflow 架构

## 总体

`sharp-formflow` 是配置驱动的表单引擎。核心抽象三层：

1. **元数据层**：`Form`（表单）+ `CpnConfigurer`（控件配置）+ `FormCpn`（表单↔控件关联）+ `FormCpnValue`（实例值，仅 INNER_TABLE 用）。全部为 sharp-database2 的 `@Table` 实体。
2. **控件/校验器层**：`Cpn<T>` SPI（20 种内置实现，`@Component` 自动注册到 `CpnManager`）+ `Validator<T>` SPI（12 种内置实现，注册到 `ValidatorManager`）。
3. **服务/入口层**：`FormService`（核心）+ `FormCpnService`/`CpnConfigurerService`（配置）+ 4 个 Controller（页面、Ajax、表单管理、控件管理）+ `FormAdvice`/`CpnValueConverter` SPI。

## 包结构

```
com.rick.formflow
├── config
│   └── FormFlowServiceAutoConfiguration      自动配置（@ComponentScan("com.rick.formflow.form")）
└── form
    ├── controller
    │   ├── FormController                     POST /forms, /forms/configs, /forms/{id}, /forms/{id}/configs
    │   ├── CpnConfigurerController            POST /forms/configurers
    │   └── instance
    │       ├── PageInstanceController         GET/POST /forms/page/{formId}[/{instanceId}]  (Thymeleaf)
    │       └── AjaxInstanceController         GET/POST/PUT/DELETE /forms/ajax/{formId}[/{instanceId}]
    ├── cpn
    │   ├── core
    │   │   ├── Cpn<T> / AbstractCpn<T>        控件 SPI + 默认实现
    │   │   ├── CpnManager                     静态注册中心（按 CpnTypeEnum 取 Cpn）
    │   │   ├── CpnInstanceProcessor           单次提交的字段处理器
    │   │   ├── CpnConfigurer                  控件配置实体（sys_form_configurer）
    │   │   ├── CpnTypeEnum                    20 种控件类型
    │   │   ├── Form                           表单实体（sys_form）
    │   │   ├── FormCpn                        关联实体（sys_form_cpn_configurer）
    │   │   └── FormCpnValue                   实例值实体（sys_form_cpn_value）
    │   ├── Text Select TextArea CheckBox ...   20 种内置控件实现
    │   └── (各控件 @Component，由 CpnManager 收集)
    ├── dao
    │   ├── FormDAO / FormCpnDAO / CpnConfigurerDAO / FormCpnValueDAO
    │   └── CpnConfigurerDAO 重写 select，按 datasource 自动从 DictService 装载选项
    ├── service
    │   ├── FormService                        核心：getFormBO / post / delete
    │   ├── FormCpnService                     表单↔控件绑定
    │   ├── CpnConfigurerService              控件配置 CRUD
    │   ├── FormAdvice                         业务钩子 SPI
    │   ├── CpnValueConverter                 值转换 SPI
    │   ├── FormConstants                     additionalInfo key 常量
    │   ├── FormUtils                          进程内 FormCache 缓存（@UtilityClass）
    │   ├── bo.FormBO                          渲染/返回的不可变业务对象
    │   ├── convert.DateTimeToStringConverter  内置 CpnValueConverter
    │   └── model.FormCache                    Form + FormCpnList + configIdMap（Serializable，用于克隆）
    └── valid
        ├── core.Validator<T> / AbstractValidator / ValidatorManager / ValidatorTypeEnum
        └── Required Length Size CustomizeRegex EmailRegex MobileRegex NumberRegex
            DecimalRegex PositiveInteger StringIntegerNumber TextNumberSize DateRegex TimeRegex
```

## 关键抽象关系

```
Form 1───* FormCpn *───1 CpnConfigurer
                                │
                                │ cpnType
                                ▼
                             Cpn<T>  (20 种实现，CpnManager 注册)
                                │
                                │ validatorList
                                ▼
                          Validator<T>  (12 种实现，ValidatorManager 注册)

Form.formAdviceName ──> FormAdvice Bean (Map<String,FormAdvice>)
CpnConfigurer.cpnValueConverterName ──> CpnValueConverter Bean
Form.repositoryName ──> EntityDAO Bean (CREATE_TABLE 时使用)
```

- **表单模型**与**控件处理器**解耦：`Form`/`CpnConfigurer` 是配置数据，`Cpn<T>` 是行为（类型转换/校验/选项检查）。运行时 `CpnManager.getCpnByType(configurer.getCpnType())` 桥接两者。
- **校验器**两层来源：`CpnConfigurer.validators`（DB 配置，懒加载为 `List<Validator>`）+ `Cpn.cpnValidators()`（控件自带，如 `Date` 自带 `DateRegex`）。`CpnInstanceProcessor.valid()` 跑合并后的列表，但仅对 `Cpn.hasValidator(v)` 为真的校验器生效。

## 数据流：一次 Ajax 提交

以 `POST /forms/ajax/{formId}`（`AjaxInstanceController.save` → `FormService.post(formId, values)`）为例：

```
HTTP JSON  values={code:u001, name:张三, roleIds:[1,2], id:""} 
   │
   ▼
FormService.handle(formId, instanceId=null, values)
   │
   ├─ getFormBOById(formId)  ── 命中 FormUtils.formCacheMap ? 克隆 : 查 DB 组装 FormCache
   │     └─ FormCache = {Form, List<FormCpn>, Map<configId, CpnConfigurer>}
   │     └─ 逐 FormCpn 取 CpnConfigurer，按 storageStrategy 装载实例值（此处无 instanceId → defaultValue）
   │     └─ CpnValueConverter（若配了）转换 value
   │     └─ Cpn.parseValue(value) → Java 对象
   │     └─ FormBO.Property{formCpnId, name, configurer, value}
   │     └─ FormAdvice.beforeReturn / afterGetInstance / beforeRender（渲染路径才有）
   │
   ├─ 若 values.id 非空 → instanceId = Long(values.id)  (覆盖)
   │  否则 INNER_TABLE → IdGenerator.getSequenceId()
   │
   ├─ for each Property:
   │     CpnInstanceProcessor(property, paramValue, bindingResult)
   │       └─ cpn = CpnManager.getCpnByType(configurer.cpnType)
   │       └─ cpnValue  = cpn.httpConverter(paramValue)   // HTTP → Java
   │       └─ stringValue = cpn.getStringValue(cpnValue)  // Java → DB string
   │       └─ valid():  cpn.valid(cpnValue, options) + 各 Validator.valid(cpnValue)
   │     失败 → bindingResult.addError(FieldError)  (不中断)
   │     成功 → values[name] = cpnValue;  FormCpnValue{formCpnId, instanceId, formId, configId, value=stringValue}
   │
   ├─ if bindingResult.hasErrors() → throw BindException
   │
   ├─ FormAdvice.beforeInstanceHandle(form, instanceId, values)   // 业务可改写 values
   │
   ├─ storageStrategy:
   │     INNER_TABLE : formCpnValueDAO.deleteByInstanceId(instanceId) → insertOrUpdate(list)
   │     CREATE_TABLE: FormAdvice.insertOrUpdate(values)==true ? 跳过 : EntityDAO(repositoryName).insertOrUpdate(values)
   │
   └─ FormAdvice.afterInstanceHandle(form, instanceId, values)
```

`GET /forms/page/{formId}/{instanceId}` 渲染路径相同地取 `FormBO`，再调 `FormAdvice.beforeRender(parameterMap, formBO)`，最后 Thymeleaf 模板按 `formBO.propertyList` 与 `p.configurer.cpnType` 分支渲染各控件。

## 对外依赖

| 依赖 | scope | 用途 |
|---|---|---|
| `spring-boot-starter-web` | provided | Controller、Thymeleaf（实际由宿主提供）、`@Validated` |
| `javax.validation` | provided | `@NotBlank/@NotNull/@NotEmpty` Bean Validation |
| `com.rick:sharp-database2` | compile | `@Table`/`@Column`/`EntityDAO`/`EntityCodeDAOImpl`/`BaseEntity`/`BaseCodeEntity`/`GridService`/`OperatorUtils` |
| `com.rick:sharp-meta` | compile | `DictService`/`Dict`/`DictValue`（控件选项数据源） |
| `com.rick:sharp-fileupload` | provided | 附件/图片控件的上传后端（`Attachment` 控件仅在前端模板用 `/documents/upload`，模块本身不强依赖） |
| `com.rick.common:*` (transitive) | — | `JsonUtils`/`Time2StringUtils`/`HttpServletRequestUtils`/`HtmlTagUtils`/`Result`/`ResultUtils`/`IdGenerator`/`Maps`/`ClassUtils`/`ReflectUtils` |

**无任何 BPMN/工作流引擎依赖**（无 Flowable/Camunda/Activiti）。

## 扩展点

| 扩展点 | 实现 | 注册方式 |
|---|---|---|
| 自定义控件类型 | 实现 `Cpn<T>`（一般继承 `AbstractCpn<T>`） | `@Component`，`CpnManager` 自动收集；需在 `CpnTypeEnum` 加枚举值 |
| 自定义校验器 | 继承 `AbstractValidator<T>` | `@Component`，`ValidatorManager` 自动收集（`REGEX` 例外，强制映射 `CustomizeRegex`） |
| 业务钩子 | 实现 `FormAdvice` | `@Component`，`Form.formAdviceName` 关联 beanName |
| 值转换器 | 实现 `CpnValueConverter<K,V>` | `@Component`，`CpnConfigurer.cpnValueConverterName` 关联 |
| 自定义存储 DAO | 宿主提供 `EntityDAO` Bean | `Form.repositoryName` 关联 beanName |
| 自定义渲染模板 | Thymeleaf 模板 | `Form.tplName` 指定；参考 `resources/templates/tpl/form.html` |
| Thymeleaf 标签 | `SharpFormProcessor`（在 sharp-admin） | `<sharp:form id=".."">` 标签渲染（不在本模块） |

## 配置与启动

- 自动配置：`META-INF/spring.factories` 与 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 都声明 `FormFlowServiceAutoConfiguration`。
- 启用条件：容器中存在唯一的 `GridService`（来自 sharp-database2），且 `SharpDatabaseAutoConfiguration` 已加载。
- 扫描包：`com.rick.formflow.form`（`@ComponentScan`），`config` 包由 `@Configuration` 显式声明。
- 默认 Bean：若宿主未提供 `FormAdvice`，自动配置提供一个空实现（`@ConditionalOnMissingBean`）。`DateTimeToStringConverter` 强制注册为 Bean（CpnValueConverter 候选）。
- 无 `application.properties`/`@ConfigurationProperties`。

## 数据库表

由 sharp-database2 的 `@Table` 注解驱动建表/同步（具体机制见 sharp-database2 文档）。

| 表 | 实体 | 关键列 | 用途 |
|---|---|---|---|
| `sys_form` | `Form` | `id, code, name, form_advice_name, table_name, repository_name, storage_strategy, tpl_name, additional_info` + `BaseCodeEntity` 公共列 | 表单定义 |
| `sys_form_configurer` | `CpnConfigurer` | `id, name, label, type, validators(varchar 512), options, data_source, default_value, placeholder, is_disabled, cpn_value_converter_name, additional_info` | 控件配置 |
| `sys_form_cpn_configurer` | `FormCpn` | `id, form_id, config_id, order_num, additional_info` | 表单↔控件关联（带顺序） |
| `sys_form_cpn_value` | `FormCpnValue` | `id, form_cpn_id, form_id, config_id, instance_id, value` | INNER_TABLE 策略下的实例值 |

`CREATE_TABLE` 策略下不写 `sys_form_cpn_value`，而是写到 `Form.tableName` 指定的业务表（通过 `Form.repositoryName` 指定的 `EntityDAO`）。业务表结构由具体业务模块定义，本模块不感知。

## 缓存与一致性

- `FormUtils.formCacheMap`：进程内 `HashMap<Long, FormCache>`，存 `Form`+`FormCpn`+`configIdMap`。`getFormBOById` 命中后 `SerializationUtils.clone` 使用，避免共享可变状态。
- 失效策略：**无自动失效**。表单结构（Form/FormCpn/CpnConfigurer）变更后，旧缓存仍命中——需重启进程或调 `FormUtils.update(id, newCache)` 覆盖（当前无失效 API，见 CLAUDE.md 陷阱）。
- 实例值（`FormCpnValue`/业务表）不缓存，每次 `getFormBOByIdAndInstanceId` 实时查 DB。
