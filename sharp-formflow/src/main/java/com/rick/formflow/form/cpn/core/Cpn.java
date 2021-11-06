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

    Set<ValidatorTypeEnum> validatorSupports();

    boolean hasValidator(Validator validator);

    Set<Validator> cpnValidators();

}
