package com.rick.db.plugin.dao.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick
 * @createdAt 2021-10-31 09:29:00
 */
public interface BaseDAO<T> {
    int insert(T t);

    int insertOrUpdate(T t);

    int insert(Object[] params);

    int[] insert(Collection<?> paramsList);

    int[] insertOrUpdate(Collection<T> entities);

    int deleteById(Long id);

    int deleteByIds(String ids);

    int deleteByIds(Long ...ids);

    int deleteByIds(Collection<?> ids);

    int delete(String deleteColumn, String deleteValues);

    int delete(String deleteColumn, Collection<?> deleteValues);

    int delete(Object[] params, String conditionSQL);

    int delete(Map<String, Object> params, String conditionSQL);

    int deleteLogicallyById(Long id);

    int deleteLogicallyByIds(String ids);

    int deleteLogicallyByIds(Collection<?> ids);

    /**
     * 性能不好，慎用
     * @return
     */
    int deleteAll();

    int update(Object[] params, Long id);

    int update(T t);

    int update(T t, Object[] params, String conditionSQL);

    int[] update(Collection<T> entities);

    int update(String updateColumnNames, Object[] params, Long id);

    int update(String updateColumnNames, Object[] params, String conditionSQL);

    int[] update(String updateColumnNames, List<Object[]> srcParamsList, String conditionSQL);

    int update(String updateColumnNames, Map<String, Object> params, String conditionSQL);

    Optional<T> selectById(Long id);

    Map<Long, T> selectByIdsAsMap(String ids);

    Map<Long, T> selectByIdsAsMap(Long ...ids);

    Map<Long, T> selectByIdsAsMap(Collection<?> ids);

    List<T> selectByIds(String ids);

    List<T> selectByIds(Long ...ids);

    List<T> selectByIds(Collection<?> ids);

    Optional<Long> selectIdByParams(T t, String conditionSQL);

    List<Long> selectIdsByParams(T t, String conditionSQL);

    List<Long> selectIdsByParams(Map<String, ?> params, String conditionSQL);

    List<T> selectByParams(String queryString);

    List<T> selectByParams(String queryString, String conditionSQL);

    List<T> selectByParams(T t);

    List<T> selectByParams(T t, String conditionSQL);

    List<T> selectByParams(Map<String, ?> params);

    List<T> selectAll();

    long countByParams(Map<String, ?> params, String conditionSQL);

    boolean existsByParams(Map<String, ?> params, String conditionSQL);

    List<T> selectByParams(Map<String, ?> params, String conditionSQL);

    <E> List<E> selectByParams(Map<String, ?> params, String selectSQL, String conditionSQL, Class<E> clazz);

    <K, V> Map<K, V> selectByParamsAsMap(Map<String, ?> params, String columnNames, String conditionSQL);

    void checkId(Long id);

    void checkId(Long id, Map<String, Object> conditionParams, String condition);

    void checkIds(Collection<Long> ids);

    void checkIds(Collection<Long> ids, Map<String, Object> conditionParams, String condition);

    void selectAsSubTable(List<Map<String, Object>> masterData, String property, String refColumnName);

    Map<Long, List<T>> groupByColumnName(String refColumnName, Collection<?> refValues);

    String getSelectSQL();

    String getTableName();

    Class getEntity();

    Map<String, Object> entityToMap(T t);

    /**
     * 记录超过1个会抛出异常
     * @param list
     * @param <E>
     * @return
     */
    <E> Optional<E> expectedAsOptional(List<E> list);

}
