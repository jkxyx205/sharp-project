package com.rick.common.http.exception;

import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import lombok.Getter;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 12/16/19 7:04 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Getter
public class BizException extends RuntimeException {

    private Result result;

    private Object[] params;

    public BizException(String msg) {
        this(ResultUtils.fail(msg));
    }

    public BizException(int code, String msg) {
        this(ResultUtils.fail(code, msg));
    }

    public <T> BizException(int code, String msg, T data) {
        this(ResultUtils.fail(code, msg, data));
    }

    public BizException(ExceptionCode exceptionCode) {
        this(ResultUtils.fail(exceptionCode.getCode(), exceptionCode.getMessage()));
    }

    public BizException(ExceptionCode exceptionCode, Object[] params) {
        this(ResultUtils.fail(exceptionCode.getCode(), exceptionCode.getMessage()), params);
    }

    public BizException(Result result) {
        super(result.getMsg());
        this.result = result;
    }

    public BizException(Result result, Object[] params) {
        super(String.format(result.getMsg(), params));
        result.setMsg(getMessage());
        this.result = result;
        this.params = params;
    }

    public BizException(Result result , Throwable t) {
        super(result.getMsg(), t);
        this.result = result;
    }

    public BizException(Result result, Object[] params, Throwable t) {
        super(String.format(result.getMsg(), params), t);
        result.setMsg(getMessage());
        this.params = params;
        this.result = result;
    }
}
