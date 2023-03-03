package com.rick.db.plugin.dao.annotation;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-09-27 09:19:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Inherited
public @interface Column {

    String value() default "";

    boolean updatable() default true;

    boolean nullable() default true;

    String comment() default  "";

    String columnDefinition() default "";
}