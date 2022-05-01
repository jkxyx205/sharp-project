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
public @interface OneToMany {

    String subTable();

    /**
     * 级联操作的时候需要该值
     * @return
     */
    String reversePropertyName() default "";

    boolean cascadeSaveOrUpdate() default false;

    String joinValue();

    boolean oneToOne() default false;
}
