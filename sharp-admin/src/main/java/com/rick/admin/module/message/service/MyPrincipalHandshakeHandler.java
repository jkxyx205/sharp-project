package com.rick.admin.module.message.service;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * 发给特定用户
 * @author Rick.Xu
 * @date 2024/9/20 16:29
 */
@Component
public class MyPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {

//        HttpSession httpSession = getSession(request);
//        final String userId = (String)httpSession.getAttribute("userId");

//        ServletRequestUtils.getIntParameter(request, "userId");
//
//        ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
//
//        String accessToken = serverRequest.getServletRequest().getParameter("access_token");

//        System.out.printf("sessionId: " + ((ServletServerHttpRequest) request).getServletRequest().getSession().getId());

        final String userId = ((ServletServerHttpRequest) request).getServletRequest().getSession().getAttribute("user").toString();

        return new Principal() {
            @Override
            public boolean equals(Object another) {
                return this.equals(another);
            }

            @Override
            public String toString() {
                return userId;
            }

            @Override
            public int hashCode() {
                return userId.hashCode();
            }

            @Override
            public String getName() {
                return userId;
            }
        };
    }
}
