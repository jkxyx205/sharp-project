package com.rick.admin.auth.authentication.filter;

import com.rick.admin.auth.common.JWTUtils;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TokenAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String accessToken;
        if (header == null || !header.startsWith("Bearer ")) {
            accessToken = request.getParameter("access_token");
        } else {
            accessToken = header.substring(7);
        }

        if (StringUtils.isBlank(accessToken)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = JWTUtils.toAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            HttpServletResponseUtils.write(response, "application/json;charset=UTF-8"
                    , JsonUtils.toJson(ResultUtils.fail(HttpStatus.FORBIDDEN.value(), e.getMessage())));
            e.printStackTrace();
        }
    }
}