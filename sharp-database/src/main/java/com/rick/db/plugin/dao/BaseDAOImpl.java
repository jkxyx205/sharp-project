package com.rick.db.plugin.dao;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.common.util.*;
import com.rick.db.config.Constants;
import com.rick.db.constant.EntityConstants;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-09-23 16:41:00
 */
@Slf4j
public class BaseDAOImpl<T> {

    @Autowired
    private SharpService sharpService;

    @Autowired(required = false)
    private ConditionAdvice conditionAdvice;

    @Autowired(required = false)
    private ColumnAutoFill columnAutoFill;

    private TableMeta tableMeta;

    private List<String> columnNameList;

    private List<String> updateColumnNameList;

    private List<String> propertyList;

    private List<String> updatePropertyList;

    private Class<?> entityClass;

    private String selectSQL;

    private Field[] entityFields;

    private String refColumnName;

    private Map<String, Field> propertyFieldMap;

    private Map<String, String> columnNameToPropertyNameMap;

    public BaseDAOImpl() {
        this.init();
    }

    public BaseDAOImpl(String tableName, String columnNames, String primaryColumn) {
        this.tableMeta = new TableMeta(null, tableName, columnNames, null, resolveUpdateColumnNames(columnNames, primaryColumn), null, primaryColumn, null);
        this.init();
    }

    /**
     * 插入单条数据
     *
     * @param t 参数对象
     * @return
     */
    public int insert(T t) {
        Object[] params;
        int index = columnNameList.indexOf(tableMeta.getIdColumnName());
        if (isMapClass()) {
            Map map = (Map) t;
            params = handleAutoFill(t, mapToParamsArray(map, this.columnNameList), columnNameList, ColumnFillType.INSERT);
            map.put(this.columnNameList, params[index]);
        } else {
            params = handleAutoFill(t, instanceToParamsArray(t), columnNameList, ColumnFillType.INSERT);
            setPropertyValue(t, tableMeta.getIdColumnName(), params[index]);
        }

        return SQLUtils.insert(tableMeta.getTableName(), tableMeta.getColumnNames(), params);
    }

    /**
     * 插入单条数据
     *
     * @param params 参数数组
     * @return
     */
    public int insert(Object[] params) {
        return SQLUtils.insert(tableMeta.getTableName(), tableMeta.getColumnNames(), handleAutoFill(null, params, columnNameList, ColumnFillType.INSERT));
    }

    /**
     * 批量插入数据
     */
    public int[] insert(List<?> paramsList) {
        if (CollectionUtils.isEmpty(paramsList)) {
            return new int[]{};
        }
        Class<?> paramClass = paramsList.get(0).getClass();
        List<T> instanceList = Lists.newArrayListWithCapacity(paramsList.size());
        if (paramClass == this.entityClass || Map.class.isAssignableFrom(paramClass)) {
            List<Object[]> params = Lists.newArrayListWithCapacity(paramsList.size());

            for (Object o : paramsList) {
                instanceList.add((T) o);
                if (isMapClass()) {
                    params.add(mapToParamsArray((Map) o, this.columnNameList));
                } else {
                    params.add(instanceToParamsArray((T) o));
                }
            }
            return SQLUtils.insert(tableMeta.getTableName(), tableMeta.getColumnNames(), handleAutoFill(instanceList, params, columnNameList, ColumnFillType.INSERT));
        } else if (paramClass == Object[].class) {
            return SQLUtils.insert(tableMeta.getTableName(), tableMeta.getColumnNames(), handleAutoFill(paramsList, (List<Object[]>) paramsList, columnNameList, ColumnFillType.INSERT));
        }

        throw new RuntimeException("List不支持的批量操作范型类型，目前仅支持Object[]、entity、Map");
    }

    /**
     * 通过主键id刪除
     *
     * @param id
     */
    public int deleteById(Serializable id) {
        return deleteByIds(Lists.newArrayList(id));
    }

