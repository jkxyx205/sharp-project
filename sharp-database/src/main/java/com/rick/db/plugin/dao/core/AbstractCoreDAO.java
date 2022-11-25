package com.rick.db.plugin.dao.core;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.Lists;
import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.common.util.EnumUtils;
import com.rick.common.util.JsonUtils;
import com.rick.db.constant.BaseEntityConstants;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.SharpService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/**
 * // TODO
 * @author Rick
 * @createdAt 2022-11-25 15:16:00
 */
public abstract class AbstractCoreDAO<ID> implements CoreDAO<ID> {

    @Autowired
    protected SharpService sharpService;

    private String idColumnName;

    private String columnNames;

    private String tableName;

    private List<String> columnNameList;

    private String fullColumnNames;

    public AbstractCoreDAO(String tableName, String columnNames, String idColumnName) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.idColumnName = idColumnName;
        this.init();
    }

    @Override
    public int insert(Map<String, ?> params) {
        Object[] arrayParams = mapToParamsArray(params, this.columnNameList);
        return insert(arrayParams);
    }

    /**
     * 插入单条数据
     *
     * @param params 参数数组
     * @return
     */
    @Override
    public int insert(Object[] params) {
        Assert.notEmpty(params, "参数不能为空");
        return SQLUtils.insert(this.tableName, this.columnNames, params);
    }

    @Override
    public int update(Map<String, ?> params) {
        Object[] arrayParams = mapToParamsArray(params, this.columnNameList);
        return SQLUtils.update(this.getTableName(), this.columnNames, arrayParams, (Serializable) params.get(this.idColumnName), this.getIdColumnName());
    }

    @Override
    public int[] insert(Collection<?> paramsList) {
        return new int[0];
    }

    /**
     * 通过主键id刪除
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(ID id) {
        Assert.notNull(id, "id不能为空");
        return deleteByIds(Lists.newArrayList(id));
    }

    /**
     * 通过主键id批量刪除 eg：ids -> “1,2,3,4”
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(String ids) {
        Assert.hasText(ids, "id不能为空");
        return deleteByIds(Arrays.asList(ids.split(",")));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(ID ...ids) {
        Assert.notEmpty(ids, "id不能为空");
        return deleteByIds(Arrays.asList(ids));
    }

    /**
     * 通过主键id批量刪除 eg：ids -> [1, 2, 3, 4]
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Collection<?> ids) {
        return delete(this.idColumnName, ids);
    }

    /**
     * 根据某个自定义的字段删除
     *
     * @param deleteColumn
     * @param deleteValues
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String deleteColumn, String deleteValues) {
        Assert.hasText(deleteValues, "deleteValues不能为空");
        return delete(deleteColumn, Arrays.asList(deleteValues.split(",")));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String deleteColumn, Collection<?> deleteValues) {
        Assert.hasText(deleteColumn, "deleteColumn不能为空");
        return SQLUtils.delete(this.tableName, deleteColumn, deleteValues);
    }

    /**
     * 构造条件和参数删除。注意占位符用？
     *
     * @param params
     * @param conditionSQL id IN (?, ?) AND group_id = ?
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Object[] params, String conditionSQL) {
        return SQLUtils.delete(this.tableName, params, conditionSQL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Map<String, Object> params, String conditionSQL) {
        int count = sharpService.update("DELETE FROM " + getTableName() + " WHERE " + conditionSQL, params);
        return count;
    }

    /**
     * 通过主键逻辑id刪除
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogicallyById(ID id) {
        Assert.notNull(id, "id不能为空");
        return update(BaseEntityConstants.LOGIC_DELETE_COLUMN_NAME, new Object[]{1, id}, this.idColumnName + " = ?");
    }

    /**
     * 通过主键id批量逻辑刪除 eg：ids -> “1,2,3,4”
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogicallyByIds(String ids) {
        Assert.hasText(ids, "id不能为空");
        return deleteLogicallyByIds(Arrays.asList(ids.split(BaseEntityConstants.COLUMN_NAME_SEPARATOR_REGEX)));
    }

    /**
     * 通过主键id批量逻辑刪除 eg：ids -> [1, 2, 3, 4]
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogicallyByIds(Collection<?> ids) {
        Assert.notEmpty(ids, "id不能为空");

        Object[] mergedParams = new Object[ids.size() + 1];
        mergedParams[0] = 1;
        System.arraycopy(ids.toArray(), 0, mergedParams, 1, ids.size());
        return update(BaseEntityConstants.LOGIC_DELETE_COLUMN_NAME, mergedParams, this.idColumnName + " IN " + SQLUtils.formatInSQLPlaceHolder(ids.size()));
    }

    @Override
    public int deleteAll() {
        return 0;
    }

    /**
     * 更新所有字段
     *
     * @param params
     * @param id
     */
    @Override
    public int update(Object[] params, ID id) {
        return updateById(this.columnNames, params, id);
    }

    /**
     * 指定更新字段，构造条件更新
     *
     * @param updateColumnNames name, age
     * @param params            {"Rick", 23, LocalDateTime.now, 1}
     * @param conditionSQL      created_at > ? AND created_by = ?
     */
    @Override
    public int update(String updateColumnNames, Object[] params, String conditionSQL) {
        return update(tableName, updateColumnNames, params, conditionSQL);
    }

    @Override
    public int[] update(String updateColumnNames, List<Object[]> srcParamsList, String conditionSQL) {
        if (CollectionUtils.isEmpty(srcParamsList)) {
            return new int[] {};
        }
        return SQLUtils.update(this.tableName, updateColumnNames, srcParamsList, conditionSQL);
    }

    /**
     * 动态查询
     * @param updateColumnNames name, age, user_name
     * @param params Map<String, Object> {name = rick, age = 5, user_name = Jim, id = 5}
     * @param conditionSQL id = :id
     * @return
     */
    @Override
    public int update(String updateColumnNames, Map<String, Object> params, String conditionSQL) {
        Assert.hasText(updateColumnNames, "updateColumnNames不能为空");
        Assert.hasText(conditionSQL, "conditionSQL不能为空");
        return sharpService.update(getParamsUpdateSQL(updateColumnNames, params, conditionSQL), params);
    }

    @Override
    public List<ID> selectIdsByParams(Map<String, ?> params) {
        return null;
    }

    @Override
    public List<ID> selectIdsByParams(Map<String, ?> params, String conditionSQL) {
        return null;
    }

    @Override
    public long countByParams(Map<String, ?> params, String conditionSQL) {
        return 0;
    }

    @Override
    public boolean existsByParams(Map<String, ?> params, String conditionSQL) {
        return false;
    }

    @Override
    public <E> List<E> selectByParams(Map<String, ?> params, String columnNames, Class<E> clazz) {
        return selectByParams(params, columnNames, null, clazz);
    }

    @Override
    public <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, Class<E> clazz) {
        return sharpService.query("SELECT " + columnNames + " FROM " + getTableName() +  " WHERE " + conditionSQL, params, clazz);
    }

    @Override
    public <K, V> Map<K, V> selectByParamsAsMap(Map<String, ?> params, String columnNames) {
        return null;
    }

    @Override
    public <K, V> Map<K, V> selectByParamsAsMap(Map<String, ?> params, String columnNames, String conditionSQL) {
        return null;
    }

    @Override
    public void checkId(ID id) {

    }

    @Override
    public void checkId(ID id, Map<String, Object> params, String condition) {

    }

    @Override
    public void checkIds(Collection<ID> ids) {

    }

    @Override
    public void checkIds(Collection<ID> ids, Map<String, Object> params, String condition) {

    }

    @Override
    public void selectAsSubTable(List<Map<String, Object>> data, String refColumnName, String valueKey, String property) {

    }

    @Override
    public String getSelectSQL() {
        return "SELECT "+fullColumnNames+" FROM " + getTableName();
    }

    @Override
    public String getColumnNames() {
        return this.columnNames;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public String getIdColumnName() {
        return this.idColumnName;
    }

    public String getFullColumnNames() {
        return fullColumnNames;
    }

    public List<String> getColumnNameList() {
        return columnNameList;
    }

    protected Object resolveValue(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        // 处理BaseDAOImpl特殊类型
        if (Enum.class.isAssignableFrom(value.getClass())) {
            return EnumUtils.getCode((Enum) value);
        } else if (value.getClass() == Instant.class) {
            return Timestamp.from((Instant) value);
        } else if (JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(value.getClass())) {
            return toJson(value);
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            Collection<?> coll = (Collection<?>) value;
            if (coll.size() == 0) {
                return "[]";
            } else {
                return toJson(value);
            }/*else if (JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(coll.iterator().next().getClass())) {
                return toJson(value);
            }*/
        } else if (Map.class.isAssignableFrom(value.getClass())) {
            Map<String, ?> map = (Map<String, ?>)value;
            if (map.size() == 0) {
                return "{}";
            } else {
                return toJson(value);
            }
        } else if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            if (length == 0) {
                return null;
            }
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < length; i ++) {
                Object o = Array.get(value, i);
                values.append(o).append(",");
            }

            return values.deleteCharAt(values.length() - 1);
        } else if (BaseDAOManager.isEntityClass((value.getClass()))) {
            // 实体对象
            return BaseDAOManager.getPropertyValue(value, BaseDAOManager.getTableMeta(value.getClass()).getIdPropertyName());
        }

        // JDBC 支持类型
        int sqlTypeValue = StatementCreatorUtils.javaTypeToSqlParameterType(value.getClass());

        if (SqlTypeValue.TYPE_UNKNOWN != sqlTypeValue) {
            return value;
        } else {
            return String.valueOf(value);
        }
    }

    private void init() {
        this.columnNameList = convertToArray(this.columnNames);
        initFullColumnNames();
    }

    private void initFullColumnNames() {
        Converter<String, String> converter = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL);
        int columnSize = this.columnNameList.size();
        StringBuilder selectSQLBuilder = new StringBuilder();
        for (int i = 0; i < columnSize; i++) {
            selectSQLBuilder.append(this.getTableName() + "." + this.columnNameList.get(i))
                    .append(" AS \"").append(converter.convert(this.columnNameList.get(i))).append("\",");
        }
        selectSQLBuilder.deleteCharAt(selectSQLBuilder.length() - 1);

        this.fullColumnNames = selectSQLBuilder.toString();
    }

    @Override
    public int updateById(String updateColumnNames, Object[] params, ID id) {
        Assert.notNull(id, "id不能为空");
        Object[] objects = mergeIdParam(params, id);
        return update(updateColumnNames, (Object[]) objects[0], (String) objects[1]);
    }

    private int update(String tableName, String updateColumnNames, Object[] params, String conditionSQL) {
        return SQLUtils.update(tableName, updateColumnNames, params, conditionSQL);
    }

    /**
     * 将id合并到参数中
     * @param params
     * @param id
     * @return
     */
    private Object[] mergeIdParam(Object[] params, ID id) {
        Object[] mergedParams;
        if (ArrayUtils.isEmpty(params)) {
            mergedParams = new Object[] {id};
        } else {
            mergedParams = new Object[params.length + 1];
            mergedParams[params.length] = id;
            System.arraycopy(params, 0, mergedParams, 0, params.length);
        }

        return new Object[] {mergedParams, ""+getIdColumnName()+" = ?"};
    }

    private String toJson(Object value) {
        try {
            return JsonUtils.toJson(value);
        } catch (IOException e) {
            return null;
        }
    }

    private List<String> convertToArray(String values) {
        return Arrays.asList(values.split(BaseEntityConstants.COLUMN_NAME_SEPARATOR_REGEX));
    }

    private Object[] mapToParamsArray(Map map, List<String> updateColumnNameList) {
        Object[] params = new Object[updateColumnNameList.size()];

        for (int i = 0; i < updateColumnNameList.size(); i++) {
            Object param = resolveValue(map.get(updateColumnNameList.get(i)));
            params[i] = param;
        }

        return params;
    }

    private String getParamsUpdateSQL(String updateColumnNames, Map<String, Object> paramsMap, String conditionSQL) {
        List<String> updateColumnList = convertToArray(updateColumnNames);
        Object[] params = new Object[updateColumnList.size()];

        List<String> newUpdateColumnList = convertToArray(updateColumnNames);

        StringBuilder updateColumnNameBuilder = new StringBuilder();
        int i = 0;
        for (String updateColumnName : newUpdateColumnList) {
            updateColumnNameBuilder.append(updateColumnName).append(" = :").append(updateColumnName).append(",");
            Object newParam = params[i++];
            if (Objects.isNull(paramsMap.get(updateColumnName))) {
                paramsMap.put(updateColumnName, newParam);
            }
        }
        updateColumnNameBuilder.deleteCharAt(updateColumnNameBuilder.length() - 1);

        String updateSQL = "UPDATE " + this.tableName + " SET " + updateColumnNameBuilder + " WHERE " + conditionSQL;
        return updateSQL;
    }

}
