package com.rick.db.repository;

import com.rick.db.repository.support.TableMeta;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2025/8/14 11:52
 */
public interface EntityDAO<T, ID> {

    Optional<T> selectById(@NotNull ID id);

    List<T> selectByIds(@NotEmpty Collection<ID> ids);

    List<T> selectAll();

    List<T> select(String condition, Object... args);

    List<T> select(String condition, Map<String, ?> paramMap);

    List<T> selectWithColumns(String columns, String condition, Object... args);

    <E> List<E> select(@NotNull Class<E> clazz, @NotBlank String columns, String condition, Object... args);

    List<T> select(String columns, String condition, Map<String, ?> paramMap);

    List<T> select(String condition, T example);

    List<T> select(String columns, String condition, T example);

    <E> Optional<E> selectOne(@NotNull Class<E> clazz, @NotBlank String columns, String condition, T example);

    <E> List<E> select(@NotNull Class<E> clazz, @NotBlank String columns, String condition, T example);

    <E> List<E> select(@NotNull Class<E> clazz, @NotBlank String columns, String condition, Map<String, ?> paramMap);

    Boolean exists(String condition, Object... args);

    Boolean exists(String condition, T example);

    int deleteById(@NotNull ID id);

    int deleteByIds(@NotEmpty Collection<ID> ids);

    int deleteAll();

    int delete(String condition, Object... args);

    int delete(String condition, Map<String, ?> paramMap);

    T insert(@Valid @NotNull T entity);

    T update(@Valid @NotNull T entity);

    T insertOrUpdate(@Valid @NotNull T entity);

    Collection<T> insertOrUpdate(@Valid Collection<T> entityList);

    int updateById(String columns, @NotNull ID id, Object... args);

    int update(@NotBlank String columns, String condition, Object... args);

    int update(@NotBlank String columns, String condition, Map<String, ?> paramMap);

    int updateById(String columns, ID id, Map<String, ?> paramMap);

    int updateByIds(String columns, Collection<ID> ids, Map<String, ?> paramMap);

    TableMeta getTableMeta();

    TableDAO getTableDAO();

}
