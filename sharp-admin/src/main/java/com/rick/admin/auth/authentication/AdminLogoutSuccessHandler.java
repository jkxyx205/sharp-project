package com.rick.admin.auth.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author rick
 */
@Component
@Slf4j
public class AdminLogoutSuccessHandler implements LogoutSuccessHandler {

    @Resource
    private SessionRegistry sessionRegistry;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        AdminUserDetails userDetails = ((AdminUserDetails) authentication.getPrincipal());
        log.info("LOGOUT:用户{}退出系统", userDetails.getUsername());
        expireSession(request, userDetails);
        response.sendRedirect("/login");
    }

    private  void expireSession(HttpServletRequest request, AdminUserDetails user) {
        List<SessionInformation> sessionsInfo = null;
        if (null != user) {
            List<Object> o = sessionRegistry.getAllPrincipals();
            for (Object principal : o) {
                if ((user.getUsername().equals(((AdminUserDetails) principal).getUsername()))) {
                    sessionsInfo = sessionRegistry.getAllSessions(principal, false);
                }
            }
        } else if (null != request) {
            SecurityContext sc = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
            if (null != sc.getAuthentication().getPrincipal()) {
                sessionsInfo = sessionRegistry.getAllSessions(sc.getAuthentication().getPrincipal(), false);
                sc.setAuthentication(null);
            }
        }
        if (null != sessionsInfo && sessionsInfo.size() > 0) {
            for (SessionInformation sessionInformation : sessionsInfo) {
                //当前session失效
                sessionInformation.expireNow();
                sessionRegistry.removeSessionInformation(sessionInformation.getSessionId());
            }
        }
    }

}
