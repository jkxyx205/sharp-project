package com.rick.common.http.convert;

import com.rick.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * 处理范型转换利用TypeDescriptor获取范型信息
 * String(json) => Collection
 * @author Rick
 * @createdAt 2021-10-12 22:36:00
 */
public class JsonStringToCollectionConverter implements ConditionalGenericConverter {

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        ResolvableType[] generics = targetType.getResolvableType().getGenerics();
        if (generics.length == 0) {
            return false;
        }

        return sourceType.getType() == String.class
                && Collection.class.isAssignableFrom(targetType.getType());
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (StringUtils.isBlank((String) source)) {
            return null;
        }
        try {
            return JsonUtils.toList(source.toString(), targetType.getResolvableType().getGeneric(0).resolve());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
