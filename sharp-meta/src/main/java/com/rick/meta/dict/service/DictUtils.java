package com.rick.meta.dict.service;


import com.rick.common.util.ObjectUtils;
import com.rick.db.repository.TableDAO;
import com.rick.db.util.OperatorUtils;
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

    static TableDAO tableDAO;

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

    /**
     * 任意对象， 可以是 entity Map List Dict
     * @param obj
     */
    public static void fillDictLabel(Object obj) {
        if (obj == null) {
            return;
        }

        if (Iterable.class.isAssignableFrom(obj.getClass())) {
            // 集合
            Iterable iterable = (Iterable) obj;
            Iterator iterator = iterable.iterator();
            while (iterator.hasNext()) {
                obj = iterator.next();
                fillDictLabel(obj);
            }
        } else if (Map.class.isAssignableFrom(obj.getClass())) {
            // Map
            Map map = (Map)obj;
            fillDictLabel(map.keySet());
            fillDictLabel(map.values());
        }

        if (obj instanceof DictValue) {
            DictValue dictValue = (DictValue) obj;
            if (StringUtils.isNotBlank(dictValue.getCode()) && StringUtils.isNotBlank(dictValue.getType())) {
                getDictLabel(dictValue.getType(), dictValue.getCode()).ifPresent(dict -> dictValue.setLabel(dict.getLabel()));
            }

            return;
        } else if (!mayEntityObject(obj)) {
            return;
        }

        Field[] allFields = FieldUtils.getAllFields(obj.getClass());
        for (Field field : allFields) {
            Method method;
            try {
                method = obj.getClass().getMethod("get" + String.valueOf(field.getName().charAt(0)).toUpperCase() + field.getName().substring(1));
                Object fieldValue = ReflectionUtils.invokeMethod(method, obj);
                if (fieldValue == null) {
                    continue;
                }

                DictType dictType = field.getAnnotation(DictType.class);
                if (field.getType() == DictValue.class && dictType != null && fieldValue != null) {

                    DictValue dictValue = (DictValue) fieldValue;
                    if (StringUtils.isNotBlank(dictValue.getCode())) {
                        String type = dictType.type();
                        if (StringUtils.isNotBlank(type)) {
                            Optional<Dict> dictLabel = getDictLabel(type, dictValue.getCode());
                            dictLabel.ifPresent(value -> {
                                dictValue.setLabel(value.getLabel());
                                dictValue.setType(value.getType());
                            });
                        } else {
                            OperatorUtils.expectedAsOptional(tableDAO.select(dictType.sql(), dictValue.getCode())).ifPresent(value -> {
                                dictValue.setLabel((String) value.get("label"));
                            });
                        }
                    }
                } else if (Iterable.class.isAssignableFrom(fieldValue.getClass())) {
                    // 集合
                    Iterable iterable = (Iterable) fieldValue;
                    Iterator iterator = iterable.iterator();
                    while (iterator.hasNext()) {
                        fieldValue = iterator.next();
                        if (fieldValue instanceof DictValue) {
                            Optional<Dict> dictLabel = getDictLabel(dictType.type(), ((DictValue) fieldValue).getCode());
                            if (dictLabel.isPresent()) {
                                ((DictValue)fieldValue).setLabel(dictLabel.get().getLabel());
                                ((DictValue)fieldValue).setType(dictType.type());
                            }
                        } else if (mayEntityObject(fieldValue)) {
                            fillDictLabel(fieldValue);
                        }
                    }
                } else if (Map.class.isAssignableFrom(fieldValue.getClass())) {
                    // Map
                    Map map = (Map)fieldValue;
                    fillDictLabel(map.keySet());
                    fillDictLabel(map.values());
                } else if (mayEntityObject(fieldValue)) {
                    fillDictLabel(fieldValue);
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static boolean mayEntityObject(Object obj) {
        return ObjectUtils.mayPureObject(obj);
    }
}