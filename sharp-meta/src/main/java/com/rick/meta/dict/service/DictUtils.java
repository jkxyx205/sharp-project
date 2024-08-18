package com.rick.meta.dict.service;


import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/27/19 10:40 AM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
final public class DictUtils {

    public static Map<String, List<Dict>> dictMap;

    public static List<Dict> getDict(String key) {
        return ListUtils.unmodifiableList(ListUtils.emptyIfNull(dictMap.get(key)));
    }

    public static Optional<Dict> getDictLabel(String key, String name) {
        List<Dict> dictList = getDict(key);
        if (CollectionUtils.isEmpty(dictList)) {
            return Optional.empty();
        }

        for (Dict dict : dictList) {
            if (Objects.equals(dict.getName(), name)) {
                return Optional.of(dict);
            }
        }

        return Optional.empty();
    }

    public static void fillDictLabel(Object obj) {
        if (obj == null) {
            return;
        }

        Field[] allFields = FieldUtils.getAllFields(obj.getClass());
        for (Field field : allFields) {
            Method method;
            try {
                method = obj.getClass().getMethod("get" + String.valueOf(field.getName().charAt(0)).toUpperCase() + field.getName().substring(1));
                Object fieldValue = ReflectionUtils.invokeMethod(method, obj);

                DictType dictType = field.getAnnotation(DictType.class);
                if (field.getType() == DictValue.class && dictType != null && fieldValue != null) {
                    String type = dictType.type();
                    DictValue dictValue = (DictValue) fieldValue;
                    if (StringUtils.isNotBlank(dictValue.getCode())) {
                        Optional<Dict> dictLabel = getDictLabel(type, dictValue.getCode());
                        dictLabel.ifPresent(value -> dictValue.setLabel(value.getLabel()));
                    }
                } else if (mayEntityObject(fieldValue)) {
                    fillDictLabel(fieldValue);
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static boolean mayEntityObject(Object obj) {
        if (obj == null) {
            return false;
        }

        if (Number.class.isAssignableFrom(obj.getClass())) {
            return false;
        } else if (obj instanceof DictValue) {
            return false;
        } else if (obj instanceof String) {
            return false;
        } else if (obj instanceof Character) {
            return false;
        } else if (obj instanceof Boolean) {
            return false;
        } else if (obj.getClass().isEnum()) {
            return false;
        } else if (obj.getClass().isArray()) {
            return false;
        } else if (Temporal.class.isAssignableFrom(obj.getClass())) {
            return false;
        } else if (obj.getClass().isPrimitive()) {
            return false;
        } else if (Collection.class.isAssignableFrom(obj.getClass()) || Map.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        return true;
    }
}