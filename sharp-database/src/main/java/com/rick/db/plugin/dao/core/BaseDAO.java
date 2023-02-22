package com.rick.db.plugin.dao.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Rick
 * @createdAt 2021-10-31 09:29:00
 */
public interface BaseDAO<T, ID> extends CoreDAO<ID> {

    int insert(T entity);

    int insertOrUpdate(T entity);

    int[] insertOrUpdate(Collection<T> entities);

    int update(T entity);

    int update(T entity, Object[] params, String conditionSQL);

    int[] update(Collection<T> entities);

    Optional<T> selectById(ID id);

    Map<ID, T> selectByIdsAsMap(String ids);

    Map<ID, T> selectByIdsAsMap(ID ...ids);

    Map<ID, T> selectByIdsAsMap(Collection<?> ids);

    Map<ID, T> selectByIdsAsMap(Map<String, ?> params, String conditionSQL);

    List<T> selectByIds(String ids);

    List<T> selectByIds(ID ...ids);

    List<T> selectByIds(Collection<?> ids);

    Optional<ID> selectIdByParams(T example);

    Optional<ID> selectIdByParams(T example, String conditionSQL);

    List<ID> selectIdsByParams(T example);

    List<ID> selectIdsByParams(T example, String conditionSQL);

    List<T> selectByParams(String queryString);

    List<T> selectByParams(String queryString, String conditionSQL);

    List<T> selectByParams(T example);

    List<T> selectByParams(T example, String conditionSQL);

    List<T> selectAll();

    List<T> selectByParams(Map<String, ?> params);

    List<T> selectByParams(Map<String, ?> params, String conditionSQL);

    List<T> selectByParamsWithoutCascade(T example);

    List<T> selectByParamsWithoutCascade(T example, String conditionSQL);

    List<T> selectByParamsWithoutCascade(Map<String, ?> params);

    List<T> selectByParamsWithoutCascade(Map<String, ?> params, String conditionSQL);

    List<T> selectByParamsWithoutCascade(Map<String, ?> params, String columnNames, String conditionSQL);

    Map<ID, List<T>> groupByColumnName(String refColumnName, Collection<?> refValues);

    <M> Map<ID, List<M>> groupByColumnName(String refColumnName, Collection<?> refValues, String columnNames, Function<T, M> function);

    Class<T> getEntityClass();

    TableMeta getTableMeta();

    Map<String, Object> entityToMap(T example);

}
