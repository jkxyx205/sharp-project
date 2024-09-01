package com.rick.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.ReflectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-09-11 14:06:00
 */
@UtilityClass
public class ClassUtils {

    /**
     * 获取字段的范型类型
     * @param field
     * @return
     */
    public static Class<?> getFieldGenericClass(Field field) {
        Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType)type).getRawType();
        }

        return null;
    }

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
                    } else if (actualTypeArguments[i] instanceof Class) {
                        classes[i] = (Class<?>) actualTypeArguments[i];
                    } else if (actualTypeArguments[i] instanceof TypeVariableImpl){
                        classes[i] = ((TypeVariableImpl)actualTypeArguments[i]).getClass();
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

    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        while (Objects.nonNull(clazz)) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (!list.stream().map(Field::getName).collect(Collectors.toSet()).contains(field.getName())) {
                    list.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return list.toArray(new Field[] {});
    }

    /**
     * POJO setter
     * @param obj
     * @param field
     * @param value
     */
    public static void setFieldValue(Object obj, Field field,  Object value) {
        if (obj == null) {
            return;
        }

        String propertyName = field.getName();

        try {
            ReflectionUtils.invokeMethod(obj.getClass().getMethod("set" + String.valueOf(propertyName.charAt(0)).toUpperCase() + propertyName.substring(1), field.getType()), obj, value);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
