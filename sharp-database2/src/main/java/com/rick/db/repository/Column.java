package com.rick.db.repository;

import java.lang.annotation.*;

/**
 * 使用了 columnDefinition ，nullable、comment 失效
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