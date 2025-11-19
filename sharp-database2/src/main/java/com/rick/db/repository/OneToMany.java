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

    /**
     * 当 cascadeSave = true 的时候，是否执行删除操作， cascadeSaveNotDelete 只会在 cascadeSave = true 生效
     * @return
     */
    boolean cascadeSaveItemDelete() default true;

    /**
     * 删除前检查callback
     * 当 cascadeSaveItemDelete = true 的时候，是否执行删除前检查操作， cascadeSaveItemDeleteCheck 只会在 cascadeSaveItemDelete = true 生效
     * @return
     */
    boolean cascadeSaveItemDeleteCheck() default false;

    String mappedBy() default "";

    String joinColumnId() default "";

    boolean oneToOne() default false;
}