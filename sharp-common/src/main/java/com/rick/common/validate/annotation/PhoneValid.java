package com.rick.common.validate.annotation;



import com.rick.common.validate.PhoneValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rick
 * @version 1.0.0
 * @Description
 * @createTime 2021-01-05 19:42:00
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {PhoneValidator.class})
public @interface PhoneValid {

    String message() default "手机号码格式不正确";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
