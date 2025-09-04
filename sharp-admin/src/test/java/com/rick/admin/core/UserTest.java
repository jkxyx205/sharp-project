package com.rick.admin.core;

import com.google.common.collect.Lists;
import com.rick.common.util.Maps;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.valid.CustomizeRegex;
import com.rick.formflow.form.valid.Length;
import com.rick.formflow.form.valid.Required;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.*;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/5/28 15:07
 */
@SpringBootTest
public class UserTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private FormService formService;

    @Autowired
    private FormSupport formSupport;

    @Test
    public void testForm() {
        // 创建表
        Form form = formService.saveOrUpdate(Form.builder()
                .id(694980924206493696L)
                .code("sys_user")
                .tableName("sys_user")
//                // 1. 弹出框
//                .tplName("tpl/form/form-full") // 弹出框显示需要使用 tpl/form/form-full
//                .additionalInfo(Params.builder(1).pv("showSaveFormBtn", false).build())

                // 2. 弹出框 + tab
//                .tplName("tpl/form/form-tab-full") // 弹出框显示需要使用 tpl/form-tab-full
//                .additionalInfo(Params.builder(3)
//                        .pv("showSaveFormBtn", false)
//                        .pv("label-col", 2)
//                        .pv("tab-list", Arrays.asList("基本信息", "配置信息"))
//                        .build()) // 跳转新的页面编辑，适合字段多的页面

                // 3. link
                .tplName("tpl/form/form") // link使用form， 不能是full, report 设置参数 .pv("formAction", "link")
                .additionalInfo(Maps.of("label-col", 1)) // 跳转新的页面编辑，适合字段多的页面

                // 4. drawer
//                .tplName("tpl/form/form") // drawer使用form， report 设置参数 .pv("formAction", "drawer")
//                .additionalInfo(Params.builder(1)
//                        .pv("label-col", 1)
//                        .build()) // 跳转新的页面编辑，适合字段多的页面

                // 5. link + tab
//                .tplName("tpl/form/form-tab") // report 设置参数 .pv("formAction", "link")；弹出框显示需要使用 tpl/form-tab
//                 .additionalInfo(Params.builder(1)
//                        .pv("label-col", 2)
//                        .pv("tab-list", Arrays.asList("基本信息", "配置信息"))
//                        .build()) // 跳转新的页面编辑，适合字段多的页面

//                CpnConfigurer volumeCpn = CpnConfigurer.builder()
//                        .additionalInfo(Maps.of("tab-index", "2")) // 所属的tab index
//                        .build();
                .name("用户信息")
                .formAdviceName("userFormAdvice")
                .repositoryName("userDAO")
                .storageStrategy(Form.StorageStrategyEnum.CREATE_TABLE)
                .build());

        // 设计控件
        List<CpnConfigurer> cpnConfigurerList = createCpnConfigurerList();

        formSupport.bind(form.getId(), cpnConfigurerList);

        System.out.println("form id = " + form.getId());
    }

    private List<CpnConfigurer> createCpnConfigurerList() {
        // 文本校验器
        List<Validator> textValidatorList = Lists.newArrayListWithExpectedSize(2);
        textValidatorList.add(new Required(true));
        textValidatorList.add(new Length(16));

        CustomizeRegex codeRegex = new CustomizeRegex("^[0-9a-zA-Z_\\/%\\-]{1,}$", "CODE只能包含数字、字母、下划线、中划线");

        List<Validator> codeRegexValidatorList = Lists.newArrayList(textValidatorList);
        codeRegexValidatorList.add(codeRegex);

        CpnConfigurer usernameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("code")
                .label("用户名")
                .placeholder("请输入用户名")
                .validatorList(codeRegexValidatorList)
                .additionalInfo(Maps.of("tab-index", "1"))
                .build();

        CpnConfigurer nameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("name")
                .label("姓名")
                .placeholder("请输入姓名")
                .validatorList(textValidatorList)
                .additionalInfo(Maps.of("tab-index", "1"))
                .build();

        CpnConfigurer birthdayCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.DATE)
                .name("birthday")
                .label("出生日期")
                .placeholder("请输入出生日期")
                .additionalInfo(Maps.of("tab-index", "1"))
                .build();

        CpnConfigurer availableCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SWITCH)
                .name("available")
                .label("可用")
                .defaultValue("1")
                .additionalInfo(Maps.of("tab-index", "2"))
                .build();

        CpnConfigurer roleCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.CHECKBOX)
                .datasource("sys_role")
