package com.rick.db.plugin.dao.core;

import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Rick
 * @createdAt 2021-10-31 09:29:00
 */
public interface EntityDAO<T, ID> extends CoreDAO<ID> {

    int insert(T entity);

    int insertOrUpdate(T entity);

    int[] insertOrUpdate(Collection<T> entities);

    /**
     * 以 refColumnName 分组的全量更新， 多用于父表不级联， 手动调用该方法处理子表的更新操作
     * @param refColumnName
     * @param refValue
     * @param entities
     * @return
     */
    int[] insertOrUpdate(@NonNull String refColumnName, @NonNull Object refValue, Collection<T> entities);

    int update(T entity);

    int update(T entity, String updateColumnNames);

    int update(T entity, Object[] params, String conditionSQL);

    int[] update(Collection<T> entities);

    Optional<T> selectById(ID id);

    Map<ID, T> selectByIdsAsMap(String ids);

    Map<ID, T> selectByIdsAsMap(ID... ids);

    Map<ID, T> selectByIdsAsMap(Collection<?> ids);

    Map<ID, T> selectByIdsAsMap(Map<String, ?> params, String conditionSQL);

    List<T> selectByIds(String ids);

    List<T> selectByIds(ID... ids);

    List<T> selectByIds(Collection<?> ids);

    Optional<ID> selectIdByParams(T example);

    Optional<ID> selectIdByParams(T example, String conditionSQL);

    <S> Optional<S> selectSingleValueById(ID id, String columnName, Class<S> clazz);

    List<ID> selectIdsByParams(T example);

    List<ID> selectIdsByParams(T example, String conditionSQL);

    List<T> selectByParams(String queryString);

    List<T> selectByParams(String queryString, String conditionSQL);

    List<T> selectByParams(T example);

    List<T> selectByParams(T example, String conditionSQL);

    List<T> selectAll();

    List<T> selectByParams(Map<String, ?> params);

    List<T> selectByParams(Map<String, ?> params, String conditionSQL);

    List<T> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL);

    List<T> selectByParamsWithoutCascade(T example);

    List<T> selectByParamsWithoutCascade(T example, String conditionSQL);

    List<T> selectByParamsWithoutCascade(Map<String, ?> params);

    List<T> selectByParamsWithoutCascade(Map<String, ?> params, String conditionSQL);

    List<T> selectByParamsWithoutCascade(Map<String, ?> params, String columnNames, String conditionSQL);

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

    Class<T> getEntityClass();

    TableMeta getTableMeta();

    Map<String, Object> entityToMap(T entity);

    <E> E mapToEntity(Map<String, ?> map);

    <E> E mapToEntity(Map<String, ?> map, Class<E> entityClass);

    String getSelectConditionSQL(Map<String, ?> params);

    void selectPropertyBySql(List<T> list);

    Map<String, String> getPropertyNameToColumnNameMap();

}
