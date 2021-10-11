package com.rick.common.http.exception;

import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultCode;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.http.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.AccessDeniedException;

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
        String message = MessageUtils.getMessage(result.getMsg(), ex.getParams());
        result.setMsg(message);

        if (log.isErrorEnabled()) {
            log.error(result.getCode() + "," + message, ex.getCause());
            this.logStackTrace(ex);
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
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result methodArgumentNotValidExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException, ServletException {
        return exceptionHandler(request, response, ex, ResultCode.ARGUMENT_NOT_VALID);
    }

    /**
     * 未知异常
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus
    public Result elseExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException, ServletException {
        return exceptionHandler(request, response, ex, ResultCode.FAIL);
    }

    private Result exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex, ResultCode resultCode) throws ServletException, IOException {
        if (log.isErrorEnabled()) {
            this.logStackTrace(ex);
        }

        if (HttpServletRequestUtils.isAjaxRequest(request)) {
            return ResultUtils.exception(resultCode.getCode(), resultCode.getMsg());
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
}
