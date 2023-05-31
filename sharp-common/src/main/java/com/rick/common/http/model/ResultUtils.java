package com.rick.common.http.model;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/10/19 1:57 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
public final class ResultUtils {

    public static Result success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result(true, ResultCode.OK.getCode(), ResultCode.OK.getMessage(), data);
    }

    public static Result fail() {
        return fail(null);
    }

    public static <T> Result<T> fail(T data) {
        return new Result(false, ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage(), data);
    }

    public static Result fail(String message) {
        return fail(ResultCode.ERROR.getCode(), message, null);
    }

    public static Result fail(int code, String message) {
        return fail(code, message, null);
    }

    public static <T> Result fail(String message, T data) {
        return fail(ResultCode.ERROR.getCode(), message, data);
    }

    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result(false, code, message, data);
    }

}