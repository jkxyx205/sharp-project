package com.rick.common.util;

import com.rick.common.http.convert.*;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.format.support.DefaultFormattingConversionService;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

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
    public static Class<?>[] getFieldGenericClass(Field field) {
        Type[] type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        return typeToClass(type);
    }

    /**
     * 获取类范型的真正class
     * @param clazz
     * @return
     */
    public static Class<?>[] getClassGenericsTypes(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass)
                    .getActualTypeArguments();
            return typeToClass(actualTypeArguments);
        }

        return null;
    }

    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        return FieldUtils.getDeclaredField(clazz, name, true);
    }

    public static Field[] getAllFields(Class<?> clazz) {
        return FieldUtils.getAllFields(clazz);
    }

    public Object getPropertyValue(Object entity, Field field) {
        if (entity == null || field == null) {
            throw new IllegalArgumentException("entity 和 field 不能为空");
        }
        try {
            field.setAccessible(true); // 关闭访问检查
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法获取字段值: " + field.getName(), e);
        }
    }

    public Object getPropertyValue(Object entity, String propertyName) {
        if (Objects.isNull(entity)) {
            return null;
        }
        try {
            BeanWrapperImpl wrapper = new BeanWrapperImpl(entity);
            if (!wrapper.isReadableProperty(propertyName)) {
                return null;
            }
            return wrapper.getPropertyValue(propertyName);
        } catch (BeansException exception) {
            return null;
        }
    }

    /**
     * POJO setter
     * @param bean
     * @param field
     * @param value
     */
    public static void setFieldValue(Object bean, Field field,  Object value) {
//        setPropertyValue(bean, field.getName(), value);
        try {
            field.setAccessible(true);
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法设置字段值: " + field.getName(), e);
        }
    }

    public void setPropertyValue(Object bean, String propertyName, Object value) {
        String[] parts = propertyName.split("\\.");
        Object current = bean;
        BeanWrapperImpl wrapper = new BeanWrapperImpl(current);

        DefaultFormattingConversionService dbConversionService= new DefaultFormattingConversionService();

        dbConversionService.addConverterFactory(new StringToLocalDateConverterFactory());
        dbConversionService.addConverterFactory(new CodeToEnumConverterFactory());
        dbConversionService.addConverter(new JsonStringToListMapConverter());
        dbConversionService.addConverterFactory(new JsonStringToObjectConverterFactory());
        dbConversionService.addConverterFactory(new JsonStringToMapConverterFactory());
        dbConversionService.addConverter(new JsonStringToCollectionConverter());
        dbConversionService.addConverter(new JsonStringToSetMapConverter());
        dbConversionService.addConverter(new LocalDateTimeToInstantConverter());
        wrapper.setConversionService(dbConversionService);

        try {
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                Object property = wrapper.getPropertyValue(part);

                if (property == null) {
                    Class<?> type = wrapper.getPropertyType(part);
                    if (type == null) {
                        return; // 属性不存在
                    }
                    // 通过无参构造器创建中间对象
                    Object newInstance = type.getDeclaredConstructor().newInstance();
                    wrapper.setPropertyValue(part, newInstance);
                    property = newInstance;
                }

                // 切换 wrapper 到下一级
                current = property;
                wrapper = new BeanWrapperImpl(current);
            }

            // 设置最终属性
            if (!wrapper.isWritableProperty(parts[parts.length - 1])) {
                return;
            }

            wrapper.setPropertyValue(parts[parts.length - 1], value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set property: " + propertyName, e);
        }
    }

    private static Class<?>[] typeToClass(Type[] actualTypeArguments) {
        Class<?>[] classes = null;
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

        return classes;
    }
}
