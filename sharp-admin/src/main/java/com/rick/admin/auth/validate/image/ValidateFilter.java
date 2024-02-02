package com.rick.admin.auth.validate.image;

import com.rick.admin.auth.exception.ValidateCodeException;
import com.rick.admin.common.AdminConstants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author rick
 */
@Component("validateFilter")
@RequiredArgsConstructor
public class ValidateFilter extends OncePerRequestFilter implements InitializingBean {

    private final AuthenticationFailureHandler authenticationFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String uiCode = request.getParameter("code");
        CaptchaImageVO captchaImageVO = (CaptchaImageVO) request.getSession().getAttribute(AdminConstants.KAPTCHA_SESSION_KEY);

        if ("/login".equals(request.getServletPath()) && request.getMethod().equals("POST")) {
            if (Objects.nonNull(captchaImageVO) && (captchaImageVO.isExpired() || !StringUtils.equals(uiCode, captchaImageVO.getCode()))) {
                authenticationFailureHandler.onAuthenticationFailure(request, response, new ValidateCodeException());
                return;
            } else {
                request.getSession().removeAttribute(AdminConstants.KAPTCHA_SESSION_KEY);
            }
        }

        // 别忘了这个..
        filterChain.doFilter(request, response);
    }
}
