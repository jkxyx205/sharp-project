package com.rick.admin.core;

import com.google.common.collect.Lists;
import com.rick.admin.module.demo.entity.ComplexModel;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.valid.CustomizeRegex;
import com.rick.formflow.form.valid.Length;
import com.rick.formflow.form.valid.Required;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.meta.dict.model.DictValue;
import com.rick.meta.dict.service.DictUtils;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.HiddenReportColumn;
import com.rick.report.core.model.QueryField;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.SordEnum;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick.Xu
 * @date 2023/5/28 15:07
 */
@SpringBootTest
public class DictTest {

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
                .id(695312747063197696L)
                .code("sys_dict")
                .tableName("sys_dict")
                .tplName("tpl/form/form-full") // 弹出框显示必须是 form-full 不能是form
                .additionalInfo(Params.builder(1).pv("showSaveFormBtn", false).build())
                .formAdviceName("dictFormService")
                .name("字典")
                .repositoryName("dictDAO")
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
                .cpnType(CpnTypeEnum.SELECT)
                .datasource("sys_dict_type")
                .name("type")
                .label("分类")
                .placeholder("请输入分类")
                .validatorList(codeRegexValidatorList)
                .build();

        CpnConfigurer nameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("name")
                .label("编码")
                .placeholder("请输入编码")
                .validatorList(codeRegexValidatorList)
                .build();

        CpnConfigurer availableCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("label")
                .label("显示值")
                .placeholder("请输入显示值")
                .validatorList(textValidatorList)
                .build();

        CpnConfigurer roleCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.INTEGER_NUMBER)
                .name("sort")
                .label("排序号")
                .defaultValue("0")
                .build();

        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(usernameCpn, nameCpn, availableCpn, roleCpn);
        return cpnConfigurerList;
    }

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(695316160014499840L)
                .code("sys_dict")
                .tplName("tpl/list/list")
                .name("字典管理")
                .additionalInfo(Params.builder(1).pv("formId", "695312747063197696").build())
                .querySql("select id, type, name, label, sort from sys_dict where type = :type order by type, sort asc")
                .queryFieldList(Arrays.asList(
                        new QueryField("type", "分类", QueryField.Type.SELECT, "sys_dict_type")
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("name", "编码"),
                        new ReportColumn("label", "显示值"),
                        new ReportColumn("type", "分类", true),
                        new ReportColumn("sort", "排序号").setColumnWidth(30)
                ))
                .pageable(false)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }

    @Test
    public void testFillDictLabel() {
        DictValue dictValue = new DictValue("PACKAGING", "CategoryEnum");
        DictUtils.fillDictLabel(dictValue);
        assertThat(dictValue.getLabel()).isNotBlank();
        assertThat(dictValue.getType()).isNotBlank();

        ComplexModel complexModel = new ComplexModel();
        complexModel.setCategoryDict(new DictValue("PACKAGING", "CategoryEnum"));
        DictUtils.fillDictLabel(complexModel);
        assertThat(complexModel.getCategoryDict().getLabel()).isNotBlank();
        assertThat(complexModel.getCategoryDict().getType()).isNotBlank();

        List<DictValue> list = Arrays.asList(new DictValue("PACKAGING", "CategoryEnum"));
        DictUtils.fillDictLabel(list);
        assertThat(list.get(0).getLabel()).isNotBlank();
        assertThat(list.get(0).getType()).isNotBlank();

        Map<Object, Object> map = new HashMap<>();
        ComplexModel complexModel2 = new ComplexModel();
        complexModel2.setCategoryDict(new DictValue("PACKAGING", "CategoryEnum"));

        ComplexModel complexModel3 = new ComplexModel();
        complexModel3.setCategoryDict(new DictValue("PACKAGING", "CategoryEnum"));

        map.put("id", complexModel2);
        map.put("list", Arrays.asList(complexModel3));
        map.put("1", 2);
        map.put("2", new DictValue("PACKAGING", "CategoryEnum"));
        map.put("3", new DictValue("PACKAGING", "CategoryEnum"));
        map.put(new DictValue("MATERIAL", "CategoryEnum"), 3);
        map.put(new DictValue("PACKAGING", "CategoryEnum"), Arrays.asList(new DictValue("PACKAGING", "CategoryEnum")));
        DictUtils.fillDictLabel(map);
    }
}