    /**
     * 通过主键id批量刪除 eg：ids -> “1,2,3,4”
     *
     * @param ids
     */
    public int deleteByIds(String ids) {
        return deleteByIds(Arrays.asList(ids.split(",")));
    }

    /**
     * 通过主键id批量刪除 eg：ids -> [1, 2, 3, 4]
     *
     * @param ids
     */
    public int deleteByIds(Collection<?> ids) {
        return delete(tableMeta.getIdColumnName(), ids);
    }

    /**
     * 根据某个自定义的字段删除
     *
     * @param deleteColumn
     * @param deleteValues
     * @return
     */
    public int delete(String deleteColumn, String deleteValues) {
        return delete(deleteColumn, Arrays.asList(deleteValues.split(",")));
    }

    public int delete(String deleteColumn, Collection<?> deleteValues) {
        Object[] objects = handleConditionAdvice();
        if (hasSubTables()) {
            return SQLUtils.deleteCascade(tableMeta.getTableName(), refColumnName, deleteValues,  (Object[]) objects[0], (String) objects[1], tableMeta.getSubTables());
        }
        return SQLUtils.delete(tableMeta.getTableName(), deleteColumn, deleteValues, (Object[]) objects[0], (String) objects[1]);
    }

    /**
     * 构造条件和参数删除
     *
     * @param params
     * @param conditionSQL id IN (?, ?) AND group_id = ?
     * @return
     */
    public int delete(Object[] params, String conditionSQL) {
        Object[] objects = handleConditionAdvice(params, conditionSQL);
        if (hasSubTables()) {
            // TODO 没有测试过
            List<Map<String, Object>> deletedList = sharpService.getNamedJdbcTemplate().getJdbcTemplate().queryForList(getSelectSQL() + " WHERE " + (String) objects[1], (Object[]) objects[0]);
            Set<?> deletedIds = deletedList.stream().map(r -> r.get(tableMeta.getIdColumnName())).collect(Collectors.toSet());
            return SQLUtils.deleteCascade(tableMeta.getTableName(), refColumnName, deletedIds,  (Object[]) objects[0], (String) objects[1], tableMeta.getSubTables());
        }
        return SQLUtils.delete(tableMeta.getTableName(), (Object[]) objects[0], (String) objects[1]);
    }

    /**
     * 通过主键逻辑id刪除
     * 不会做级联操作
     * @param id
     */
    public int deleteLogicallyById(Serializable id) {
        Assert.notNull(id, "主键不能为null");
        return update(EntityConstants.LOGIC_DELETE_COLUMN_NAME, new Object[]{1, id}, tableMeta.getIdColumnName() + " = ?");
    }

    /**
     * 通过主键id批量逻辑刪除 eg：ids -> “1,2,3,4”
     * 不会做级联操作
     * @param ids
     */
    public int deleteLogicallyByIds(String ids) {
        if (StringUtils.isBlank(ids)) {
            return 0;
        }
        return deleteLogicallyByIds(Arrays.asList(ids.split(EntityConstants.COLUMN_NAME_SEPARATOR_REGEX)));
    }

