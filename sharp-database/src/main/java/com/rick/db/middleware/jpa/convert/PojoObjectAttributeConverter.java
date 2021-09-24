package com.rick.db.middleware.jpa.convert;

import com.rick.common.util.ClassUtils;
import com.rick.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Objects;

/**
 * 对象转Json字符串存储
 * @author Rick
 * @createdAt 2021-09-24 09:42:00
 */
public abstract class PojoObjectAttributeConverter<T> implements AttributeConverter<T, String> {

    @Override
    public String convertToDatabaseColumn(T t) {
        if (Objects.isNull(t)) {
            return null;
        }

        try {
            return JsonUtils.toJson(t);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public T convertToEntityAttribute(String json) {
        Class<?> clazz = ClassUtils.getActualTypeArgument(this.getClass())[0];
        if (StringUtils.isBlank(json)) {
            return null;
        }

        try {
            return (T) JsonUtils.toObject(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
