package com.rick.admin.auth.exception;

import com.rick.admin.common.exception.ExceptionCodeEnum;
import org.springframework.security.core.AuthenticationException;

/**
 * @author rick
 */
public class ValidateCodeException extends AuthenticationException {

    public ValidateCodeException() {
        super(ExceptionCodeEnum.VALIDATE_CODE_ERROR.getMessage());
    }
}
