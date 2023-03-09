package com.rick.formflow.form.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.util.IdGenerator;
import com.rick.formflow.config.FormFlowProperties;
import com.rick.formflow.form.cpn.core.*;
import com.rick.formflow.form.dao.CpnConfigurerDAO;
import com.rick.formflow.form.dao.FormCpnDAO;
import com.rick.formflow.form.dao.FormCpnValueDAO;
import com.rick.formflow.form.dao.FormDAO;
import com.rick.formflow.form.service.bo.FormBO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-03 17:14:00
 */
@Service
@RequiredArgsConstructor
@Validated
public class FormService {

    private final FormDAO formDAO;

    private final FormCpnDAO formCpnDAO;

    private final CpnConfigurerDAO cpnConfigurerDAO;

    private final FormCpnValueDAO formCpnValueDAO;

    private final CpnManager cpnManager;

    private final FormFlowProperties formFlowProperties;

    private final Map<String, FormAdvice> formAdviceMap;

    public Form save(@Valid Form form) {
        formDAO.insert(form);
        return form;
    }

    public FormBO getFormBOById(Long formId) {
        return getFormBOByIdAndInstanceId(formId, null);
    }

    public FormBO getFormBOByIdAndInstanceId(Long formId, Long instanceId) {
        boolean isInstanceForm = Objects.nonNull(instanceId);
        Form form = formDAO.selectById(formId).get();
        List<FormCpn> formCpnList = formCpnDAO.listByFormId(formId);
        Map<Long, CpnConfigurer> configIdMap = cpnConfigurerDAO.selectByIdsAsMap(formCpnList.stream().map(fc -> fc.getConfigId()).collect(Collectors.toSet()));

        List<FormBO.Property> propertyList = Lists.newArrayListWithExpectedSize(formCpnList.size());

        Map<Long, FormCpnValue> formCpnValueMap = null;
        if (isInstanceForm) {
            formCpnValueMap = formCpnValueDAO.selectByInstanceIdAsMap(instanceId);
        }

        for (FormCpn formCpn : formCpnList) {
            CpnConfigurer cpnConfigurer = configIdMap.get(formCpn.getConfigId());
            Cpn cpn = cpnManager.getCpnByType(cpnConfigurer.getCpnType());
            String value = null;
            if (formFlowProperties.isInsertCpnValue()) {
                if (isInstanceForm) {
                    FormCpnValue formCpnValue = formCpnValueMap.get(formCpn.getId());
                    if (Objects.nonNull(formCpnValue)) {
                        value = formCpnValue.getValue();
                    }
                } else {
                    value = cpnConfigurer.getDefaultValue();
                }
            } else {
                if (isInstanceForm) {
                    FormAdvice formAdvice = formAdviceMap.get(form.getServiceName());
                    Map<String, Object> valueMap = formAdvice.getValue(formId, instanceId);
                    value = valueMap.get(cpnConfigurer.getName()) == null ? null : String.valueOf(valueMap.get(cpnConfigurer.getName()));
                } else {
                    value = cpnConfigurer.getDefaultValue();
                }
            }

            Object dist = StringUtils.isBlank(value) ? value : cpn.parseStringValue(value);
            propertyList.add(new FormBO.Property(formCpn.getId(), cpnConfigurer.getName(), cpnConfigurer, dist));
        }

        return new FormBO(form, instanceId, propertyList);
    }

    public void post(Long formId, Map<String, Object> values) throws BindException {
        handle(formId, IdGenerator.getSequenceId(), values);
    }

    @Transactional(rollbackFor = Exception.class)
    public void post(Long formId, Long instanceId, Map<String, Object> values) throws BindException {
        formCpnValueDAO.deleteByInstanceId(instanceId);
        handle(formId, instanceId, values);
    }

    private void handle(Long formId, Long instanceId, Map<String, Object> values) throws BindException {
        FormBO form = getFormBOById(formId);

        List<FormCpnValue> FormCpnValueList = Lists.newArrayListWithExpectedSize(form.getPropertyList().size());

        Map<String, Object> map = Maps.newHashMap();
        BindingResult bindingResult =  new MapBindingResult(map, getClass().getName());

        for (FormBO.Property property : form.getPropertyList()) {
            CpnInstanceProcessor processor = new CpnInstanceProcessor(property, cpnManager, values.get(property.getName()), bindingResult);
            processor.valid();
            if (bindingResult.hasErrors()) {
                continue;
            }

            String value = processor.getParamValue();

            FormCpnValueList.add(FormCpnValue.builder()
                    .value(value)
                    .formCpnId(property.getId())
                    .instanceId(instanceId)
                    .formId(formId)
                    .configId(property.getConfigurer().getId())
                    .build());
        }

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        if (formFlowProperties.isInsertCpnValue()) {
            formCpnValueDAO.insert(FormCpnValueList);
        }

        values.put("formId", formId);
        values.put("instanceId", instanceId);
        // postHandler mongoDB 文档存储
        FormAdvice formAdvice = formAdviceMap.get(form.getForm().getServiceName());
        if (formAdvice != null) {
            formAdvice.afterInstanceSave(formId, instanceId, values);
        }

    }

    public int delete(Long instanceId) {
        return formCpnValueDAO.deleteByInstanceId(instanceId);
    }
}