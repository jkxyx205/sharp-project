package com.rick.common.http.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.google.common.collect.Lists;
import com.rick.common.util.ClassUtils;
import com.rick.common.util.JsonUtils;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EntityWithCodePropertyDeserializer: 反序列号到属性字符串 code 上
 * EntityWithLongIdPropertyDeserializer 反序列号到属性Long id 上
 *
 * NamePropertyDeserializer: 类型可以是字符串或者Long类型
 * 1.如果反序列化Bean对象：
 *         @JsonAlias("businessPartnerCode")
 *         @JsonDeserialize(using = NamePropertyDeserializer.class)
 *         private CodeValue businessPartner;
 *  这样就能监测到反序列化到属性 code 上
 *
 *  如果反序列化Bean集合对象
 *         @JsonAlias({"businessPartnerCodes", "businessPartnerCodeList"})
 *         @JsonDeserialize(using = NamePropertyDeserializer.class)
 *         private List<CodeValue> businessPartnerList;
 *         // or
 *         @JsonAlias({"businessPartnerCodes", "businessPartnerCodeList"})
 *  *      @JsonDeserialize(using = NamePropertyDeserializer.class)
 *         private List<CodeValue> businessPartners;
 *
 *  这样就能监测到反序列化到属性 code 上
 *
 *
 * @author Rick
 * @createdAt 2022-10-27 14:14:00
 */
@NoArgsConstructor
public class NamePropertyDeserializer<T> extends JsonDeserializer<T> implements ContextualDeserializer {

    private static final Pattern PROPERTY_NAME_PATTERN = Pattern.compile("^(?i)([a-zA-Z]+)(s|List)$");

    private JavaType javaType;

    private String propertyName;

    public NamePropertyDeserializer(JavaType javaType, String propertyName) {
        this.javaType = javaType;
        this.propertyName = propertyName;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String currentName = jsonParser.getParsingContext().getCurrentName();
        Class deserializePropertyClass;

        if (List.class.isAssignableFrom(this.javaType.getRawClass())) {
            if (node.size() == 0) {
                return (T) Collections.emptyList();
            }

            Class contentClass = javaType.getContentType().getRawClass();

            Matcher propertyNameMatcher = PROPERTY_NAME_PATTERN.matcher(this.propertyName);

            String propertyName = null;
            if (propertyNameMatcher.find()) {
                propertyName = propertyNameMatcher.group(1);
            }

            Pattern aliasPattern = Pattern.compile("^"+propertyName+"([a-zA-Z]+)(s|List)$");
            Matcher aliasMatcher = aliasPattern.matcher(currentName);
            String deserializePropertyName = null;
            if (aliasMatcher.find()) {
                deserializePropertyName = firstLetterLowerCase(aliasMatcher.group(1));
            }

            try {
                deserializePropertyClass = ClassUtils.getField(contentClass, deserializePropertyName).getType();
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }

            List<?> values = JsonUtils.toList(node, deserializePropertyClass);

            if (CharSequence.class.isAssignableFrom(deserializePropertyClass) || Number.class.isAssignableFrom(deserializePropertyClass)) {
                if (CollectionUtils.isEmpty(values)) {
                    return (T) Collections.emptyList();
                }

                List<T> list = Lists.newArrayListWithExpectedSize(values.size());
                for (Object value : values) {
                    list.add(invokeSetPropertyMethod(deserializePropertyName, value, contentClass, deserializePropertyClass));
                }
                return (T) list;
            } else {
                return (T) values;
            }
        } else {
            String deserializePropertyName = currentName.replace(this.propertyName, "");
            deserializePropertyName = firstLetterLowerCase(deserializePropertyName);

            try {
                deserializePropertyClass = ClassUtils.getField(javaType.getRawClass(), deserializePropertyName).getType();
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            if (CharSequence.class.isAssignableFrom(deserializePropertyClass)) {
                return invokeSetPropertyMethod(deserializePropertyName, node.asText(), javaType.getRawClass(), deserializePropertyClass);
            } else if (Long.class.isAssignableFrom(deserializePropertyClass)) {
                String text = node.asText();
                Object value = StringUtils.isNotBlank(text) ? Long.parseLong(text) : null;
                return invokeSetPropertyMethod(deserializePropertyName, value, javaType.getRawClass(), deserializePropertyClass);
            }
        }

        return (T) JsonUtils.toObject(node, this.javaType.getRawClass());
    }

    private T invokeSetPropertyMethod(String propertyName, Object value, Class<?> clazz, Class<?> deserializePropertyClass) {
        try {
            Object o = clazz.newInstance();
            ReflectionUtils.invokeMethod(clazz.getMethod("set" + String.valueOf(propertyName.charAt(0)).toUpperCase() + propertyName.substring(1), deserializePropertyClass), o, value);
            return (T) o;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        return new NamePropertyDeserializer<>(beanProperty.getType(), beanProperty.getName());
    }

    private String firstLetterLowerCase(String name) {
        return String.valueOf(name.charAt(0)).toLowerCase() + name.substring(1);
    }
}
