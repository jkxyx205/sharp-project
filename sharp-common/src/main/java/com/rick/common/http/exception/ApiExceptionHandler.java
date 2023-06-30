package com.rick.common.http.exception;

import com.google.common.collect.Maps;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultCode;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.http.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 12/16/19 6:59 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    /**
     * 业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler(BizException.class)
    public Result exceptionHandler(HttpServletRequest request, HttpServletResponse response, BizException ex) throws IOException, ServletException {
        Result result = ex.getResult();
        // resolve i18n
        String message = MessageUtils.getMessage(result.getMessage(), ex.getParams());
        result.setMessage(message);

        if (log.isErrorEnabled()) {
            log.error(result.getCode() + "," + message, ex.getCause());
            this.logStackTrace(ex);
        }

        if (HttpServletRequestUtils.isAjaxRequest(request)) {
            response.setStatus(result.getCode());
            return result;
        }

        request.setAttribute("result", result);
        request.setAttribute("message", message);
        request.getRequestDispatcher("/error/index").forward(request, response);
        return null;
    }

    /**
     * 未授权异常
     * @param ex
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result accessDeniedHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException, ServletException {
        return exceptionHandler(request, response, ex, ResultCode.ACCESS_FORBIDDEN_ERROR);
    }

    /**
     * 验证失败
     * @param request
     * @param response
     * @param ex
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @ExceptionHandler({BindException.class, ConstraintViolationException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result methodArgumentNotValidExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException, ServletException {
        if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) ex;
            return exceptionHandler(request, response, ex, ResultCode.ARGUMENT_NOT_VALID, formatErrors(cve.getConstraintViolations()));
        } else if (ex instanceof BindException) {
            BindException be = (BindException) ex;
            return exceptionHandler(request, response, ex, ResultCode.ARGUMENT_NOT_VALID, formatErrors(be.getAllErrors()));
        } else if (ex instanceof IllegalArgumentException) {
            return exceptionHandler(request, response, ex, ResultCode.ARGUMENT_NOT_VALID, ex.getMessage());
        }

        MethodArgumentNotValidException me = (MethodArgumentNotValidException) ex;
        return exceptionHandler(request, response, ex, ResultCode.ARGUMENT_NOT_VALID, formatErrors(me.getBindingResult().getAllErrors()));
    }

    /**
     * 未知异常
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result elseExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException, ServletException {
        return exceptionHandler(request, response, ex, ResultCode.ERROR);
    }

    private Result exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex, ResultCode resultCode) throws ServletException, IOException {
        return exceptionHandler(request, response, ex, resultCode, null);
    }

    private Result exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex, ResultCode resultCode, Object data) throws ServletException, IOException {
        return exceptionHandler(request, response, ex, resultCode.getCode(), resultCode.getMessage(), data);
    }

    private Result exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex, int code, String message, Object data) throws ServletException, IOException {
        if (log.isErrorEnabled()) {
            this.logStackTrace(ex);
        }

        response.setStatus(code);

        if (HttpServletRequestUtils.isAjaxRequest(request)) {
            return ResultUtils.fail(code, message, data);
        }
        request.getRequestDispatcher("/error").forward(request, response);
        return null;
    }

    private void logStackTrace(Exception  ex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ex.printStackTrace(new PrintStream(baos));
        String exception = baos.toString();
        if (log.isErrorEnabled()) {
            log.error(exception);
        }
    }

    private List<Map<String, Object>> formatErrors(List<ObjectError> objectErrors) {
        return objectErrors.stream().map(objectError -> {
                    FieldError fieldError = (FieldError) objectError;
                    Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
                    params.put("field", fieldError.getField());
                    params.put("message", objectError.getDefaultMessage());
                    params.put("rejectedValue", ((FieldError) objectError).getRejectedValue());
                    return params;
                }
           ).collect(Collectors.toList());
    }

    private List<Map<String, Object>> formatErrors(Set<ConstraintViolation<?>> constraintViolationSet) {
        return constraintViolationSet.stream().map(constraintViolation -> {
            Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
            params.put("field", StringUtils.substringAfterLast("."+ constraintViolation.getPropertyPath().toString(), "."));
            params.put("message", constraintViolation.getMessage());
            params.put("rejectedValue", constraintViolation.getInvalidValue());
            return params;
        }).collect(Collectors.toList());
    }
}
