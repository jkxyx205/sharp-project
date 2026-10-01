# sharp-formflow 模块说明（给 Claude / 维护者）

## 模块边界

- 本模块是**配置驱动的表单引擎**：表单定义 + 控件配置 + 校验 + 存储。**不是工作流/审批引擎**——尽管目录名为 `formflow`，里面没有任何流程节点、审批、状态机、BPMN。命名中的 "flow" 指表单数据流转（定义→渲染→提交→存储）。
- 不含持久层实现，复用 `sharp-database2`（`@Table`/`EntityDAO`/`BaseEntity`）与 `sharp-meta`（`DictService`）。
- 不含前端框架，只提供一份 Bootstrap 5 + jQuery 的 Thymeleaf 模板样例（`resources/templates/tpl/form.html`）和少量 JS（`jquery.form2json.js`、`editable-table`、`ajaxfileupload.js`、`bootstrap-datepicker`）。宿主（如 sharp-admin）通常自带 Thymeleaf dialect 与自定义模板，覆盖本模块的默认模板。

## 目录约定

- 源码根包：`com.rick.formflow`。所有业务类在 `com.rick.formflow.form` 子包。
- `config/`：仅自动配置类。
- `form/cpn/core/`：抽象与实体（`Cpn`/`AbstractCpn`/`CpnManager`/`CpnConfigurer`/`Form`/`FormCpn`/`FormCpnValue`/`CpnTypeEnum`/`CpnInstanceProcessor`）。
- `form/cpn/`：20 种内置控件实现，每个一个文件一个 `@Component`，类名 = 控件英文名。
- `form/valid/core/`：校验器抽象与注册中心。`form/valid/`：内置校验器实现。
- `form/dao/`：4 个 DAO，均继承 sharp-database2 的 `EntityDAOImpl`/`EntityCodeDAOImpl`。
- `form/service/`：Service + SPI 接口（`FormAdvice`/`CpnValueConverter`）+ 工具（`FormUtils`/`FormConstants`）+ BO（`bo.FormBO`）+ 模型（`model.FormCache`）+ 转换器（`convert.DateTimeToStringConverter`）。
- `form/controller/`：4 个 Controller。`instance/` 子包是实例的页面/Ajax 入口。
- `resources/templates/`：Thymeleaf 模板。`resources/static/`：JS/CSS 静态资源。

## 编码约定

- 实体用 sharp-database2 的 `@Table`/`@Column`/`@Transient`，基类 `BaseEntity<Long>` 或 `BaseCodeEntity<Long>`（带 `code`）。Lombok `@Getter/@Setter/@NoArgsConstructor/@AllArgsConstructor/@SuperBuilder`。
- 控件实现统一继承 `AbstractCpn<T>`，泛型 `T` 即控件值类型（如 `Text extends AbstractCpn<String>`、`CheckBox extends AbstractCpn<List<String>>`、`Table extends AbstractCpn<List<List>>`、`Attachment extends AbstractCpn<List<Map<String,Object>>>`）。
- 校验器统一继承 `AbstractValidator<T>`，重写 `equals/hashCode` 为按 class 比较（用于去重）。
- SPI 扩展点用 Spring `@Component` 自动注册到 `CpnManager`/`ValidatorManager`，宿主侧无需额外配置。
- `Form` 用 `formAdviceName`（字符串 beanName）而非类型关联 `FormAdvice`，便于不同表单挂不同业务实现。
- 模板按 `CpnTypeEnum` 分支渲染（`th:if="${p.configurer.cpnType == T(...).TEXT}"`），新增控件类型必须同步改模板。

## 常见改动清单

### 加一种字段控件
1. 在 `CpnTypeEnum` 加枚举值（保持 `@JsonValue` 序列化为枚举名）。
2. 在 `form/cpn/` 新建类 `Xxx extends AbstractCpn<T>`，标 `@Component`，实现 `getCpnType()` 返回新枚举。
3. 如需特殊类型转换/选项校验，重写 `parseValue`/`httpConverter`/`valid`/`check`。
4. 如该控件自带校验器（如 `Date` 自带 `DateRegex`），重写 `cpnValidators()` 返回 `Set<Validator>`，并在 `ValidatorTypeEnum` 加对应枚举（若新校验器）。
5. 如需限制可配的校验器，重写 `internalValidatorSupports()`。
6. 改 `resources/templates/tpl/form.html`（及宿主自定义模板）增加对应 `CpnTypeEnum` 分支。
7. 若控件值需特殊存储格式，确认 `getStringValue` 输出符合 DB 列约束。

### 加一个流程节点 / 审批
**不支持**。本模块无流程引擎。如需审批流，需在宿主层另行引入 Flowable/Camunda 或自研，并通过 `FormAdvice.beforeInstanceHandle`/`afterInstanceHandle` 与本模块对接。

### 加一个校验器
1. 在 `ValidatorTypeEnum` 加枚举值。
2. 在 `form/valid/` 新建类 `XxxValidator extends AbstractValidator<T>`，标 `@Component`，实现 `valid`/`getValidatorType`/`getMessage`。
3. `ValidatorManager` 启动时自动收集。注意 `REGEX` 被强制映射到 `CustomizeRegex`，自定义 REGEX 校验器不会被 `getValidatorClassByType(REGEX)` 返回——若需自定义正则，用 `new CustomizeRegex(regex, msg)` 实例配到 `CpnConfigurer.validatorList`，而非注册 Bean。
4. 控件侧：若某控件应支持该校验器，重写其 `internalValidatorSupports()` 加入对应枚举。

