package com.rick.db.plugin.dao.core;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.db.constant.BaseEntityConstants;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Id;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 根据实体类创建表
 * @author Rick
 * @createdAt 2022-03-01 12:05:00
 */
@RequiredArgsConstructor
@Slf4j
public class TableGenerator {

    private final JdbcTemplate jdbcTemplate;

    private ThreadLocal<Set<String>> tableNameCreatedContainer = ThreadLocal.withInitial(() -> new HashSet<>());

    public void createTable(Class<?> clazz) {
        TableMeta tableMeta = TableMetaResolver.resolve(clazz);
        if (tableNameCreatedContainer.get().contains(tableMeta.getTableName())) {
            return;
        }

        StringBuilder createTableSql = new StringBuilder("create table ");

        Id.GenerationType strategy;
        if (tableMeta.getId() == null) {
            strategy = Id.GenerationType.SEQUENCE;
        } else {
            strategy =  tableMeta.getId().strategy();
        }

        createTableSql.append(tableMeta.getTableName())
                .append("(")
                .append(""+(strategy == Id.GenerationType.ASSIGN ? ""+tableMeta.getIdColumnName()+" varchar(32)" : ""+tableMeta.getIdColumnName()+" bigint")+" not null"+ (strategy == Id.GenerationType.IDENTITY ? " AUTO_INCREMENT" : "") +" comment '主键' primary key,");

        List<String> columnNames = Arrays.asList(tableMeta.getColumnNames().split(BaseEntityConstants.COLUMN_NAME_SEPARATOR_REGEX));

        for (String columnName : columnNames) {
            if (tableMeta.getIdPropertyName().equals(columnName)) {
                continue;
            }

            Field field = tableMeta.getColumnNameFieldMap().get(columnName);
            createTableSql.append(columnName).append(" ").append(determineSqlType(field.getType()));

            Column column = tableMeta.getColumnNameMap().get(columnName);
            if (Objects.nonNull(column)) {
                if (column.nullable()) {
                    createTableSql.append(" not null");
                }

                if (StringUtils.isNotBlank(column.comment())) {
                    createTableSql.append(" comment '"+column.comment()+"'");
                }
            }
            createTableSql.append(",");
        }

        createTableSql.deleteCharAt(createTableSql.length() - 1).append(")");

        if (Objects.nonNull(tableMeta.getTable()) && StringUtils.isNotBlank(tableMeta.getTable().comment())) {
            createTableSql.append(" comment '"+tableMeta.getTable().comment()+"'");
        }
        log.info(createTableSql.toString());

        createManyToManyTable(tableMeta.getManyToManyAnnotationList());

        jdbcTemplate.execute(createTableSql.toString());
        tableNameCreatedContainer.get().add(tableMeta.getTableName());
    }

    private void createManyToManyTable(List<TableMeta.ManyToManyProperty> manyToManyPropertyList) {
        if (CollectionUtils.isNotEmpty(manyToManyPropertyList)) {
            for (TableMeta.ManyToManyProperty manyToManyProperty : manyToManyPropertyList) {
                ManyToMany manyToMany = manyToManyProperty.getManyToMany();
                if (tableNameCreatedContainer.get().contains(manyToMany.thirdPartyTable())) {
                    continue;
                }
                jdbcTemplate.execute("create table "+manyToMany.thirdPartyTable()+"" +
                        "                        ("+manyToMany.columnDefinition()+" bigint not null,\n" +
                        "                        "+manyToMany.referenceColumnName()+" bigint not null,\n" +
                        "                        is_deleted bit default b'0' not null,\n" +
                        "                        constraint "+manyToMany.thirdPartyTable()+"_pk\n" +
                        "                unique ("+manyToMany.columnDefinition()+", "+manyToMany.referenceColumnName()+")\n" +
                        "                )");

                tableNameCreatedContainer.get().add(manyToMany.thirdPartyTable());
            }
        }
    }

    private String determineSqlType(Class<?> type) {
        if (type == Long.class) {
            return "bigint";
        } else if (type == Integer.class) {
            return "int";
        } else if (CharSequence.class.isAssignableFrom(type)) {
            return "varchar(32)";
        } else if (type.isEnum()) {
            return "varchar(16)";
        } else if (type == Boolean.class) {
            return "bit";
        } else if (type == BigDecimal.class) {
            return "decimal(10,4)";
        } else if (type == Instant.class || type == LocalDateTime.class) {
            return "datetime";
        } else if (type == LocalDate.class) {
            return "date";
        } else if (type == LocalTime.class) {
            return "time";
        } else if (type == Map.class || type == List.class || JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(type)) {
//            return "text";
            return "json";
        } else if(BaseEntity.class.isAssignableFrom(type)) {
            return "bigint";
        }

        return "varchar(32)";
    }

}
