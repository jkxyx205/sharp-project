package com.rick.admin.auth.exception;


import org.springframework.security.core.AuthenticationException;

/**
 * @author rick
 */
public class MaxTryLoginException extends AuthenticationException {

    public MaxTryLoginException(String message) {
        super(message);
    }
}
