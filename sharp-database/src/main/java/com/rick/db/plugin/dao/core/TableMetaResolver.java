package com.rick.db.plugin.dao.core;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rick.common.util.ReflectUtils;
import com.rick.db.plugin.dao.annotation.*;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:41:00
 */
@UtilityClass
class TableMetaResolver {

    private static final String TABLE_PREFIX = "";

    private static final String PRIMARY_COLUMN = "id";

    public static TableMeta resolve(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        Converter<String, String> converter = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

        String name = (TABLE_PREFIX + converter.convert(clazz.getSimpleName()));

        String tableName;
        Set<String> subTables = Sets.newHashSet();
        if (Objects.nonNull(tableAnnotation)) {
            tableName = tableAnnotation.value();
            subTables.addAll(Arrays.asList(tableAnnotation.subTables()));
        } else {
            tableName = name;
        }

        Field[] fields = ReflectUtils.getAllFields(clazz);
        StringBuilder columnNamesBuilder = new StringBuilder();
        StringBuilder updateColumnNamesBuilder = new StringBuilder();
        StringBuilder propertiesBuilder = new StringBuilder();
        StringBuilder updatePropertiesBuilder = new StringBuilder();
        Map<String, TableMeta.OneToManyProperty> oneToManyAnnotationMap = Maps.newHashMap();
        Map<String, TableMeta.ManyToOneProperty> manyToOneAnnotationMap = Maps.newHashMap();
        Map<String, TableMeta.ManyToManyProperty> manyToManyAnnotationMap = Maps.newHashMap();
        Map<String, Field> columnNameFieldMap = Maps.newHashMap();
        Map<String, Column> columnNameMap = Maps.newHashMap();

        String idColumnName = null;
        for (Field field : fields) {
            OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
            if (oneToManyAnnotation != null) {
                subTables.add(oneToManyAnnotation.subTable());
                oneToManyAnnotationMap.put(oneToManyAnnotation.subTable(), new TableMeta.OneToManyProperty(oneToManyAnnotation, field));
            }

            ManyToOne manyToOneAnnotation = field.getAnnotation(ManyToOne.class);
            if (manyToOneAnnotation != null) {
                manyToOneAnnotationMap.put(manyToOneAnnotation.parentTable(), new TableMeta.ManyToOneProperty(manyToOneAnnotation, field));
            }

            ManyToMany manyToManyAnnotation = field.getAnnotation(ManyToMany.class);
            if (manyToManyAnnotation != null) {
                subTables.add(manyToManyAnnotation.thirdPartyTable());
                manyToManyAnnotationMap.put(manyToManyAnnotation.thirdPartyTable(), new TableMeta.ManyToManyProperty(manyToManyAnnotation, field));
            }

            if (AnnotatedElementUtils.hasAnnotation(field, Transient.class)) {
                continue;
            }

            Id id = field.getAnnotation(Id.class);
            if (Objects.nonNull(id)) {
                idColumnName = field.getName();
            }

            Column annotation = AnnotatedElementUtils.getMergedAnnotation(field, Column.class);
            String columnName = Objects.nonNull(annotation) && StringUtils.isNotBlank(annotation.value()) ? annotation.value() : converter.convert(field.getName());
            columnNameFieldMap.put(columnName, field);
            columnNameMap.put(columnName, annotation);

            propertiesBuilder.append(field.getName()).append(",");
            columnNamesBuilder.append(columnName).append(",");

            boolean isUpdatableColumn = (Objects.isNull(annotation) || annotation.updatable()) && Objects.isNull(id);
            if (isUpdatableColumn) {
                updateColumnNamesBuilder.append(columnName).append(",");
                updatePropertiesBuilder.append(field.getName()).append(",");
            }

        }
        propertiesBuilder.deleteCharAt(propertiesBuilder.length() - 1);
        columnNamesBuilder.deleteCharAt(columnNamesBuilder.length() - 1);
        updateColumnNamesBuilder.deleteCharAt(updateColumnNamesBuilder.length() - 1);
        updatePropertiesBuilder.deleteCharAt(updatePropertiesBuilder.length() - 1);

        idColumnName = Objects.isNull(idColumnName) ? PRIMARY_COLUMN : idColumnName;
        return new TableMeta(tableAnnotation, name, tableName, columnNamesBuilder.toString(), propertiesBuilder.toString(), updateColumnNamesBuilder.toString(),
                updatePropertiesBuilder.toString(), idColumnName, subTables, oneToManyAnnotationMap, manyToOneAnnotationMap, manyToManyAnnotationMap
                , columnNameFieldMap, columnNameMap);
    }
}
