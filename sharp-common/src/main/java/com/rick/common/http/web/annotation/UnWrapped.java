package com.rick.common.http.web.annotation;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-10-25 13:42:00
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UnWrapped {
}
