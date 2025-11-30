package com.rick.common.http.json.deserializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NUMBER;
import static com.fasterxml.jackson.databind.node.JsonNodeType.STRING;

public class EnumCustomizeDeserializer extends JsonDeserializer<Enum<?>> implements ContextualDeserializer {

    private Class<? extends Enum> enumClass;

    public EnumCustomizeDeserializer() {
    }

    public EnumCustomizeDeserializer(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JavaType type = ctxt.getContextualType();
        if (type == null && property != null) {
            type = property.getType();
        }

        if (type != null) {
            Class<?> rawClass = type.getRawClass();
            if (Enum.class.isAssignableFrom(rawClass)) {
                return new EnumCustomizeDeserializer((Class<? extends Enum>) rawClass);
            }
        }

        return this;
    }

    @Override
    public Enum<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (enumClass == null) {
            throw new JsonParseException(jsonParser, "Enum class is not initialized");
        }

        JsonToken currentToken = jsonParser.getCurrentToken();

        if (currentToken == JsonToken.VALUE_STRING) {
            String code = jsonParser.getText();
            return deserializeByCode(enumClass, code, String.class, jsonParser);
        }

        if (currentToken == JsonToken.VALUE_NUMBER_INT) {
            int code = jsonParser.getIntValue();
            return deserializeByCode(enumClass, code, int.class, jsonParser);
        }

        if (currentToken == JsonToken.START_OBJECT) {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            if (node.has("code")) {
                JsonNodeType codeNode = node.get("code").getNodeType();

                if (codeNode == STRING) {
                    String code = node.get("code").asText();
                    return deserializeByCode(enumClass, code, String.class, jsonParser);
                }

                if (codeNode == NUMBER) {
                    int code = node.get("code").asInt();
                    return deserializeByCode(enumClass, code, int.class, jsonParser);
                }


            }
            throw new JsonParseException(jsonParser, "Expected 'code' field in enum object");
        }

        // 处理null值
        if (currentToken == JsonToken.VALUE_NULL) {
            return null;
        }

        throw new JsonParseException(jsonParser, "Expected STRING or OBJECT for enum deserialization, but got: " + currentToken);
    }

    /**
     * 通过code反序列化枚举
     */
    private Enum<?> deserializeByCode(Class<? extends Enum> enumClass, Object code, Class codeClass, JsonParser jsonParser) throws IOException {
        if (code == null || (code instanceof String && ((String)code).trim().isEmpty())) {
            return null;
        }

        try {
            // 尝试调用枚举的 valueOfCode 方法
            Method valueOfCodeMethod = enumClass.getMethod("valueOfCode", codeClass);
            return (Enum<?>) valueOfCodeMethod.invoke(null, code);
        } catch (NoSuchMethodException e) {
            // 如果没有 valueOfCode 方法，使用标准的 valueOf
            try {
                return Enum.valueOf(enumClass, code.toString());
            } catch (IllegalArgumentException ex) {
                throw new JsonParseException(jsonParser,
                        "Cannot deserialize enum " + enumClass.getSimpleName() + " with code: " + code, ex);
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                throw new JsonParseException(jsonParser,
                        "Invalid enum code for " + enumClass.getSimpleName() + ": " + code, cause);
            }
            throw new IOException("Error deserializing enum " + enumClass.getSimpleName(), e);
        } catch (Exception e) {
            throw new IOException("Error deserializing enum " + enumClass.getSimpleName(), e);
        }
    }
}