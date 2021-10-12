package com.rick.common.http.convert;

import com.rick.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.io.IOException;

/**
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
final public class StringToJsonConverterFactory<T> implements ConverterFactory<String, StringToJsonConverterFactory.JsonValue> {

    public StringToJsonConverterFactory() {
    }


    @Override
    public <T extends StringToJsonConverterFactory.JsonValue> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToJson(targetType);
    }

    private static class StringToJson<T> implements Converter<String, T> {
        private final Class<T> clazz;

        StringToJson(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T convert(String source) {
            if (StringUtils.isBlank(source)) {
                return null;
            }

            try {
                return JsonUtils.toObject(source, clazz);
            } catch (IOException e) {
                return null;
            }
        }
    }

    public interface JsonValue {}
}
