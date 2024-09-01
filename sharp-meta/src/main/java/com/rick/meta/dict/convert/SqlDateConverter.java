package com.rick.meta.dict.convert;

import org.springframework.stereotype.Component;

import java.sql.Date;

/**
 * @author Rick
 * @createdAt 2023-03-17 13:45:00
 */
@Component
public class SqlDateConverter implements ValueConverter<Object, Date> {

    @Override
    public String convert(Object context, Date value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
