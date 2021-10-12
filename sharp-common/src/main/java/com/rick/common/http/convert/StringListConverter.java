package com.rick.common.http.convert;

import com.rick.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 处理范型转换利用TypeDescriptor获取范型信息
 *
 * @author Rick
 * @createdAt 2021-10-12 22:36:00
 */
public class StringListConverter implements ConditionalGenericConverter {

    @Override
    public boolean matches(TypeDescriptor typeDescriptor, TypeDescriptor targetType) {
        ResolvableType[] generics = targetType.getResolvableType().getGenerics();
        if (generics.length == 0) {
            return false;
        }

        return typeDescriptor.getType() == String.class
                && List.class.isAssignableFrom(targetType.getType())
                && StringToJsonConverterFactory.JsonValue.class.isAssignableFrom(targetType.getResolvableType().getGeneric(new int[]{0}).resolve());
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return null;
    }

    @Override
    public Object convert(Object source, TypeDescriptor typeDescriptor, TypeDescriptor targetType) {
        if (StringUtils.isBlank((String) source)) {
            return null;
        }
        try {
            return JsonUtils.toList(source.toString(), targetType.getResolvableType().getGeneric(0).resolve());
        } catch (IOException e) {
            return null;
        }
    }
}
