package com.rick.db.plugin.dao.core;

import java.io.Serializable;
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

    int insert(Object[] params);

    int[] insert(List<?> paramsList);

    int deleteById(Serializable id);

    int deleteByIds(String ids);

    int deleteByIds(Collection<?> ids);

    int delete(String deleteColumn, String deleteValues);

    int delete(String deleteColumn, Collection<?> deleteValues);

    int delete(Object[] params, String conditionSQL);

    int deleteLogicallyById(Serializable id);

    int deleteLogicallyByIds(String ids);

    int deleteLogicallyByIds(Collection<?> ids);

    int update(Object[] params, Serializable id);

    int update(T t);

    int[] update(Collection<T> collection);

    int update(String updateColumnNames, Object[] params, Serializable id);

    int update(String updateColumnNames, Object[] params, String conditionSQL);

    int[] update(String updateColumnNames, List<Object[]> srcParamsList, String conditionSQL);

    int update(String updateColumnNames, Map<String, Object> params, String conditionSQL);

    Optional<T> selectById(Serializable id);

    Map<Serializable, T> selectByIdsAsMap(String ids);

    Map<Serializable, T> selectByIdsAsMap(Collection<?> ids);

    List<T> selectByIds(String ids);

    List<T> selectByIds(Collection<?> ids);

    List<T> selectByParams(String queryString);

    List<T> selectByParams(String queryString, String conditionSQL);

    List<T> selectByParams(T t);

    List<T> selectByParams(T t, String conditionSQL);

    List<T> selectByParams(Map<String, ?> params);

    List<T> selectAll();

    List<T> selectByParams(Map<String, ?> params, String conditionSQL);

    void selectAsSubTable(List<Map<String, Object>> masterData, String property, String refColumnName);

    Map<Long, List<T>> groupByColumnName(String refColumnName, Collection<?> refValues);

    String getSelectSQL();

    String getTableName();

    boolean isMapClass();

    boolean hasSubTables();

}
