package com.rick.admin.core;

import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.service.CpnConfigurerService;
import com.rick.formflow.form.service.FormCpnService;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.service.bo.FormBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2024/9/20 11:12
 */
@Component
public class FormSupport {

    @Autowired
    private FormService formService;

    @Autowired
    private FormCpnService formCpnService;

    @Autowired
    private CpnConfigurerService cpnConfigurerService;

    public void bind(Long formId, List<CpnConfigurer> cpnConfigurerList) {
        // 绑定原先控件id
        FormBO formBO = formService.getFormBO(formId, null);
        Map<String, FormBO.Property> propertyMap = formBO.getPropertyMap();
        cpnConfigurerList.forEach(configurer -> {
            FormBO.Property property = propertyMap.get(configurer.getName());
            if (property != null) {
                configurer.setId(property.getConfigurer().getId());
            }
        });

        cpnConfigurerService.saveOrUpdate(cpnConfigurerList);
        // 关联关系
        formCpnService.saveOrUpdateByConfigIds(formId, cpnConfigurerList.stream().map(CpnConfigurer::getId).collect(Collectors.toList()));
    }
}
