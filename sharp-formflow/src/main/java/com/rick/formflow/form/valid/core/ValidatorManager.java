package com.rick.formflow.form.valid.core;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-11-02 21:32:00
 */
@Getter
@Component
@RequiredArgsConstructor
public class ValidatorManager implements InitializingBean {

    private final Set<Validator> validatorSet;

    private static Map<ValidatorTypeEnum, Class<? extends Validator>> validatorMap;

    @Override
    public void afterPropertiesSet() {
        validatorMap = Maps.newHashMapWithExpectedSize(validatorSet.size());
        for (Validator validator : validatorSet) {
            validatorMap.put(validator.getValidatorType(), validator.getClass());
        }
    }

    public static Class getValidatorClassByType(ValidatorTypeEnum validatorTypeEnum) {
        return validatorMap.get(validatorTypeEnum);
    }

}
