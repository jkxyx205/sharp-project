package com.rick.common.http.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.rick.common.http.convert.CodeToEnumConverterFactory;
import com.rick.common.http.web.param.ParamNameProcessor;
import com.rick.common.util.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rick
 * @createdAt 2023-03-15 14:14:00
 */
public class SharpWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 排在前面优先使用 如果没有找到code仍然会尝试NAME。所以SexEnum可以通过1或者DEFAULT去反序列化
        registry.addConverterFactory(new CodeToEnumConverterFactory());
        List<ConverterFactory> converterFactories = converterFactories();
        if (CollectionUtils.isNotEmpty(converterFactories)) {
            for (ConverterFactory converterFactory : converterFactories) {
                registry.addConverterFactory(converterFactory);
            }
        }
    }

    public List<ConverterFactory> converterFactories() {
        return null;
    }

    @Autowired(required = false)
    public void register(ObjectMapper objectMapper) {
        if (objectMapper != null) {
            // jackson 自带的 EnumDeserializer 代替 EnumJsonDeserializer
//            SimpleModule simpleModule = new SimpleModule();
//            simpleModule.addDeserializer(Enum.class, new EnumJsonDeserializer());
//            objectMapper.registerModule(simpleModule);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        }
    }

    @Bean
    protected ParamNameProcessor paramNameProcessor() {
        return new ParamNameProcessor();
    }

    @Bean
    public BeanPostProcessor beanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof RequestMappingHandlerAdapter) {
                    RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) bean;
                    List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(adapter.getArgumentResolvers());
                    argumentResolvers.add(0, paramNameProcessor());
                    adapter.setArgumentResolvers(argumentResolvers);
                }
                return bean;
            }
        };
    }

    @Bean
    public Filter snakeConverter() {
        return new OncePerRequestFilter() {

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                final Map<String, String[]> formattedParams = new ConcurrentHashMap<>();

                for (String param : request.getParameterMap().keySet()) {
                    String formattedParam = StringUtils.stringToCamel(param);
                    formattedParams.put(formattedParam, request.getParameterValues(param));
                }

                filterChain.doFilter(new HttpServletRequestWrapper(request) {
                    @Override
                    public String getParameter(String name) {
                        return formattedParams.containsKey(name) ? formattedParams.get(name)[0] : null;
                    }

                    @Override
                    public Enumeration<String> getParameterNames() {
                        return Collections.enumeration(formattedParams.keySet());
                    }

                    @Override
                    public String[] getParameterValues(String name) {
                        return formattedParams.get(name);
                    }

                    @Override
                    public Map<String, String[]> getParameterMap() {
                        return formattedParams;
                    }
                }, response);
            }
        };
    }


}
