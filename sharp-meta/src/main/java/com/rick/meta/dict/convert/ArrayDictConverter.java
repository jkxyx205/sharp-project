package com.rick.meta.dict.convert;

import com.rick.common.util.JsonUtils;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2023-03-17 13:45:00
 */
@Component
@RequiredArgsConstructor
public class ArrayDictConverter implements ValueConverter<String>  {

    private final DictService dictService;

    @Override
    public String convert(Object dictType, String values) {
        if (values == null) {
            return null;
        }

        List<String> valueList = JsonUtils.toList(values, String.class);
        return valueList.stream().map(value -> dictService.getDictByTypeAndName((String) dictType, value).get().getLabel()).collect(Collectors.joining(","));
    }
}
