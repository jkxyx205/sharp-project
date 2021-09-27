package com.rick.db.config.annotation;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-09-27 09:15:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface TableName {
    String value() default "";
}
