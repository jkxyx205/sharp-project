package com.rick.db.repository.support;

import com.google.common.collect.Lists;
import com.rick.db.config.Context;
import com.rick.db.repository.*;
import com.rick.db.repository.model.DatabaseType;
import lombok.Getter;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.rick.db.repository.support.Constants.COLUMN_NAME_SEPARATOR_REGEX;

/**
 * @author Rick.Xu
 * @date 2025/8/21 18:54
 */
@Getter
public class TableMeta<T> {

    private final Class<T> entityClass;

    private final Table table;

    private final String tableName;

    private final String referenceColumnId;

    private final Map<Field, Reference> referenceMap;

    private final Map<Field, String> fieldColumnNameMap;

    private final Map<Field, String> fieldPropertyNameMap;

    private final Map<String, String> columnPropertyNameMap;

    private final Map<String, Column> columnNameMap;

    IdMeta idMeta;

    Field versionField;

    private String selectColumn;

    private String updateColumn;

    private String columnNames;

    private String selectConditionSQLCache;

    private String conditionSQLCache;

    public TableMeta(Class<T> entityClass, Table table, String tableName, String referenceColumnId, Map<Field, Reference> referenceMap, Map<Field, String> fieldColumnNameMap, Map<Field, String> fieldPropertyNameMap, Map<String, String> columnPropertyNameMap, Map<String, Column> columnNameMap) {
        this.entityClass = entityClass;
        this.table = table;
        this.tableName = tableName;
        this.referenceColumnId = referenceColumnId;
        this.referenceMap = referenceMap;
        this.fieldColumnNameMap = fieldColumnNameMap;
        this.fieldPropertyNameMap = fieldPropertyNameMap;
        this.columnPropertyNameMap = columnPropertyNameMap;
        this.columnNameMap = columnNameMap;
    }

    public Field getFieldByColumnName(String columnName) {
        for (Map.Entry<Field, String> entry : fieldColumnNameMap.entrySet()) {
            if (Objects.equals(columnName, entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    public Field getFieldByPropertyName(String propertyName) {
        for (Map.Entry<Field, String> entry : fieldPropertyNameMap.entrySet()) {
            if (Objects.equals(propertyName, entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    public String getSelectConditionSQL() {
        if (StringUtils.isBlank(selectConditionSQLCache)) {
            selectConditionSQLCache = getSelectSQL(selectColumn) + " WHERE " + getConditionSQL();
        }
        return selectConditionSQLCache;
    }

    public String getConditionSQL() {
        if (StringUtils.isBlank(conditionSQLCache)) {
            conditionSQLCache =  appendColumnVar(columnNames, true, " AND ");
        }
        return conditionSQLCache;
    }

    public String getSelectSQL(String columns) {
        return "SELECT " + columns + " FROM " + getTableName();
    }

    public String appendColumnVar(String columns, boolean namedVar) {
        return appendColumnVar(columns, namedVar, ", ");
    }

    public String appendColumnVar(String columns, boolean namedVar, CharSequence delimiter) {
        String[] columnArr = columns.split(COLUMN_NAME_SEPARATOR_REGEX);

        return Arrays.stream(columnArr).map(column -> {
            String suffixType = "";
            if (Context.getDialect().getType() == DatabaseType.PostgreSQL) {
                Column annotation = columnNameMap.get(column);
                if (Objects.nonNull(annotation)) {
                    if ("json".equals(annotation.columnDefinition())) {
                        suffixType = "::json";
                    } else if ("jsonb".equals(annotation.columnDefinition())) {
                        suffixType = "::jsonb";
                    }
                }
            }

            return column + " = " + (namedVar ? ":" + getColumnPropertyNameMap().get(column) : "?") + suffixType;
        }).collect(Collectors.joining(delimiter));
    }

    public List<String> getSortedColumns() {
        List<String> sortedColumnNames = Lists.newArrayListWithCapacity(getColumnPropertyNameMap().keySet().size());
        List<String> tailColumnNames = new ArrayList<>();

        for (String columnName : getColumnNames().split(", ")) {
            if (isHeaderColumn(columnName)) {
                sortedColumnNames.add(columnName);
            } else {
                tailColumnNames.add(columnName);
            }
        }

        sortedColumnNames.addAll(tailColumnNames);
        return sortedColumnNames;
    }

    private boolean isHeaderColumn(String columnName) {
        if (columnName.equals(Constants.ID_COLUMN_NAME) ||
                columnName.equals(Constants.CODE_COLUMN_NAME) ||
                columnName.equals(Constants.DESCRIPTION_COLUMN_NAME)
        ) {
            return true;
        }

        return false;
    }

    @Getter
    public static class Reference {

        Field field;

        Class<?> referenceClass;

        ManyToOne manyToOne;

        ManyToMany manyToMany;

        OneToMany oneToMany;

        Select select;
    }

    @Value
    public static class IdMeta<ID> {

        Class<ID> idClass;

        Id id;

        String idColumnName;

        String idPropertyName;

        Field idField;

        public String getIdPropertyName() {
            return idField.getName();
        }
    }

    void setSelectColumn(String selectColumn) {
        this.selectColumn = selectColumn;
    }

    void setUpdateColumn(String updateColumn) {
        this.updateColumn = updateColumn;
    }

    void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }
}


