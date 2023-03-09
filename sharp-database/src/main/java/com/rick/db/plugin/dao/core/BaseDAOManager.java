package com.rick.db.plugin.dao.core;

import com.google.common.collect.Maps;
import com.rick.db.plugin.dao.annotation.Embedded;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.BeanInitializationException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.rick.common.util.ReflectUtils.getAllFields;

/**
 * @author Rick
 * @createdAt 2021-10-31 09:09:00
 */
@Slf4j
public class BaseDAOManager {

    public static List<BaseDAO> baseDAOList;

    public static Map<String, BaseDAO> baseDAOTableNameMap;

    public static Map<Class, BaseDAO> baseDAOEntityMap;

    public static Map<Class, List<Field>> entityEmbeddedMap;

    public static Map<Class, Map<String, PropertyDescriptor>> entityPropertyDescriptorMap;

    public static Set<Class> entitiesClass = new HashSet<>();

    private static boolean hasAutowired = false;

    public static void setBaseDAOList(List<BaseDAO> baseDAOList) {
        if (!hasAutowired) {
            baseDAOList = Objects.isNull(baseDAOList) ? Collections.emptyList() : baseDAOList;
            BaseDAOManager.baseDAOList = baseDAOList;
            BaseDAOManager.baseDAOTableNameMap = Objects.nonNull(baseDAOList) ? BaseDAOManager.baseDAOList.stream().collect(Collectors.toMap(d -> d.getTableName(), v -> v)) : Collections.emptyMap();
            BaseDAOManager.baseDAOEntityMap = Objects.nonNull(baseDAOList) ? BaseDAOManager.baseDAOList.stream().collect(Collectors.toMap(d -> d.getEntityClass(), v -> v)) : Collections.emptyMap();
            BaseDAOManager.entityEmbeddedMap = Objects.nonNull(baseDAOList) ? BaseDAOManager.baseDAOList.stream().collect(Collectors.toMap(d -> d.getEntityClass(), v -> v.getTableMeta().getEmbeddedPropertyList().stream().map(TableMeta.EmbeddedProperty::getField).collect(Collectors.toList()))) : Collections.emptyMap();
            BaseDAOManager.entityPropertyDescriptorMap = Maps.newHashMapWithExpectedSize(baseDAOList.size());

            for (BaseDAO baseDAO : baseDAOList) {
                Field[] entityFields = getAllFields(baseDAO.getEntityClass());
                handleFields(entityFields,  entityPropertyDescriptorMap, baseDAO.getEntityClass());
                entitiesClass.add(baseDAO.getEntityClass());
            }

            BaseDAOManager.hasAutowired = true;
        }
    }

    private static void handleFields(Field[] fields, Map<Class, Map<String, PropertyDescriptor>> entityPropertyDescriptorMap, Class<?> clazz) {
        Map<String, PropertyDescriptor> propertyDescriptorMap = Maps.newHashMapWithExpectedSize(fields.length);;
        for (Field field : fields) {
            try {
                propertyDescriptorMap.put(field.getName(), new PropertyDescriptor(field.getName(), clazz));

                Embedded annotation = field.getAnnotation(Embedded.class);
                if (annotation != null) {
                    Field[] embeddedFields = getAllFields(field.getType());
                    handleFields(embeddedFields, entityPropertyDescriptorMap, field.getType());
                }
            } catch (IntrospectionException e) {
                throw new BeanInitializationException(field.getType() + "初始化异常", e);
            }
        }

        entityPropertyDescriptorMap.put(clazz, propertyDescriptorMap);
    }

    public static boolean isEntityClass(Class clazz) {
        return BaseDAOManager.entitiesClass.contains(clazz);
    }

    public static TableMeta getTableMeta(Class clazz) {
        return baseDAOEntityMap.get(clazz).getTableMeta();
    }

    public static void setPropertyValue(Object value, String propertyName, Object propertyValue) {
        if (Objects.isNull(propertyValue) || Objects.isNull(value)) {
            return;
        }

        setPropertyValue(value, propertyName, propertyValue, getEntityClass(value));
    }

    private static boolean setPropertyValue(Object value, String propertyName, Object propertyValue, Class<?> entityClass) {
        if (Objects.isNull(propertyValue) || Objects.isNull(value)) {
            return false;
        }

        try {
            Map<String, PropertyDescriptor> propertyDescriptorMapping = BaseDAOManager.entityPropertyDescriptorMap.get(entityClass);
            if (propertyDescriptorMapping.get(propertyName) == null) {
                // embedded
                List<Field> embeddedFields = entityEmbeddedMap.get(entityClass);
                if (CollectionUtils.isNotEmpty(embeddedFields)) {
                    for (Field embeddedField : embeddedFields) {
                        if (setPropertyValue(getPropertyValue(value, embeddedField.getName()), propertyName, propertyValue, embeddedField.getType())) {
                            return true;
                        }

                    }
                }

            } else {
                boolean hasPropertyName = propertyDescriptorMapping.get(propertyName) != null;
                if (hasPropertyName) {
                    propertyDescriptorMapping.get(propertyName).getWriteMethod().invoke(value, propertyValue);
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            log.error("Cannot get ["+propertyName+"] value, may you lost " + value.getClass().getSimpleName() + "DAO or lost ["+propertyName+"] property for class ["+value.getClass().getSimpleName()+"]");
            e.printStackTrace();
        }

        return false;
    }

    public static Object getPropertyValue(Object value, String propertyName) {
        if (Objects.isNull(value)) {
            return null;
        }

        return getPropertyValue(value, propertyName, getEntityClass(value));
    }

    private static Object getPropertyValue(Object value, String propertyName, Class<?> entityClass) {
        if (Objects.isNull(value)) {
            return null;
        }

        String[] split = propertyName.split("\\.");

        try {
            Object propertyNameValue = null;
            Map<String, PropertyDescriptor> propertyDescriptorMapping = BaseDAOManager.entityPropertyDescriptorMap.get(entityClass);
            if (propertyDescriptorMapping.get(split[0]) == null) {
                // embedded
                List<Field> embeddedFields = entityEmbeddedMap.get(entityClass);
                if (CollectionUtils.isNotEmpty(embeddedFields)) {
                    for (Field embeddedField : embeddedFields) {
                        propertyNameValue = getPropertyValue(value, embeddedField.getName() + "." + propertyName, entityClass);
                        if (propertyNameValue != null) {
                            return propertyNameValue;
                        }
                    }
                }

            } else {
                propertyNameValue = propertyDescriptorMapping.get(split[0]).getReadMethod().invoke(value);
            }

            if (split.length == 1) {
                return propertyNameValue;
            }

            if(propertyNameValue == null) {
                return null;
            }

            return getPropertyValue(propertyNameValue, split[1], propertyNameValue.getClass());

        } catch (Exception e) {
            log.error("Cannot get ["+propertyName+"] value, may you lost " + value.getClass().getSimpleName() + "DAO or lost ["+propertyName+"] property for class ["+value.getClass().getSimpleName()+"]");
            e.printStackTrace();
        }
        return null;
    }

    private static Class getEntityClass(Object value) {
        Class<?> entityClass = value.getClass();
        while (Objects.nonNull(entityClass)) {
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            if (Objects.isNull(tableAnnotation)) {
                entityClass = entityClass.getSuperclass();
            } else {
                break;
            }
        }

        return entityClass;
    }



}
