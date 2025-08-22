package com.rick.db.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rick
 * @createdAt 2023-03-06 13:30:00
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Transient
public @interface Embedded {

    String columnPrefix() default "";

    String comment() default "";
}
