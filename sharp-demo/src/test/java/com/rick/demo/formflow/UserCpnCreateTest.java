package com.rick.demo.formflow;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.service.CpnConfigurerService;
import com.rick.formflow.form.service.FormCpnService;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.valid.Length;
import com.rick.formflow.form.valid.Required;
import com.rick.formflow.form.valid.Size;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.QueryField;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.SordEnum;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-10-13 15:27:00
 */
@SpringBootTest
public class UserCpnCreateTest {

    @Autowired
    private CpnConfigurerService cpnConfigurerService;

    @Autowired
    private FormCpnService formCpnService;

    @Autowired
    private FormService formService;

    @Autowired
    private ReportService reportService;

    @AfterAll
    public static void init() {
//        SQLUtils.execute("TRUNCATE TABLE t_user");
    }

//    @Test
    public void testAddConfigurerToForm() {
        // 设计控件
        List<CpnConfigurer> cpnConfigurerList = createCpnConfigurerList();
        cpnConfigurerService.saveOrUpdate(cpnConfigurerList);

        // 创建表
        Form form = formService.saveOrUpdate(Form.builder()
//                .id(670604125091708928L)
                .code("t_user")
                .tableName("t_user")
                .name("人员信息表")
                .repositoryName("userDAO")
                .formAdviceName("userFormAdvice")
                .storageStrategy(Form.StorageStrategyEnum.CREATE_TABLE)
                .build());

        // 关联关系
        formCpnService.saveOrUpdateByConfigIds(form.getId(), cpnConfigurerList.stream().map(CpnConfigurer::getId).collect(Collectors.toList()));
    }

    @Test
    public void testAddReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(668074769748373504L)
                .name("人员信息表")
                .querySql("SELECT t_user.name              AS \"name\",\n" +
                        "#        t_user.gender            AS \"gender\",\n" +
                        "       sd.label AS \"gender\",\n" +
                        "       t_user.age               AS \"age\",\n" +
                        "       t_user.birthday          AS \"birthday\",\n" +
                        "       t_user.mobile            AS \"mobile\",\n" +
                        "       t_user.score            AS \"score\",\n" +
                        "       t_user.email             AS \"email\",\n" +
                        "       t_user.native_place      AS \"nativePlace\",\n" +
                        "       t_user.hobby             AS \"hobbyList\",\n" +
                        "       IF(t_user.marriage, '是', '否')         AS \"marriage\",\n" +
                        "       t_user.introduce         AS \"introduce\",\n" +
                        "       t_user.attachment        AS \"attachmentList\",\n" +
                        "       t_user.school_experience AS \"schoolExperienceList\",\n" +
                        "       t_user.status            AS \"status\",\n" +
                        "       t_user.created_by        AS \"createdBy\",\n" +
                        "       t_user.created_at        AS \"createdAt\",\n" +
                        "       t_user.updated_by        AS \"updatedBy\",\n" +
                        "       t_user.updated_at        AS \"updatedAt\",\n" +
                        "       t_user.is_deleted        AS \"deleted\",\n" +
                        "       t_user.id                AS \"id\"\n" +
                        "FROM t_user left JOIN sys_dict sd ON t_user.gender = sd.name" +
                        " WHERE is_deleted = 0 AND t_user.name like :name AND t_user.gender = :gender AND t_user.mobile = :mobile " +
                        "AND t_user.native_place IN (:nativePlace) AND t_user.status = :status AND t_user.hobby like :hobbyList" +
                                " AND t_user.birthday >= :birthday0 AND t_user.birthday <= :birthday1 AND marriage = :marriage")
                .reportColumnList(Arrays.asList(
                        new ReportColumn("name", "姓名", true),
                        new ReportColumn("gender", "性别"),
                        new ReportColumn("age", "年龄"),
                        new ReportColumn("birthday", "出生日期",true, null, Arrays.asList("localDateConverter")),
                        new ReportColumn("mobile", "手机号码"),
                        new ReportColumn("score", "得分"),
                        new ReportColumn("email", "邮箱"),
                        new ReportColumn("nativePlace", "籍贯"),
                        new ReportColumn("hobbyList", "兴趣爱好", false, "hobbyList", Arrays.asList("arrayDictConverter")),
                        new ReportColumn("marriage", "婚否"),
                        new ReportColumn("introduce", "自我介绍"),
//                        new ReportColumn("attachmentList", "附件"),
//                        new ReportColumn("schoolExperienceList", "学习经历"),
                        new ReportColumn("status", "状态", false, "status", Arrays.asList("dictConverter"))
                ))
                .queryFieldList(Arrays.asList(
                        new QueryField("name", "姓名"),
                        new QueryField("mobile", "手机号码"),
                        // sys_dic#gender
                        new QueryField("gender", "性别", QueryField.Type.SELECT, "gender"),
                        new QueryField("birthday", "出生日期", QueryField.Type.DATE_RANGE),
                        // 配置文件
                        new QueryField("nativePlace", "籍贯", QueryField.Type.MULTIPLE_SELECT, "nativePlace"),
                        // 接口实现
                        new QueryField("hobbyList", "兴趣爱好", QueryField.Type.SELECT, "hobbyList"),
                        // 配置文件
                        new QueryField("status", "状态", QueryField.Type.SELECT, "status"),
                        new QueryField("marriage", "婚否", QueryField.Type.SELECT, "bol")
                ))
                .pageable(true)
                .summaryColumnNames("age, score")
                .sidx("name")
                .sord(SordEnum.ASC)
                .build());
    }

    public List<CpnConfigurer> createCpnConfigurerList() {
        // 文本校验器
        Set<Validator> textValidatorList = Sets.newHashSetWithExpectedSize(2);
        textValidatorList.add(new Required(true));
        textValidatorList.add(new Length(16));

        Set<Validator> requiredValidatorList = Sets.newHashSetWithExpectedSize(1);
        requiredValidatorList.add(new Required(true));

        CpnConfigurer nameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("name")
                .label("姓名")
                .placeholder("请输入姓名")
                .defaultValue("Rick.Xu")
                .validatorList(textValidatorList)
                .build();

        CpnConfigurer genderCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.RADIO)
                .name("gender")
                .label("性别")
                .options(Arrays.asList(new CpnConfigurer.CpnOption("0", "未知"), new CpnConfigurer.CpnOption("1", "男"), new CpnConfigurer.CpnOption("2", "女")))
                .defaultValue("1")
                .validatorList(requiredValidatorList)
                .build();

        Set<Validator> ageValidatorList = Sets.newHashSetWithExpectedSize(1);
        ageValidatorList.add(new Size(18, 100));
        ageValidatorList.add(new Required(true));
        CpnConfigurer ageCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.INTEGER_NUMBER)
                .name("age")
                .label("年龄")
                .placeholder("请输入年龄")
                .defaultValue("28")
                .validatorList(ageValidatorList)
                .build();

        Set<Validator> scoreValidatorList = Sets.newHashSetWithExpectedSize(1);
        scoreValidatorList.add(new Size(0, 100));
        CpnConfigurer scoreCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.NUMBER_TEXT)
                .name("score")
                .label("成绩")
                .placeholder("请输入成绩")
                .defaultValue("99.5")
                .validatorList(scoreValidatorList)
                .build();

        CpnConfigurer birthdayCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.DATE)
                .name("birthday")
                .label("出生时间")
                .placeholder("请输入出生时间")
                .defaultValue("1989-12-11")
                .build();

        CpnConfigurer mobileCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.MOBILE)
                .name("mobile")
                .label("手机号码")
                .placeholder("请输入手机号码")
                .defaultValue("18898765543")
                .validatorList(requiredValidatorList)
                .build();

        CpnConfigurer emailCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.EMAIL)
                .name("email")
                .label("邮箱")
                .placeholder("请输入邮箱")
                .defaultValue("jkxyx@163.com")
                .build();

        CpnConfigurer nativePlaceCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("native_place")
                .label("籍贯")
                .options(Arrays.asList(new CpnConfigurer.CpnOption("陕西"), new CpnConfigurer.CpnOption("江苏"), new CpnConfigurer.CpnOption("四川"), new CpnConfigurer.CpnOption("上海")))
                .defaultValue("江苏")
                .validatorList(requiredValidatorList)
                .build();

        CpnConfigurer hobbyCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.CHECKBOX)
                .name("hobby")
                .label("兴趣爱好")
                .options(Arrays.asList(new CpnConfigurer.CpnOption("BASKETBALL", "篮球"), new CpnConfigurer.CpnOption("FOOTBALL", "足球"), new CpnConfigurer.CpnOption("BADMINTON", "羽毛球"), new CpnConfigurer.CpnOption("VOLLEYBALL", "排球")))
                .defaultValue("[\"BASKETBALL\", \"FOOTBALL\"]")
                .validatorList(requiredValidatorList)
                .build();

        CpnConfigurer marriageCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.CHECKBOX)
                .name("marriage")
                .label("婚否")
