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
    boolean updatable() default false;

    String parentTable();

    boolean cascadeInsertOrUpdate() default false;

    /**
     * 只做插入操作，不做更新操作。使用场景如，批次物料库存移动的时候，如果是新的批次，则添加，否则不做更新
     * @return
     */
    boolean cascadeInsert() default false;

    String comment() default  "";

}
