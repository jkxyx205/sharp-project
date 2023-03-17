package com.rick.meta.dict.convert;

import com.rick.meta.dict.service.DictUtils;
import org.springframework.stereotype.Component;

/**
 * @author Rick
 * @createdAt 2023-03-17 13:45:00
 */
@Component
public class DictConverter implements ValueConverter<String> {

    @Override
    public String convert(Object dictType, String value) {
        if (value == null) {
            return null;
        }

        return DictUtils.getDictLabel((String) dictType, value).get().getLabel();
    }
}
