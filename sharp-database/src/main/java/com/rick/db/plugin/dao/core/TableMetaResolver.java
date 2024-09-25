package com.rick.db.plugin.dao.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.util.ClassUtils;
import com.rick.db.dto.type.*;
import com.rick.db.plugin.dao.annotation.*;
import com.rick.db.plugin.dao.support.BaseEntityUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static com.rick.common.util.StringUtils.camelToSnake;

/**
 * @author Rick
 * @date 2021-09-27 11:41:00
 */
@UtilityClass
@Slf4j
class TableMetaResolver {

    private static final String TABLE_PREFIX = "";

    private static final String DEFAULT_PRIMARY_COLUMN = "id";

    public static TableMeta resolve(Class<?> clazz) {
        if (!BaseEntityUtils.isEntityClass(clazz)) {
            log.warn(clazz.getSimpleName() + " forgot extends from Entity?");
        }

        Table tableAnnotation = clazz.getAnnotation(Table.class);

        if (Objects.isNull(tableAnnotation)) {
            throw new IllegalArgumentException("Lost @Table annotation");
        }

        String name = (TABLE_PREFIX + camelToSnake(clazz.getSimpleName()));
        String tableName = StringUtils.isBlank(tableAnnotation.value()) ? name : tableAnnotation.value();

        Field[] fields = ClassUtils.getAllFields(clazz);
        StringBuilder columnNamesBuilder = new StringBuilder();
        StringBuilder updateColumnNamesBuilder = new StringBuilder();
        StringBuilder propertiesBuilder = new StringBuilder();
        StringBuilder updatePropertiesBuilder = new StringBuilder();

        List<TableMeta.EmbeddedProperty> embeddedPropertyList = Lists.newArrayList();
        List<TableMeta.SelectProperty> selectAnnotationList = Lists.newArrayList();
        List<TableMeta.SqlProperty> sqlAnnotationList = Lists.newArrayList();
        List<TableMeta.OneToManyProperty> oneToManyAnnotationList = Lists.newArrayList();
        List<TableMeta.ManyToOneProperty> manyToOneAnnotationList = Lists.newArrayList();
        List<TableMeta.ManyToManyProperty> manyToManyAnnotationList = Lists.newArrayList();
        Map<String, Field> columnNameFieldMap = Maps.newHashMap();
        Map<String, Field> fieldMap = Maps.newHashMap();
        Map<String, Column> columnNameMap = Maps.newHashMap();
        Set<Method> computedMethods = new HashSet<>();

        IdCollector idCollector = new IdCollector();
        VersionCollector versionCollector = new VersionCollector();
        resolveFields(clazz, fields, "", "", idCollector, versionCollector, embeddedPropertyList, selectAnnotationList, sqlAnnotationList,
                oneToManyAnnotationList, manyToOneAnnotationList,
                manyToManyAnnotationList, columnNameFieldMap, columnNameMap, fieldMap,
                columnNamesBuilder, updateColumnNamesBuilder, propertiesBuilder, updatePropertiesBuilder);

        propertiesBuilder.deleteCharAt(propertiesBuilder.length() - 1);
        columnNamesBuilder.deleteCharAt(columnNamesBuilder.length() - 1);

        if (StringUtils.isNotBlank(updateColumnNamesBuilder.toString())) {
            updateColumnNamesBuilder.deleteCharAt(updateColumnNamesBuilder.length() - 1);
            updatePropertiesBuilder.deleteCharAt(updatePropertiesBuilder.length() - 1);
        }

        if (Objects.isNull(idCollector.id)) {
            idCollector.columnName = idCollector.propertyName = DEFAULT_PRIMARY_COLUMN;
            idCollector.field = fieldMap.get(idCollector.propertyName);
            idCollector.clazz = getIdClass(clazz, idCollector.propertyName);
        }

        // 获取计算属性
        Method[] methods = clazz.getMethods();
        Set<String> fieldsName = Arrays.stream(fields).map(field -> field.getName()).collect(Collectors.toSet());
        for (Method method : methods) {
            if (method.getName().startsWith("get")) {
                String computedProperty = EntityDAOHelper.getComputedProperty(method.getName());
                if (!fieldsName.contains(computedProperty) && !computedProperty.equals("class")) {
                    computedMethods.add(method);
                }
            }
        }

        return new TableMeta(tableAnnotation, idCollector.id, idCollector.field, idCollector.clazz, new TableMeta.VersionProperty(versionCollector.columnName, versionCollector.propertyName), name, tableName, columnNamesBuilder.toString(), propertiesBuilder.toString(), updateColumnNamesBuilder.toString(),
                updatePropertiesBuilder.toString(), idCollector.columnName, idCollector.propertyName, embeddedPropertyList, selectAnnotationList, sqlAnnotationList, oneToManyAnnotationList, manyToOneAnnotationList, manyToManyAnnotationList
                , columnNameFieldMap, fieldMap, columnNameMap, computedMethods);
    }

