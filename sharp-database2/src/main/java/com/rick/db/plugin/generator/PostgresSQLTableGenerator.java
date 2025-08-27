package com.rick.db.plugin.generator;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.common.util.EnumUtils;
import com.rick.common.util.ObjectUtils;
import com.rick.db.repository.Column;
import com.rick.db.repository.Id;
import com.rick.db.repository.ManyToMany;
import com.rick.db.repository.model.DatabaseType;
import com.rick.db.repository.support.EntityUtils;
import com.rick.db.repository.support.TableMeta;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * PostgresSQL 根据实体类创建表
 * @author Rick
 * @createdAt 2025-07-01 12:05:00
 */
public class PostgresSQLTableGenerator extends TableGenerator {

    public PostgresSQLTableGenerator(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected void idColumnHandler(StringBuilder createTableSql, Field field, String columnName, Id.GenerationType strategy, Class idClass) {
        createTableSql.append((strategy == Id.GenerationType.ASSIGN ? columnName +" VARCHAR(32)" : columnName + " " + (strategy == Id.GenerationType.IDENTITY ? "serial" : determineSqlType(idClass))) +" NOT NULL PRIMARY KEY,");
    }

    @Override
    protected void versionColumnHandler(StringBuilder createTableSql, Field field, String columnName, Column column) {
        createTableSql.append(" NOT NULL,");
    }

    @Override
    protected void columnHandler(StringBuilder createTableSql, Field field, String columnName, Column column) {
        if (field.getType().isEnum()) {
            try {
                Method getCodeMethod = field.getType().getMethod("getCode");
                if (!(Number.class.isAssignableFrom(getCodeMethod.getReturnType()) || getCodeMethod.getReturnType() == int.class || getCodeMethod.getReturnType() == long.class)) {
                    String enumValues = String.join(",", EnumUtils.getCodes(field.getType()).stream().map(code -> "'" + code + "'").collect(Collectors.toList()));
                    createTableSql.append(" CHECK ("+columnName+" IN ("+enumValues+"))");
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected String createManyToManyThirdPartyTable(ManyToMany manyToMany) {
        return "CREATE TABLE "+manyToMany.tableName()+"" +
                "                        ("+manyToMany.joinColumnId()+" BIGINT NOT NULL,\n" +
                "                        "+manyToMany.inverseJoinColumnId()+" BIGINT NOT NULL,\n" +
                "                        is_deleted BOOLEAN DEFAULT FALSE NOT NULL,\n" +
                "                        CONSTRAINT "+manyToMany.tableName()+"_pk\n" +
                "                UNIQUE ("+manyToMany.joinColumnId()+", "+manyToMany.inverseJoinColumnId()+")\n" +
                "                )";
    }

//    @Override
//    protected void beforeCreateTableHandler(StringBuilder createTableSql, TableMeta tableMeta) {
//        // 创建枚举
//        List<String> columnNames = tableMeta.getSortedColumns();
//        for (String columnName : columnNames) {
//            Field field = tableMeta.getColumnNameFieldMap().get(columnName);
//            if (field.getType().isEnum()) {
//                try {
//                    Method getCodeMethod = field.getType().getMethod("getCode");
//                    if (!(Number.class.isAssignableFrom(getCodeMethod.getReturnType()) || getCodeMethod.getReturnType() == int.class || getCodeMethod.getReturnType() == long.class)) {
//                        String enumValues = String.join(",", EnumUtils.getCodes(field.getType()).stream().map(code -> "'" + code + "'").collect(Collectors.toList()));
//                        createTableSql.append("CREATE TYPE "+getTypeNameAsSnake(field.getType())+" AS ENUM ("+enumValues+");");
//                    }
//                } catch (NoSuchMethodException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//    }

    @Override
    protected <T> void afterCreateTableHandler(StringBuilder createTableSql, TableMeta<T> tableMeta) {
        createTableSql.append(";");

        if (Objects.nonNull(tableMeta.getTable()) && StringUtils.isNotBlank(tableMeta.getTable().comment())) {
            createTableSql.append(" comment on table "+tableMeta.getTableName()+" is  '"+tableMeta.getTable().comment()+"';");
        }

        // 添加备注
        List<String> columnNames = tableMeta.getSortedColumns();
        for (String columnName : columnNames) {
            if (tableMeta.getIdMeta().getIdColumnName().equals(columnName)) {
                createTableSql.append("comment on column " + tableMeta.getTableName() + "." + columnName + " is '主键';");
                continue;
            }

            if (columnName.equals(tableMeta.getFieldColumnNameMap().get(tableMeta.getVersionField()))) {
                createTableSql.append("comment on column " + tableMeta.getTableName() + "." + columnName + " is '版本号';");
                continue;
            }

            Column column = tableMeta.getColumnNameMap().get(columnName);
            if (Objects.nonNull(column) && StringUtils.isNotBlank(column.comment())) {
                createTableSql.append("comment on column " + tableMeta.getTableName() + "." + columnName + " is '" + column.comment() + "';");
            }
        }
    }

    protected String determineSqlType(Class type) {
        if (type == Long.class) {
            return "BIGINT";
        } else if (type == Integer.class) {
            return "INTEGER";
        } else if (type == Short.class) {
            return "SMALLINT";
        }  else if (CharSequence.class.isAssignableFrom(type)) {
            return "VARCHAR(32)";
        } else if (type == Character.class) {
            return "CHAR(1)";
        } else if (type.isEnum()) {
            try {
                Method getCodeMethod = type.getMethod("getCode");
                if (Number.class.isAssignableFrom(getCodeMethod.getReturnType()) || getCodeMethod.getReturnType() == int.class || getCodeMethod.getReturnType() == long.class) {
                    return "INTEGER";
                }
            } catch (NoSuchMethodException e) {
//                throw new RuntimeException(e);
            }
//            return getTypeNameAsSnake(type);
            return "VARCHAR(32)";
        } else if (type == Boolean.class) {
            return "BOOLEAN DEFAULT FALSE";
        } else if (type == BigDecimal.class) {
            return "NUMERIC(10,2)";
        } else if (type == LocalDateTime.class) {
            return "TIMESTAMP";
        } else if (type == Instant.class) {
            return "TIMESTAMP";
        } else if (type == LocalDate.class) {
            return "DATE";
        } else if (type == LocalTime.class) {
            return "TIME";
        } else if (type == Map.class || type == List.class || JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(type)) {
            return "JSON";
        } else if (EntityUtils.isEntityClass(type)) {
            return "BIGINT";
        } else if (ObjectUtils.mayPureObject(type)) {
            return "JSON";
        }

        return "VARCHAR(32)";
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.PostgreSQL;
    }

    private String getTypeNameAsSnake(Class type) {
        return com.rick.common.util.StringUtils.camelToSnake(StringUtils.substringAfterLast(type.getTypeName(), ".").replace('$', '_'));
    }

}
