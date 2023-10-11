package com.rick.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * @author Rick
 * @createdAt 2021-10-11 21:25:00
 */
@UtilityClass
public class EnumUtils {

    private static final String VALUE_OF_CODE_METHOD_NAME = "valueOfCode";

    private static final String GET_CODE_METHOD_NAME = "getCode";

    /**
     * @param enumType 枚举class
     * @param code     code
     * @return 如果没有找到返回null
     */
    public static Enum valueOfCode(Class enumType, String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }

        Method[] methods = enumType.getMethods();
        try {
            Enum en;
            for (Method method : methods) {
                if (method.getName().equals(VALUE_OF_CODE_METHOD_NAME)) {
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
            e.printStackTrace();
        }

        return Enum.valueOf(enumType, code);
    }

    /**
     * 获取code值
     * @param en
     * @return
     */
    public static Object getCode(Enum en) {
        try {
            Method getCodeMethod = en.getClass().getMethod(GET_CODE_METHOD_NAME);
            return getCodeMethod.invoke(en);
        } catch (Exception e) {
            return en.name();
        }
    }
}
