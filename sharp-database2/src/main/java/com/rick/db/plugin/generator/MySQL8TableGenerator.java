package com.rick.db.plugin.generator;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.db.repository.model.DatabaseType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * MySQL 根据实体类创建表
 * @author Rick
 * @createdAt 2022-03-01 12:05:00
 */
public class MySQL8TableGenerator extends MySQL5TableGenerator {

    public MySQL8TableGenerator(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String determineSqlType(Class type) {
        if (type == Map.class || type == List.class || JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(type)) {
            return "JSON";
        }
        return super.determineSqlType(type);
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MySQL8;
    }

}
