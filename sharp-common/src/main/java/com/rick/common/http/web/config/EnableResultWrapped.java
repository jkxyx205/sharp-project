package com.rick.common.http.web.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Rick
 * @createdAt 2021-10-25 13:42:00
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(ResultWrappedConfig.class)
@Documented
public @interface EnableResultWrapped {
}
