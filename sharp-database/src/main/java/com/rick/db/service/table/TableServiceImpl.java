package com.rick.db.service.table;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.rick.common.util.ClassUtils;
import com.rick.db.config.Constants;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.SharpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.util.*;

/**
 * @author Rick
 * @createdAt 2021-09-23 16:41:00
 */
public class TableServiceImpl<T> {

    private final SharpService sharpService;

    private final JdbcTemplate jdbcTemplate;

    private final String tableName;

    /**
     * column1, column2, column3
     */
    private final String columnNames;

    private final List<String> columnNameList;

    private static final String PRIMARY_COLUMN = "id";

    @Autowired(required = false)
    private TableColumnAutoFill tableColumnAutoFill;

    public TableServiceImpl(SharpService sharpService, String tableName, String columnNames) {
        this.sharpService = sharpService;
        this.jdbcTemplate = sharpService.getNamedJdbcTemplate().getJdbcTemplate();
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.columnNameList = getColumnNameListFromString(columnNames);
    }

    /**
     * 插入单条数据
     * @param params
     * @return
     */
    public int insert(Object[] params) {
        return SQLUtils.insert(tableName, columnNames, handleAutoFill(params, columnNameList));
    }

    /**
     * 批量插入数据
     */
    public int[] insert(List<Object[]> paramsList) {
        return SQLUtils.insert(tableName, columnNames, handleAutoFill(paramsList, columnNameList));
    }

    /**
     * 通过主鍵id刪除
     *
     * @param id
     */
    public int deleteById(Serializable id) {
        return SQLUtils.delete(tableName, PRIMARY_COLUMN, String.valueOf(id));
    }

    /**
     * 通过主鍵id批量刪除 eg：ids -> “1,2,3,4”
     * @param ids
     */
    public int deleteByIds(String ids) {
        return SQLUtils.delete(tableName, PRIMARY_COLUMN, ids);
    }

    /**
     * 通过主鍵id批量刪除 eg：ids -> [1, 2, 3, 4]
     * @param ids
     */
    public int deleteByIds(Collection<?> ids) {
        return SQLUtils.delete(tableName, PRIMARY_COLUMN, ids);
    }

    /**
     * 根据某个自定义的字段删除
     * @param deleteColumn
     * @param deleteValues
     * @return
     */
    public int delete(String deleteColumn, String deleteValues) {
        return SQLUtils.delete(tableName, deleteColumn, deleteValues);
    }

    /**
     * 构造条件和参数删除
     * @param params
     * @param conditionSQL id IN (?, ?) AND group_id = ?
     * @return
     */
    public int delete(Object[] params, String conditionSQL) {
        return SQLUtils.delete(tableName, params, conditionSQL);
    }

    /**
     * 更新所有字段
     * @param params
     * @param id
     */
    public int update(Object[] params, Serializable id) {
        return SQLUtils.update(tableName, columnNames, handleAutoFill(params, columnNameList), id);
    }

    /**
     * 指定更新字段
     * @param updateColumnNames
     * @param params
     * @param id
     */
    public int update(String updateColumnNames, Object[] params, Serializable id) {
        return SQLUtils.update(tableName, updateColumnNames, handleAutoFill(params, getColumnNameListFromString(updateColumnNames)), id);
    }
    /**
     * 指定更新字段，构造条件更新
     * @param updateColumnNames
     * @param params
     * @param conditionSQL
     */
    public int update(String updateColumnNames, Object[] params, String conditionSQL) {
        return SQLUtils.update(tableName, updateColumnNames, handleAutoFill(params, getColumnNameListFromString(updateColumnNames)), conditionSQL);
    }

    /**
     * 通过ID查找
     * @param id
     * @return
     */
    public Optional<T> selectById(Serializable id) {
        List<T> list = selectByParams(PRIMARY_COLUMN + "=" + id, PRIMARY_COLUMN + " = :id");
        return Optional.ofNullable(list.size() == 1 ? list.get(0) : null);
    }