### 加一个表单业务钩子
1. 在宿主模块实现 `FormAdvice`，标 `@Component`，beanName 即 `Form.formAdviceName` 的取值。
2. 按需实现 `beforeInstanceHandle`/`afterInstanceHandle`/`beforeRender`/`afterGetInstance`/`beforeDeleteInstance`/`afterDeleteInstance`/`insertOrUpdate`。
3. 在 `Form` 记录中设 `formAdviceName` = beanName。

### 加一种存储策略
当前仅 `NONE`/`INNER_TABLE`/`CREATE_TABLE`。新增需改 `FormService.handle` 与 `getFormBOByIdAndInstanceId` 的 `storageStrategy` 分支、`delete` 的分支，并可能新增 DAO。改动面较大，优先评估能否复用 `CREATE_TABLE + FormAdvice.insertOrUpdate`。

## 构建测试命令

```bash
# 在项目根目录
mvn -pl sharp-formflow -am clean install -DskipTests
# 或仅编译
mvn -pl sharp-formflow compile

# 跑该模块关联的集成测试（在 sharp-admin 中，FormTagTest/FormCpnRenderTest/FormSupport）
mvn -pl sharp-formflow,sharp-admin -am test -Dtest=FormTagTest,FormCpnRenderTest
```

模块自身无 `src/test`。测试都在宿主 `sharp-admin/src/test`，依赖宿主 Spring 上下文 + 数据库。

## 陷阱

1. **`FormUtils.formCacheMap` 无自动失效**：表单结构（`Form`/`FormCpn`/`CpnConfigurer`）变更后必须重启进程或手动调 `FormUtils.update(id, newCache)` 覆盖，否则 `getFormBO` 仍返回旧结构。开发期频繁改表单时尤其注意。缓存用 `SerializationUtils.clone` 避免可变共享，但 `Form`/`CpnConfigurer` 必须可序列化（`FormCache implements Serializable`）。
2. **`instanceId` 来源优先级**：`FormService.post(formId, instanceId, values)` 中，若 `values` 含非空 `id`，会用 `Long.parseLong(values.id)` **覆盖**传入的 `instanceId`。前端隐藏域 `id` 必须正确（新增时为空，编辑时为实例 ID）。
3. **`INNER_TABLE` 提交是"删后全量插"**：`handle` 中 `formCpnValueDAO.deleteByInstanceId(instanceId)` → `insertOrUpdate(formCpnValueList)`。并发提交同一 `instanceId` 会丢失数据（无版本号/锁）。`CREATE_TABLE` 走 `EntityDAO.insertOrUpdate(values)`，并发由底层乐观锁决定。
4. **校验器去重靠 class**：`AbstractValidator.equals` 按 class 比较，`CpnConfigurer.getValidatorList()` 会先加 `cpnValidators()` 再加 DB 配置的；若同一类型校验器配两次，`Set<Map>` 在 DB 层去重但 `List` 不去重，可能重复 `valid`。配置时避免同类型重复。
5. **`REGEX` 校验器映射写死**：`ValidatorManager.afterPropertiesSet` 末尾 `validatorMap.put(REGEX, CustomizeRegex.class)` 覆盖任何 `@Component` 注册的 REGEX 类。自定义正则校验器请用 `CustomizeRegex` 实例，不要注册 Bean 期望覆盖。
6. **`Cpn.valid` 默认选项校验对多选/单选生效，但对 `Table`/`Attachment` 不生效**：`AbstractCpn.valid` 只在 `value instanceof String` 且 options 非空时校验。`Table`/`CheckBox`/`Attachment` 各自重写了 `valid`/`httpConverter`，行为不同，改动时务必看具体类。
7. **`datasource` 选项装载发生在 DAO 层**：`CpnConfigurerDAO.select` 重写会对每条记录调 `dictService.getDictByType(datasource)` 填充 `options`。若 `DictService` 未就绪或字典 type 不存在，会抛异常或返回空，影响所有 `select`/`selectByIds` 调用（包括 `getFormBO`）。
8. **`CpnConfigurer.SINGLE_CHECKBOX` 枚举有值无实现**：`CpnTypeEnum.SINGLE_CHECKBOX` 存在，但 `form/cpn/` 下无对应 `@Component`，`CpnManager.getCpnByType(SINGLE_CHECKBOX)` 会返回 `null`，提交时 `CpnInstanceProcessor` 构造抛 NPE。使用此类型前需补实现或避免使用。
9. **模板路径约定**：`Form.tplName` 缺省 `tpl/form/form`（提交失败回退为 `form`）。宿主 `sharp-admin` 通常用自己的模板（如 `demos/student/form-tag`）并可能用 `<sharp:form>` 标签（`SharpFormProcessor`）渲染。改默认模板会影响所有未指定 `tplName` 的表单。
10. **历史数据迁移**：`Form`/`CpnConfigurer` 用 `BaseEntity`/`BaseCodeEntity`，含 `create_time`/`update_time`/`create_by`/`is_deleted` 等公共列。改存储策略（如 `INNER_TABLE`→`CREATE_TABLE`）不会迁移 `sys_form_cpn_value` 中的历史数据，需自行写迁移脚本。
11. **`FormController` 中 `FormService` 字段名大写**：`private final FormService FormService;`（首字母大写），调用 `FormService.saveOrUpdate(...)`。这是既存代码风格，改字段名会影响该类内所有引用。
