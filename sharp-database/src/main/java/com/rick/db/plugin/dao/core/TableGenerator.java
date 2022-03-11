package com.rick.db.plugin.dao.core;

import com.rick.db.constant.EntityConstants;
import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 根据实体类创建表
 * @author Rick
 * @createdAt 2022-03-01 12:05:00
 */
@RequiredArgsConstructor
@Slf4j
public class TableGenerator {

    private final JdbcTemplate jdbcTemplate;

    public void createTable(Class<?> clazz) {
        TableMeta tableMeta = TableMetaResolver.resolve(clazz);
        StringBuilder createTableSql = new StringBuilder("create table ");
        createTableSql.append(tableMeta.getTableName())
                .append("(")
                .append("id bigint not null comment '主键' primary key,");

        List<String> columnNames = Arrays.asList(tableMeta.getColumnNames().split(EntityConstants.COLUMN_NAME_SEPARATOR_REGEX));

        ;
        for (String columnName : columnNames) {
            if (EntityConstants.ID_COLUMN_NAME.equals(columnName)) {
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
        jdbcTemplate.execute(createTableSql.toString());
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
        } else if (type == Map.class || type == List.class) {
            return "text";
        } else if(BasePureEntity.class.isAssignableFrom(type)) {
            return "bigint";
        }

        return "varchar(32)";
    }

}
