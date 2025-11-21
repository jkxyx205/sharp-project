package com.rick.meta.dict.model;

import com.rick.meta.config.validator.Dict2Validator;
import com.rick.meta.config.validator.DictValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {DictValidator.class, Dict2Validator.class})
@Documented
public @interface DictType {
    String message() default "code %s 不存在";

    String type() default "";

    String sql() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}