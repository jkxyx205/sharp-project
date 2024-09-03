package com.rick.common.http.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author Rick
 * @createdAt 2024-09-27 14:14:00
 */
@NoArgsConstructor
public class BooleanPropertyDeserializer extends JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (node.isNull()) {
            return false;
        }

        if (node.isBoolean()) {
            return node.asBoolean();
        } if (node.isArray()) {
            return !node.isEmpty();
        } else if (node.isNumber()){
            return node.numberValue().intValue() == 0 ? false : true;
        }  else if (node.isTextual()) {
            String text = node.asText();

            if ("false".equals(text) || "0".equals(text) || "否".equals(text)) {
                return false;
            }
            return StringUtils.isNotBlank(text);
        }

        return true;
    }
}
