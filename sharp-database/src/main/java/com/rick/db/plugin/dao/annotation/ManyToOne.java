package com.rick.db.plugin.dao.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-10-31 09:30:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ColumnName
public @interface ManyToOne {

    @AliasFor(annotation = ColumnName.class)
    String value() default "";

    String parentTable();

}
