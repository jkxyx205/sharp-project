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
public class DictConverter implements ValueConverter<String, Object> {

    private final DictService dictService;

    private final ArrayDictConverter arrayDictConverter;

    @Override
    public String convert(String dictType, Object value) {
        if (value == null) {
            return null;
        }

        if (value.getClass() == String.class && ((String)value).startsWith("[")) {
            return arrayDictConverter.convert(dictType, ((String)value));
        }

        return dictService.getDictByTypeAndName(dictType, String.valueOf(value))
                .orElseThrow(() -> new IllegalArgumentException(dictType + " doesn't contain " + value)).getLabel();
    }
}
