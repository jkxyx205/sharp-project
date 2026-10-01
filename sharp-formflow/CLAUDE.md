# sharp-formflow

> 动态表单引擎。**非工作流/审批引擎**——"flow" 指表单数据流转链路，无 Flowable/Camunda。详见 `API.md` / `ARCHITECTURE.md`。

## 模块边界

- 输入：表单元数据（Form）+ 字段控件配置（CpnConfigurer）+ 实例数据（Map）。
- 输出：校验后的实例落库 + 可回填的 FormBO + Thymeleaf 表单页面。
- 不负责：用户认证、权限、业务逻辑（交由宿主 + FormAdvice）、文件上传存储（交由 sharp-fileupload，本模块只存元信息 JSON）、字典维护（交由 sharp-meta）。

## 目录约定

```
src/main/java/com/rick/formflow/
├── config/        自动装配
├── form/
│   ├── cpn/core/  接口+实体+管理器（核心抽象，不轻易动）
│   ├── cpn/*.java 具体控件（一文件一控件，文件名=类型名）
│   ├── valid/core/ 验证器接口+管理器
│   ├── valid/*.java 具体验证器（一文件一验证器）
│   ├── controller/ REST/MVC 入口
│   ├── dao/        EntityDAO（一行继承，无自定义逻辑）
│   └── service/    业务服务+扩展点接口
src/main/resources/
├── META-INF/spring.factories   自动装配注册
├── templates/tpl/form.html     默认表单模板（Thymeleaf）
├── static/                     前端辅助 JS（文件上传/可编辑表格）
└── static/plugins/             datepicker
sql/form.sql   示例 DDL + 演示数据
```

约定：实体类带 `@Table`；DAO 一行继承 `EntityDAOImpl`/`EntityCodeDAOImpl`，无逻辑的不要加方法；控件类 `@Component` 且 `getCpnType()` 必须返回对应枚举；验证器类同。

## 编码约定

- 控件泛型 `T` 即业务值类型，DB 存储统一为 String（`getStringValue` 复杂对象走 `JsonUtils.toJson`）。
- 控件分两类验证：`cpnValidators()`（自带、不可关闭，如 MobileRegex）vs `internalValidatorSupports()`（声明可由用户在 configurer.validators 中配置的类型）。
- `AbstractCpn.parseValue` 默认实现按反射拿到的 `cpnClass` 做 String/Integer/BigDecimal/JSON 转换，能复用就别重写。
- Validator 必须 `@Component`（CustomizeRegex 例外，通过 validators JSON 构造）。`AbstractValidator.equals/hashCode` 按类去重，同类多实例判等。
- Controller 返回统一 `Result`（`ResultUtils.success`），异常靠全局处理。
- 实体继承体系：`Form extends BaseCodeEntity<Long>`（带 code）；其余 `CpnConfigurer`/`FormCpn`/`FormCpnValue extends BaseEntityWithLongId`。

## 常见改动清单

### 加一种字段控件
1. `CpnTypeEnum` 加枚举值（`@JsonValue` 自动生效）。
2. `cpn/` 下新建 `XxxCpn extends AbstractCpn<T>`，`@Component`，实现 `getCpnType()` 返回新枚举。
3. 按需重写 `httpConverter`（HTTP→T）、`parseValue`（DB→T）、`getStringValue`、`valid`、`cpnValidators`、`internalValidatorSupports`。
4. 若需要 Thymeleaf 渲染，在默认模板 `templates/tpl/form.html`（或宿主模板）加 `th:if` 分支。纯 AJAX 可跳过。
5. 无需注册——`CpnManager` 启动收集所有 `Cpn` Bean。

### 加一种校验器
1. `ValidatorTypeEnum` 加枚举值。
2. `valid/` 下新建 `XxxValidator extends AbstractValidator<T>`，`@Component`，实现 `valid`/`getValidatorType`/`getMessage`。
3. `ValidatorManager` 自动收集。无需改 `CpnConfigurer`，`validators` JSON 填 `{validatorType:"XXX", ...}` 即可。
4. 在目标控件的 `internalValidatorSupports()` 中声明支持该类型（否则 `hasValidator` 返回 false，提交时被跳过）。

