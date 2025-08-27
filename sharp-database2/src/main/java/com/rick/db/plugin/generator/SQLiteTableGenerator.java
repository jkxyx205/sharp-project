package com.rick.db.plugin.generator;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.common.util.ObjectUtils;
import com.rick.db.repository.Column;
import com.rick.db.repository.Id;
import com.rick.db.repository.ManyToMany;
import com.rick.db.repository.model.DatabaseType;
import com.rick.db.repository.support.EntityUtils;
import com.rick.db.repository.support.TableMeta;
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

/**
 * @author Rick.Xu
 * @date 2025/8/22 10:23
 */
public class SQLiteTableGenerator extends TableGenerator {


    public SQLiteTableGenerator(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected void idColumnHandler(StringBuilder createTableSql, Field field, String columnName, Id.GenerationType strategy, Class idClass) {
        createTableSql.append(((strategy == Id.GenerationType.ASSIGN ? ""+columnName+" TEXT(32)" : ""+columnName + " " + determineSqlType(idClass)) +" PRIMARY KEY" + (strategy == Id.GenerationType.IDENTITY ? " AUTOINCREMENT" : "")) +" NOT NULL,");
    }

    @Override
    protected void versionColumnHandler(StringBuilder createTableSql, Field field, String columnName, Column column) {
        createTableSql.append(" NOT NULL,");
    }

    @Override
    protected void columnHandler(StringBuilder createTableSql, Field field, String columnName, Column column) {

    }

    @Override
    protected String createManyToManyThirdPartyTable(ManyToMany manyToMany) {
        return "CREATE TABLE "+manyToMany.tableName()+"" +
                "                        ("+manyToMany.joinColumnId()+"INTEGER NOT NULL,\n" +
                "                        "+manyToMany.inverseJoinColumnId()+" INTEGER NOT NULL,\n" +
                "                        is_deleted TEXT DEFAULT FALSE NOT NULL,\n" +
                "                        CONSTRAINT "+manyToMany.tableName()+"_pk\n" +
                "                UNIQUE ("+manyToMany.joinColumnId()+", "+manyToMany.inverseJoinColumnId()+")\n" +
                "                )";
    }

    @Override
    protected <T> void afterCreateTableHandler(StringBuilder createTableSql, TableMeta<T> tableMeta) {
    }

    protected String determineSqlType(Class type) {
        if (type == Long.class) {
            return "INTEGER";
        } else if (type == Integer.class) {
            return "INTEGER";
        } else if (type == Short.class) {
            return "INTEGER";
        }  else if (CharSequence.class.isAssignableFrom(type)) {
            return "TEXT(32)";
        } else if (type == Character.class) {
            return "TEXT(1)";
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
            return "TEXT(32)";
        } else if (type == Boolean.class) {
            return "TEXT(5)";
        } else if (type == BigDecimal.class) {
            return "REAL";
        } else if (type == LocalDateTime.class) {
            return "TEXT(10)";
        } else if (type == Instant.class) {
            return "TEXT(16)";
        } else if (type == LocalDate.class) {
            return "TEXT(10)";
        } else if (type == LocalTime.class) {
            return "TEXT(8)";
        } else if (type == Map.class || type == List.class || JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(type)) {
            return "TEXT";
        } else if (EntityUtils.isEntityClass(type)) {
            return "INTEGER";
        } else if (ObjectUtils.mayPureObject(type)) {
            return "TEXT";
        }

        return "TEXT(32)";
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.SQLite;
    }
}
