package com.rick.db.config.annotation;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-10-07 07:57:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface Id {
    String value() default "";
}
