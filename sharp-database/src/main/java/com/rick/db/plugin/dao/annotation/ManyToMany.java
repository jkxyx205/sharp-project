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
public @interface ManyToMany {

    String thirdPartyTable();

    String referenceTable();

    String referenceColumnName();

    String columnDefinition();

    String sortColumnName() default "";

    boolean cascadeQuery() default true;
}
