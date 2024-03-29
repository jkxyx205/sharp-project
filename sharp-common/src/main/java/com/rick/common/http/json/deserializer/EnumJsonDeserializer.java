package com.rick.common.http.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.rick.common.util.ClassUtils;
import com.rick.common.util.EnumUtils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 自定义枚举解析器替换默认的枚举：枚举要遵循固定的格式：
 * <p>
 *     <b>1. name作为code</b>
 *     <blockquote><pre>
 *    {@literal @}AllArgsConstructor
 *    {@literal @}Getter
 *     public enum TypeEnum {
 *         PRIVATE("私立"),
 *         PUBLIC("公立");
 *
 *        {@literal @}JsonValue
 *         public String getCode() {
 *             return this.name();
 *         }
 *         private final String label;
 *         public static TypeEnum valueOfCode(String code) {
 *             return valueOf(code);
 *         }
 *     }</pre></blockquote>
 *     <b>2. 自定义数字code</b>
 *     <blockquote><pre>
 *    {@literal @}AllArgsConstructor
 *    {@literal @}Getter
 *     public enum SexEnum {
 *         UNKNOWN(0, "Unknown"),
 *         MALE(1, "Male"),
 *         FEMALE(2, "Female");
 *         private static final Map<Integer, SexEnum> codeMap = new HashMap<>();
 *         static {
 *             for (SexEnum e : values()) {
 *                 codeMap.put(e.code, e);
 *             }
 *         }
 *         private final int code;
 *         private final String label;
 *        {@literal @}JsonValue
 *         public int getCode() {
 *             return this.code;
 *         }
 *         public static SexEnum valueOfCode(int code) {
 *             return codeMap.get(code);
 *         }
 *     }
 *     </pre></blockquote>
 *
 * </p>
 * @author Rick
 * @createdAt 2021-10-11 21:56:00
 * EnumDeserializer 自带的完成相同的功能，按照顺序依次解析：
 *  如果有@JsonValue：先根据value，没有再根据order，没有报错
 *  如果没有@JsonValue：先根据name，没有再根据order，没有报错
 */
@Deprecated
public class EnumJsonDeserializer extends JsonDeserializer<Enum> {

    @Override
    public Enum deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
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