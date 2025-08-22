package com.rick.db.repository;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Transient
public @interface OneToMany {

    boolean cascadeDelete() default true;

    boolean cascadeSelect() default true;

    boolean cascadeSave() default true;

    String mappedBy() default "";

    String joinColumnId() default "";

    boolean oneToOne() default false;
}