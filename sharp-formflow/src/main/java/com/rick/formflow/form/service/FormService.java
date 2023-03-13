package com.rick.formflow.form.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.util.IdGenerator;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.core.MapDAO;
import com.rick.db.plugin.dao.core.MapDAOImpl;
import com.rick.formflow.form.cpn.core.*;
import com.rick.formflow.form.dao.CpnConfigurerDAO;
import com.rick.formflow.form.dao.FormCpnDAO;
import com.rick.formflow.form.dao.FormCpnValueDAO;
import com.rick.formflow.form.dao.FormDAO;
import com.rick.formflow.form.service.bo.FormBO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
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
import java.util.Optional;
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

    private final Map<String, FormAdvice> formAdviceMap;

    private final ApplicationContext applicationContext;

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

        Map<String, Object> valueMap = null;
        Map<Long, FormCpnValue> formCpnValueMap = null;

        if (isInstanceForm) {
            if (form.getStorageStrategy() == Form.StorageStrategyEnum.INNER_TABLE) {
                formCpnValueMap = formCpnValueDAO.selectByInstanceIdAsMap(instanceId);
            } else if (form.getStorageStrategy() == Form.StorageStrategyEnum.CREATE_TABLE) {
                if (StringUtils.isNotBlank(form.getRepositoryName())) {
                    EntityDAO entityDAO = applicationContext.getBean(form.getRepositoryName(), EntityDAO.class);
                    Optional optional = entityDAO.selectById(instanceId);
                    valueMap = entityDAO.entityToMap(optional.get());
                } else {
                    MapDAO<Long> dao = MapDAOImpl.of(applicationContext, form.getTableName(), Long.class);
                    Optional<Map<String, Object>> mapOptional = dao.selectById(instanceId);
                    valueMap = mapOptional.get();
                }
            }
        }

        for (FormCpn formCpn : formCpnList) {
            CpnConfigurer cpnConfigurer = configIdMap.get(formCpn.getConfigId());
            Cpn cpn = cpnManager.getCpnByType(cpnConfigurer.getCpnType());

            // 可配置验证 和 控件验证 合并
            cpnConfigurer.getValidatorList().addAll(cpn.cpnValidators());

            String value = null;
            Object distValue = null;

            if (isInstanceForm) {
                if (form.getStorageStrategy() == Form.StorageStrategyEnum.INNER_TABLE) {
                    FormCpnValue formCpnValue = formCpnValueMap.get(formCpn.getId());
                    if (Objects.nonNull(formCpnValue)) {
                        value = formCpnValue.getValue();
                    }
                } else if (form.getStorageStrategy() == Form.StorageStrategyEnum.CREATE_TABLE) {
                    Object tableValue = valueMap.get(cpnConfigurer.getName());

                    if (tableValue != null && tableValue instanceof String) {
                        value = (String) tableValue;
                    } else {
                        distValue = tableValue;
                    }
                }

            } else {
                value = cpnConfigurer.getDefaultValue();
            }

            if (distValue == null) {
                distValue = value == null ? null : cpn.parseStringValue(value);
            }

            propertyList.add(new FormBO.Property(formCpn.getId(), cpnConfigurer.getName(), cpnConfigurer, distValue));
        }

        return new FormBO(form, instanceId, propertyList);
    }

    public void post(Long formId, Map<String, Object> values) throws BindException {
        handle(formId, null, values);
    }

    @Transactional(rollbackFor = Exception.class)
    public void post(Long formId, Long instanceId, Map<String, Object> values) throws BindException {
        formCpnValueDAO.deleteByInstanceId(instanceId);
        handle(formId, instanceId, values);
    }

    private void handle(Long formId, Long instanceId, Map<String, Object> values) throws BindException {
        values.put("formId", formId);
        values.put("instanceId", instanceId);
        values.put("id", instanceId);

        Long innerTableId = instanceId == null ? IdGenerator.getSequenceId() : instanceId;

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
                    .instanceId(innerTableId)
                    .formId(formId)
                    .configId(property.getConfigurer().getId())
                    .build());
        }

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        if (form.getForm().getStorageStrategy() == Form.StorageStrategyEnum.INNER_TABLE) {
            formCpnValueDAO.insert(FormCpnValueList);
        } else if (form.getForm().getStorageStrategy() == Form.StorageStrategyEnum.CREATE_TABLE) {
            if (StringUtils.isNotBlank(form.getForm().getRepositoryName())) {
                EntityDAO entityDAO = applicationContext.getBean(form.getForm().getRepositoryName(), EntityDAO.class);
                entityDAO.insertOrUpdate(values);
            } else {
                MapDAO<Long> mapDAO = MapDAOImpl.of(applicationContext, form.getForm().getTableName(), Long.class);
                mapDAO.insertOrUpdate(values);
            }
        }

        // postHandler mongoDB 文档存储
        FormAdvice formAdvice = formAdviceMap.get(form.getForm().getFormAdviceName());
        if (formAdvice != null) {
            formAdvice.afterInstanceHandle(formId, instanceId, values);
        }

    }

    public int delete(Long instanceId) {
        return formCpnValueDAO.deleteByInstanceId(instanceId);
    }
}