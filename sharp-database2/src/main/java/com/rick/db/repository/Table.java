package com.rick.db.repository;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-09-27 09:15:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Table {
    String value() default "";
    String comment() default  "";
    /**
     * 默认是类名小写第一个字母 + "_id"
     * @return
     */
    String referenceColumnId() default "";
}
