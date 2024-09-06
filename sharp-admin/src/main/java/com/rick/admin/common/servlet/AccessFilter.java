package com.rick.admin.common.servlet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Rick.Xu
 * @date 2024/9/6 15:54
 */
public class AccessFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        //如果是POST走自己的继承的HttpServletRequestWrapper类请求，否则走正常的请求
        if(StringUtils.equalsIgnoreCase(HttpMethod.POST.name(), request.getMethod()) || StringUtils.equalsIgnoreCase(HttpMethod.PUT.name(), request.getMethod())){
            //一定要在判断中new对象，否则还会出现Stream closed问题
            filterChain.doFilter(new AccessRequestWrapper(request),servletResponse);
        }else{
            filterChain.doFilter(servletRequest,servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}