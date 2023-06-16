package com.rick.formflow.form.service;


import com.rick.formflow.form.cpn.core.Cpn;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnManager;
import com.rick.formflow.form.dao.CpnConfigurerDAO;
import com.rick.formflow.form.valid.core.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-11-03 11:23:00
 */
@Service
@RequiredArgsConstructor
@Validated
public class CpnConfigurerService {

    private final CpnConfigurerDAO cpnConfigurerDAO;

    public int[] saveOrUpdate(List<CpnConfigurer> configurers) {
        for (CpnConfigurer configurer : configurers) {
            CpnManager.getCpnByType(configurer.getCpnType()).check(configurer);
        }

        return cpnConfigurerDAO.insertOrUpdate(configurers);
    }

//    public CpnConfigurer save(CpnConfigurer configurer) {
//        checkIfAvailable(configurer);
//        cpnConfigurerDAO.insert(configurer);
//        return configurer;
//    }

//    public int[] save(List<CpnConfigurer> configurers) {
//        for (CpnConfigurer configurer : configurers) {
//            checkIfAvailable(configurer);
//        }
//        return cpnConfigurerDAO.insert(configurers);
//    }

    public CpnConfigurer findById(Long id) {
        CpnConfigurer cpnConfigurer = cpnConfigurerDAO.selectById(id).get();
        return cpnConfigurer;
    }

    private void checkIfAvailable(CpnConfigurer configurer) {
        Cpn cpn = CpnManager.getCpnByType(configurer.getCpnType());

        for (Validator validator : configurer.getValidatorList()) {
            if (!cpn.hasValidator(validator)) {
                throw new IllegalArgumentException(cpn.getCpnType() + "不支持验证器" + validator.getValidatorType());
            }
        }

        cpn.check(configurer);
    }

}