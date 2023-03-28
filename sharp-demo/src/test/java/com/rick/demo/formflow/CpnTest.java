package com.rick.demo.formflow;

import com.google.common.collect.Lists;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.service.CpnConfigurerService;
import com.rick.formflow.form.valid.Length;
import com.rick.formflow.form.valid.Required;
import com.rick.formflow.form.valid.Size;
import com.rick.formflow.form.valid.core.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-10-13 15:27:00
 */
@SpringBootTest
public class CpnTest {

    @Autowired
    private CpnConfigurerService cpnConfigurerService;

    /**
     * 添加字段
     */
    @Test
    public void testAddCpnConfigurer() {
        cpnConfigurerService.saveOrUpdate(createCpnConfigurerList());
    }

    public List<CpnConfigurer> createCpnConfigurerList() {
        // 文本
        List<Validator> textValidatorList = Lists.newArrayListWithExpectedSize(2);
        textValidatorList.add(new Required(true));
        textValidatorList.add(new Length(16));

        CpnConfigurer cpnConfigurerText = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("name")
                .label("姓名")
                .placeholder("请输入姓名")
                .defaultValue("Rick")
                .validatorList(textValidatorList)
                .build();

        CpnConfigurer cpnConfigurerRadio = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.RADIO)
                .name("sex")
                .label("性别")
                .placeholder("请输入性别")
                .options(Arrays.asList(new CpnConfigurer.CpnOption("男"), new CpnConfigurer.CpnOption("女"), new CpnConfigurer.CpnOption("保密")))
                .defaultValue("保密")
                .validatorList(textValidatorList)
                .build();

        // 数字
        List<Validator> numberValidatorList = Lists.newArrayListWithExpectedSize(1);
        numberValidatorList.add(new Size(18, 100));
        CpnConfigurer cpnConfigurerNumber = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.NUMBER_TEXT)
                .name("age")
                .label("年龄")
                .placeholder("请输入年龄")
                .defaultValue("18")
                .validatorList(numberValidatorList)
                .build();

        // 单选
        List<Validator> selectValidatorList = Lists.newArrayListWithExpectedSize(1);
        selectValidatorList.add(new Required(true));
        CpnConfigurer cpnConfigurerSelect = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("native_place")
                .label("籍贯(单选)")
                .placeholder("请输入兴趣爱好")
                .options(Arrays.asList(new CpnConfigurer.CpnOption("陕西"), new CpnConfigurer.CpnOption("江苏"), new CpnConfigurer.CpnOption("四川"), new CpnConfigurer.CpnOption("上海")))
                .defaultValue("江苏")
                .validatorList(selectValidatorList)
                .build();

        // 多选
        List<Validator> checkboxValidatorList = Lists.newArrayListWithExpectedSize(1);
        selectValidatorList.add(new Required(true));
        CpnConfigurer cpnConfigurerCheckbox = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.CHECKBOX)
                .name("hobby")
                .label("兴趣爱好(多选)")
                .placeholder("请输入兴趣爱好")
                .options(Arrays.asList(new CpnConfigurer.CpnOption("足球"), new CpnConfigurer.CpnOption("篮球"), new CpnConfigurer.CpnOption("乒乓球"), new CpnConfigurer.CpnOption("羽毛球"))                 )
                .defaultValue("[\"足球\", \"篮球\"]")
                .validatorList(checkboxValidatorList)
                .build();

        // 表格
        List<Validator> tableValidatorList = Lists.newArrayListWithExpectedSize(1);
        numberValidatorList.add(new Required(true));
        CpnConfigurer cpnConfigurerTable = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TABLE)
                .name("info")
                .label("信息收集")
                .placeholder("请输入信息收集")
                .additionalInfo(Params.builder()
                          .pv("columns", Arrays.asList(cpnConfigurerText, cpnConfigurerNumber))
                        .build())
                .validatorList(tableValidatorList)
                .build();

        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(cpnConfigurerText, cpnConfigurerNumber, cpnConfigurerSelect, cpnConfigurerTable, cpnConfigurerCheckbox, cpnConfigurerRadio);
        return cpnConfigurerList;
    }

}
