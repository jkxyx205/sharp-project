package com.rick.demo.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.rick.common.util.JsonUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;

/**
 * @author Rick
 * @createdAt 2022-10-27 14:14:00
 */
public class EntityIdJsonDeserializer<T> extends JsonDeserializer<T> {

    private Class clazz;

    public EntityIdJsonDeserializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        try {
            if (node.isNumber()) {
                Long id = node.asLong();
                return invokeSetIdMethods(id);
            } else if (node.isTextual()) {
                Long id = Long.valueOf(node.asText());
                return invokeSetIdMethods(id);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

        return (T) JsonUtils.toObject(node, this.clazz);
    }

    private T invokeSetIdMethods(Long id) throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        Object o = this.clazz.newInstance();
        ReflectionUtils.invokeMethod(this.clazz.getMethod("setId", Long.class), o, id);
        return (T)o;
    }
}
