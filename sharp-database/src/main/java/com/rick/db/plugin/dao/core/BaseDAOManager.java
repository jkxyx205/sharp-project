package com.rick.db.plugin.dao.core;

import com.google.common.collect.Maps;
import com.rick.common.util.ReflectUtils;
import org.springframework.beans.factory.BeanInitializationException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-10-31 09:09:00
 */
public class BaseDAOManager {

    public static List<BaseDAO> baseDAOList;

    public static Map<String, BaseDAO> baseDAOTableNameMap;

    public static Map<Class, BaseDAO> baseDAOEntityMap;

    public static Map<Class, Map<String, PropertyDescriptor>> entityPropertyDescriptorMap;

    public static Set<Class> entitiesClass = new HashSet<>();

    private static boolean hasAutowired = false;

    public static void setBaseDAOList(List<BaseDAO> baseDAOList) {
        if (!hasAutowired) {
            baseDAOList = Objects.isNull(baseDAOList) ? Collections.emptyList() : baseDAOList;
            BaseDAOManager.baseDAOList = baseDAOList;
            BaseDAOManager.baseDAOTableNameMap = Objects.nonNull(baseDAOList) ? BaseDAOManager.baseDAOList.stream().collect(Collectors.toMap(d -> d.getTableName(), v -> v)) : Collections.emptyMap();
            BaseDAOManager.baseDAOEntityMap = Objects.nonNull(baseDAOList) ? BaseDAOManager.baseDAOList.stream().collect(Collectors.toMap(d -> d.getEntityClass(), v -> v)) : Collections.emptyMap();
            BaseDAOManager.entityPropertyDescriptorMap = Maps.newHashMapWithExpectedSize(baseDAOList.size());

            for (BaseDAO baseDAO : baseDAOList) {
                if (baseDAO.getEntityClass() == Map.class) {
                    continue;
                }
                Field[] entityFields = ReflectUtils.getAllFields(baseDAO.getEntityClass());
                Map<String, PropertyDescriptor> propertyDescriptorMap = Maps.newHashMapWithExpectedSize(entityFields.length);;
                for (Field entityField : entityFields) {
                    try {
                        propertyDescriptorMap.put(entityField.getName(), new PropertyDescriptor(entityField.getName(), baseDAO.getEntityClass()));
                    } catch (IntrospectionException e) {
                        throw new BeanInitializationException(baseDAO.getTableName() + "初始化异常", e);
                    }
                }

                entityPropertyDescriptorMap.put(baseDAO.getEntityClass(), propertyDescriptorMap);
                entitiesClass.add(baseDAO.getEntityClass());
            }

            BaseDAOManager.hasAutowired = true;
        }
    }

    public static boolean isEntityClass(Class clazz) {
        return BaseDAOManager.entitiesClass.contains(clazz);
    }

}
