package com.rick.meta.dict.convert;

import com.rick.common.util.Time2StringUtils;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * @author Rick
 * @createdAt 2023-03-17 13:45:00
 */
@Component
public class SqlTimestampConverter implements ValueConverter<Object, Timestamp> {

    @Override
    public String convert(Object context, Timestamp value) {
        if (value == null) {
            return null;
        }

        return Time2StringUtils.format(value.toLocalDateTime());
    }
}
