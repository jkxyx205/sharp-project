package com.rick.db.plugin.dao.core;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.common.util.EnumUtils;
import com.rick.common.util.ObjectUtils;
import com.rick.db.config.Constants;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Id;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import com.rick.db.plugin.dao.support.BaseEntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 根据实体类创建表
 * @author Rick
 * @createdAt 2022-03-01 12:05:00
 */
@RequiredArgsConstructor
@Slf4j
public class TableGenerator {

    private final JdbcTemplate jdbcTemplate;

    private final SharpDatabaseProperties sharpDatabaseProperties;

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

        Field idField = tableMeta.getColumnNameFieldMap().get(tableMeta.getIdColumnName());
        Assert.notNull(idField, "cannot find id field, forgot to extends BaseEntity??");
        createTableSql.append(tableMeta.getTableName())
                .append("(")
                .append(""+(strategy == Id.GenerationType.ASSIGN ? ""+tableMeta.getIdColumnName()+" varchar(32)" : ""+tableMeta.getIdColumnName()+" " + determineSqlType(tableMeta.getIdClass()))+" not null"+ (strategy == Id.GenerationType.IDENTITY ? " AUTO_INCREMENT" : "") +" comment '主键' primary key,");

        List<String> columnNames = tableMeta.getSortedColumns();

        for (String columnName : columnNames) {
            if (tableMeta.getIdColumnName().equals(columnName) ) {
                continue;
            }

            Field field = tableMeta.getColumnNameFieldMap().get(columnName);
            createTableSql.append(columnName).append(" ");

            if (columnName.equals(tableMeta.getVersionProperty().getColumnName())) {
                createTableSql.append(determineSqlType(field.getType())).append(" not null comment '版本号',");
                continue;
            }

            Column column = tableMeta.getColumnNameMap().get(columnName);

            if (Objects.nonNull(column)) {
                if (StringUtils.isNotBlank(column.columnDefinition())) {
                    createTableSql.append(column.columnDefinition());
                } else {
                    createTableSql.append(determineSqlType(field.getType()));
                    if (!column.nullable()) {
                        createTableSql.append(" not null");
                    } else {
                        createTableSql.append(" null");
                    }

                    if (StringUtils.isNotBlank(column.comment())) {
                        createTableSql.append(" comment '"+column.comment()+"'");
                    }
                }
            } else {
                createTableSql.append(determineSqlType(field.getType())).append(" null");
            }

            createTableSql.append(",");
        }

        createTableSql.deleteCharAt(createTableSql.length() - 1).append(")");

        if (Objects.nonNull(tableMeta.getTable()) && StringUtils.isNotBlank(tableMeta.getTable().comment())) {
            createTableSql.append(" comment '"+tableMeta.getTable().comment()+"'");
        }

        createTableSql.append(" ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");
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
                        "                ) ENGINE=InnoDB  DEFAULT CHARSET=utf8");

                tableNameCreatedContainer.get().add(manyToMany.thirdPartyTable());
            }
        }
    }

    private String determineSqlType(Class type) {
        if (type == Long.class) {
            return "bigint";
        } else if (type == Integer.class) {
            return "int";
        } else if (CharSequence.class.isAssignableFrom(type)) {
            return "varchar(32)";
        } else if (type.isEnum()) {
            try {
                Method getCodeMethod = type.getMethod("getCode");
                if (Number.class.isAssignableFrom(getCodeMethod.getReturnType()) || getCodeMethod.getReturnType() == int.class || getCodeMethod.getReturnType() == long.class) {
                    return "INT";
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
//            return "varchar(16)";
            String enumValues = String.join(",", EnumUtils.getCodes(type).stream().map(code -> "'" + code + "'").collect(Collectors.toList()));
            return "ENUM("+enumValues+")";
        } else if (type == Boolean.class) {
            return "bit DEFAULT b'0'";
        } else if (type == BigDecimal.class) {
            return "decimal(10,2)";
        } else if (type == LocalDateTime.class) {
            return "datetime";
        } else if (type == Instant.class) {
            // mysql 5.7 也是对应 datetime
            // mysql 8.0 对应 timestamp
            return "timestamp";
        } else if (type == LocalDate.class) {
            return "date";
        } else if (type == LocalTime.class) {
            return "time";
        } else if (type == Map.class || type == List.class || JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(type)) {
//            return "text";
            return Constants.DB_MYSQL.equals(sharpDatabaseProperties.getType()) && sharpDatabaseProperties.getDatabaseProductVersion().charAt(0) <= '5' ? "text" : "json";
        } else if (BaseEntityUtils.isEntityClass(type)) {
            return "bigint";
        } else if (ObjectUtils.mayPureObject(type)) {
            return "json";
        }

        return "varchar(32)";
    }

}
