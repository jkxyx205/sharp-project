package com.rick.common.http.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.google.common.collect.Lists;
import com.rick.common.util.JsonUtils;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2022-10-27 14:14:00
 */
@NoArgsConstructor
public class EntityWithCodePropertyDeserializer<T> extends JsonDeserializer<T> implements ContextualDeserializer {

    private JavaType javaType;

    public EntityWithCodePropertyDeserializer(JavaType javaType) {
        this.javaType = javaType;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (node.isTextual()) {
            return invokeSetCodeMethod(node.asText(), this.javaType.getRawClass());
        }

        if (List.class.isAssignableFrom(this.javaType.getRawClass())) {
            if (node.size() == 0) {
                return (T) Collections.emptyList();
            }

            Class<?> contentClass = javaType.getContentType().getRawClass();
            if (node.get(0).isTextual()) {
                List<String> codes = JsonUtils.toList(node, String.class);
                if (CollectionUtils.isEmpty(codes)) {
                    return (T) Collections.emptyList();
                }

                List list = Lists.newArrayListWithExpectedSize(codes.size());
                for (String code : codes) {
                    list.add(invokeSetCodeMethod(code, contentClass));
                }
                return (T) list;
            } else {
                return (T) JsonUtils.toList(node, contentClass);
            }
        }

        return (T) JsonUtils.toObject(node, this.javaType.getRawClass());
    }

    private T invokeSetCodeMethod(String code, Class<?> clazz) {
        try {
            Object o = clazz.newInstance();
            ReflectionUtils.invokeMethod(clazz.getMethod("setCode", String.class), o, code);
            return (T) o;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        return new EntityWithCodePropertyDeserializer<>(beanProperty.getType());
    }
}
