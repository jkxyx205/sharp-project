package com.rick.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
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
    public static Class<?>[] getClassGenericsTypes(Class<?> clazz) {
        Class<?>[] classes = null;
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass)
                    .getActualTypeArguments();

            if (ArrayUtils.isNotEmpty(actualTypeArguments)) {
                int length = actualTypeArguments.length;
                 classes = new Class<?>[length];
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    if (actualTypeArguments[i] instanceof ParameterizedTypeImpl) {
                        classes[i] = ((ParameterizedTypeImpl)actualTypeArguments[i]).getRawType();
                    } else {
                        classes[i] = (Class<?>) actualTypeArguments[i];
                    }
                }
            }
        }

        return classes;
    }

    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            if (clazz == Object.class) {
                throw e;
            }
            return getField(clazz.getSuperclass(), name);
        }

    }
}
