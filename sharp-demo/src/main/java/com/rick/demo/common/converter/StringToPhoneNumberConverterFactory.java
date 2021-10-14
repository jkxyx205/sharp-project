package com.rick.demo.common.converter;

import com.rick.demo.module.project.domain.entity.PhoneNumber;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

/**
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
@Component
final public class StringToPhoneNumberConverterFactory implements ConverterFactory<String, PhoneNumber> {

    @Override
    public <T extends PhoneNumber> Converter<String, T> getConverter(Class<T> aClass) {
        return new StringToPhoneNumber();
    }

    private static class StringToPhoneNumber<T> implements Converter<String, T> {

        @Override
        public T convert(String source) {
            if (StringUtils.isBlank(source)) {
                return null;
            }

            String[] arr = source.split("-");
            return (T) PhoneNumber.builder().code(arr[0]).number(arr[1]).build();
        }
    }

}
