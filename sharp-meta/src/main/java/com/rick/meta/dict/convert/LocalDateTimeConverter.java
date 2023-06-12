package com.rick.meta.dict.convert;

import com.rick.common.util.Time2StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Rick
 * @createdAt 2023-03-17 13:45:00
 */
@Component
public class LocalDateTimeConverter implements ValueConverter<LocalDateTime> {

    @Override
    public String convert(Object context, LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return Time2StringUtils.format(value);
    }
}
