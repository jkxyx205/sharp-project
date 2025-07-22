package com.rick.db.plugin.dao.core;

import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Id;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 根据实体类创建表
 * @author Rick
 * @createdAt 2022-03-01 12:05:00
 */
@RequiredArgsConstructor
@Slf4j
public abstract class TableGenerator {

    private final JdbcTemplate jdbcTemplate;

    private ThreadLocal<Set<String>> tableNameCreatedContainer = ThreadLocal.withInitial(() -> new HashSet<>());

    public void createTable(Class<?> clazz) {
        TableMeta tableMeta = TableMetaResolver.resolve(clazz);
        if (tableNameCreatedContainer.get().contains(tableMeta.getTableName())) {
            return;
        }

        StringBuilder createTableSql = new StringBuilder();
        beforeCreateTableHandler(createTableSql, tableMeta);

        createTableSql.append("CREATE TABLE ");

        Id.GenerationType strategy;
        if (tableMeta.getId() == null) {
            strategy = Id.GenerationType.SEQUENCE;
        } else {
            strategy =  tableMeta.getId().strategy();
        }

        Field idField = tableMeta.getColumnNameFieldMap().get(tableMeta.getIdColumnName());
        Assert.notNull(idField, "cannot find id field, forgot to extends BaseEntity??");
        createTableSql.append(tableMeta.getTableName())
                .append("(");

        idColumnHandler(createTableSql, idField, tableMeta.getIdColumnName(), strategy, tableMeta.getIdClass());

        List<String> columnNames = tableMeta.getSortedColumns();

        for (String columnName : columnNames) {
            if (tableMeta.getIdColumnName().equals(columnName) ) {
                continue;
            }

            Field field = tableMeta.getColumnNameFieldMap().get(columnName);
            createTableSql.append(columnName).append(" ");

            if (columnName.equals(tableMeta.getVersionProperty().getColumnName())) {
                createTableSql.append(determineSqlType(field.getType()));
                versionColumnHandler(createTableSql, field, columnName, null);
                continue;
            }

            Column column = tableMeta.getColumnNameMap().get(columnName);

            if (Objects.nonNull(column)) {
                if (StringUtils.isNotBlank(column.columnDefinition())) {
                    createTableSql.append(column.columnDefinition());
                } else {
                    createTableSql.append(determineSqlType(field.getType()));
                    if (!column.nullable()) {
                        createTableSql.append(" NOT NULL");
                    } else {
                        createTableSql.append(" NULL");
                    }

                }
            } else {
                createTableSql.append(determineSqlType(field.getType())).append(" NULL");
            }

            columnHandler(createTableSql, field, columnName, column);

            createTableSql.append(",");
        }

        createTableSql.deleteCharAt(createTableSql.length() - 1).append(")");

        afterCreateTableHandler(createTableSql, tableMeta);
        log.info(createTableSql.toString());

        createManyToManyTable(tableMeta.getManyToManyAnnotationList());

        jdbcTemplate.execute(createTableSql.toString());
        tableNameCreatedContainer.get().add(tableMeta.getTableName());
    }

    protected void beforeCreateTableHandler(StringBuilder createTableSql, TableMeta tableMeta) {}

    abstract protected void idColumnHandler(StringBuilder createTableSql, Field field, String columnName, Id.GenerationType generationType, Class idClass);

    abstract protected void versionColumnHandler(StringBuilder createTableSql, Field field, String columnName, Column column);

    abstract protected void columnHandler(StringBuilder createTableSql, Field field, String columnName, Column column);

    abstract protected String createManyToManyThirdPartyTable(ManyToMany manyToMany);

    abstract protected void afterCreateTableHandler(StringBuilder createTableSql, TableMeta tableMeta);

    abstract protected String determineSqlType(Class type);

    private void createManyToManyTable(List<TableMeta.ManyToManyProperty> manyToManyPropertyList) {
        if (CollectionUtils.isNotEmpty(manyToManyPropertyList)) {
            for (TableMeta.ManyToManyProperty manyToManyProperty : manyToManyPropertyList) {
                ManyToMany manyToMany = manyToManyProperty.getManyToMany();
                if (tableNameCreatedContainer.get().contains(manyToMany.thirdPartyTable())) {
                    continue;
                }

                jdbcTemplate.execute(createManyToManyThirdPartyTable(manyToMany));
                tableNameCreatedContainer.get().add(manyToMany.thirdPartyTable());
            }
        }
    }

}
