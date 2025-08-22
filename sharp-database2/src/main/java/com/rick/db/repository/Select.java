package com.rick.db.repository;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2023-03-06 13:30:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Transient
public @interface Select {

    String value();

    String params() default "";

    /**
     * 允许某个参数为null的时候， 直接返回结果null，无需数据库查询
     * @return
     */
    String[] nullWhenParamsIsNull() default {};

}
