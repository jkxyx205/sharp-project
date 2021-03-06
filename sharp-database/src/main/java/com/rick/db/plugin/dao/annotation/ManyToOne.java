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
@Column
public @interface ManyToOne {

    @AliasFor(annotation = Column.class)
    String value() default "";

    @AliasFor(annotation = Column.class)
    boolean updatable() default true;

    String parentTable();

    boolean cascadeSaveOrUpdate() default false;

    String comment() default  "";

}
