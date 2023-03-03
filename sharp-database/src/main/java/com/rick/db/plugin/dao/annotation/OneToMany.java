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

    boolean cascadeDelete() default true;

    String joinValue();

    boolean oneToOne() default false;

    /**
     * 仅插入，场景物料凭证修改的时候，不会更新item数据
     * @return
     */
    boolean cascadeSave() default false;
}
