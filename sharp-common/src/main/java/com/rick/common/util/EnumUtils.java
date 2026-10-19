package com.rick.common.util;

import com.rick.common.util.model.IEnum;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-10-11 21:25:00
 */
@Slf4j
@UtilityClass
public class EnumUtils {

    private static final String VALUE_OF_CODE_METHOD_NAME = "valueOfCode";

    private static final String GET_CODE_METHOD_NAME = "getCode";

    private static final String GET_LABEL_METHOD_NAME = "getLabel";

    /** IEnum 枚举：code -> 常量 映射缓存，fromCode 与 valueOfCode 共享 */
    private static final Map<Class<?>, Map<Object, Enum<?>>> CACHE = new ConcurrentHashMap<>();

    /** 非 IEnum 枚举：静态 valueOfCode 方法缓存，Optional.empty() 表示无该方法 */
    private static final Map<Class<?>, Optional<Method>> VALUE_OF_CODE_METHOD_CACHE = new ConcurrentHashMap<>();

    /**
     * 类型安全版本：IEnum 枚举按 code 查找（缓存 map，O(1)）。
     * <p>与 {@link #valueOfCode(Class, String)} 的区别：返回值带类型 E、code 支持任意类型
     * （如 IEnum&lt;Integer&gt; 传 Integer）；后者只接受 String code。
     *
     * @param enumType 枚举class
     * @param code     code（允许为 ''，不做 isBlank 短路）
     * @return 如果没有找到返回null
     * @throws IllegalStateException 枚举存在重复 code（缓存构建时抛出）
     */
    public static <C, E extends Enum<E> & IEnum<C>> E fromCode(Class<E> enumType, C code) {
        if (enumType == null || code == null || !enumType.isEnum()) {
            return null;
        }

        return enumType.cast(getCodeMap(enumType).get(code));
    }

    /**
     * String code 版本，与 {@link #fromCode} 共享 code 缓存。
     * <p>IEnum 枚举直接走缓存 map；非 IEnum 枚举优先调用其静态 valueOfCode 方法（Method 已缓存），
     * 调用失败（如 int code 枚举传入名称导致 parseInt 失败）或无该方法时，
     * 落回 {@code Enum.valueOf} 按常量名称解析（名称也不匹配时抛 {@link IllegalArgumentException}）。
     *
     * @param enumType 枚举class
     * @param code     code（允许为 ''，不做 isBlank 短路）
     * @return 如果没有找到返回null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Enum<?> valueOfCode(Class<?> enumType, String code) {
        if (enumType == null || code == null || !enumType.isEnum()) {
            return null;
        }

        if (IEnum.class.isAssignableFrom(enumType)) {
            return getCodeMap(enumType).get(code);
        }

        Method method = VALUE_OF_CODE_METHOD_CACHE
                .computeIfAbsent(enumType, EnumUtils::findValueOfCodeMethod)
                .orElse(null);

        if (method != null) {
            try {
                Class<?> parameterType = method.getParameterTypes()[0];
                Object arg = (parameterType == Integer.class || parameterType == int.class)
                        ? Integer.parseInt(code)
                        : code;
                return (Enum<?>) method.invoke(null, arg);
            } catch (Exception e) {
                // 属于正常回退路径（如按名称查找），不吞栈但仅记 debug
                log.debug("调用 {} 的静态 {}(\"{}\") 失败，回退按常量名称解析",
                        enumType.getName(), VALUE_OF_CODE_METHOD_NAME, code, e);
            }
        }

        return Enum.valueOf((Class) enumType, code);
    }

    public static List<String> getCodes(Class enumType) {
        if (IEnum.class.isAssignableFrom(enumType)) {
            IEnum[] enumConstants = (IEnum[]) enumType.getEnumConstants();
            return Arrays.stream(enumConstants).map(enumConstant -> String.valueOf(enumConstant.getCode())).collect(Collectors.toList());
        } else {
            Object[] enumConstants = enumType.getEnumConstants();
            return Arrays.stream(enumConstants).map(enumConstant -> String.valueOf(enumConstant)).collect(Collectors.toList());
        }
    }

    /**
     * 获取code值
     * @param en
     * @return
     */
    public static Object getCode(Enum en) {
        return getData(en, GET_CODE_METHOD_NAME);
    }

    /**
     * 获取code值
     * @param en
     * @return
     */
    public static Object getLabel(Enum en) {
        return getData(en, GET_LABEL_METHOD_NAME);
    }

    /**
     * 获取值
     * @param getMethodName
     * @return
     */
    public static Object getData(Enum en, String getMethodName) {
        try {
            Method getCodeMethod = en.getClass().getMethod(getMethodName);
            return getCodeMethod.invoke(en);
        } catch (Exception e) {
            return en.name();
        }
    }

    private static Map<Object, Enum<?>> getCodeMap(Class<?> enumClass) {
        return CACHE.computeIfAbsent(enumClass, type -> {
            Map<Object, Enum<?>> map = new HashMap<>();

            for (Object constant : enumClass.getEnumConstants()) {
                IEnum<?> e = (IEnum<?>) constant;
                Object old = map.put(e.getCode(), (Enum<?>) constant);

                if (old != null) {
                    throw new IllegalStateException(
                            "重复的枚举编码：" + e.getCode()
                    );
                }
            }
            return Collections.unmodifiableMap(map);
        });
    }

    private static Optional<Method> findValueOfCodeMethod(Class<?> enumType) {
        return Arrays.stream(enumType.getMethods())
                .filter(m -> m.getName().equals(VALUE_OF_CODE_METHOD_NAME))
                .filter(m -> m.getParameterCount() == 1)
                .findFirst();
    }

}
