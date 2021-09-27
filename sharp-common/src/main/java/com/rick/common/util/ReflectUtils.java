package com.rick.common.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:39:00
 */
@UtilityClass
public class ReflectUtils {

    /**
     * 获取自身和父类的所有属性
     * @param clazz
     * @return
     */
    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> list = new ArrayList();
        while (Objects.nonNull(clazz)) {
            Field[] fields = clazz.getDeclaredFields();
            list.addAll(Arrays.asList(fields));
            clazz = clazz.getSuperclass();
        }
        return list.toArray(new Field[] {});
    }
}
