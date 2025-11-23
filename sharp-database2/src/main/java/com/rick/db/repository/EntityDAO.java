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
import java.util.function.Consumer;

/**
 * @author Rick.Xu
 * @date 2025/8/14 11:52
 */
public interface EntityDAO<T, ID> {

    Optional<T> selectById(@NotNull ID id);

    List<T> selectByIds(@NotEmpty Collection<ID> ids);

    List<T> selectAll();

    <K, V> Map<K, V> selectForKeyValue(@NotBlank String columns, String condition, Map<String, Object> paramMap);

    <K, V> Map<K, V> selectForKeyValue(@NotBlank String columns, String condition, Object... args);

    <K, V> Map<K, V> selectForKeyValue(@NotBlank String columns, String condition, T example);

    List<T> select(String condition, Object... args);

    List<T> select(Map<String, Object> paramMap);

    List<T> select(String condition, Map<String, Object> paramMap);

    List<T> selectWithColumns(@NotBlank String columns, String condition, Object... args);

    <E> List<E> select(@NotNull Class<E> clazz, @NotBlank String columns, String condition, Object... args);

    <E> List<E> selectWithoutCascade(@NotNull Class<E> clazz, @NotBlank String columns, String condition, Object... args);

    List<T> select(@NotBlank String columns, String condition, Map<String, Object> paramMap);

    List<T> select(T example);

    List<T> select(String condition, T example);

    List<T> select(@NotBlank String columns, String condition, T example);

    <E> List<E> select(@NotNull Class<E> clazz, @NotBlank String columns, String condition, T example);

    <E> List<E> select(@NotNull Class<E> clazz, @NotBlank String columns, String condition, Map<String, Object> paramMap);

    <E> List<E> selectWithoutCascade(Class<E> clazz, String columns, String condition, Map<String, Object> paramMap);

    <E> List<E> selectWithoutCascade(Class<E> clazz, String columns, String condition, T example);

    void cascadeSelect(List<T> list);

    boolean exists(@NotNull ID id);

    boolean exists(String condition, Object... args);

    boolean exists(String condition, Map<String, Object> paramMap);

    boolean exists(String condition, T example);

    long count(String condition, Object... args);

    long count(String condition, Map<String, Object> paramMap);

    long count(String condition, T example);

    int deleteById(@NotNull ID id);

    int deleteByIds(@NotEmpty Collection<ID> ids);

    int deleteAll();

    int delete(String condition, Object... args);

    int delete(String condition, Map<String, Object> paramMap);

    T insert(@Valid @NotNull T entity);

    T insertOrUpdate(@Valid @NotNull Map<String, Object> paramMap);

    T update(@Valid @NotNull T entity);

    T insertOrUpdate(@Valid @NotNull T entity);

    Collection<T> insertOrUpdate(@Valid Collection<T> entityList);

    Collection<T> insertOrUpdateTable(Collection<T> entityList);

    Collection<T> insertOrUpdateTable(Collection<T> entityList, boolean deleteItem, Consumer<Collection<ID>> deletedIdsConsumer);

    Collection<T> insertOrUpdate(Collection<T> entityList, @NotNull String refColumnName, @NotNull Object refValue);

    Collection<T> insertOrUpdate(Collection<T> entityList, @NotNull String refColumnName, @NotNull Object refValue, boolean deleteItem, Consumer<Collection<ID>> deletedIdsConsumer);

    int update(@NotBlank String columns, String condition, Object... args);

    int update(@NotBlank String columns, String condition, Map<String, Object> paramMap);

    int update(@NotBlank String columns, String condition, T example);

    int updateById(@NotBlank String columns, @NotNull ID id, Object... args);

    int updateById(@NotBlank String columns, @NotNull ID id, Map<String, Object> paramMap);

    int updateById(@NotBlank String columns, @NotNull ID id, T example);

    int updateByIds(@NotBlank String columns, Collection<ID> ids, Map<String, Object> paramMap);

    int updateByIds(@NotBlank String columns, Collection<ID> ids, T example);

    TableMeta getTableMeta();

    TableDAO getTableDAO();

    Map<String, Object> entityToMap(T entity);

}
