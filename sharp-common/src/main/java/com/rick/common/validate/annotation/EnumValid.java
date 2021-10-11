package com.rick.common.validate.annotation;

import com.rick.common.validate.EnumValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-10-11 17:58:00
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {EnumValidator.class})
public @interface EnumValid {

    String message() default "枚举值不存在值${validatedValue}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * 目标枚举类
     */
    Class<?> target() default Object.class;
}