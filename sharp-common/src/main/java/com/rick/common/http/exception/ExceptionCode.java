package com.rick.common.http.exception;

import com.rick.common.http.model.ResultUtils;

import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2022-03-04 12:10:00
 */
public interface ExceptionCode {

    default void throwException() {
        throw new BizException(getCode(), getMessage());
    }

    default void throwException(Object[] params) {
        throw new BizException(getCode(), String.format(getMessage(), params));
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
        throw new BizException(404, message);
    }

    static <T> void notExists(String message, T data) {
        throw new BizException(404, message, data);
    }

    static void state(boolean expression, String message) {
        if (!expression) {
            throw new BizException(message);
        }
    }

    static <T> void state(boolean expression, String message, T data) {
        if (!expression) {
            throw new BizException(ResultUtils.fail(message, data));
        }
    }

    int getCode();

    String getMessage();
}
