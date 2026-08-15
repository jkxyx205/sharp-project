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

    Class<?> entityClass() default Void.class;

    String params() default "";

    /**
     * 允许某个参数为null的时候， 直接返回结果null，无需数据库查询
     * update：直接某个参数为null的时候， 直接返回结果null，不需要配置该属性
     * @return
     */
    @Deprecated
    String[] nullWhenParamsIsNull() default {};

    /**
     * 级联查找
     * @return
     */
    boolean cascadeSelect() default true;

}
