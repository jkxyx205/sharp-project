package com.rick.meta.dict.model;

import com.rick.meta.config.validator.*;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {DictDictValueValidator.class, DictDictValueListValidator.class, DictStringValidator.class, DictStringListValidator.class, DefaultDictValidator.class})
@Documented
public @interface DictType {
    String message() default "code %s 不存在";

    String type() default "";

    String sql() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}