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
 * MySQL 根据实体类创建表
 * @author Rick
 * @createdAt 2022-03-01 12:05:00
 */
public class MySQL5TableGenerator extends TableGenerator {

    public MySQL5TableGenerator(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected void idColumnHandler(StringBuilder createTableSql, Field field, String columnName, Id.GenerationType strategy, Class idClass) {
        createTableSql.append(columnName + " " + (strategy == Id.GenerationType.IDENTITY ? "AUTO_INCREMENT" :  determineSqlType(idClass)) +" NOT NULL COMMENT '主键' PRIMARY KEY,");
    }

    @Override
    protected void versionColumnHandler(StringBuilder createTableSql, Field field, String columnName, Column column) {
        createTableSql.append(" NOT NULL COMMENT '版本号',");
    }

    @Override
    protected void columnHandler(StringBuilder createTableSql, Field field, String columnName, Column column) {
        if (Objects.nonNull(column) && StringUtils.isNotBlank(column.comment())) {
            createTableSql.append(" COMMENT '"+column.comment()+"'");
        }
    }

    @Override
    protected String createManyToManyThirdPartyTable(ManyToMany manyToMany) {
        return "CREATE TABLE "+manyToMany.tableName()+"" +
                "                        ("+manyToMany.joinColumnId()+" BIGINT NOT NULL,\n" +
                "                        "+manyToMany.inverseJoinColumnId()+" BIGINT NOT NULL,\n" +
                "                        is_deleted BIT DEFAULT b'0' NOT NULL,\n" +
                "                        CONSTRAINT "+manyToMany.tableName()+"_pk\n" +
                "                UNIQUE ("+manyToMany.joinColumnId()+", "+manyToMany.inverseJoinColumnId()+")\n" +
                "                ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
    }

    @Override
    protected void afterCreateTableHandler(StringBuilder createTableSql, TableMeta tableMeta) {
        if (Objects.nonNull(tableMeta.getTable()) && StringUtils.isNotBlank(tableMeta.getTable().comment())) {
            createTableSql.append(" COMMENT '"+tableMeta.getTable().comment()+"'");
        }

        createTableSql.append(" ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;");
    }

    protected String determineSqlType(Class type) {
        if (type == Long.class) {
            return "BIGINT";
        } else if (type == Integer.class) {
            return "INT";
        } else if (type == Short.class) {
            return "SMALLINT";
        } else if (CharSequence.class.isAssignableFrom(type)) {
            return "VARCHAR(32)";
        } else if (type == Character.class) {
            return "CHAR(1)";
        } else if (type.isEnum()) {
            try {
                Method getCodeMethod = type.getMethod("getCode");
                if (Number.class.isAssignableFrom(getCodeMethod.getReturnType()) || getCodeMethod.getReturnType() == int.class || getCodeMethod.getReturnType() == long.class) {
                    return "INT";
                }
            } catch (NoSuchMethodException e) {
//                throw new RuntimeException(e);
            }
//            return "varchar(16)";
            String enumValues = String.join(",", EnumUtils.getCodes(type).stream().map(code -> "'" + code + "'").collect(Collectors.toList()));
            return "ENUM("+enumValues+")";
        } else if (type == Boolean.class) {
            return "BIT DEFAULT b'0'";
        } else if (type == BigDecimal.class) {
            return "DECIMAL(10,2)";
        } else if (type == LocalDateTime.class) {
            return "DATETIME";
        } else if (type == Instant.class) {
            // mysql 5.7 也是对应 datetime
            // mysql 8.0 对应 timestamp
            return "TIMESTAMP";
        } else if (type == LocalDate.class) {
            return "DATE";
        } else if (type == LocalTime.class) {
            return "TIME";
        } else if (type == Map.class || type == List.class || JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(type)) {
            return "TEXT";
        } else if (EntityUtils.isEntityClass(type)) {
            return "BIGINT";
        } else if (ObjectUtils.mayPureObject(type)) {
            return "JSON";
        }

        return "VARCHAR(32)";
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MySQL5;
    }

}
