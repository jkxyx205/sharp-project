package com.rick.formflow.form.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.util.IdGenerator;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.core.MapDAO;
import com.rick.db.plugin.dao.core.MapDAOImpl;
import com.rick.db.plugin.dao.support.BaseEntityUtils;
import com.rick.formflow.form.cpn.core.*;
import com.rick.formflow.form.dao.CpnConfigurerDAO;
import com.rick.formflow.form.dao.FormCpnDAO;
import com.rick.formflow.form.dao.FormCpnValueDAO;
import com.rick.formflow.form.dao.FormDAO;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.formflow.form.service.model.FormCache;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.*;
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

    private final Map<String, FormAdvice> formAdviceMap;

    private final Map<String, CpnValueConverter> cpnValueConverterMap;

    private final ApplicationContext applicationContext;

    private final DictService dictService;

    public Form saveOrUpdate(@Valid Form form) {
        formDAO.insertOrUpdate(form);
        return form;
    }

    public FormBO getFormBO(Long formId, Long instanceId) {
        FormBO formBO;
        if (Objects.nonNull(instanceId)) {
            formBO = getFormBOByIdAndInstanceId(formId, instanceId);
        } else {
            formBO = getFormBOById(formId);
        }

        return formBO;
    }

    public FormBO getFormBOById(Long formId) {
        return getFormBOByIdAndInstanceId(formId, null);
    }

    public FormBO getFormBOByIdAndInstanceId(Long formId, Long instanceId) {
        boolean isInstanceForm = Objects.nonNull(instanceId);

        FormCache formCache = FormUtils.getFormCacheById(formId);

        Form form;
        Map<Long, CpnConfigurer> configIdMap;
        List<FormBO.Property> propertyList;;
        List<FormCpn> formCpnList;

        if (formCache == null) {
            form = formDAO.selectById(formId).get();
            if (form.getAdditionalInfo() == null) {
                form.setAdditionalInfo(new HashMap<>());
            }
            formCpnList = formCpnDAO.listByFormId(formId);
            configIdMap = cpnConfigurerDAO.selectByIdsAsMap(formCpnList.stream().map(fc -> fc.getConfigId()).collect(Collectors.toSet()));
            formCache = new FormCache(form, formCpnList, configIdMap);

            FormUtils.update(formId, formCache);
        }

        FormCache clone = SerializationUtils.clone(formCache);

        form = clone.getForm();
        configIdMap = clone.getConfigIdMap();
        formCpnList = clone.getFormCpnList();
        propertyList = Lists.newArrayListWithExpectedSize(formCpnList.size());

        Map<String, Object> valueMap = new HashMap<>();;
        Map<Long, FormCpnValue> formCpnValueMap = null;

        if (isInstanceForm) {
            if (form.getStorageStrategy() == Form.StorageStrategyEnum.INNER_TABLE) {
                formCpnValueMap = formCpnValueDAO.selectByInstanceIdAsMap(instanceId);
            } else if (form.getStorageStrategy() == Form.StorageStrategyEnum.CREATE_TABLE) {
                if (StringUtils.isNotBlank(form.getRepositoryName())) {
                    EntityDAO entityDAO = applicationContext.getBean(form.getRepositoryName(), EntityDAO.class);
                    Optional optional = entityDAO.selectById(instanceId);
                    valueMap = entityDAO.entityToMap(optional.get());

                    // 属性值 放到 map 中
                    Map<String, String> popertyNameToColumnNameMap = entityDAO.getPropertyNameToColumnNameMap();
                    for (Map.Entry<String, String> popertyNameToColumnNameEntry : popertyNameToColumnNameMap.entrySet()) {
                        valueMap.put(popertyNameToColumnNameEntry.getValue(), valueMap.get(popertyNameToColumnNameEntry.getKey()));
                    }
                } else {
                    MapDAO<Long> dao = MapDAOImpl.of(applicationContext, form.getTableName(), Long.class);
                    Optional<Map<String, Object>> mapOptional = dao.selectById(instanceId);
                    valueMap = mapOptional.get();
                }
            }
        }

        FormAdvice formAdvice = formAdviceMap.get(form.getFormAdviceName());
        if (formAdvice != null) {
            formAdvice.beforeGetInstance(instanceId, valueMap);
        }

        for (FormCpn formCpn : formCpnList) {
            CpnConfigurer cpnConfigurer = configIdMap.get(formCpn.getConfigId());

            if (StringUtils.isNotBlank(cpnConfigurer.getDatasource())) {
                List<Dict> dictList = dictService.getDictByType(cpnConfigurer.getDatasource());
                cpnConfigurer.setOptions(dictList.stream().map(dictDO -> new CpnConfigurer.CpnOption(dictDO.getName(), dictDO.getLabel())).collect(Collectors.toList()));
            }

            Cpn cpn = CpnManager.getCpnByType(cpnConfigurer.getCpnType());

            Object value = null;

            if (isInstanceForm) {
                if (form.getStorageStrategy() == Form.StorageStrategyEnum.INNER_TABLE) {
                    FormCpnValue formCpnValue = formCpnValueMap.get(formCpn.getId());
                    if (Objects.nonNull(formCpnValue)) {
                        value = formCpnValue.getValue();
                    }
                } else if (form.getStorageStrategy() == Form.StorageStrategyEnum.CREATE_TABLE) {
                    value = valueMap.get(cpnConfigurer.getName());
                }

            } else {
                value = cpnConfigurer.getDefaultValue();
            }

            CpnValueConverter cpnValueConverter = cpnValueConverterMap.get(cpnConfigurer.getCpnValueConverterName());

            if (cpnValueConverter != null) {
                value = cpnValueConverter.convert(value);
            }

            value = value == null ? null : cpn.parseValue(value);

            propertyList.add(new FormBO.Property(formCpn.getId(), cpnConfigurer.getName(), cpnConfigurer, value));
            valueMap.put(cpnConfigurer.getName(), value);
        }

        if (formAdvice != null) {
            formAdvice.afterGetInstance(form, instanceId, propertyList, valueMap);
        }

        return new FormBO(form, instanceId, propertyList, valueMap);
    }

    @Transactional(rollbackFor = Exception.class)
    public void post(Long formId, Map<String, Object> values) throws BindException {
        handle(formId, null, values);
    }

    @Transactional(rollbackFor = Exception.class)
    public void post(Long formId, Long instanceId, Map<String, Object> values) throws BindException {
        handle(formId, instanceId, values);
    }

    private void handle(Long formId, Long instanceId, Map<String, Object> values) throws BindException {
        FormBO form = getFormBOById(formId);

        values.put("formId", formId);
        values.put("id", instanceId);

        Long innerTableId = instanceId == null ? IdGenerator.getSequenceId() : instanceId;
        values.put("instanceId", innerTableId);

        List<FormCpnValue> formCpnValueList = Lists.newArrayListWithExpectedSize(form.getPropertyList().size());

        Map<String, Object> map = Maps.newHashMap();
        BindingResult bindingResult = new MapBindingResult(map, getClass().getName());

        for (FormBO.Property property : form.getPropertyList()) {
            CpnInstanceProcessor processor = new CpnInstanceProcessor(property, values.get(property.getName()), bindingResult);
            processor.valid();
            if (bindingResult.hasErrors()) {
                continue;
            }

            String value = processor.getParamValue();
            values.put(property.getName(), processor.getCpnValue());

            formCpnValueList.add(FormCpnValue.builder()
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

        FormAdvice formAdvice = formAdviceMap.get(form.getForm().getFormAdviceName());
        if (formAdvice != null) {
            // 进行业务数据的再处理
            formAdvice.beforeInstanceHandle(form, instanceId, values);
        }

        if (form.getForm().getStorageStrategy() == Form.StorageStrategyEnum.INNER_TABLE) {
            formCpnValueDAO.deleteByInstanceId(instanceId);
            formCpnValueDAO.insert(formCpnValueList);
        } else if (form.getForm().getStorageStrategy() == Form.StorageStrategyEnum.CREATE_TABLE) {
            boolean customInsertOrUpdate = false;

            if (formAdvice != null) {
                // 进行业务再处理
                customInsertOrUpdate = formAdvice.insertOrUpdate(values);
            }

            if (!customInsertOrUpdate) {
                if (StringUtils.isNotBlank(form.getForm().getRepositoryName())) {
                    EntityDAO entityDAO = applicationContext.getBean(form.getForm().getRepositoryName(), EntityDAO.class);
                    // 内部 map 转 entity，可以做级联操作；客户端可以对 DAO 的方法insertOrUpdate(Map<String, Object> params)
                    entityDAO.insertOrUpdate(values);

//              // entityDAO.insertOrUpdate(entityDAO.mapToEntity(values)); 在这里不需要显示地转
                } else {
                    MapDAO<Long> mapDAO = MapDAOImpl.of(applicationContext, form.getForm().getTableName(), Long.class);
                    mapDAO.insertOrUpdate(values);
                }
            }
        }

        // postHandler mongoDB 文档存储
        if (formAdvice != null) {
            formAdvice.afterInstanceHandle(form, instanceId, values);
        }

        values.put("id", innerTableId);
    }

    public int delete(Long formId, Long[] instanceIds) {
        for (Long instanceId : instanceIds) {
            delete(formId, instanceId);
        }

        return 1;
    }

    public int delete(Long formId, Long instanceId) {
        Form form =  formDAO.selectById(formId).get();

        FormAdvice formAdvice = formAdviceMap.get(form.getFormAdviceName());
        if (formAdvice != null) {
            formAdvice.beforeDeleteInstance(instanceId);
        }

        if (form.getStorageStrategy() == Form.StorageStrategyEnum.INNER_TABLE) {
            return formCpnValueDAO.deleteByInstanceId(instanceId);
        } else if (form.getStorageStrategy() == Form.StorageStrategyEnum.CREATE_TABLE) {
            if (StringUtils.isNotBlank(form.getRepositoryName())) {
                EntityDAO entityDAO = applicationContext.getBean(form.getRepositoryName(), EntityDAO.class);

                if (BaseEntityUtils.isEntityClass(entityDAO.getEntityClass())) {
                    return entityDAO.deleteLogicallyById(instanceId);
                }

                return entityDAO.deleteById(instanceId);
            }
        }

        return 0;
    }
}