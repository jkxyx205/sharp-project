package com.rick.formflow.form.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.util.IdGenerator;
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

import java.io.Serializable;
import java.util.Collections;
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


    public Form save(Form form) {
        formDAO.insert(form);
        return form;
    }

    public FormBO getFormBOById(Long formId) {
        return getFormBOByIdAndInstanceId(formId, null);
    }

    public FormBO getFormBOByIdAndInstanceId(Long formId, Long instanceId) {
        Form form = formDAO.selectById(formId).get();
        List<FormCpn> formCpnList = formCpnDAO.listByFormId(formId);
        Map<Serializable, CpnConfigurer> configIdMap = cpnConfigurerDAO.selectByIdsAsMap(formCpnList.stream().map(fc -> fc.getConfigId()).collect(Collectors.toSet()));

        List<FormBO.Property> propertyList = Lists.newArrayListWithExpectedSize(formCpnList.size());

        Map<Long, FormCpnValue> formCpnValueMap;
        if (Objects.nonNull(instanceId)) {
            formCpnValueMap = formCpnValueDAO.selectByInstanceIdAsMap(instanceId);
        } else {
            formCpnValueMap = Collections.emptyMap();
        }

        for (FormCpn formCpn : formCpnList) {
            CpnConfigurer cpnConfigurer = configIdMap.get(formCpn.getConfigId());
            FormCpnValue formCpnValue = formCpnValueMap.get(formCpn.getId());
            Cpn cpn = cpnManager.getCpnByType(cpnConfigurer.getCpnType());
            String stringValue = Objects.nonNull(formCpnValue) ? formCpnValue.getValue() : cpnConfigurer.getDefaultValue();
            Object value = StringUtils.isBlank(stringValue) ? stringValue : cpn.parseStringValue(stringValue);
            cpnConfigurer.getValidatorList().addAll(cpn.cpnValidators());
            propertyList.add(new FormBO.Property(formCpn.getId(), formCpn.getName(), cpnConfigurer, value));
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

            FormCpnValueList.add(FormCpnValue.builder().value(value)
                    .formCpnId(property.getId())
                    .instanceId(instanceId)
                    .formId(formId)
                    .configId(property.getConfigurer().getId())
                    .build());
        }

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        formCpnValueDAO.insert(FormCpnValueList);
    }

    public int delete(Long instanceId) {
        return formCpnValueDAO.deleteByInstanceId(instanceId);
    }
}