package com.rick.admin.demo;

import com.google.common.collect.Lists;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.service.CpnConfigurerService;
import com.rick.formflow.form.service.FormCpnService;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.valid.CustomizeRegex;
import com.rick.formflow.form.valid.Length;
import com.rick.formflow.form.valid.Required;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.*;
import com.rick.report.core.service.ReportService;
import com.rick.report.core.support.ReportConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/5/28 15:07
 */
@SpringBootTest
public class FormTagTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private FormCpnService formCpnService;

    @Autowired
    private CpnConfigurerService cpnConfigurerService;

    @Autowired
    private FormService formService;

    @Test
    public void testForm() {
        // 设计控件
        List<CpnConfigurer> cpnConfigurerList = createCpnConfigurerList();
        cpnConfigurerService.saveOrUpdate(cpnConfigurerList);
        // 创建表
        Form form = formService.saveOrUpdate(Form.builder()
                .code("sys_user_form_tag")
                .tableName("sys_user")
                .tplName("demos/student/form-tag") // report 设置参数 .pv("formAction", "link")
                .additionalInfo(Params.builder(1)
                        .pv("label-col", 1)
                        .build()) // 跳转新的页面编辑，适合字段多的页面
                .name("用户信息")
                .formAdviceName("userFormAdvice")
                .repositoryName("userDAO")
                .storageStrategy(Form.StorageStrategyEnum.CREATE_TABLE)
                .build());
        // 关联关系
        formCpnService.saveOrUpdateByConfigIds(form.getId(), cpnConfigurerList.stream().map(CpnConfigurer::getId).collect(Collectors.toList()));
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
                .additionalInfo(Params.builder(1).pv("tab-index", "1").build())
                .build();

        CpnConfigurer nameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("name")
                .label("姓名")
                .placeholder("请输入姓名")
                .validatorList(textValidatorList)
                .additionalInfo(Params.builder(1).pv("tab-index", "1").build())
                .build();

        CpnConfigurer availableCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SWITCH)
                .name("available")
                .label("可用")
                .defaultValue("1")
                .additionalInfo(Params.builder(1).pv("tab-index", "2").build())
                .build();

        CpnConfigurer roleCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.CHECKBOX)
                .datasource("sys_role")
//                .options(Arrays.asList(new CpnConfigurer.CpnOption("694587732420202496", "管理员")))
                .name("roleIds")
                .label("角色")
                .additionalInfo(Params.builder(1).pv("tab-index", "2").build())
                .build();

        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(usernameCpn, nameCpn, availableCpn, roleCpn);
        return cpnConfigurerList;
    }

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .code("sys_user_form_tag")
                .tplName("tpl/list") // 如果由特殊需求比如显示字段的格式化，字段跳转，可以使用自己的模版
                .name("用户管理")
                .reportAdviceName("userReportAdvice")

                // report 和 form 进行关联，可以修改表单
                .additionalInfo(Params.builder(1)
                        .pv("formId", "859875429241106432")
                        .pv("formAction", "link")
                        .pv(ReportConstants.ADDITIONAL_OPERATOR_BAR, true)
                        .build())
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
                        new ReportColumn("birthday", "出生日期", true),
                        new ReportColumn("role_name", "角色"),
                        new ReportColumn("is_available", "是否可用").setColumnWidth(80).setAlign(AlignEnum.CENTER),
                        new ReportColumn("create_name", "创建人").setColumnWidth(100),
                        new ReportColumn("create_time", "创建时间").setColumnWidth(180).setAlign(AlignEnum.CENTER)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}