    /**
     * 通过多个ID查找//eg：ids -> “1,2,3,4”
     * @param ids
     * @return
     */
    public List<T> selectByIds(String ids) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("ids", ids);
        return selectByParams(params, PRIMARY_COLUMN + " IN(:ids)");
    }

    public List<T> selectByIds(Collection<?> ids) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("ids", ids);
        return selectByParams(params, PRIMARY_COLUMN + " IN(:ids)");
    }

    /**
     * name=23&age=15,13 => name=:name AND age IN(:age)
     * 注意`=`两边的空格问题
     * @param queryString
     * @return
     */
    public List<T> selectByParams(String queryString) {
        return selectByParams(queryString, null);
    }

    public List<T> selectByParams(String queryString, String conditionSQL) {
        final Map<String, String> map = Splitter.on('&').trimResults().withKeyValueSeparator('=').split(queryString);
        return selectByParams(map, conditionSQL);
    }

    /**
     * 根据条件查找
     *
     * @param
     * @return
     */
    public List<T> selectByParams(Map<String, ?> params) {
        return selectByParams(params, null);
    }

    /**
     * 依赖sharpService，可以进行不定条件的查询
     * @param params
     * @param conditionSQL
     * @return
     */
    public List<T> selectByParams(Map<String, ?> params, String conditionSQL) {
        return (List<T>) sharpService.query(getSelectSQL() + " WHERE " + (Objects.isNull(conditionSQL) ? getConditionSQL(params) : conditionSQL),
                params,
                getActualTypeArgument());
    }

    /**
     * 获取所有
     *
     * @return
     */
    public List<T> selectAll() {
        return (List<T>) jdbcTemplate.query(getSelectSQL(), new BeanPropertyRowMapper<>(getActualTypeArgument()));
    }

    private Class<?> getActualTypeArgument() {
        return ClassUtils.getActualTypeArgument(this.getClass())[0];
    }

    private String getSelectSQL() {
        return "SELECT " + columnNames + " FROM " + tableName;
    }

    private String getConditionSQL(Map<String, ?> params) {
        StringBuilder sb = new StringBuilder();
        for (String columnName : columnNameList) {
            Object value = params.get(columnName);
            sb.append(columnName).append(decideParamHolder(columnName, value)).append(" AND ");
        }

        return sb.substring(0, sb.length() - 5);
    }

    private String decideParamHolder(String columnName, Object value) {
        if (Objects.isNull(value)) {
            return " = :" + columnName;
        }
        if (value instanceof Iterable || value.getClass().isArray() || (((String) value).split(Constants.PARAM_IN_SEPARATOR).length > 1)) {
            return " IN (:" + columnName + ")";
        } /*else if (((String) value).startsWith(Constants.PARAM_LIKE_SEPARATOR)) {
            params.put(columnName, ((String) value).substring(1));
            return " LIKE :" + columnName;
        }*/

        return " = :" + columnName;
    }

    private List<Object[]> handleAutoFill(List<Object[]> paramsList, List<String> columnNameList) {
        for (Object[] params : paramsList) {
            handleAutoFill(params, columnNameList);
        }
        return paramsList;
    }

    private Object[] handleAutoFill(Object[] params, List<String> columnNameList) {
        if (Objects.nonNull(tableColumnAutoFill)) {
            Map<String, Object> fill = tableColumnAutoFill.fill();

            for (Map.Entry<String, Object> en : fill.entrySet()) {
                String fillColumnName = en.getKey();
                Object fillColumnValue = en.getValue();

                int index = columnNameList.indexOf(fillColumnName);
                if (index > -1) {
                    params[index] = fillColumnValue;
                }
            }
        }

        return params;
    }

    private List<String>  getColumnNameListFromString(String columnNames) {
        return Arrays.asList(columnNames.split(",\\s*"));
    }
}