### 加一个 FormAdvice
1. 业务模块（如 sharp-admin）实现 `FormAdvice`，`@Component`，类名即 Bean 名。
2. `sys_form.form_advice_name` 填 Bean 名。
3. 选择性覆盖回调：`beforeInstanceHandle`（改写提交值）、`insertOrUpdate`（接管落库）、`afterInstanceHandle`（后置副作用）、`beforeRender`（改 FormBO）。

### 改默认表单模板
`Form.tplName` 指定模板名（不含扩展名）；默认 `tpl/form/form`。宿主可注册自己的模板覆盖。`SharpFormProcessor` 的 `form-page` 属性可指定片段模板。

## 构建测试命令

```bash
# 模块根目录
cd sharp-formflow
mvn clean install -DskipTests        # 编译打包（依赖 sharp-database/sharp-meta 先装）
mvn compile                          # 仅编译
mvn test                             # 本模块无测试，需依赖 sharp-admin 的集成测试

# 全量（在项目根目录）
./mvn.sh clean install -DskipTests
```

测试入口在 `sharp-admin/src/test`（`FormTagTest`、`FormCpnRenderTest`、`UserTest` 等），通过 `FormSupport` 调用 FormService。

## 陷阱

1. **FormCache 进程级缓存无失效**：`FormUtils.formCacheMap` 是静态 HashMap，**修改 Form/CpnConfigurer/FormCpn 后不会自动失效**。开发期改 DB 后需重启或手动清缓存。生产无 TTL/无失效广播，集群环境多实例间缓存不一致（待确认是否有上层清理机制）。

2. **CpnTypeEnum 单例校验**：`CpnManager.setCpnList` 构造时若 `cpnMap != null` 抛 `IllegalArgumentException("cpnMap has been init")`——防止重复初始化，但意味着 Cpn Bean 不能热加载。

3. **未实现的控件类型 NPE**：`CpnTypeEnum.SINGLE_CHECKBOX` 等枚举若对应 `cpn/` 下无 `@Component` 实现，`CpnManager.getCpnByType` 返回 **null**，`CpnInstanceProcessor` 构造时 `cpn.httpConverter` 直接 NPE。自定义类型必须配套实现类。

4. **CustomizeRegex 非 @Component**：REGEX 类型验证器不走 `ValidatorManager` 自动收集，在 `afterPropertiesSet` 中手动 `put(REGEX, CustomizeRegex.class)`。配置 `validators` JSON 时必须用 `validatorType:"REGEX"` + `regex` + `message` 字段。

5. **validators JSON 必含 validatorType**：`CpnConfigurer.getValidatorList()` 用 `validatorInfo.get("validatorType")` 反查 class，缺失会 NPE。

6. **post 的 instanceId 优先级**：`values` 含非空 `id`（String）时**覆盖**传入的 instanceId。INNER_TABLE 新建且无 id 时自动生成雪花 id 写回 values。CREATE_TABLE 依赖 `values.id` 落库，漏传会 NPE/空指针。

7. **校验失败的 NPE**：`AjaxInstanceController` 不捕获 `BindException`，需宿主全局异常处理返回前端友好信息。`PageInstanceController` 捕获后回填重渲染。

8. **CheckBox.parseValue 多态**：Boolean/Enum/DictValue/Collection 多分支，Boolean 会产出 `["true","1"]` 双值——业务方读值需注意。DictValue 需 sharp-meta 的 `DictValue` 类型。

9. **TABLE 控件 additionalInfo**：列定义在 `configurer.additionalInfo.columns`（模板用 `c.validatorProperties`），与 `labels` 历史字段并存，待确认哪个为准（模板当前读 `columns`）。

10. **datasource 覆盖 options**：`CpnConfigurer.datasource` 非空时 `getFormBO` 实时用 DictService 覆盖 options——DB 里配的静态 options 会被忽略。改字典即时生效（因为每次 getFormBO 都查）。

11. **FormCache 可序列化要求**：`SerializationUtils.clone` 深拷贝，Form/FormCpn/CpnConfigurer 必须可序列化（已继承 Serializable，但新增非序列化字段会炸）。

## 外部集成（供参考）

- **sharp-admin**：通过 `SharpFormProcessor`（Thymeleaf 方言）暴露 `<sharp:form>` 标签；`UserFormAdvice`/`DictFormService` 实现 FormAdvice；`PageInstanceLayoutController` 扩展页面渲染支持 layout。
- **sharp-fileupload**：附件/图片控件的文件实际存储后端。
- **sharp-meta**：DictService 提供动态选项。
