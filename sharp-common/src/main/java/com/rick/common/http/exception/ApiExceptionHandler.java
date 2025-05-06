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
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
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
     * @param e
     * @return
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Result exceptionHandler(HttpServletRequest request, HttpServletResponse response, BizException e) throws IOException, ServletException {
        Result result = e.getResult();
        // resolve i18n
        String message = MessageUtils.getMessage(result.getMessage(), e.getParams());
        result.setMessage(message);

        if (log.isErrorEnabled()) {
            log.error(e.getMessage(), e);
        }

        if (HttpServletRequestUtils.isAjaxRequest(request)) {
            return result;
        }

        request.setAttribute("result", result);
        request.setAttribute("message", message);
        request.getRequestDispatcher("/error/index").forward(request, response);
        return null;
    }

    /**
     * 文件上传超过最大限制
     * @param e
     * @return
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Result maxUploadSizeExceededExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException, ServletException {
        return exceptionHandler(request, response, e, 5001, "文件大小不能超出5M", null);
    }


    /**
     * 未授权异常
     * @param e
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result accessDeniedHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException, ServletException {
        return exceptionHandler(request, response, e, ResultCode.ACCESS_FORBIDDEN_ERROR);
    }

    /**
     * 验证失败
     * @param request
     * @param response
     * @param e
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @ExceptionHandler({BindException.class, ConstraintViolationException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result methodArgumentNotValidExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException, ServletException {
        if (e instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) e;
            return exceptionHandler(request, response, e, ResultCode.ARGUMENT_NOT_VALID, formatErrors(cve.getConstraintViolations()));
        } else if (e instanceof BindException) {
            BindException be = (BindException) e;
            return exceptionHandler(request, response, e, ResultCode.ARGUMENT_NOT_VALID, formatErrors(be.getAllErrors()));
        } else if (e instanceof IllegalArgumentException) {
            return exceptionHandler(request, response, e, ResultCode.ARGUMENT_NOT_VALID, e.getMessage());
        }

        MethodArgumentNotValidException me = (MethodArgumentNotValidException) e;
        return exceptionHandler(request, response, e, ResultCode.ARGUMENT_NOT_VALID, formatErrors(me.getBindingResult().getAllErrors()));
    }

    /**
     * 未知异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result elseExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException, ServletException {
        return exceptionHandler(request, response, e, ResultCode.ERROR);
    }

    private Result exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e, ResultCode resultCode) throws ServletException, IOException {
        return exceptionHandler(request, response, e, resultCode, null);
    }

    private Result exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e, ResultCode resultCode, Object data) throws ServletException, IOException {
        return exceptionHandler(request, response, e, resultCode.getCode(), resultCode.getMessage(), data);
    }

    private Result exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e, int code, String message, Object data) throws ServletException, IOException {
        if (log.isErrorEnabled()) {
            log.error(e.getMessage(), e);
        }

        if (HttpServletRequestUtils.isAjaxRequest(request) || request.getRequestURI().equals("/documents/upload")) {
            return ResultUtils.fail(code, message, data);
        }

        request.setAttribute("javax.servlet.error.status_code", code);
        request.getRequestDispatcher("/error").forward(request, response);
        return null;
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
