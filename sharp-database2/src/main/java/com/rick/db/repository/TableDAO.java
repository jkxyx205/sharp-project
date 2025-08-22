package com.rick.db.repository;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2025/8/14 11:52
 */
public interface TableDAO {

    <E> List<E> select(@NotNull Class<E> clazz, @NotBlank String sql, Object... args);

    <E> List<E> select(@NotNull Class<E> clazz, @NotBlank String sql, Map<String, ?> paramMap);

    List<Map<String, Object>> select(String sql, Map<String, ?> params);

    <K, V> Map<K, V> selectForKeyValue(String sql, Map<String, ?> paramMap);

    Optional<Map<String, Object>> selectForObject(String sql, Map<String, ?> paramMap);

    <E> List<E> select(String sql, Map<String, ?> paramMap, JdbcTemplateCallback<E> jdbcTemplateCallback);

    Boolean exists(@NotBlank String sql, Object... args);

    Boolean exists(@NotBlank String sql, Map<String, ?> paramMap);

    int update(String tableName, String columns, String condition, Object... args);

    int update(String tableName, String columns, String condition, Map<String, ?> paramMap);

    int delete(String tableName, String condition, Object... args);

    int delete(String tableName, String condition, Map<String, ?> paramMap);

    int insert(String tableName, Map<String, Object> paramMap);

    Number insertAndReturnKey(String tableName, Map<String, Object> params, String... idColumnName);

    void execute(String sql);

}