    /**
     * 通过主键id批量逻辑刪除 eg：ids -> [1, 2, 3, 4]
     * 不会做级联操作
     * @param ids
     */
    public int deleteLogicallyByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }

        Object[] mergedParams = new Object[ids.size() + 1];
        mergedParams[0] = 1;
        System.arraycopy(ids.toArray(), 0, mergedParams, 1, ids.size());
        return update(EntityConstants.LOGIC_DELETE_COLUMN_NAME, mergedParams, tableMeta.getIdColumnName() + " IN " + SQLUtils.formatInSQLPlaceHolder(ids.size()));
    }

    /**
     * 更新所有字段
     *
     * @param params
     * @param id
     */
    public int update(Object[] params, Serializable id) {
        return updateById(null, tableMeta.getUpdateColumnNames(), params, this.updateColumnNameList, id);
    }

    /**
     * 更新所有字段
     *
     * @param t
     * @return
     */
    public int update(T t) {
        Object[] objects = resolverParamsAndId(t);
        return updateById(t, tableMeta.getUpdateColumnNames(), (Object[]) objects[0], this.updateColumnNameList, (Serializable) objects[1]);
    }

    /**
     * 批量更新
     * @param collection
     * @return
     */
    public int[] update(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return new int[] {};
        }

        List<Object[]> paramsList = Lists.newArrayListWithCapacity(collection.size());

        String conditionSQL = null;
        for (T t : collection) {
            Object[] resolverParamsAndIdObjects = resolverParamsAndId(t);
            Object[] mergeIdParamObjects = mergeIdParam((Object[]) resolverParamsAndIdObjects[0], (Serializable) resolverParamsAndIdObjects[1]);
            Object[] finalObjects = handleConditionAdvice(handleAutoFill(t, (Object[]) mergeIdParamObjects[0], updateColumnNameList, ColumnFillType.UPDATE), (String) mergeIdParamObjects[1]);
            paramsList.add((Object[]) finalObjects[0]);
            conditionSQL = (String) finalObjects[1];
        }

        return SQLUtils.update(tableMeta.getTableName(), tableMeta.getUpdateColumnNames(), paramsList, conditionSQL);
    }

    private Object[] resolverParamsAndId(T t) {
        Object[] params;
        Serializable id;
        if (isMapClass()) {
            Map map = (Map) t;
            params = mapToParamsArray(map, this.updateColumnNameList);
            id = (Serializable) map.get(tableMeta.getIdColumnName());
        } else {
            params = instanceToParamsArray(t, updatePropertyList);
            id = (Serializable) getPropertyValue(t, tableMeta.getIdColumnName());
        }

        return new Object[] {params, id};
    }

    /**
     * 指定更新字段，update_at不会autoFill，需要自己指定
     *
     * @param updateColumnNames name, age, updated_at
     * @param params            {"Rick", 23, null}
     * @param id                1
     */
    public int update(String updateColumnNames, Object[] params, Serializable id) {
        return updateById(null, updateColumnNames, params, convertToArray(updateColumnNames), id);
    }

    /**
     * 指定更新字段，构造条件更新
     *
     * @param updateColumnNames name, age
     * @param params            {"Rick", 23, LocalDateTime.now, 1}
     * @param conditionSQL      created_at > ? AND created_by = ?
     */
    public int update(String updateColumnNames, Object[] params, String conditionSQL) {
        return update(null, updateColumnNames, params, convertToArray(updateColumnNames), conditionSQL);
    }

    public int[] update(String updateColumnNames, List<Object[]> srcParamsList, String conditionSQL) {
        if (CollectionUtils.isEmpty(srcParamsList)) {
            return new int[] {};
        }

        List<Object[]> paramsList = Lists.newArrayListWithCapacity(srcParamsList.size());
        for (Object[] params : srcParamsList) {
            Object[] finalObjects = handleConditionAdvice(handleAutoFill(null, params, convertToArray(updateColumnNames), ColumnFillType.UPDATE), conditionSQL);
            paramsList.add((Object[]) finalObjects[0]);
            conditionSQL = (String) finalObjects[1];
        }
        return SQLUtils.update(tableMeta.getTableName(), updateColumnNames, paramsList, conditionSQL);
    }

    /**
     * 通过ID查找
     *
     * @param id
     * @return
     */
    public Optional<T> selectById(Serializable id) {
        Assert.notNull(id, "主键不能为null");
//        List<T> list = selectByParams(tableMeta.getIdColumnName() + " = " + id, tableMeta.getIdColumnName() + " = :id");

        Map<String, Object> params = Params.builder(1)
                .pv(tableMeta.getIdColumnName(), id)
                .build();

        List<T> list = selectByParams(params, tableMeta.getIdColumnName() + " = :id");
        return Optional.ofNullable(list.size() == 1 ? list.get(0) : null);
    }


    public Map<Serializable, T> selectByIdsAsMap(String ids) {
        return listToMap(selectByIds(ids));
    }

    public Map<Serializable, T> selectByIdsAsMap(Collection<?> ids) {
        List<T> list = selectByIds(ids);
        return listToMap(list);
    }

    /**
     * 通过多个ID查找//eg：ids -> “1,2,3,4”
     *
     * @param ids
     * @return
     */
    public List<T> selectByIds(String ids) {
        Map<String, Object> params = Params.builder(1).pv("ids", ids).build();
        return selectByParams(params, tableMeta.getIdColumnName() + " IN(:ids)");
    }

    public List<T> selectByIds(Collection<?> ids) {
        Map<String, Object> params = Params.builder(1).pv("ids", ids).build();
        return selectByParams(params, tableMeta.getIdColumnName() + " IN(:ids)");
    }

    /**
     * name=23&age=15,13 => name=:name AND age IN(:age)
     * 注意`=`两边的空格问题
     *
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
    public List<T> selectByParams(T t) {
        return selectByParams(t, null);
    }

    public List<T> selectByParams(T t, String conditionSQL) {
        Map params;
        if (isMapClass()) {
            params = (Map) t;
        } else {
//            params = JsonUtils.objectToMap(t);
            params = Maps.newHashMapWithExpectedSize(updateColumnNameList.size());
            for (String updateColumnName : updateColumnNameList) {
                String propertyName = columnNameToPropertyNameMap.get(updateColumnName);
                params.put(propertyName, getPropertyValue(t, propertyName));
            }
        }
        return selectByParams(params, conditionSQL);
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
     * 获取所有
     *
     * @return
     */
    public List<T> selectAll() {
        return selectByParams(Collections.emptyMap(), null);
    }

    /**
     * 依赖sharpService，可以进行不定条件的查询
     *
     * @param params
     * @param conditionSQL
     * @return
     */
    public List<T> selectByParams(Map<String, ?> params, String conditionSQL) {
        Map<String, Object> conditionParams;

        String additionCondition = "";
        if (Objects.nonNull(conditionAdvice)) {
            conditionParams = conditionAdvice.getCondition();
            if (MapUtils.isNotEmpty(conditionParams)) {
                additionCondition = getConditionSQL(conditionParams.keySet().stream().filter(key -> this.columnNameList.contains(key) && !isConditionSQLContainsColumnName(conditionSQL, key)).collect(Collectors.toList()), conditionParams);
                conditionParams.putAll(params);
            }
        } else {
            conditionParams = (Map<String, Object>) params;
        }

        return (List<T>) sharpService.query(this.selectSQL + " WHERE " + (Objects.isNull(conditionSQL) ? getConditionSQL(conditionParams) : conditionSQL)
                        + (StringUtils.isBlank(additionCondition) ? "" : " AND " + additionCondition),
                conditionParams,
                this.entityClass);
    }

    public void selectAsSubTable(List<Map<String, Object>> masterData, String property, String refColumnName) {
        Map<Long, List<T>> refColumnNameMap = selectByParams(Params.builder(1).pv("refColumnName", masterData.stream().map(row -> row.get(EntityConstants.ID_COLUMN_NAME)).toArray()).build(),
                refColumnName + " IN (:refColumnName)").stream().collect(Collectors.groupingBy(t-> (Long)getPropertyValue(t, columnNameToPropertyNameMap.get(refColumnName))));

        for (Map<String, Object> row : masterData) {
            List<T> subTableList = refColumnNameMap.get(row.get(EntityConstants.ID_COLUMN_NAME));
            row.put(property, CollectionUtils.isEmpty(subTableList) ? Collections.emptyList() : subTableList);
        }
    }

    public String getSelectSQL() {
        return this.selectSQL;
    }

    public boolean isMapClass() {
        return this.entityClass == Map.class;
    }

    public boolean hasSubTables() {
        return ArrayUtils.isNotEmpty(tableMeta.getSubTables());
    }

    private void init() {
        Class<?>[] actualTypeArgument = ClassUtils.getClassGenericsTypes(this.getClass());
        if (Objects.nonNull(actualTypeArgument)) {
            this.entityClass = actualTypeArgument[0];
            if (Map.class.isAssignableFrom(this.entityClass)) {
                this.entityClass = Map.class;
            } else {
                TableMeta tableMeta = TableMetaResolver.resolve(this.entityClass);
                this.refColumnName = tableMeta.getName() + "_" + EntityConstants.ID_COLUMN_NAME;
                this.propertyList = convertToArray(tableMeta.getProperties());
                this.updatePropertyList = convertToArray(tableMeta.getUpdateProperties());
                this.entityFields = ReflectUtils.getAllFields(this.entityClass);

                propertyFieldMap = Maps.newHashMapWithExpectedSize(this.entityFields.length);
                for (Field entityField : this.entityFields) {
                    propertyFieldMap.put(entityField.getName(), entityField);
                }

                if (Objects.nonNull(this.tableMeta)) {
                    this.tableMeta = new TableMeta(tableMeta.getName(), this.tableMeta.getName(), this.tableMeta.getColumnNames(), null, this.tableMeta.getUpdateColumnNames(), tableMeta.getUpdateProperties(), this.tableMeta.getIdColumnName(), tableMeta.getSubTables());
                } else {
                    this.tableMeta = tableMeta;
                }
                log.debug("properties: {}", tableMeta.getProperties());
            }
        } else {
            this.entityClass = Map.class;
        }

        if (Objects.isNull(this.tableMeta)) {
            throw new RuntimeException("Map需要有参数构造函数");
        }

        this.columnNameList = convertToArray(this.tableMeta.getColumnNames());
        this.updateColumnNameList = convertToArray(this.tableMeta.getUpdateColumnNames());

        if (CollectionUtils.isNotEmpty(propertyList)) {
            columnNameToPropertyNameMap = Maps.newHashMapWithExpectedSize(this.entityFields.length);
            for (int i = 0; i < columnNameList.size(); i++) {
                columnNameToPropertyNameMap.put(columnNameList.get(i), propertyList.get(i));
            }
        }

        initSelectSQL();

        log.debug("tableName: {}, tableMeta.getColumnNames(): {}", tableMeta.getTableName(), tableMeta.getColumnNames());
    }

    private static String getConditionSQL(Collection<String> paramNameList, Map<String, ?> params) {
        StringBuilder sb = new StringBuilder();
        for (String columnName : paramNameList) {
            Object value = params.get(columnName);
            sb.append(columnName).append(decideParamHolder(columnName, value)).append(" AND ");
        }

        return StringUtils.isBlank(sb) ? "" : sb.substring(0, sb.length() - 5);
    }

    private static String decideParamHolder(String columnName, Object value) {
        if (Objects.isNull(value)) {
            return " = :" + columnName;
        }
        if (value instanceof Iterable
                || value.getClass().isArray()
                || (value.getClass() == String.class && ((String) value).split(Constants.PARAM_IN_SEPARATOR).length > 1)) {
            return " IN (:" + columnName + ")";
        } /*else if (((String) value).startsWith(Constants.PARAM_LIKE_SEPARATOR)) {
            params.put(columnName, ((String) value).substring(1));
            return " LIKE :" + columnName;
        }*/

        return " = :" + columnName;
    }

    private String getConditionSQL(Map<String, ?> params) {
       return getConditionSQL(this.columnNameList, params);
    }

    private List<Object[]> handleAutoFill(List<?> list, List<Object[]> paramsList, List<String> columnNameList, ColumnFillType fillType) {
        for (int i = 0; i < paramsList.size(); i++) {
            handleAutoFill(list.get(i), paramsList.get(i), columnNameList, fillType);
        }
        return paramsList;
    }

    private Object[] handleAutoFill(Object t, Object[] params, List<String> columnNameList, ColumnFillType fillType) {
        if (Objects.nonNull(columnAutoFill)) {
            Map<String, Object> fill = (ColumnFillType.INSERT == fillType) ? columnAutoFill.insertFill() : columnAutoFill.updateFill();

            for (Map.Entry<String, Object> en : fill.entrySet()) {
                String fillColumnName = en.getKey();
                Object fillColumnValue = en.getValue();

                int index = columnNameList.indexOf(fillColumnName);
                if (index > -1 && Objects.isNull(params[index])) {
                    params[index] = resolverValue(fillColumnValue);

                    // 将auto值回写到实体中
                    if (t == null || t == params) {
                        continue;
                    }
                    if (isMapClass()) {
                        Map map = (Map) t;
                        map.put(fillColumnName, fillColumnValue);
                    } else {
                        setPropertyValue((T)t, columnNameToPropertyNameMap.get(fillColumnName), fillColumnValue);
                    }
                }
            }
        }

        return params;
    }

    private List<String> convertToArray(String values) {
        return Arrays.asList(values.split(EntityConstants.COLUMN_NAME_SEPARATOR_REGEX));
    }

    private void initSelectSQL() {
        if (CollectionUtils.isEmpty(this.propertyList)) {
            this.selectSQL = "SELECT " + tableMeta.getColumnNames() + " FROM " + tableMeta.getTableName();
            return;
        }

        int columnSize = this.columnNameList.size();
        StringBuilder selectSQLBuilder = new StringBuilder("SELECT ");
        for (int i = 0; i < columnSize; i++) {
            selectSQLBuilder.append(this.columnNameList.get(i))
                    .append(" AS \"").append(this.propertyList.get(i)).append("\",");
        }
        selectSQLBuilder.deleteCharAt(selectSQLBuilder.length() - 1).append(" FROM ").append(tableMeta.getTableName());

        this.selectSQL = selectSQLBuilder.toString();
    }

    private Object getPropertyValue(T t, String propertyName) {
        try {
            Field field = propertyFieldMap.get(propertyName);
            field.setAccessible(true);
            if (field.getName().equals(propertyName)) {
                return field.get(t);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private void setPropertyValue(T t, String propertyName, Object propertyValue) {
        try {
            Field field = propertyFieldMap.get(propertyName);
            field.setAccessible(true);
            if (field.getName().equals(propertyName)) {
                field.set(t, propertyValue);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] instanceToParamsArray(T t, List<String> includePropertyList) {
        Object[] params = new Object[includePropertyList.size()];
        for (int i = 0; i < includePropertyList.size(); i++) {
            params[i] = resolverValue(getPropertyValue(t, includePropertyList.get(i)));
        }
        return params;
    }

    private Object[] instanceToParamsArray(T t) {
        Object[] params = new Object[this.columnNameList.size()];
        try {
            for (int i = 0; i < this.entityFields.length; i++) {
                Field field = this.entityFields[i];
                field.setAccessible(true);
                Object param = resolverValue(this.entityFields[i], t);
                params[i] = param;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return params;
    }

    private Object[] mapToParamsArray(Map map, List<String> updateColumnNameList) {
        Object[] params = new Object[updateColumnNameList.size()];

        for (int i = 0; i < updateColumnNameList.size(); i++) {
            Object param = resolverValue(map.get(updateColumnNameList.get(i)));
            params[i] = param;
        }

        return params;
    }

    private Object resolverValue(Field field, T t) throws IllegalAccessException {
        Object value = field.get(t);
        return resolverValue(value);
    }

    private Object resolverValue(Object value) {
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
            } else if (JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(coll.iterator().next().getClass())) {
                return toJson(value);
            }
        }

        // JDBC 支持类型
        int sqlTypeValue = StatementCreatorUtils.javaTypeToSqlParameterType(value.getClass());

        if (SqlTypeValue.TYPE_UNKNOWN != sqlTypeValue) {
            return value;
        } else {
            return String.valueOf(value);
        }
    }

    private String toJson(Object value) {
        try {
            return JsonUtils.toJson(value);
        } catch (IOException e) {
            return null;
        }
    }

    private String resolveUpdateColumnNames(String columnNames, String primaryColumn) {
        // \bid\b\s*,?
        String updateColumnNames = columnNames.replaceAll("(?i)(\\b" + primaryColumn + "\\b\\s*,?)", "").trim();
        return updateColumnNames.endsWith(",") ? updateColumnNames.substring(0, updateColumnNames.length() - 1) : updateColumnNames;
    }

    private Map<Serializable, T> listToMap(List<T> list) {
        return list.stream().collect(Collectors.toMap(t -> (isMapClass()) ? (Serializable) ((Map) t).get(tableMeta.getIdColumnName()) : (Serializable) getPropertyValue(t, tableMeta.getIdColumnName()), v -> v));
    }

    private int updateById(T t, String updateColumnNames, Object[] params, List<String> updateColumnNameList, Serializable id) {
        Object[] objects = mergeIdParam(params, id);
        return update(t, updateColumnNames, (Object[]) objects[0], updateColumnNameList, (String) objects[1]);
    }

    private int update(T t, String updateColumnNames, Object[] params, List<String> updateColumnNameList, String conditionSQL) {
        Object[] objects = handleConditionAdvice(handleAutoFill(t, params, updateColumnNameList, ColumnFillType.UPDATE), conditionSQL);
        return SQLUtils.update(tableMeta.getTableName(),
                updateColumnNames,
                (Object[])objects[0],
                (String) objects[1]);
    }

    /**
     * 将id合并到参数中
     * @param params
     * @param id
     * @return
     */
    private Object[] mergeIdParam(Object[] params, Serializable id) {
        Object[] mergedParams = new Object[params.length + 1];
        mergedParams[params.length] = id;
        System.arraycopy(params, 0, mergedParams, 0, params.length);
        return new Object[] {mergedParams, "id = ?"};
    }

    private Object[] handleConditionAdvice() {
        return handleConditionAdvice(new Object[]{}, null);
    }

    /**
     * 处理条件自动注入
     * @param params
     * @param conditionSQL
     * @return
     */
    private Object[] handleConditionAdvice(Object[] params, String conditionSQL) {
        Object[] mergedParams = params;
        if (Objects.nonNull(this.conditionAdvice)) {
            Map<String, Object> conditionParams = conditionAdvice.getCondition();

            if (MapUtils.isNotEmpty(conditionParams)) {
                String additionCondition = getConditionSQL(conditionParams.keySet().stream().filter(key -> this.columnNameList.contains(key)).collect(Collectors.toList()), conditionParams);
                if (StringUtils.isNotBlank(additionCondition)) {
                    additionCondition = SQLUtils.paramsHolderToQuestionHolder(additionCondition);
                    conditionSQL = StringUtils.isNotBlank(conditionSQL) ? (conditionSQL + " AND " + additionCondition) : additionCondition;

                    mergedParams = new Object[params.length + conditionParams.size()];
                    System.arraycopy(params, 0, mergedParams, 0, params.length);

                    int i = 0;
                    for (String key : conditionParams.keySet()) {
                        mergedParams[params.length + i++] = conditionParams.get(key);
                    }
                }
            }
        }
        return new Object[]{mergedParams, conditionSQL};
    }

    private boolean isConditionSQLContainsColumnName(String conditionSQL, String columnName) {
        if (StringUtils.isBlank(conditionSQL)) {
            return false;
        }

        return conditionSQL.matches(".*((?i)(to_char|NVL)?\\s*([(][^([(]|[)])]*[)])|("+columnName+"))"+ "\\s*" + "(?i)(like|!=|>=|<=|<|>|=|\\s+in|\\s+not\\s+in|regexp).*");
    }
}
