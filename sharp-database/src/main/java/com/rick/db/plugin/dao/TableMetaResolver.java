package com.rick.db.plugin.dao;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.rick.common.util.ReflectUtils;
import com.rick.db.config.annotation.ColumnName;
import com.rick.db.config.annotation.Id;
import com.rick.db.config.annotation.TableName;
import com.rick.db.config.annotation.Transient;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:41:00
 */
@UtilityClass
class TableMetaResolver {

    private static final String TABLE_PREFIX = "";

    private static final String PRIMARY_COLUMN = "id";

    public static TableMeta resolve(Class<?> clazz) {
        TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
        Converter<String, String> converter = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

        String name = (TABLE_PREFIX + converter.convert(clazz.getSimpleName()));

        String tableName;
        String[] subTables = null;
        if (Objects.nonNull(tableNameAnnotation)) {
            tableName = tableNameAnnotation.value();
            subTables = tableNameAnnotation.subTables();
        } else {
            tableName = name;
        }

        Field[] fields = ReflectUtils.getAllFields(clazz);
        StringBuilder columnNamesBuilder = new StringBuilder();
        StringBuilder updateColumnNamesBuilder = new StringBuilder();
        StringBuilder propertiesBuilder = new StringBuilder();
        StringBuilder updatePropertiesBuilder = new StringBuilder();
        String idColumnName = null;
        for (Field field : fields) {
            if (field.getAnnotation(Transient.class) != null) {
                continue;
            }

            Id id = field.getAnnotation(Id.class);
            if (Objects.nonNull(id)) {
                idColumnName = field.getName();
            }

            ColumnName annotation = field.getAnnotation(ColumnName.class);
            String columnName = ((Objects.isNull(annotation) ? converter.convert(field.getName()) : annotation.value()));
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
        return new TableMeta(name, tableName, columnNamesBuilder.toString(), propertiesBuilder.toString(), updateColumnNamesBuilder.toString(),
                updatePropertiesBuilder.toString(), idColumnName, subTables);
    }
}
