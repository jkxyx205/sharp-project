package com.rick.demo.formflow;

import com.rick.common.util.JsonUtils;
import com.rick.common.util.Maps;
import com.rick.db.repository.TableDAO;
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

import java.util.*;

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

    private static Long instanceId = 667878193193070592L;

    private static Long[] configIds = new Long[] {487671506907070464L, 487671506907070465L,487671506907070466L,487671506907070468L,
            487671506907070470L,487671506907070469L,487671506907070471L,487671506907070473L,
            487671506907070474L,487671506907070475L,487671506907070476L, 487671506907070467L
    };

    private static TableDAO tableDAO;

    // 通过非静态的setter方法注入到静态属性
    @Autowired
    public void setTableDAO(TableDAO tableDAO) {
        this.tableDAO = tableDAO;
    }

    @AfterAll
    public static void init() {
        tableDAO.deleteNotIn("sys_form", "id", Arrays.asList(formId, 670604125091708928L));

        List<Long> configIdList = new ArrayList<>();
        configIdList.addAll(Arrays.asList(configIds));
        configIdList.addAll(Arrays.asList(new Long[] {
                670609451929092096L,
                670609451933286400L,
                670609451933286401L,
                670609451933286402L,
                670609451933286403L,
                670609451933286404L,
                670609451933286405L,
                670609451933286406L,
                670609451933286407L,
                670609451933286408L,
                670609451937480704L,
                670609451937480705L,
                670609451937480706L,
                670609451937480707L

        }));

        tableDAO.deleteNotIn("sys_form_configurer", "id", configIdList);
        tableDAO.deleteNotIn("sys_form_cpn_configurer", "form_id", Arrays.asList(formId, 670604125091708928L));
        tableDAO.deleteNotIn("sys_form_cpn_value", "instance_id", Arrays.asList(instanceId));
    }

    @Test
    public void testSaveForm() {
        formService.saveOrUpdate(Form.builder().code("001" + System.currentTimeMillis()).name("我的第一个表单").build());
    }

    @Test
    public void testAddConfigurerToForm() {
        formCpnService.saveOrUpdateByConfigIds(formId, configIds);
        formService.saveOrUpdate(Form.builder().code("first"+ System.currentTimeMillis()).name("我的第一个表单").build());
    }

    @Test
    public void testAddConfigurerToForm2() {
        Form form = Form.builder().name("我的第二个表单").code("second").build();
        formService.saveOrUpdate(form);

        List<CpnConfigurer> cpnConfigurerList = new CpnTest().createCpnConfigurerList();
        formCpnService.saveOrUpdateByConfigurer(form.getId(), cpnConfigurerList);
    }

    @Test
    public void testAddInstanceToForm() throws BindException {
        Map<String, Object> value = new HashMap<>();
        value.put("name", "Rick");
        value.put("age", "32");
        value.put("native_place", "江苏");
        value.put("info", Arrays.asList(Arrays.asList("rick", 32)));
        value.put("hobby", Arrays.asList("足球", "篮球"));
        value.put("agree", Arrays.asList("同意"));
        value.put("remark", "简介");
        value.put("sex", "男");
        value.put("mobile", "18898987765");
        value.put("file", Arrays.asList(Maps.of("url", "https://xhope.top/"),
                Maps.of("url", "https://xhope.top/")));
        value.put("email", "jkxyx205@163.com");
        value.put("date", "1990-12-23");
        formService.post(formId,value);
//        System.out.println(JsonUtils.toJson(form));
    }

    @Test
    public void testFindInstanceToForm() {
        FormBO form1 = formService.getFormBOById(formId);
        if (form1.getForm().getStorageStrategy() == Form.StorageStrategyEnum.INNER_TABLE) {
            FormBO form = formService.getFormBOByIdAndInstanceId(formId, instanceId);
            System.out.println(JsonUtils.toJson(form));
        } else if (form1.getForm().getStorageStrategy() == Form.StorageStrategyEnum.NONE) {
//            FormBO form2 = formService.getFormBOByIdAndInstanceId(formId, 665628482239229955L);
        }

    }

}
