package com.rick.db.plugin.generator;

import com.rick.db.repository.Column;
import com.rick.db.repository.Id;
import com.rick.db.repository.ManyToMany;
import com.rick.db.repository.model.DatabaseType;
import com.rick.db.repository.support.TableMeta;
import com.rick.db.repository.support.TableMetaResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.*;

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

    public <T> void createTable(Class<T> clazz) {
        TableMeta<T> tableMeta = TableMetaResolver.resolve(clazz);
        if (tableNameCreatedContainer.get().contains(tableMeta.getTableName())) {
            return;
        }

        StringBuilder createTableSql = new StringBuilder();
        beforeCreateTableHandler(createTableSql, tableMeta);

        createTableSql.append("CREATE TABLE ");

        Id.GenerationType strategy;
        if (Objects.isNull(tableMeta.getIdMeta())) {
            strategy = Id.GenerationType.SEQUENCE;
        } else {
            strategy =  tableMeta.getIdMeta().getId().strategy();
        }

        Field idField = tableMeta.getIdMeta().getIdField();
        Assert.notNull(idField, "cannot find id field, forgot to extends BaseEntity??");
        createTableSql.append(tableMeta.getTableName())
                .append("(");

        idColumnHandler(createTableSql, idField, tableMeta.getIdMeta().getIdColumnName(), strategy, tableMeta.getIdMeta().getIdClass());

        List<String> columnNames = tableMeta.getSortedColumns();

        for (String columnName : columnNames) {
            if (tableMeta.getIdMeta().getIdColumnName().equals(columnName) ) {
                continue;
            }

            Field field = tableMeta.getFieldByColumnName(columnName);
            createTableSql.append(columnName).append(" ");

            if (columnName.equals(tableMeta.getFieldColumnNameMap().get(tableMeta.getVersionField()))) {
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

        createManyToManyTable(tableMeta.getReferenceMap());

        jdbcTemplate.execute(createTableSql.toString());
        tableNameCreatedContainer.get().add(tableMeta.getTableName());
    }

    protected void beforeCreateTableHandler(StringBuilder createTableSql, TableMeta tableMeta) {}

    abstract protected void idColumnHandler(StringBuilder createTableSql, Field field, String columnName, Id.GenerationType generationType, Class idClass);

    abstract protected void versionColumnHandler(StringBuilder createTableSql, Field field, String columnName, Column column);

    abstract protected void columnHandler(StringBuilder createTableSql, Field field, String columnName, Column column);

    abstract protected String createManyToManyThirdPartyTable(ManyToMany manyToMany);

    abstract protected <T> void afterCreateTableHandler(StringBuilder createTableSql, TableMeta<T> tableMeta);

    abstract protected String determineSqlType(Class type);

    public abstract DatabaseType getType();

    private void createManyToManyTable(Map<Field, TableMeta.Reference> referenceMap) {
        for (Map.Entry<Field, TableMeta.Reference> entry : referenceMap.entrySet()) {
            if (Objects.nonNull(entry.getValue().getManyToMany())) {
                ManyToMany manyToMany = entry.getValue().getManyToMany();
                if (tableNameCreatedContainer.get().contains(manyToMany.tableName())) {
                    continue;
                }

                jdbcTemplate.execute(createManyToManyThirdPartyTable(manyToMany));
                tableNameCreatedContainer.get().add(manyToMany.tableName());
            }
        }
    }

}
