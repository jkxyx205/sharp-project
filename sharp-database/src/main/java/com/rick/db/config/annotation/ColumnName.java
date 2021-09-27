package com.rick.db.config.annotation;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-09-27 09:19:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface ColumnName {
    String value() default "";
}