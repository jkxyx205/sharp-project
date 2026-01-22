package com.rick.admin.auth.common;

import com.rick.admin.auth.authentication.AdminUserDetails;
import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.sys.user.entity.User;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.SQLUtils;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author rick
 */
@Slf4j
public class UrlHandlerInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) {
        UserContextHolder.remove();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) {
        Object principal = request.getUserPrincipal();
        String username = "anon";
        String name = "anon";
        AdminUserDetails userDetails;
        if (Objects.nonNull(principal)) {
            userDetails = (AdminUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            User user = userDetails.getUser();
            UserContextHolder.set(user);
            username = user.getCode();
            name = user.getName();
        }

        Device device = DeviceUtils.getCurrentDevice(request);
        if (Objects.nonNull(device)) {
            request.setAttribute("device", device);
        }

        writeAccessInfo(request, username, name);

        String servletPath = request.getServletPath();

        if (HttpServletRequestUtils.isAjaxRequest(request) && "/login".equals(servletPath)) {
            if (HttpServletRequestUtils.isAjaxRequest(request)) {
                throw new BizException(ExceptionCodeEnum.INVALID_SESSION);
            }
        }

        return true;
    }

    /**
     * 实现参考： https://www.cnblogs.com/leilcoding/p/15131470.html
     * @param request
     * @param username
     * @param name
     */
    private void writeAccessInfo(HttpServletRequest request, String username, String name) {
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("user-agent"));
        //客户端类型
        String clientType = userAgent.getOperatingSystem().getDeviceType().getName();
        //客户端操作系统类型
        String osType = userAgent.getOperatingSystem().getName();
        //客户端ip
        String clientIp = HttpServletRequestUtils.getClientIpAddress(request);
        //客户端port
        int clientPort = request.getRemotePort();
        //请求方式
        String requestMethod = request.getMethod();
        //客户端请求URI
        String requestURI = request.getRequestURI();
        //客户端请求参数值
        String requestParam = "";

        if (request.getRequestURI().matches(".*[.](js|css|png|jpeg|jpg)") || "anon".equals(username) ||
                request.getRequestURI().equals("/") ||
                request.getRequestURI().endsWith("/error") ||
                request.getRequestURI().endsWith("/forbidden") ||
                request.getRequestURI().endsWith("/password") ||
                request.getRequestURI().endsWith("/kaptcha") ||
                request.getRequestURI().endsWith("/login") ||
                request.getRequestURI().endsWith("/logs") ||
                request.getRequestURI().endsWith("/logs/api")) {
            return;
        }

        //如果请求是POST获取body字符串，否则GET的话用request.getQueryString()获取参数值
        if (StringUtils.equalsIgnoreCase(HttpMethod.POST.name(), requestMethod) || StringUtils.equalsIgnoreCase(HttpMethod.PUT.name(), requestMethod)) {
            requestParam = HttpServletRequestUtils.getBodyString(request);
        } else {
            requestParam = ObjectUtils.defaultIfNull(request.getQueryString(), "");
        }

        //客户端整体请求信息
        StringBuilder clientInfo = new StringBuilder();
        clientInfo.append("用户信息:[username:").append(username)
                .append(", name:").append(name)
                .append("]")
                .append(", 客户端信息:[类型:").append(clientType)
                .append(", 操作系统类型:").append(osType)
                .append(", ip:").append(clientIp)
                .append(", port:").append(clientPort)
                .append(", 请求方式:").append(requestMethod)
                .append(", URI:").append(requestURI)
                .append(", 请求参数值:").append(requestParam.replaceAll("\\s*", ""))
                .append("]");

        //***这里的clientInfo就是所有信息了，请根据自己的日志框架进行收集***
        log.info(clientInfo.toString());

        SQLUtils.insert("sys_access_info", "content, create_time", new Object[] {clientInfo.toString(), LocalDateTime.now()});
    }
}
