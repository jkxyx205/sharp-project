package com.rick.admin.auth.common;

import com.rick.admin.auth.authentication.AdminUserDetails;
import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.sys.user.entity.User;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        String name = "anno";
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

        String params = "";

        if (!request.getRequestURI().startsWith("/password")) {
            params = HttpServletRequestUtils.getParameterMap(request).toString();
        }

        log.info("VISIT: 用户{}-{}访问地址{}, method={}, ip={}, 设备类型={}, 参数={}", username, name,  request.getRequestURI()
                , request.getMethod()
                , HttpServletRequestUtils.getClientIpAddress(request)
                , device
                , " params => " + params);

        String servletPath = request.getServletPath();

        if (HttpServletRequestUtils.isAjaxRequest(request) && "/login".equals(servletPath)) {
            if (HttpServletRequestUtils.isAjaxRequest(request)) {
                throw new BizException(ExceptionCodeEnum.INVALID_SESSION);
            }
        }

        return true;
    }

}
