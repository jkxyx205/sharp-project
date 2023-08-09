package com.rick.meta.dict.convert;

import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2023-03-17 13:45:00
 */
@Component
public class BoolConverter implements ValueConverter<Object> {
    @Override
    public String convert(Object context, Object value) {
        if (value == null) {
            return null;
        }

        boolean isTrue;
        if (Boolean.class.isAssignableFrom(value.getClass())) {
            isTrue = Objects.equals(value, Boolean.TRUE);
        } else if (Number.class.isAssignableFrom(value.getClass())) {
            isTrue = Objects.equals(((Number)value).intValue(), 1);
        } else if (value instanceof String) {
            isTrue = ("true".equalsIgnoreCase(value.toString()) || "1".equalsIgnoreCase(value.toString()));
        } else {
            throw new IllegalArgumentException();
        }

        return isTrue ? "是" : "否";
    }

}
