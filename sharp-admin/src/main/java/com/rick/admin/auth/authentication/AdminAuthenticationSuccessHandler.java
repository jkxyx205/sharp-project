package com.rick.admin.auth.authentication;


import com.rick.admin.auth.common.AuthConstants;
import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.sys.user.entity.User;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.LiteDeviceResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @author rick
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CacheManager cacheManager;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        Cache<String, Integer> tryCache = cacheManager.getCache("loginMaxTry");

        tryCache.remove(authentication.getName().toUpperCase());
        request.getSession().removeAttribute(AuthConstants.IMAGE_CODE_SESSION_KEY);

        AdminUserDetails userDetails = ((AdminUserDetails) authentication.getPrincipal());
        User user = userDetails.getUser();
        user.setImgName(StringUtils.generateImgName(user.getName()));
        request.getSession().setAttribute("user", user);

        // 管理员放行
        LiteDeviceResolver liteDeviceResolver = new LiteDeviceResolver();
        Device device = liteDeviceResolver.resolveDevice(request);

        log.info("LOGIN:用户{}进入系统,ip:{},device={}", userDetails.getUsername(), HttpServletRequestUtils.getClientIpAddress(request), device.getDevicePlatform());

        String url = "/";
        UserContextHolder.set(userDetails.getUser());
        response.sendRedirect(url);
    }
}
