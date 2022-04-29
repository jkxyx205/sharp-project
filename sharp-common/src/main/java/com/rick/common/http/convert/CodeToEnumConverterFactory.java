package com.rick.common.http.convert;

import com.rick.common.util.EnumUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * code => enum
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
final public class CodeToEnumConverterFactory implements ConverterFactory<String, Enum> {

    public CodeToEnumConverterFactory() {
    }

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new CodeToEnumConverterFactory.StringToEnum(targetType);
    }

    private static class StringToEnum<T extends Enum> implements Converter<String, T> {
        private final Class<T> enumType;

        StringToEnum(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            T t = (T) EnumUtils.valueOfCode(enumType, source);

            if (t == null) {
                throw new IllegalArgumentException(
                        "No enum constant " + enumType.getCanonicalName() + "." + source);
            }
            return t;
        }
    }
}
