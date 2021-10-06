package com.rick.db.plugin.dao;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.rick.common.util.ReflectUtils;
import com.rick.db.config.annotation.ColumnName;
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

    public static TableMeta resolve(Class<?> clazz) {
        TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
        Converter<String, String> converter = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
        String tableName = Objects.isNull(tableNameAnnotation) ? (TABLE_PREFIX + converter.convert(clazz.getSimpleName()))
                : tableNameAnnotation.value();

        Field[] fields = ReflectUtils.getAllFields(clazz);
        StringBuilder columnNamesBuilder = new StringBuilder();
        StringBuilder propertiesBuilder = new StringBuilder();
        for (Field field : fields) {
            if (field.getAnnotation(Transient.class) != null) {
                continue;
            }

            ColumnName annotation = field.getAnnotation(ColumnName.class);
            String columnName = ((Objects.isNull(annotation) ? converter.convert(field.getName()) : annotation.value()));
            propertiesBuilder.append(field.getName()).append(",");
            columnNamesBuilder.append(columnName).append(",");
        }
        propertiesBuilder.deleteCharAt(propertiesBuilder.length() - 1);
        columnNamesBuilder.deleteCharAt(columnNamesBuilder.length() - 1);

        return new TableMeta(tableName, columnNamesBuilder.toString(), propertiesBuilder.toString());
    }
}
