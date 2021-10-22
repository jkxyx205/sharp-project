package com.rick.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.rick.common.http.convert.CodeToEnumConverterFactory;
import com.rick.common.http.exception.ApiExceptionHandler;
import com.rick.common.http.json.deserializer.EnumJsonDeserializer;
import com.rick.common.http.util.MessageUtils;
import com.rick.common.validate.ServiceMethodValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

/**
 * @author Rick
 * @createdAt 2021-10-13 12:17:00
 */
@Configuration
@Import({ApiExceptionHandler.class, MessageUtils.class, ServiceMethodValidationInterceptor.class})
//@ComponentScan(basePackageClasses = MessageUtils.class)
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 排在前面优先使用 如果没有找到code仍然会尝试NAME。所以SexEnum可以通过1或者DEFAULT去反序列化
        registry.addConverterFactory(new CodeToEnumConverterFactory());
    }
    @PostConstruct
    public void postConstruct() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Enum.class, new EnumJsonDeserializer());
        objectMapper.registerModule(simpleModule);
    }

}
