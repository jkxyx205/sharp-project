package com.rick.common.validate;

import com.rick.common.util.EnumUtils;
import com.rick.common.validate.annotation.EnumValid;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-10-11 17:59:00
 */
@Slf4j
public class EnumValidator implements ConstraintValidator<EnumValid, Object> {

    /**
     * 枚举校验注解
     */
    private EnumValid annotation;

    @Override
    public void initialize(EnumValid constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        Class<?> clazz = annotation.target();
        try {
            return Objects.nonNull(EnumUtils.valueOfCode(clazz, String.valueOf(value)));
        } catch (Exception e) {
            return false;
        }
    }
}