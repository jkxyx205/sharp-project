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

    static void isNull(Object obj, String name) {
        if (Objects.nonNull(obj)) {
            throw new BizException(new ExceptionResult<String>(400, String.format("%s需要为空", new String[]{name})));
        }
    }

    static void notNull(Object obj, String name) {
        if (Objects.isNull(obj)) {
            throw new BizException(new ExceptionResult<String>(400, String.format("%s不能为空", new String[]{name})));
        }
    }

    static void notExists(String name) {
        throw new BizException(new ExceptionResult<String>(404, String.format("%s不存在", new String[]{name})));
    }

    int getCode();

    String getMsg();
}
