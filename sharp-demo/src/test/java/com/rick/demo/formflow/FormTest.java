package com.rick.demo.formflow;

import com.google.common.collect.Lists;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.service.FormCpnService;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.service.bo.FormBO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.BindException;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-10-13 15:27:00
 */
@SpringBootTest
public class FormTest {

    @Autowired
    private FormService formService;

    @Autowired
    private FormCpnService formCpnService;

    private static Long formId = 487677232379494400L;

    private static Long instanceId = 487684156282011648L;

    private static Long[] configIds = new Long[] {487671506907070464L, 487671506907070465L,487671506907070466L,487671506907070468L,
            487671506907070470L,487671506907070469L,487671506907070471L,487671506907070473L,
            487671506907070467L};

    @AfterAll
    public static void init() {
        SQLUtils.deleteNotIn("sys_form", "id", Arrays.asList(formId));
        SQLUtils.deleteNotIn("sys_form_configurer", "id", Arrays.asList(configIds));
        SQLUtils.deleteNotIn("sys_form_cpn_configurer", "form_id", Arrays.asList(formId));
        SQLUtils.deleteNotIn("sys_form_cpn_value", "instance_id", Arrays.asList(instanceId));
    }

    @Test
    public void testSaveForm() {
        formService.save(Form.builder().name("我的第一个表单").build());
    }

    @Test
    public void testAddConfigurerToForm() {
        formCpnService.saveOrUpdateByConfigIds(formId, configIds);
        formService.save(Form.builder().name("我的第一个表单").build());
    }

    @Test
    public void testAddConfigurerToForm2() {
        Form form = Form.builder().name("我的第二个表单").build();
        formService.save(form);

        List<CpnConfigurer> cpnConfigurerList = new CpnTest().createCpnConfigurerList();
        formCpnService.saveOrUpdateByConfigurer(form.getId(), cpnConfigurerList);
    }

    @Test
    public void testAddInstanceToForm() throws BindException {
        List<Object[]> list = Lists.newArrayListWithExpectedSize(1);
        list.add(new Object[] {"rick", 32});

        formService.post(formId,  Params.builder()
                .pv("HNepFsUCaN", "Jkxyx205")
                .pv("WpOZNqQasd", "32")
                .pv("NUqlBwLPfW", "足球")
                .pv("xYfeMutDQK", list)
                .pv("checkbox", Arrays.asList("足球", "篮球"))
                .pv("bz", "备注")
                .build());
//        System.out.println(JsonUtils.toJson(form));
    }

    @Test
    public void testFindInstanceToForm() {
        FormBO form1 = formService.getFormBOById(formId);
        FormBO form2 = formService.getFormBOByIdAndInstanceId(formId, instanceId);
    }

}
