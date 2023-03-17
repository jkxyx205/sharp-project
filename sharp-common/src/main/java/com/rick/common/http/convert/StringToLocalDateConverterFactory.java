package com.rick.common.http.convert;

import com.rick.common.util.String2TimeUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.time.LocalDate;

/**
 * code => enum
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
final public class StringToLocalDateConverterFactory implements ConverterFactory<String, LocalDate> {
    @Override
    public <T extends LocalDate> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToLocalDateConverterFactory.StringToLocalDate();
    }

    private static class StringToLocalDate<T extends LocalDate> implements Converter<String, T> {

        @Override
        public T convert(String source) {
          return (T) String2TimeUtils.toLocalDate(source);
        }
    }
}
