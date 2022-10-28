package com.rick.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.rick.common.http.convert.CodeToEnumConverterFactory;
import com.rick.common.http.exception.ApiExceptionHandler;
import com.rick.common.http.json.deserializer.EnumJsonDeserializer;
import com.rick.common.http.util.MessageUtils;
import com.rick.common.validate.ServiceMethodValidationInterceptor;
import com.rick.db.plugin.dao.support.IdToEntityConverterFactory;
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

    /**
     * 注入，这样才能在@PostConstruct获取 BaseDAOManager
     * @param registry
     */
//    private final BaseDAOManager baseDAOManager;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 排在前面优先使用 如果没有找到code仍然会尝试NAME。所以SexEnum可以通过1或者DEFAULT去反序列化
        registry.addConverterFactory(new CodeToEnumConverterFactory());
        registry.addConverterFactory(new IdToEntityConverterFactory());
//        registry.addConverterFactory(new IdToEntityConverterFactory());
    }

    @PostConstruct
    public void postConstruct() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Enum.class, new EnumJsonDeserializer());

        // id 映射到 实体对象
        // 如果 addDeserializer 就会替换原来的 BeanDeserializer，有问题
//        for (Class clazz : BaseDAOManager.baseDAOEntityMap.keySet()) {
//            simpleModule.addDeserializer(clazz, new EntityIdJsonDeserializer(clazz));
//        }
//        simpleModule.addDeserializer(IdCard.class, new EntityIdJsonDeserializer<>(IdCard.class));

        objectMapper.registerModule(simpleModule);
    }

}
