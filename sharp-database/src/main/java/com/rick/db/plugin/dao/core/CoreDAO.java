package com.rick.db.plugin.dao.core;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick
 * @createdAt 2021-10-31 09:29:00
 */
public interface CoreDAO<ID> {

    int insert(Object[] params);

    int insert(Map<String, ?> params);

    int update(Map<String, ?> params);

    int[] insert(Collection<?> paramsList);

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

    int updateById(String updateColumnNames, Object[] params, ID id);

    int update(String updateColumnNames, Object[] params, String conditionSQL);

    int[] update(String updateColumnNames, List<Object[]> srcParamsList, String conditionSQL);

    int update(String updateColumnNames, Map<String, Object> params, String conditionSQL);


    List<ID> selectIdsByParams(Map<String, ?> params);

    List<ID> selectIdsByParams(Map<String, ?> params, String conditionSQL);


    long countByParams(Map<String, ?> params, String conditionSQL);

    boolean existsByParams(Map<String, ?> params, String conditionSQL);


    <E> List<E> selectByParams(Map<String, ?> params, String columnNames, Class<E> clazz);

    <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, Class<E> clazz);


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

    String getSelectSQL();

    String getColumnNames();

    String getTableName();

    String getIdColumnName();

    /**
     * 记录超过1个会抛出异常
     * @param list
     * @param <E>
     * @return
     */
    default <E> Optional<E> expectedAsOptional(List<E> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Optional.empty();
        }

        if (list.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, list.size());
        }

        return Optional.of(list.get(0));
    }

}
