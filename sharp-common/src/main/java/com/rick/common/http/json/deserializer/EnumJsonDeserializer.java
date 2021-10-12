package com.rick.common.http.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.rick.common.util.ClassUtils;
import com.rick.common.util.EnumUtils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @author Rick
 * @createdAt 2021-10-11 21:56:00
 */
public class EnumJsonDeserializer extends JsonDeserializer<Enum> {

    @Override
    public Enum deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String code = node.asText();

        JsonStreamContext parsingContext = jsonParser.getParsingContext();
        Object currentValue = parsingContext.getCurrentValue();
        // 字段名
        String currentName = parsingContext.getCurrentName();

        Field field = null;
        Enum value = null;
        try {
            field = ClassUtils.getField(currentValue.getClass(),  currentName);
            value = EnumUtils.valueOfCode(field.getType(), code);
        } catch (Exception e) {}

        if (value == null) {
            throw new IllegalArgumentException(
                    "No enum constant " + field.getName() + "." + code);
        }

        return value;
    }
}