    private void resolveFields(Class clazz, Field[] fields, String propertyNamePrefix, String columnPrefix, IdCollector idCollector, VersionCollector versionCollector, List<TableMeta.EmbeddedProperty> embeddedPropertyList, List<TableMeta.SelectProperty> selectAnnotationList,
                               List<TableMeta.SqlProperty> sqlAnnotationList,
                               List<TableMeta.OneToManyProperty> oneToManyAnnotationList, List<TableMeta.ManyToOneProperty> manyToOneAnnotationList,
                               List<TableMeta.ManyToManyProperty> manyToManyAnnotationList, Map<String, Field> columnNameFieldMap, Map<String, Column> columnNameMap, Map<String, Field> fieldMap,
                               StringBuilder columnNamesBuilder, StringBuilder updateColumnNamesBuilder, StringBuilder propertiesBuilder, StringBuilder updatePropertiesBuilder) {
        for (Field field : fields) {
            String propertyName = propertyNamePrefix + field.getName();
            fieldMap.put(propertyName, field);

            Select selectAnnotation = field.getAnnotation(Select.class);
            if (selectAnnotation != null) {
                Class<?> subEntityClass;

                if (selectAnnotation.oneToOne()) {
                    subEntityClass = field.getType();
                } else {
                    subEntityClass = ClassUtils.getFieldGenericClass(field);
                }

                selectAnnotationList.add(new TableMeta.SelectProperty(selectAnnotation, field, subEntityClass));
            }

            Sql sqlAnnotation = field.getAnnotation(Sql.class);
            if (sqlAnnotation != null) {
                Class<?> targetClass;

                if (Collection.class.isAssignableFrom(field.getType())) {
                    targetClass = ClassUtils.getFieldGenericClass(field);
                } else {
                    targetClass = field.getType();
                }

                sqlAnnotationList.add(new TableMeta.SqlProperty(sqlAnnotation, field, targetClass));
            }

            OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
            if (oneToManyAnnotation != null) {
                Class<?> subEntityClass;

                if (oneToManyAnnotation.oneToOne()) {
                    subEntityClass = field.getType();
                } else {
                    subEntityClass = ClassUtils.getFieldGenericClass(field);
                }

                oneToManyAnnotationList.add(new TableMeta.OneToManyProperty(oneToManyAnnotation, field,
                        oneToManyAnnotation.subEntityClass() == Void.class ? subEntityClass : oneToManyAnnotation.subEntityClass()));
            }

            ManyToOne manyToOneAnnotation = field.getAnnotation(ManyToOne.class);
            if (manyToOneAnnotation != null) {
                manyToOneAnnotationList.add(new TableMeta.ManyToOneProperty(manyToOneAnnotation, field));
            }

            ManyToMany manyToManyAnnotation = field.getAnnotation(ManyToMany.class);
            if (manyToManyAnnotation != null) {
                manyToManyAnnotationList.add(new TableMeta.ManyToManyProperty(manyToManyAnnotation, field));
            }

            Embedded embedded = field.getAnnotation(Embedded.class);
            if (embedded != null) {
                embeddedPropertyList.add(new TableMeta.EmbeddedProperty(embedded, field));
                Field[] embeddedFields = ClassUtils.getAllFields(field.getType());
                resolveFields(clazz, embeddedFields, propertyName + ".", embedded.columnPrefix(), idCollector, versionCollector, embeddedPropertyList,
                        selectAnnotationList, sqlAnnotationList, oneToManyAnnotationList, manyToOneAnnotationList,
                        manyToManyAnnotationList, columnNameFieldMap, columnNameMap, fieldMap,
                        columnNamesBuilder, updateColumnNamesBuilder, propertiesBuilder, updatePropertiesBuilder);
            }

            if (AnnotatedElementUtils.hasAnnotation(field, Transient.class)) {
                continue;
            }

            if (idCollector.id == null) {
                idCollector.id = field.getAnnotation(Id.class);
                if (Objects.nonNull(idCollector.id)) {
                    idCollector.columnName = StringUtils.isNotBlank(idCollector.id.value()) ? idCollector.id.value() : camelToSnake(field.getName());
                    idCollector.propertyName = propertyName;
                    idCollector.field = field;
                    idCollector.clazz = getIdClass(clazz, idCollector.propertyName);
                }
            }

            Column annotation = AnnotatedElementUtils.getMergedAnnotation(field, Column.class);
            String columnName = columnPrefix + (Objects.nonNull(annotation) && StringUtils.isNotBlank(annotation.value())
                    ? annotation.value() : camelToSnake(Objects.nonNull(manyToOneAnnotation) ? field.getType().getSimpleName() + "_" + DEFAULT_PRIMARY_COLUMN : field.getName()));

            columnNameFieldMap.put(columnName, field);
            columnNameMap.put(columnName, annotation);

            Version version = field.getAnnotation(Version.class);
            if (version != null) {
                versionCollector.propertyName = propertyName;
                versionCollector.columnName =columnName;
            }

            propertiesBuilder.append(propertyName).append(",");
            columnNamesBuilder.append(columnName).append(",");

            boolean isUpdatableColumn = (Objects.isNull(annotation) || annotation.updatable()) && Objects.isNull(field.getAnnotation(Id.class));
            if (isUpdatableColumn) {
                updateColumnNamesBuilder.append(columnName).append(",");
                updatePropertiesBuilder.append(propertyName).append(",");
            }

        }
    }

