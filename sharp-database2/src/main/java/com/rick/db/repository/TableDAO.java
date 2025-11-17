package com.rick.db.repository;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2025/8/14 11:52
 */
public interface TableDAO {

    <E> List<E> select(@NotNull Class<E> clazz, @NotBlank String sql, Object... args);

    <E> List<E> select(@NotNull Class<E> clazz, @NotBlank String sql, Map<String, Object> paramMap);

    List<Map<String, Object>> select(String sql, Map<String, Object> paramMap);

    <K, V> Map<K, V> selectForKeyValue(String sql, Map<String, Object> paramMap);

    Optional<Map<String, Object>> selectForObject(String sql, Map<String, Object> paramMap);

    <E> List<E> select(String sql, Map<String, Object> paramMap, JdbcTemplateCallback<E> jdbcTemplateCallback);

    Boolean exists(@NotBlank String sql, Object... args);

    Boolean exists(@NotBlank String sql, Map<String, Object> paramMap);

    int update(String tableName, String columns, String condition, Object... args);

    int update(String tableName, String columns, String condition, Map<String, Object> paramMap);

    int deleteIn(String tableName, String deleteColumn, Collection<?> deleteValues);

    int deleteNotIn(String tableName, String deleteColumn, Collection<?> deleteValues);

    int delete(String tableName, String condition, Object... args);

    int delete(String tableName, String condition, Map<String, Object> paramMap);

    int insert(String tableName, String columnNames, Map<String, Object> paramMap);

    Number insertAndReturnKey(String tableName, String columnNames, Map<String, Object> params, String... idColumnName);

    void updateRefTable(String refTableName, String keyColumn, String guestColumn, Object keyInstance, Collection<?> guestInstanceIds);

    void execute(String sql);

    <T> T execute(ConnectionCallback<T> action);

    NamedParameterJdbcTemplate getNamedParameterJdbcTemplate();

}
