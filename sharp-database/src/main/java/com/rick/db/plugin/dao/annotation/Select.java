package com.rick.db.plugin.dao.annotation;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-10-31 09:30:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Transient
public @interface Select {

    String table();

    Class subEntityClass() default Void.class;

    String joinValue() default "";

    String referencePropertyName() default "";

    boolean oneToOne() default false;
}
