package com.rick.meta.config.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = DictValidator.class)
@Documented
public @interface DictValueCheck {

    String type();

    String message() default "请选择合适的值";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}