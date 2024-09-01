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
public class DictConverter implements ValueConverter<String, String> {

    private final DictService dictService;

    private final ArrayDictConverter arrayDictConverter;

    @Override
    public String convert(String dictType, String value) {
        if (value == null) {
            return null;
        }

        if (value.startsWith("[")) {
            return arrayDictConverter.convert(dictType, value);
        }

        return dictService.getDictByTypeAndName((String) dictType, value)
                .orElseThrow(() -> new IllegalArgumentException(dictType + " doesn't contain " + value)).getLabel();
    }
}
