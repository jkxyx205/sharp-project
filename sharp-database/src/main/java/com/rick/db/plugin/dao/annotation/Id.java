package com.rick.db.plugin.dao.annotation;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-10-07 07:57:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface Id {
    String value() default "";

    GenerationType strategy() default GenerationType.SEQUENCE;

    enum GenerationType {
        ASSIGN,
        SEQUENCE,
        IDENTITY,
    }
}
