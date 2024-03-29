package com.rick.meta.dict.convert;

import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Rick
 * @createdAt 2023-03-17 13:45:00
 */
@Component
@RequiredArgsConstructor
public class DictConverter implements ValueConverter<Object> {

    private final DictService dictService;

    @Override
    public String convert(Object dictType, Object value) {
        if (value == null) {
            return null;
        }

        return dictService.getDictByTypeAndName((String) dictType, String.valueOf(value))
                .orElseThrow(() -> new IllegalArgumentException(dictType + " doesn't contain " + value)).getLabel();
    }
}
