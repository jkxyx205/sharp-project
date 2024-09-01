package com.rick.meta.dict.convert;

import com.rick.common.util.JsonUtils;
import com.rick.meta.dict.model.DictValue;
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
public class ArrayDictConverter implements ValueConverter<String, String>  {

    private final DictService dictService;

    @Override
    public String convert(String dictType, String values) {
        if (values == null) {
            return null;
        }

        List<String> valueList;
        try {
            valueList = JsonUtils.toList(values, String.class);;
        } catch (Exception e) {
            valueList = JsonUtils.toList(values, DictValue.class).stream().map(dictValue -> dictValue.getCode()).collect(Collectors.toList());
        }

        return valueList.stream().map(value -> dictService.getDictByTypeAndName(dictType, value).get().getLabel()).collect(Collectors.joining(","));
    }
}
