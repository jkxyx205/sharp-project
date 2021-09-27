package com.rick.db.middleware.jpa.convert;

import com.rick.common.util.ClassUtils;
import com.rick.common.util.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 对象数组转Json字符串存储
 * @author Rick
 * @createdAt 2021-09-24 09:50:00
 */
public abstract class BaseListConverter<T> implements AttributeConverter<List<T>, String> {

    @Override
    public String convertToDatabaseColumn(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        try {
            return JsonUtils.toJson(list);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public List<T> convertToEntityAttribute(String json) {
        Class<?> clazz = ClassUtils.getClassGenericsTypes(this.getClass())[0];

        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }

        try {
            return (List<T>) JsonUtils.toList(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
