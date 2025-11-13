package com.rick.common.http.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.google.common.collect.Lists;
import com.rick.common.util.JsonUtils;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
            return invokeSetIdMethod(id, this.javaType.getRawClass());
        } else if (node.isTextual()) {
            Long id = StringUtils.isBlank(node.asText()) ? null : Long.valueOf(node.asText());
            return invokeSetIdMethod(id, this.javaType.getRawClass());
        }

        if (List.class.isAssignableFrom(this.javaType.getRawClass())) {
            if (node.size() == 0) {
                return (T) Collections.emptyList();
            }

            Class<?> contentClass = javaType.getContentType().getRawClass();
            if (node.get(0).isNumber() || node.get(0).isTextual()) {
                List<Long> ids = JsonUtils.toList(node, Long.class);
                if (CollectionUtils.isEmpty(ids)) {
                    return (T) Collections.emptyList();
                }

                List list = Lists.newArrayListWithExpectedSize(ids.size());
                for (Long id : ids) {
                    list.add(invokeSetIdMethod(id, contentClass));
                }
                return (T) list;
            } else {
                return (T) JsonUtils.toList(node, contentClass);
            }
        }

        return (T) JsonUtils.toObject(node, this.javaType.getRawClass());
    }

    private T invokeSetIdMethod(Long id, Class<?> clazz) {
        try {
            Object o = clazz.newInstance();
            ReflectionUtils.invokeMethod(clazz.getMethod("setId", clazz.getMethod("getId").getReturnType()), o, id);
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
