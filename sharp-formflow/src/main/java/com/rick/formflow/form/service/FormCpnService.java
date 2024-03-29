package com.rick.formflow.form.service;

import com.google.common.collect.Lists;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.cpn.core.FormCpn;
import com.rick.formflow.form.dao.CpnConfigurerDAO;
import com.rick.formflow.form.dao.FormCpnDAO;
import com.rick.formflow.form.dao.FormDAO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-03 17:14:00
 */
@Service
@RequiredArgsConstructor
@Validated
public class FormCpnService {

    private final FormCpnDAO formCpnDAO;

    private final CpnConfigurerDAO cpnConfigurerDAO;

    private final FormDAO formDAO;

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateByConfigurer(Form form, Collection<CpnConfigurer> configurerList) {
        formDAO.insertOrUpdate(form);
        saveOrUpdateByConfigurer(form.getId(), configurerList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateByConfigIds(Long formId, Long... configIds) {
        saveOrUpdateByConfigIds(formId, ArrayUtils.isEmpty(configIds) ? Collections.emptyList() : Arrays.asList(configIds));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateByConfigIds(Long formId, Collection<Long> configIds) {
        List<CpnConfigurer> configurerList;
        if (CollectionUtils.isEmpty(configIds)) {
            configurerList = Collections.emptyList();
        } else {
            configurerList = Lists.newArrayListWithExpectedSize(configIds.size());
            Map<Long, CpnConfigurer> configIdsMap = cpnConfigurerDAO.selectByIdsAsMap(configIds);
            for (Long configId : configIds) {
                configurerList.add(configIdsMap.get(configId));
            }
        }

        saveOrUpdateByConfigurer(formId, configurerList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateByConfigurer(Long formId, Collection<CpnConfigurer> configurerList) {
        List<FormCpn> formCpnList = Lists.newArrayListWithExpectedSize(configurerList.size());
        cpnConfigurerDAO.insertOrUpdate(configurerList);

        int i = 0;
        for (CpnConfigurer cpnConfigurer : configurerList) {
//            formCpnList.add(new FormCpn(formId, StringUtils.isBlank(cpnConfigurer.getName()) ? RandomStringUtils.randomAlphabetic(10) : cpnConfigurer.getName(), cpnConfigurer.getId(), i++));
            formCpnList.add(new FormCpn(formId, cpnConfigurer.getId(), i++, null));
        }

        saveOrUpdateByConfigIds(formId, formCpnList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateByConfigIds(Long formId, List<FormCpn> formCpnList) {
        Map<Long, FormCpn> configIdMap = formCpnDAO.listByFormId(formId).stream().collect(Collectors.toMap(FormCpn::getConfigId, v -> v));

        for (int i = 0; i < formCpnList.size(); i++) {
            FormCpn formCpn = formCpnList.get(i);
            formCpn.setFormId(formId);

            FormCpn existsFormCpn = configIdMap.get(formCpn.getConfigId());
            if (Objects.nonNull(existsFormCpn)) {
                formCpn.setId(existsFormCpn.getId());
//                formCpn.setName(existsFormCpn.getName());
            } else {
//                formCpn.setName(RandomStringUtils.randomAlphabetic(10));
            }
            formCpn.setOrderNum(i);
        }
        formCpnDAO.deleteByFormId(formId);
        formCpnDAO.insert(formCpnList);
    }
}