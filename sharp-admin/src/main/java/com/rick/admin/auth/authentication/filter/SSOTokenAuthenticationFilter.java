package com.rick.admin.auth.authentication.filter;

import com.rick.admin.auth.authentication.AdminUserDetails;
import com.rick.admin.sys.user.dao.UserDAO;
import com.rick.admin.sys.user.entity.User;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * 单点登录 Filter
 */
public class SSOTokenAuthenticationFilter extends OncePerRequestFilter {

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
            SecurityContext securityContext = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
            if (Objects.isNull(securityContext)) {
                // 根据 accessToken 解析用户信息和权限信息 构造 authentication
                String code = "";
                UserDAO userDAO = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext()).getBean(UserDAO.class);
                User user = userDAO.selectByCode(code).get();
                user.setPassword(accessToken);
                AdminUserDetails userDetails = new AdminUserDetails(user, AuthorityUtils.createAuthorityList("ROLE_STUDENT"));
                Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
                request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            HttpServletResponseUtils.write(response, "application/json;charset=UTF-8"
                    , JsonUtils.toJson(ResultUtils.fail(HttpStatus.FORBIDDEN.value(), e.getMessage())));
            e.printStackTrace();
        }
    }
}