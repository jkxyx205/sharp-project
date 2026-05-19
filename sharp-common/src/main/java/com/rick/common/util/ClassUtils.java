package com.rick.common.util;

import com.rick.common.http.convert.*;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.format.support.DefaultFormattingConversionService;

import java.lang.reflect.*;
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
        return getFieldGenericClass(null, field);
    }

    /**
     * public class OrderEntity<T extends ItemEntity> extends BaseCodeDescriptionEntity<Long> {
     *     @Valid
     *     @Size(min = 1)
     *     @OneToMany(mappedBy = "headerId", joinColumnId = "header_id")
     *     List<T> itemList;
     * }
     *
     * public class PurchaseOrder extends OrderEntity<PurchaseOrder.Item> {
     *
     * }
     *
     * 根据 itemList 的 field，获取真实范型的 class，必须传 subClass，才能获取到 PurchaseOrder.Item.class，getFieldGenericClass(PurchaseOrder.class, itemList.field)
     * 如果不传入 subClass， getFieldGenericClass(itemList.field) 只能获取 ItemEntity.class
     *
     *
     * @param subClass 实际泛型绑定在哪个类
     * @param field
     * @return
     */
    public static Class<?>[] getFieldGenericClass(Class<?> subClass, Field field) {
        // 1. 获取字段类型
        Type fieldType = field.getGenericType();

        // 2. 已经是具体类型，直接返回
        if (fieldType instanceof Class<?>) {
            return new Class<?>[] {(Class<?>) fieldType};
        }

        // 3. 字段本身就是 TypeVariable，如 private T item
        if (fieldType instanceof TypeVariable<?>) {
            Class<?> clazz = resolveTypeVariable(subClass, (TypeVariable<?>)fieldType);// ✅ T → 实际类型
            return new Class<?>[] {clazz};
        }

        // 4. List<T> 或 List<PurchaseOrder.Item> 这种带泛型的类型
        if (fieldType instanceof ParameterizedType) {
            Type[] type = ((ParameterizedType) fieldType).getActualTypeArguments();
            return typeToClass(subClass, type);
        }

        throw new IllegalArgumentException("Cannot resolve type for field: " + field.getName());
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
            return typeToClass(null, actualTypeArguments);
        }

        return null;
    }

    public static Field getField(Class<?> clazz, String name) {
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

    private static Class<?>[] typeToClass(Class<?> subClass, Type[] actualTypeArguments) {
        if (actualTypeArguments == null || actualTypeArguments.length == 0) {
            return new Class<?>[0];
        }

        Class<?>[] classes = new Class<?>[actualTypeArguments.length];

        for (int i = 0; i < actualTypeArguments.length; i++) {
            Type type = actualTypeArguments[i];

            if (type instanceof Class<?>) {
                // 普通类，例如 String.class
                classes[i] = (Class<?>) type;

            } else if (type instanceof ParameterizedType) {
                // List<String> → raw type = List.class
                classes[i] = (Class<?>) ((ParameterizedType) type).getRawType();

            } else if (type instanceof TypeVariable<?>) {
                if (Objects.nonNull(subClass)) {
                    classes[i] = resolveTypeVariable(subClass, (TypeVariable)type);
                } else {
                    // 处理 T 等泛型变量
                    TypeVariable<?> tv = (TypeVariable<?>) type;
                    Type[] bounds = tv.getBounds(); // T extends Number → bounds[0] = Number.class
                    if (bounds != null && bounds.length > 0 && bounds[0] instanceof Class<?>) {
                        classes[i] = (Class<?>) bounds[0];  // 上界
                    } else {
                        classes[i] = Object.class; // 无上界 → Object
                    }
                }
            } else if (type instanceof GenericArrayType) {
                // T[] 或 List<String>[] 这种类型
                Type componentType = ((GenericArrayType) type).getGenericComponentType();
                Class<?> componentClass = resolveToClass(componentType);
                classes[i] = java.lang.reflect.Array.newInstance(componentClass, 0).getClass();

            } else {
                // 无法处理的情况（WildcardType 等）
                classes[i] = Object.class;
            }
        }

        return classes;
    }

    private static Class<?> resolveTypeVariable(Class<?> subClass, TypeVariable<?> typeVar) {
        // typeVar 声明在哪个类上（OrderEntity），找到它的位置 index
        Class<?> declaringClass = (Class<?>) typeVar.getGenericDeclaration();
        TypeVariable<?>[] typeParams = declaringClass.getTypeParameters();

        int index = -1;
        for (int i = 0; i < typeParams.length; i++) {
            if (typeParams[i].getName().equals(typeVar.getName())) {
                index = i;
                break;
            }
        }

        // 从 subClass 向上找，直到找到 extends declaringClass<XxxType> 的那一层
        Class<?> current = subClass;
        while (current != null) {
            Type genericSuper = current.getGenericSuperclass();
            if (genericSuper instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)genericSuper;
                if (pt.getRawType().equals(declaringClass)) {
                    // 找到了 PurchaseOrder extends OrderEntity<PurchaseOrder.Item>
                    Type actualType = pt.getActualTypeArguments()[index];
                    if (actualType instanceof Class<?>) {
                        Class<?> clazz = (Class<?>) actualType;
                        return clazz; // ✅ PurchaseOrder.Item
                    }
                }
            }
            current = current.getSuperclass();
        }

        throw new IllegalArgumentException("Cannot resolve TypeVariable: " + typeVar);
    }

    private static Class<?> resolveToClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            Type[] bounds = tv.getBounds();
            if (bounds != null && bounds.length > 0 && bounds[0] instanceof Class<?>) {
                return (Class<?>) bounds[0];
            }
        } else if (type instanceof GenericArrayType) {
            Class<?> componentClass = resolveToClass(((GenericArrayType) type).getGenericComponentType());
            return java.lang.reflect.Array.newInstance(componentClass, 0).getClass();
        }
        return Object.class;
    }

}
