package com.rick.formflow.form.cpn.core;

import com.google.common.collect.Sets;
import com.rick.common.util.ClassUtils;
import com.rick.common.util.JsonUtils;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-03 09:37:00
 */
public abstract class AbstractCpn<T> implements Cpn<T>, InitializingBean {

    private Class<?> cpnClass;

    @Override
    public void valid(T value, List<CpnConfigurer.CpnOption> options) {
        // 验证选项
        if (value instanceof String && StringUtils.isBlank((CharSequence) value)) {
            return;
        }

        if (Objects.nonNull(value) && CollectionUtils.isNotEmpty(options)) {
            if (!options.stream().map(CpnConfigurer.CpnOption::getName).collect(Collectors.toSet()).contains(value)) {
                throw new IllegalArgumentException("没有找到正确的选项");
            }
        }

    }

    /**
     * 处理 String 类型的 value
     * @param value
     * @return
     */
    @Override
    public T parseValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            String stringVal = (String) value;

            if (cpnClass == String.class) {
                return (T) value;
            } else if (cpnClass == Integer.class) {
                return (T) Integer.valueOf((String) value);
            } else if (cpnClass == BigDecimal.class) {
                return (T) new BigDecimal((String) value);
            }

            // 非json字符串
            if (!(stringVal.startsWith("[") || stringVal.startsWith("{"))) {
                stringVal = "[\""+value+"\"]";
            }

            return (T) JsonUtils.toObject(stringVal, cpnClass);
        }

        if (cpnClass == String.class) {
            return (T) String.valueOf(value);
        }

        return (T) value;
    }

    @Override
    public String getStringValue(T value) {
        if (Objects.isNull(value)) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return JsonUtils.toJson(value);
    }

    @Override
    public T httpConverter(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }

        if (value instanceof String) {
            return parseValue(value);
        }

        return (T) value;
    }

    @Override
    public void check(List<CpnConfigurer.CpnOption> options) {}

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
