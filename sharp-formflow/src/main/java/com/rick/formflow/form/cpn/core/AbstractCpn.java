package com.rick.formflow.form.cpn.core;

import com.google.common.collect.Sets;
import com.rick.common.util.ClassUtils;
import com.rick.common.util.JsonUtils;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-11-03 09:37:00
 */
public abstract class AbstractCpn<T> implements Cpn<T>, InitializingBean {

    private Class<?> cpnClass;

    @Override
    public void valid(T value, String[] options) {
        // 验证选项
        if (value instanceof String && StringUtils.isBlank((CharSequence) value)) {
            return;
        }

        if (Objects.nonNull(value) && ArrayUtils.isNotEmpty(options)) {
            if (!Sets.newHashSet(options).contains(value)) {
                throw new IllegalArgumentException("没有找到正确的选项");
            }
        }

        // 验证控件特性
        for (Validator cpnValidator : cpnValidators()) {
            cpnValidator.valid(value);
        }
    }

    @Override
    public T parseStringValue(String value) {
        if (cpnClass == String.class) {
            return (T) value;
        }

        try {
            return (T) JsonUtils.toObject(value, cpnClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getStringValue(T value) {
        if (Objects.isNull(value)) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        try {
            return JsonUtils.toJson(value);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public T httpConverter(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }

        if (value instanceof String) {
            return parseStringValue((String) value);
        }

        return (T) value;
    }

    @Override
    public void check(String[] options) {}

    @Override
    public Set<ValidatorTypeEnum> validatorSupports() {
        Set<ValidatorTypeEnum> supportSet = Sets.newHashSet();
        supportSet.add(ValidatorTypeEnum.REQUIRED);

        for (Validator cpnValidator : cpnValidators()) {
            supportSet.add(cpnValidator.getValidatorType());
        }

        Set<ValidatorTypeEnum> validators = internalValidatorSupports();
        if (CollectionUtils.isNotEmpty(validators)) {
            supportSet.addAll(validators);
        }

        return supportSet;
    }

    @Override
    public boolean hasValidator(Validator validator) {
        return validatorSupports().contains(validator.getValidatorType());
    }


    /**
     * 数据库可以配置的验证
     * @return
     */
    protected Set<ValidatorTypeEnum> internalValidatorSupports() {return null;};

    @Override
    public void afterPropertiesSet() {
        Class<?>[] classGenericsTypes = ClassUtils.getClassGenericsTypes(getClass());
        this.cpnClass = classGenericsTypes[0];
    }

    @Override
    public Set<Validator> cpnValidators() {
        return Collections.emptySet();
    }
}
