package com.rick.common.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;

/**
 * @author Rick
 * @createdAt 2021-10-11 21:25:00
 */
@UtilityClass
public class EnumUtils {
    private static final String METHOD_NAME = "valueOfCode";

    /**
     *
     * @param enumType 枚举class
     * @param code code
     * @return 如果没有找到返回null
     */
    public static Enum valueOfCode(Class<?> enumType, String code) {
        Method[] methods = enumType.getMethods();
        try {
            Enum en;
            for (Method method : methods) {
                if (method.getName().equals(METHOD_NAME)) {
                    Class<?> parameterType = method.getParameterTypes()[0];
                    if (parameterType == Integer.class || parameterType == int.class) {
                        en = (Enum) method.invoke(null, Integer.parseInt(code));
                    } else {
                        en = (Enum) method.invoke(null, code);
                    }
                    return en;
                }
            }
        } catch (Exception e) {
        }

        return null;
    }
}
