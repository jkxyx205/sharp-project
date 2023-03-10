package com.rick.demo.formflow;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import java.util.Set;

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
        Set<Validator> textValidatorList = Sets.newHashSetWithExpectedSize(2);
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
                .options(new String[] {"男", "女","保密"})
                .defaultValue("保密")
                .validatorList(textValidatorList)
                .build();

        // 数字
        Set<Validator> numberValidatorList = Sets.newHashSetWithExpectedSize(1);
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
        Set<Validator> selectValidatorList = Sets.newHashSetWithExpectedSize(1);
        selectValidatorList.add(new Required(true));
        CpnConfigurer cpnConfigurerSelect = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("hobby")
                .label("兴趣爱好(单选)")
                .placeholder("请输入兴趣爱好")
                .options(new String[] {"足球", "篮球","乒乓球","羽毛球"})
                .defaultValue("足球")
                .validatorList(selectValidatorList)
                .build();

        // 多选
        Set<Validator> checkboxValidatorList = Sets.newHashSetWithExpectedSize(1);
        selectValidatorList.add(new Required(true));
        CpnConfigurer cpnConfigurerCheckbox = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.CHECKBOX)
                .name("hobby2")
                .label("兴趣爱好(多选)")
                .placeholder("请输入兴趣爱好")
                .options(new String[] {"足球", "篮球","乒乓球","羽毛球"})
                .defaultValue("[\"足球\", \"篮球\"]")
                .validatorList(checkboxValidatorList)
                .build();

        // 表格
        Set<Validator> tableValidatorList = Sets.newHashSetWithExpectedSize(1);
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
