package com.rick.common.http.web.config;

import com.rick.common.http.web.ResultWrappedResponseBodyReturnValueHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-10-25 13:55:00
 */
@Configuration
public class ResultWrappedConfig {

    @Autowired
    public void resultWrappedResponseBodyReturnValueHandler(ApplicationContext applicationContext, RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        HandlerMethodReturnValueHandler resultWrappedResponseBodyReturnValueHandler = new ResultWrappedResponseBodyReturnValueHandler(applicationContext, requestMappingHandlerAdapter);

        List<HandlerMethodReturnValueHandler> defaultHandlerMethodReturnValueHandler = requestMappingHandlerAdapter.getReturnValueHandlers();

        List<HandlerMethodReturnValueHandler> resolvers = new ArrayList<>();
        resolvers.addAll(defaultHandlerMethodReturnValueHandler);
        addBefore(resolvers, resultWrappedResponseBodyReturnValueHandler, RequestResponseBodyMethodProcessor.class);
        requestMappingHandlerAdapter.setReturnValueHandlers(resolvers);
    }

    private void addBefore(List<HandlerMethodReturnValueHandler> resolvers, HandlerMethodReturnValueHandler t, Class<? extends HandlerMethodReturnValueHandler> clazz) {
        for (int i = 0; i < resolvers.size(); i++) {
            if (resolvers.get(i).getClass() == clazz) {
                resolvers.add(i, t);
                return;
            }
        }
    }
}
