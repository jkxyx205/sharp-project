package com.rick.db.repository.support;

import com.rick.common.util.ClassUtils;
import com.rick.db.repository.*;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Rick.Xu
 * @date 2025/8/21 18:53
 */
@UtilityClass
public class TableMetaResolver {

    public <T> TableMeta resolve(Class<T> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        TableMeta tableMeta = new TableMeta(entityClass, tableAnnotation, Objects.nonNull(tableAnnotation) ? tableAnnotation.value() : com.rick.common.util.StringUtils.camelToSnake(entityClass.getSimpleName()), com.rick.common.util.StringUtils.camelToSnake(entityClass.getSimpleName()) + "_id",
                new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());

        StringBuilder selectColumnBuilder = new StringBuilder();
        StringBuilder updateColumnBuilder = new StringBuilder();

        loopAllFields(tableMeta, selectColumnBuilder, updateColumnBuilder, entityClass, "", "");

        tableMeta.setSelectColumn(StringUtils.substringBeforeLast(selectColumnBuilder.toString(), ", "));
        tableMeta.setUpdateColumn(StringUtils.substringBeforeLast(updateColumnBuilder.toString(), ", "));

        return tableMeta;
    }

    private static void loopAllFields(TableMeta tableMeta, StringBuilder selectColumnBuilder, StringBuilder updateColumnBuilder,
                                      Class<?> entityClass,
                                      String columnPrefix, String propertyPrefix) {
        Field[] allFields = uniqueFields(FieldUtils.getAllFields(entityClass));
        for (Field field : allFields) {
            Embedded embedded = field.getAnnotation(Embedded.class);
            if (Objects.nonNull(embedded)) {
                loopAllFields(tableMeta, selectColumnBuilder, updateColumnBuilder, field.getType(), embedded.columnPrefix(), field.getName() + ".");
                continue;
            }

            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            Select select = field.getAnnotation(Select.class);

            if (Objects.nonNull(manyToMany) || Objects.nonNull(manyToOne) || Objects.nonNull(oneToMany) || Objects.nonNull(select)) {
                TableMeta.Reference reference = new TableMeta.Reference();
                reference.field = field;

                if (Objects.nonNull(manyToMany)) {
                    reference.manyToMany = manyToMany;
                    reference.referenceClass = ClassUtils.getFieldGenericClass(field)[0];
                } else if (Objects.nonNull(oneToMany)) {
                    reference.oneToMany = oneToMany;
                    if (oneToMany.oneToOne()) {
                        reference.referenceClass = field.getType();
                    } else {
                        reference.referenceClass = ClassUtils.getFieldGenericClass(field)[0];
                    }
                } else if (Objects.nonNull(manyToOne)) {
                    reference.manyToOne = manyToOne;
                    reference.referenceClass = field.getType();
                    tableMeta.getReferenceMap().put(field, reference);
                } else if (Objects.nonNull(select)) {
                    reference.select = select;
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        reference.referenceClass = ClassUtils.getFieldGenericClass(field)[0];
                    } else {
                        reference.referenceClass = field.getType();
                    }
                }

                tableMeta.getReferenceMap().put(field, reference);
            }

            if (AnnotatedElementUtils.hasAnnotation(field, Transient.class)) {
                continue;
            }

            Column columnAnnotation = AnnotatedElementUtils.getMergedAnnotation(field, Column.class);

            String columnName = null;
            if (Objects.nonNull(columnAnnotation)) {
                columnName = columnAnnotation.value();
            }
            columnName = columnPrefix + StringUtils.defaultIfBlank(columnName, Objects.nonNull(manyToOne) ? com.rick.common.util.StringUtils.camelToSnake(field.getName()) + "_id" : com.rick.common.util.StringUtils.camelToSnake(field.getName()));

            Id idAnnotation = AnnotatedElementUtils.getMergedAnnotation(field, Id.class);

            if (Objects.nonNull(idAnnotation) && Objects.isNull(tableMeta.idMeta)) {
                tableMeta.idMeta = new TableMeta.IdMeta(getIdClass(entityClass, field.getName()), idAnnotation, columnName, field.getName(), field);
            }

            if (AnnotatedElementUtils.hasAnnotation(field, Version.class)) {
                tableMeta.versionField = field;
            }

            if (!tableMeta.getColumnPropertyNameMap().keySet().contains(columnName)) {
                String propertyName = propertyPrefix + field.getName();
                tableMeta.getColumnPropertyNameMap().put(columnName, propertyName);
                selectColumnBuilder.append(columnName + " AS \"" + propertyName + "\"").append(", ");

                if (!(Objects.nonNull(tableMeta.idMeta) && columnName.equals(tableMeta.idMeta.getIdPropertyName()) || Objects.nonNull(columnAnnotation) && !columnAnnotation.updatable())) {
                    updateColumnBuilder.append(columnName).append(", ");
                }

                tableMeta.getFieldColumnNameMap().put(field, columnName);
                tableMeta.getFieldPropertyNameMap().put(field, propertyName);

                tableMeta.getColumnNameMap().put(columnName, columnAnnotation);
            }

        }
    }

    private Class getIdClass(Class<?> clazz, String propertyName) {
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

    public static Field[] uniqueFields(Field[] fields) {
        Map<String, Field> map = new LinkedHashMap<>();
        for (Field f : fields) {
            // 如果不存在才放进去（只保留第一个）
            map.putIfAbsent(f.getName(), f);
        }
        return map.values().toArray(new Field[0]);
    }
}
