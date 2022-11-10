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
public interface BaseDAO<T, ID> {

    int insert(T entity);

    int insertOrUpdate(T entity);

    int insert(Object[] params);

    int[] insert(Collection<?> paramsList);

    int[] insertOrUpdate(Collection<T> entities);

    int deleteById(ID id);

    int deleteByIds(String ids);

    int deleteByIds(ID ...ids);

    int deleteByIds(Collection<?> ids);

    int delete(String deleteColumn, String deleteValues);

    int delete(String deleteColumn, Collection<?> deleteValues);

    int delete(Object[] params, String conditionSQL);

    int delete(Map<String, Object> params, String conditionSQL);

    int deleteLogicallyById(ID id);

    int deleteLogicallyByIds(String ids);

    int deleteLogicallyByIds(Collection<?> ids);

    /**
     * 性能不好，慎用
     * @return
     */
    int deleteAll();

    int update(Object[] params, ID id);

    int update(T entity);

    int update(T entity, Object[] params, String conditionSQL);

    int[] update(Collection<T> entities);

    int updateById(String updateColumnNames, Object[] params, ID id);

    int update(String updateColumnNames, Object[] params, String conditionSQL);

    int[] update(String updateColumnNames, List<Object[]> srcParamsList, String conditionSQL);

    int update(String updateColumnNames, Map<String, Object> params, String conditionSQL);

    Optional<T> selectById(ID id);

    Map<ID, T> selectByIdsAsMap(String ids);

    Map<ID, T> selectByIdsAsMap(ID ...ids);

    Map<ID, T> selectByIdsAsMap(Collection<?> ids);

    List<T> selectByIds(String ids);

    List<T> selectByIds(ID ...ids);

    List<T> selectByIds(Collection<?> ids);

    Optional<ID> selectIdByParams(T example);

    Optional<ID> selectIdByParams(T example, String conditionSQL);

    List<ID> selectIdsByParams(T example);

    List<ID> selectIdsByParams(T example, String conditionSQL);

    List<ID> selectIdsByParams(Map<String, ?> params);

    List<ID> selectIdsByParams(Map<String, ?> params, String conditionSQL);

    List<T> selectByParams(String queryString);

    List<T> selectByParams(String queryString, String conditionSQL);

    List<T> selectByParams(T example);

    List<T> selectByParams(T example, String conditionSQL);

    List<T> selectAll();

    long countByParams(Map<String, ?> params, String conditionSQL);

    boolean existsByParams(Map<String, ?> params, String conditionSQL);

    List<T> selectByParams(Map<String, ?> params);

    List<T> selectByParams(Map<String, ?> params, String conditionSQL);

    <E> List<E> selectByParams(Map<String, ?> params, String columnNames, Class<E> clazz);

    <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, Class<E> clazz);

    List<T> selectByParamsWithoutCascade(T example);

    List<T> selectByParamsWithoutCascade(T example, String conditionSQL);

    List<T> selectByParamsWithoutCascade(Map<String, ?> params);

    List<T> selectByParamsWithoutCascade(Map<String, ?> params, String conditionSQL);

    List<T> selectByParamsWithoutCascade(Map<String, ?> params, String columnNames, String conditionSQL);

    <K, V> Map<K, V> selectByParamsAsMap(Map<String, ?> params, String columnNames);

    <K, V> Map<K, V> selectByParamsAsMap(Map<String, ?> params, String columnNames, String conditionSQL);

    void checkId(ID id);

    void checkId(ID id, Map<String, Object> params, String condition);

    void checkIds(Collection<ID> ids);

    void checkIds(Collection<ID> ids, Map<String, Object> params, String condition);

    /**
     *
     * @param data 数据源 比如：Grid.list查询结果
     * @param refColumnName 外键
     * @param valueKey 数据源中外键value对应的属性名key
     * @param property 写到数据源中的key
     */
    void selectAsSubTable(List<Map<String, Object>> data, String refColumnName, String valueKey, String property);

    Map<ID, List<T>> groupByColumnName(String refColumnName, Collection<?> refValues);

    <M> Map<ID, List<M>> groupByColumnName(String refColumnName, Collection<?> refValues, String columnNames, Function<T, M> function);

    String getSelectSQL();

    String getFullColumnNames();

    String getTableName();

    Class<T> getEntity();

    String getIdColumnName();

    Map<String, Object> entityToMap(T example);

    /**
     * 记录超过1个会抛出异常
     * @param list
     * @param <E>
     * @return
     */
    <E> Optional<E> expectedAsOptional(List<E> list);

}
