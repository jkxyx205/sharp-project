package com.rick.db.repository;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Transient
public @interface ManyToMany {

    boolean cascadeDelete() default true;

    boolean cascadeSelect() default true;

    boolean cascadeSave() default false;

    /**
     * cascadeSave = true,需要执行 mappedBy
     * @return
     */
    String mappedBy() default "";

    String tableName();

    String joinColumnId();

    String inverseJoinColumnId();
}