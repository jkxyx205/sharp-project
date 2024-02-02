package com.rick.admin.common.exception;

import com.rick.common.http.exception.BizException;
import com.rick.common.http.exception.ExceptionCode;
import com.rick.common.http.model.ResultUtils;

/**
 * @author Rick.Xu
 * @date 2023/6/13 12:21
 */
public class ResourceNotFoundException extends BizException {

    public ResourceNotFoundException() {
        super(ExceptionCodeEnum.RESOURCE_NOT_EXISTS_ERROR, new Object[]{"资源"});
    }

    public ResourceNotFoundException(String objectName) {
        super(ExceptionCodeEnum.RESOURCE_NOT_EXISTS_ERROR, new Object[]{objectName});
    }

    public ResourceNotFoundException(ExceptionCode exceptionCode, Object[] params) {
        super(ResultUtils.fail(exceptionCode.getCode(), exceptionCode.getMessage()), params);
    }
}
