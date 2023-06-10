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
public interface MapDAO<ID> extends CoreDAO<ID> {

    Optional<Map<String, Object>> selectById(ID id);

    Map<ID, Map<String, Object>> selectByIdsAsMap(String ids);

    Map<ID, Map<String, Object>> selectByIdsAsMap(ID... ids);

    Map<ID, Map<String, Object>> selectByIdsAsMap(Collection<?> ids);

    Map<ID, Map<String, Object>> selectByIdsAsMap(Map<String, ?> params, String conditionSQL);

    List<Map<String, Object>> selectByIds(String ids);

    List<Map<String, Object>> selectByIds(ID... ids);

    List<Map<String, Object>> selectByIds(Collection<?> ids);

    Optional<ID> selectIdByParams(Map<String, Object> example);

    Optional<ID> selectIdByParams(Map<String, Object> example, String conditionSQL);

    <S> Optional<S> selectSingleValueById(ID id, String columnName, Class<S> clazz);

    List<Map<String, Object>> selectByParams(String queryString);

    List<Map<String, Object>> selectByParams(String queryString, String conditionSQL);

    List<Map<String, Object>> selectAll();

    List<Map<String, Object>> selectByParams(Map<String, ?> params);

    List<Map<String, Object>> selectByParams(Map<String, ?> params, String conditionSQL);

    List<Map<String, Object>> selectByParams(Map<String, ?> params, String columnName, String conditionSQL);

    /**
     *
     * @param data 数据源 比如：Grid.list查询结果
     * @param refColumnName 外键
     * @param valueKey 数据源中外键value对应的属性名key
     * @param property 写到数据源中的key
     */
    void selectAsSubMapTable(List<Map<String, Object>> data, String refColumnName, String valueKey, String property);

    Map<ID, List<Map<String, Object>>> groupByColumnName(String refColumnName, Collection<?> refValues);

    <M> Map<ID, List<M>> groupByColumnName(String refColumnName, Collection<?> refValues, String columnNames, Function<Map<String, Object>, M> function);

}
