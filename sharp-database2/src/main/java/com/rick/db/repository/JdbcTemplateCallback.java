package com.rick.db.repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

public interface JdbcTemplateCallback<T> {

    List<T> select(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, ?> paramMap);

}
