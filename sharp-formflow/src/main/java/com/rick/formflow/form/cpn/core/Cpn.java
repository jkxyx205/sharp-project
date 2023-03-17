package com.rick.formflow.form.cpn.core;


import com.rick.formflow.form.valid.core.Validator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;

import java.util.List;
import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-11-02 20:33:00
 */
public interface Cpn<T> {

    CpnTypeEnum getCpnType();

    /**
     * 数据库 => UI(String, List)
     * @param value
     * @return
     */
    T parseValue(Object value);

    /**
     * UI => 数据库(String) 内部表用
     * @param value
     * @return
     */
    String getStringValue(T value);

    T httpConverter(Object value);

    void valid(T value, List<CpnConfigurer.CpnOption> options);

    void check(List<CpnConfigurer.CpnOption> options);

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