//                .options(Arrays.asList(new CpnConfigurer.CpnOption("true", "")) )
                .options(Arrays.asList(new CpnConfigurer.CpnOption("1", "")))
                .defaultValue("true")
                .build();

        CpnConfigurer introduceCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXTAREA)
                .name("introduce")
                .label("自我介绍")
                .placeholder("请输入自我介绍")
                .defaultValue("我叫Rick...")
                .validatorList(textValidatorList)
                .build();

        CpnConfigurer attachmentCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.FILE)
                .name("attachment")
                .label("附件")
                .validatorList(requiredValidatorList)
                .build();

        CpnConfigurer yearCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("school_year")
                .label("年份")
                .defaultValue("Rick.Xu")
                .validatorList(textValidatorList)
                .build();

        CpnConfigurer schoolNameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("school_name")
                .label("学校名称")
                .defaultValue("Rick.Xu")
                .validatorList(textValidatorList)
                .build();

        CpnConfigurer schoolExperienceCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TABLE)
                .name("school_experience")
                .label("学习经历")
                .additionalInfo(Params.builder()
                        .pv("columns", Arrays.asList(yearCpn, schoolNameCpn))
                        .build())
                .defaultValue("[[\"2021-2022\", \"四川大学\"], [\"2012-2015\", \"永安高中\"]]")
                .build();

        CpnConfigurer statusCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("status")
                .label("用户状态")
                .options(Arrays.asList(new CpnConfigurer.CpnOption("LOCKED", "锁定"), new CpnConfigurer.CpnOption("NORMAL", "正常")))
                .defaultValue("LOCKED")
                .build();

        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(nameCpn, genderCpn, ageCpn, scoreCpn, birthdayCpn,
                mobileCpn, emailCpn, nativePlaceCpn, hobbyCpn, marriageCpn, introduceCpn, attachmentCpn, schoolExperienceCpn, statusCpn);
        return cpnConfigurerList;
    }

}