//                .options(Arrays.asList(new CpnConfigurer.CpnOption("694587732420202496", "管理员")))
                .name("roleIds")
                .label("角色")
                .additionalInfo(Maps.of("tab-index", "2"))
                .build();

        CpnConfigurer attachmentCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.FILE)
                .name("attachment")
                .label("附件")
                .additionalInfo(Maps.of("tab-index", "2"))
                .build();

        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(usernameCpn, nameCpn, birthdayCpn, availableCpn, roleCpn, attachmentCpn);
        return cpnConfigurerList;
    }

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(694714180413960192L)
                .code("sys_user")
                .tplName("tpl/list/list") // 如果由特殊需求比如显示字段的格式化，字段跳转，可以使用自己的模版
                .name("用户管理")
                .reportAdviceName("userReportAdvice")

                // additionalInfo(Params.builder(1).pv("endpoint", "produce_orders").build()) 可以由 report 跳转 endpoint
                // additionalInfo(Params.builder(1).pv("formId", "produce_orders").build()) 对表单进行修改
                // additionalInfo(Params.builder(1).pv("formAction", "link").build()) 对表跳转新的页面修改
                // additionalInfo(Params.builder(1).pv("css", "").build()) 注入 css 代码
                // additionalInfo(Params.builder(1).pv("js", "produce_orders").build()) 注入 js 代码
                // additionalInfo(Params.builder(1).pv("operator-bar", true).build()) list.html 是否显示操作按钮（新增/修改/删除）
                // additionalInfo(Params.builder(1).pv("js-operator-columnr", "{{ openDetailLink('复制', scope.row.id, '复制') }}").build()) row 除编辑和删除的其他操作

                // report 和 form 进行关联，可以修改表单
                .additionalInfo(Maps.of("formId", "694980924206493696", "formAction", "link"))
//                .querySql("SELECT id, code, name, IF(is_available, '是', '否') is_available, DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') create_time FROM sys_user WHERE code LIKE :code AND name LIKE :name AND is_available = :is_available AND create_time >= :create_time0 AND create_time <= :create_time1 AND id <> 1")
                // language=SQL
                .querySql(" SELECT sys_user.id, sys_user.code, sys_user.name, sys_user.birthday, IF(sys_user.is_available, '是', '否') is_available, t.name role_name, u.name create_name, DATE_FORMAT(sys_user.create_time, '%Y-%m-%d %H:%i:%s') create_time FROM sys_user\n" +
                        " LEFT JOIN sys_user u on u.id = sys_user.create_by\n" +
                        " LEFT JOIN ( SELECT sys_user.id, GROUP_CONCAT(r.name) name FROM sys_user\n" +
                        " LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = 0\n" +
                        " LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = 0\n" +
                        " group by sys_user.id order by sys_user.id asc) t on t.id = sys_user.id\n" +
                        "WHERE sys_user.code LIKE :code AND sys_user.name LIKE :name AND sys_user.is_available = :is_available AND sys_user.create_time >= :create_time0 AND sys_user.create_time <= :create_time1 AND sys_user.is_deleted = 0")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "用户名"),
                        new QueryField("name", "姓名"),
                        new QueryField("is_available", "是否可用", QueryField.Type.SELECT, "bol"),
                        new QueryField("create_time", "创建时间", QueryField.Type.DATE_RANGE)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "用户名", true),
                        new ReportColumn("name", "姓名", true),
                        new ReportColumn("birthday", "出生日期", true).setAlign(AlignEnum.CENTER),
                        new ReportColumn("role_name", "角色"),
                        new ReportColumn("is_available", "是否可用").setColumnWidth(80).setAlign(AlignEnum.CENTER),
                        new ReportColumn("create_name", "创建人").setColumnWidth(100),
                        new ReportColumn("create_time", "创建时间").setColumnWidth(120).setAlign(AlignEnum.CENTER)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }

    @Test
    public void testReportSearch() {
        reportService.saveOrUpdate(Report.builder()
                .id(786015805669142528L)
                .code("sys_user_search")
                .tplName("tpl/list/dialog_report_list")
                .name("用户查询")
                // language=SQL
                //  and id = :id and id IN (:ids) 回显需要的查询条件 (option.value)
                .querySql(" SELECT sys_user.id, sys_user.code, sys_user.name, IF(sys_user.is_available, '是', '否') is_available, t.name role_name, u.name create_name, DATE_FORMAT(sys_user.create_time, '%Y-%m-%d %H:%i:%s') create_time FROM sys_user\n" +
                        " LEFT JOIN sys_user u on u.id = sys_user.create_by\n" +
                        " LEFT JOIN ( SELECT sys_user.id, GROUP_CONCAT(r.name) name FROM sys_user\n" +
                        " LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = 0\n" +
                        " LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = 0\n" +
                        " group by sys_user.id order by sys_user.id asc) t on t.id = sys_user.id\n" +
                        "WHERE sys_user.code LIKE :code AND sys_user.name LIKE :name AND sys_user.is_available = :is_available AND sys_user.create_time >= :create_time0 AND sys_user.create_time <= :create_time1 AND sys_user.is_deleted = 0 and sys_user.id = :id and sys_user.id IN (:ids)")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "用户名"),
                        new QueryField("name", "姓名"),
                        new QueryField("is_available", "是否可用", QueryField.Type.SELECT, "bol"),
                        new QueryField("create_time", "创建时间", QueryField.Type.DATE_RANGE)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "用户名", true),
                        new ReportColumn("name", "姓名", true),
                        new ReportColumn("role_name", "角色"),
                        new ReportColumn("is_available", "是否可用").setColumnWidth(80).setAlign(AlignEnum.CENTER),
                        new ReportColumn("create_name", "创建人").setColumnWidth(100),
                        new ReportColumn("create_time", "创建时间").setColumnWidth(120).setAlign(AlignEnum.CENTER)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }


}