    private Class getIdClass(Class<?> clazz, String propertyName) {
        if (BaseEntityWithLongId.class.isAssignableFrom(clazz) ||
                BaseEntityWithIdentity.class.isAssignableFrom(clazz) ||
                BaseCodeEntityWithLongId.class.isAssignableFrom(clazz) ||
                BaseCodeEntityWithIdentity.class.isAssignableFrom(clazz) ||
                BaseCodeDescriptionEntityWithLongId.class.isAssignableFrom(clazz)) {
            return Long.class;
        } else if (BaseEntityWithStringId.class.isAssignableFrom(clazz) || BaseCodeEntityWithStringId.class.isAssignableFrom(clazz)) {
            return String.class;
        } else {
            Class<?>[] classGenericsTypes;
            Class<?> curClazz = clazz;

            while (curClazz != Object.class) {
                classGenericsTypes = ClassUtils.getClassGenericsTypes(curClazz);
                if (classGenericsTypes != null) {
                    return classGenericsTypes[0];
                }
                curClazz = curClazz.getSuperclass();
            }

            try {
                return ClassUtils.getField(clazz, propertyName).getType();
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class IdCollector {

        private Id id;

        private String columnName;

        private String propertyName;

        private Field field;

        private Class clazz; // 从 field 获取的是 Object

    }

    private class VersionCollector {

        private String columnName;

        private String propertyName;

    }

}
