package com.rick.demo.config;

import com.rick.common.http.exception.ApiExceptionHandler;
import com.rick.common.http.util.MessageUtils;
import com.rick.common.http.web.SharpWebMvcConfigurer;
import com.rick.common.validate.ServiceMethodValidationInterceptor;
import com.rick.db.repository.support.IdToEntityConverterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-10-13 12:17:00
 */
@Configuration
@Import({ApiExceptionHandler.class, MessageUtils.class, ServiceMethodValidationInterceptor.class})
//@EnableResultWrapped
//@ComponentScan(basePackageClasses = MessageUtils.class)
@RequiredArgsConstructor
public class MvcConfig extends SharpWebMvcConfigurer {

//    private final ObjectMapper objectMapper;

    /**
     * 注入，这样才能在@PostConstruct获取 BaseDAOManager
     * @param
     */
//    private final EntityDAOManager entityDAOManager;

    @Override
    public List<ConverterFactory> converterFactories() {
        return Arrays.asList(new IdToEntityConverterFactory());
    }

//    @Override
//    public void addFormatters(FormatterRegistry registry) {
//        // 排在前面优先使用 如果没有找到code仍然会尝试NAME。所以SexEnum可以通过1或者DEFAULT去反序列化
//        registry.addConverterFactory(new CodeToEnumConverterFactory());
//        registry.addConverterFactory(new IdToEntityConverterFactory());
//    }

//    @PostConstruct
//    public void postConstruct() {
//        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addDeserializer(Enum.class, new EnumJsonDeserializer());

        // id 映射到 实体对象
        // 如果 addDeserializer 就会替换原来的 BeanDeserializer，有问题，
        // 手动在bean中添加：@JsonDeserialize(using = EntityIdJsonDeserializer.class)

//        for (Class clazz : EntityDAOManager.baseDAOEntityMap.keySet()) {
//            simpleModule.addDeserializer(clazz, new EntityIdJsonDeserializer(clazz));
//        }

//        simpleModule.addDeserializer(IdCard.class, new EntityIdJsonDeserializer<>(IdCard.class));

////        simpleModule.addDeserializer(Book.class, new EntityIdJsonDeserializer<>(Book.class));

        // 只能分别注册，Book.class 注册会报错，不会使用BeanDeserializer
//        simpleModule.addDeserializer(Person.class, new EntityIdJsonDeserializer<>(Person.class));
//        simpleModule.addDeserializer(Tag.class, new EntityIdJsonDeserializer<>(Tag.class));

//        objectMapper.registerModule(simpleModule);
//    }

}
