package com.rick.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Rick
 * @createdAt 2021-09-11 14:06:00
 */
@UtilityClass
public class ClassUtils {

    /**
     * 获取类范型的真正class
     * @param clazz
     * @return
     */
    public static Class<?>[] getActualTypeArgument(Class<?> clazz) {
        Class<?>[] classes = null;
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass)
                    .getActualTypeArguments();

            if (ArrayUtils.isNotEmpty(actualTypeArguments)) {
                int length = actualTypeArguments.length;
                 classes = new Class<?>[length];
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    classes[i] = (Class<?>) actualTypeArguments[i];
                }
            }
        }

        return classes;
    }
}