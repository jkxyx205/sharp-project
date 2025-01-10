package com.rick.common.http.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.rick.common.http.convert.CodeToEnumConverterFactory;
import com.rick.common.http.web.param.ParamNameProcessor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2023-03-15 14:14:00
 */
public class SharpWebMvcConfigurer implements WebMvcConfigurer {

    @Resource
    private JacksonProperties jacksonProperties;

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
        // 子类实现
        return null;
    }

    @Autowired(required = false)
    public void register(ObjectMapper objectMapper) {
        if (objectMapper != null) {
            // jackson 自带的 EnumDeserializer 代替 EnumJsonDeserializer
            String pattern = jacksonProperties.getDateFormat() == null ? "yyyy-MM-dd HH:mm:ss" : jacksonProperties.getDateFormat();
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
            simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(pattern)));
            simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//            simpleModule.addDeserializer(Enum.class, new EnumJsonDeserializer());
            objectMapper.registerModule(simpleModule);
            objectMapper.setDateFormat(new SimpleDateFormat(pattern));
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

//            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        }
    }

    @Bean
    protected ParamNameProcessor paramNameProcessor() {
        return new ParamNameProcessor();
    }

    @Bean
    public BeanPostProcessor beanPostProcessor(ParamNameProcessor paramNameProcessor) {
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
                    argumentResolvers.add(0, paramNameProcessor);
                    adapter.setArgumentResolvers(argumentResolvers);
                }
                return bean;
            }
        };
    }
}
