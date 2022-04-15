package com.rick.common.http.exception;

import com.rick.common.http.model.ExceptionResult;

import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2022-03-04 12:10:00
 */
public interface Assert {

    default ExceptionResult<String> result() {
        return new ExceptionResult<>(getCode(), getMsg());
    }

    default ExceptionResult<String> result(Object[] params) {
        return new ExceptionResult<>(getCode(), String.format(getMsg(), params));
    }

    default void exception() {
        throw new BizException(new ExceptionResult<String>(getCode(), getMsg()));
    }

    default void exception(Object[] params) {
        throw new BizException(new ExceptionResult<String>(getCode(), String.format(getMsg(), params)));
    }

    static void isNull(Object obj, String message) {
        if (Objects.nonNull(obj)) {
            throw new IllegalArgumentException(message);
        }
    }

    static void notNull(Object obj, String message) {
        if (Objects.isNull(obj)) {
            throw new IllegalArgumentException(message);
        }
    }

    static void notExists(String message) {
        throw new BizException(new ExceptionResult<String>(404, message));
    }

    static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    int getCode();

    String getMsg();
}
