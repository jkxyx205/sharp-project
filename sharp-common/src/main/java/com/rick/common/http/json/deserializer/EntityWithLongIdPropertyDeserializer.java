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
public class EntityWithLongIdPropertyDeserializer<T> extends JsonDeserializer<T> implements ContextualDeserializer {

    private JavaType javaType;

    public EntityWithLongIdPropertyDeserializer(JavaType javaType) {
        this.javaType = javaType;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (node.isNumber()) {
            Long id = node.asLong();
            return invokeSetIdMethods(id, this.javaType.getRawClass());
        } else if (node.isTextual()) {
            Long id = Long.valueOf(node.asText());
            return invokeSetIdMethods(id, this.javaType.getRawClass());
        }

        if (List.class.isAssignableFrom(this.javaType.getRawClass())) {
            Class<?> contentClass = javaType.getContentType().getRawClass();
            if (contentClass == Long.class) {
                List<Long> ids = JsonUtils.toList(node, Long.class);
                if (CollectionUtils.isEmpty(ids)) {
                    return (T) Collections.emptyList();
                }

                List list = Lists.newArrayListWithExpectedSize(ids.size());
                for (Long id : ids) {
                    list.add(invokeSetIdMethods(id, contentClass));
                }
                return (T) list;
            } else {
                return (T) JsonUtils.toList(node, this.javaType.getContentType().getRawClass());
            }
        }

        return (T) JsonUtils.toObject(node, this.javaType.getRawClass());
    }

    private T invokeSetIdMethods(Long id, Class<?> clazz) {
        try {
            Object o = clazz.newInstance();
            ReflectionUtils.invokeMethod(clazz.getMethod("setId", Long.class), o, id);
            return (T) o;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        return new EntityWithLongIdPropertyDeserializer<>(beanProperty.getType());
    }
}
