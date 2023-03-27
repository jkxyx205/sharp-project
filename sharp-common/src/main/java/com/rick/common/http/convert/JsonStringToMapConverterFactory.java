package com.rick.common.http.convert;

import com.rick.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Map;

/**
 * string(json) => Map
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
final public class JsonStringToMapConverterFactory implements ConverterFactory<String, Map> {

    @Override
    public <T extends Map> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToMap();
    }

    private static class StringToMap<T> implements Converter<String, T> {

        @Override
        public T convert(String source) {
            if (StringUtils.isBlank(source)) {
                return null;
            }

            return (T) JsonUtils.toObject(source, Map.class);
        }
    }

}
