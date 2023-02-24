package com.rick.formflow.form.cpn.core;


import com.rick.formflow.form.valid.core.Validator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;

import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-11-02 20:33:00
 */
public interface Cpn<T> {

    CpnTypeEnum getCpnType();

    T parseStringValue(String value);

    String getStringValue(T value);

    T httpConverter(Object value);

    void valid(T value, String[] options);

    void check(String[] options);

    /**
     * 控件支持的验证类型
     * @return
     */
    Set<ValidatorTypeEnum> validatorSupports();

    boolean hasValidator(Validator validator);

    /**
     * 控件特性相关验证
     * @return
     */
    Set<Validator> cpnValidators();

}
