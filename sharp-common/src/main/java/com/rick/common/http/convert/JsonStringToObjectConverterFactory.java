package com.rick.common.http.convert;

import com.rick.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * 对象必须实现 JsonStringToObjectConverterFactory.JsonValue
 * string(json) => Object(JsonValue)
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
final public class JsonStringToObjectConverterFactory implements ConverterFactory<String, JsonStringToObjectConverterFactory.JsonValue> {

    public JsonStringToObjectConverterFactory() {
    }

    @Override
    public <T extends JsonStringToObjectConverterFactory.JsonValue> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToObject(targetType);
    }

    private static class StringToObject<T> implements Converter<String, T> {
        private final Class<T> clazz;

        StringToObject(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T convert(String source) {
            if (StringUtils.isBlank(source)) {
                return null;
            }

            return JsonUtils.toObject(source, clazz);
        }
    }

    public interface JsonValue {}
}
