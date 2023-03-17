package com.rick.meta.dict.convert;

import com.rick.common.util.JsonUtils;
import com.rick.meta.dict.service.DictUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2023-03-17 13:45:00
 */
@Component
public class ArrayDictConverter extends DictConverter {

    @Override
    public String convert(Object dictType, String values) {
        if (values == null) {
            return null;
        }

        try {
            List<String> valueList = JsonUtils.toList(values, String.class);
            return valueList.stream().map(value -> DictUtils.getDictLabel((String) dictType, value).get().getLabel()).collect(Collectors.joining(","));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
