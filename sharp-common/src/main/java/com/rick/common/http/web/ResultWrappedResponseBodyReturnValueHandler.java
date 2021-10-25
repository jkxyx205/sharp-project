package com.rick.common.http.web;

import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.http.web.annotation.UnWrapped;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-10-25 13:49:00
 */
@RequiredArgsConstructor
public class ResultWrappedResponseBodyReturnValueHandler implements HandlerMethodReturnValueHandler {

    private HandlerMethodReturnValueHandler handler;

    public ResultWrappedResponseBodyReturnValueHandler(ApplicationContext applicationContext, RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        // 委托RequestResponseBodyMethodProcessor处理
        // converters
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.addAll(requestMappingHandlerAdapter.getMessageConverters());

        // advice
        List<Object> advice = new ArrayList<>();
        List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(applicationContext);
        for (ControllerAdviceBean adviceBean : adviceBeans) {
            Class<?> beanType = adviceBean.getBeanType();
            if (beanType == null) {
                throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
            }
            advice.add(adviceBean);
        }
        handler = new RequestResponseBodyMethodProcessor(converters, advice);
    }

    @Override
    public boolean supportsReturnType(MethodParameter methodParameter) {
        return handler.supportsReturnType(methodParameter);
    }

    @Override
    public void handleReturnValue(Object o, MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest) throws Exception {
        Object wrapperObject = (o instanceof Result || methodParameter.hasMethodAnnotation(UnWrapped.class)) ? o : ResultUtils.success(o);
        handler.handleReturnValue(wrapperObject, methodParameter, modelAndViewContainer, nativeWebRequest);
    }
}
