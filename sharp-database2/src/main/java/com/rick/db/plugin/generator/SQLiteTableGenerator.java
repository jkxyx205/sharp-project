package com.rick.db.plugin.generator;

import com.rick.db.repository.Column;
import com.rick.db.repository.Id;
import com.rick.db.repository.ManyToMany;
import com.rick.db.repository.model.DatabaseType;
import com.rick.db.repository.support.TableMeta;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;

/**
 * @author Rick.Xu
 * @date 2025/8/22 10:23
 */
public class SQLiteTableGenerator extends TableGenerator {

    public SQLiteTableGenerator(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected void idColumnHandler(StringBuilder createTableSql, Field field, String columnName, Id.GenerationType generationType, Class idClass) {

    }

    @Override
    protected void versionColumnHandler(StringBuilder createTableSql, Field field, String columnName, Column column) {

    }

    @Override
    protected void columnHandler(StringBuilder createTableSql, Field field, String columnName, Column column) {

    }

    @Override
    protected String createManyToManyThirdPartyTable(ManyToMany manyToMany) {
        return null;
    }

    @Override
    protected <T> void afterCreateTableHandler(StringBuilder createTableSql, TableMeta<T> tableMeta) {

    }

    @Override
    protected String determineSqlType(Class type) {
        return null;
    }

    @Override
    public DatabaseType getType() {
        return null;
    }
}
