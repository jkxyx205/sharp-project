package com.rick.admin.auth.authentication;

import com.rick.admin.auth.common.AuthConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author rick
 */
@Component
@RequiredArgsConstructor
public class AdminAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final CacheManager cacheManager;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        Cache<String, Integer> tryCache = cacheManager.getCache("loginMaxTry");

        Integer loginMaxTryCountInCache = tryCache.get(request.getParameter("username").toUpperCase());

        if (Objects.nonNull(loginMaxTryCountInCache)) {
            // 验证码失败，还没有用户名
            if (loginMaxTryCountInCache > AuthConstants.MAX_TRY_IMAGE_CODE_COUNT) {
                request.getSession().setAttribute(AuthConstants.IMAGE_CODE_SESSION_KEY, true);
            }
        }

        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, e);
//        request.getRequestDispatcher("/login").forward(request, response); //ERROR c.i.ac.exception.AcExceptionHandler - org.springframework.web.HttpRequestMethodNotSupportedException: Request method 'POST' not supported

        response.sendRedirect("/login");
    }
}
