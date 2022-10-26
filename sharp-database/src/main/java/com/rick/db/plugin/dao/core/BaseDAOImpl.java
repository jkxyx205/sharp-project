package com.rick.db.plugin.dao.core;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.*;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.config.Constants;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.constant.EntityConstants;
import com.rick.db.dto.BaseEntity;
import com.rick.db.dto.BaseEntityWithAssign;
import com.rick.db.dto.BaseEntityWithIdentity;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.plugin.dao.annotation.Id;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import com.rick.db.plugin.dao.support.ColumnAutoFill;
import com.rick.db.plugin.dao.support.ConditionAdvice;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.rick.db.config.Constants.DB_MYSQL;
import static com.rick.db.plugin.dao.annotation.Id.GenerationType.SEQUENCE;

/**
 * @author Rick
 * @createdAt 2021-09-23 16:41:00
 */
@Slf4j
public class BaseDAOImpl<T> implements BaseDAO<T> {

    @Autowired
    protected SharpService sharpService;

    @Autowired(required = false)
    private ConditionAdvice conditionAdvice;

    @Autowired(required = false)
    private ColumnAutoFill columnAutoFill;

    @Autowired(required = false)
    protected ValidatorHelper validatorHelper;

    @Autowired
    private SharpDatabaseProperties sharpDatabaseProperties;

    private TableMeta tableMeta;

    private List<String> columnNameList;

    private List<String> updateColumnNameList;

    private List<String> propertyList;

    private List<String> updatePropertyList;

    private Class<?> entityClass;

    private String fullColumnNames;

    private Field[] entityFields;

    private String subTableRefColumnName;

    private Map<String, PropertyDescriptor> propertyDescriptorMap;

    private Map<String, String> columnNameToPropertyNameMap;

    public BaseDAOImpl() {
        this.init();
    }

    public BaseDAOImpl(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.init();
    }

    public BaseDAOImpl(String tableName, String columnNames, String primaryColumn) {
        this.tableMeta = new TableMeta(null, null, "", tableName, columnNames, "", resolveUpdateColumnNames(columnNames, primaryColumn), "", primaryColumn, Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
        this.init();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertOrUpdate(T t) {
        if (Objects.isNull(getIdValue(t))) {
            return insert(t);
        } else {
            return update(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int[] insertOrUpdate(Collection<T> entities) {
        int[] updateCount = update(entities.stream().filter(t -> Objects.nonNull(getIdValue(t))).collect(Collectors.toList()));
        int[] insertCount = insert(entities.stream().filter(t -> Objects.isNull(getIdValue(t))).collect(Collectors.toList()));

        return ArrayUtils.addAll(insertCount, updateCount);
    }

    /**
     * 插入单条数据
     *
     * @param t 参数对象
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(T t) {
        if (Objects.nonNull(validatorHelper)) {
            validatorHelper.validate(t);
        }

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

        int count  = SQLUtils.insert(tableMeta.getTableName(), tableMeta.getColumnNames(), params);
        cascadeInsertOrUpdate(t);
        return count;
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
        return SQLUtils.insert(tableMeta.getTableName(), tableMeta.getColumnNames(), handleAutoFill(null, params, columnNameList, ColumnFillType.INSERT));
    }

    /**
     * 批量插入数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int[] insert(Collection<?> paramsList) {
        if (CollectionUtils.isEmpty(paramsList)) {
            return new int[]{};
        }
        Class<?> paramClass = paramsList.iterator().next().getClass();
        List<T> instanceList = Lists.newArrayListWithCapacity(paramsList.size());
        if (paramClass == this.entityClass || Map.class.isAssignableFrom(paramClass)) {
            List<Object[]> params = Lists.newArrayListWithCapacity(paramsList.size());

            for (Object o : paramsList) {
                instanceList.add((T) o);
                if (isMapClass()) {
                    params.add(mapToParamsArray((Map) o, this.columnNameList));
                } else {
                    if (Objects.nonNull(validatorHelper)) {
                        validatorHelper.validate(o);
                    }
                    params.add(instanceToParamsArray((T) o));
                }
            }
            int[] count = SQLUtils.insert(tableMeta.getTableName(), tableMeta.getColumnNames(), handleAutoFill(instanceList, params, columnNameList, ColumnFillType.INSERT));
            cascadeInsertOrUpdate(instanceList);
            return count;
        } else if (paramClass == Object[].class) {
            return SQLUtils.insert(tableMeta.getTableName(), tableMeta.getColumnNames(), handleAutoFill((List<?>) paramsList, (List<Object[]>) paramsList, columnNameList, ColumnFillType.INSERT));
        }

        throw new RuntimeException("List不支持的批量操作范型类型，目前仅支持Object[]、entity、Map");
    }

    /**
     * 通过主键id刪除
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
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
    public int deleteByIds(Long ...ids) {
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
        return delete(tableMeta.getIdColumnName(), ids);
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
        Object[] objects = handleConditionAdvice();
        if (hasSubTables()) {
            return SQLUtils.deleteCascade(tableMeta.getTableName(), subTableRefColumnName, deleteValues,  (Object[]) objects[0], (String) objects[1], tableMeta.getSubTables().toArray(new String[] {}));
        }
        return SQLUtils.delete(tableMeta.getTableName(), deleteColumn, deleteValues, (Object[]) objects[0], (String) objects[1]);
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
        Object[] objects = handleConditionAdvice(params, conditionSQL, false);
        if (hasSubTables()) {
            List<Long> deletedIds = sharpService.getNamedJdbcTemplate().getJdbcTemplate()
                    .queryForList("SELECT id FROM " + getTableName() + " WHERE " + objects[1], Long.class, (Object[]) objects[0]);
            if (CollectionUtils.isEmpty(deletedIds)) {
                return 0;
            }
            return SQLUtils.deleteCascade(tableMeta.getTableName(), subTableRefColumnName, deletedIds, (Object[]) objects[0], (String) objects[1], tableMeta.getSubTables().toArray(new String[] {}));
        }

        return SQLUtils.delete(tableMeta.getTableName(), (Object[]) objects[0], (String) objects[1]);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Map<String, Object> params, String conditionSQL) {
        if (hasSubTables()) {
            List<Long> deletedIds = selectIdsByParams(params, conditionSQL);

            for (String subTable : tableMeta.getSubTables()) {
                SQLUtils.delete(subTable, subTableRefColumnName, deletedIds);
            }
        }

        int count = sharpService.update("DELETE FROM " + getTableName() + " WHERE " + conditionSQL, params);
        return count;
    }

    /**
     * 通过主键逻辑id刪除
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogicallyById(Long id) {
        Assert.notNull(id, "id不能为空");

        if (hasSubTables()) {
            for (String subTable : tableMeta.getSubTables()) {
                Set<String> thirdPartyTableCollect = tableMeta.getManyToManyAnnotationList().stream().map(TableMeta.ManyToManyProperty::getManyToMany).map(ManyToMany::thirdPartyTable).collect(Collectors.toSet());
                if (thirdPartyTableCollect.contains(subTable)) {
                    SQLUtils.delete(subTable, subTableRefColumnName, Arrays.asList(id));
                } else {
                    update(subTable, null, EntityConstants.LOGIC_DELETE_COLUMN_NAME, new Object[]{1, id}, subTableRefColumnName + " = ?");
                }
            }
        }

        return update(EntityConstants.LOGIC_DELETE_COLUMN_NAME, new Object[]{1, id}, tableMeta.getIdColumnName() + " = ?");
    }

    /**
     * 通过主键id批量逻辑刪除 eg：ids -> “1,2,3,4”
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogicallyByIds(String ids) {
        Assert.hasText(ids, "id不能为空");
        return deleteLogicallyByIds(Arrays.asList(ids.split(EntityConstants.COLUMN_NAME_SEPARATOR_REGEX)));
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

        if (hasSubTables()) {
            for (String subTable : tableMeta.getSubTables()) {
                Set<String> thirdPartyTableCollect = tableMeta.getManyToManyAnnotationList().stream().map(TableMeta.ManyToManyProperty::getManyToMany).map(ManyToMany::thirdPartyTable).collect(Collectors.toSet());
                if (thirdPartyTableCollect.contains(subTable)) {
                    SQLUtils.delete(subTable, subTableRefColumnName, ids);
                } else {
                    update(subTable, null, EntityConstants.LOGIC_DELETE_COLUMN_NAME, mergedParams, subTableRefColumnName + " IN " + SQLUtils.formatInSQLPlaceHolder(ids.size()));
                }
            }
        }

        return update(EntityConstants.LOGIC_DELETE_COLUMN_NAME, mergedParams, tableMeta.getIdColumnName() + " IN " + SQLUtils.formatInSQLPlaceHolder(ids.size()));
    }

    @Override
    public int deleteAll() {
        List<T> list = selectAll();
        if (CollectionUtils.isNotEmpty(list)) {
            return deleteByIds(list.stream().map(s -> getIdValue(s)).collect(Collectors.toSet()));
        }

        return 0;
    }

    /**
     * 更新所有字段
     *
     * @param params
     * @param id
     */
    @Override
    public int update(Object[] params, Long id) {
        return updateById(null, tableMeta.getUpdateColumnNames(), params, id);
    }

    /**
     * 更新所有字段
     *
     * @param t
     * @return
     */
    @Override
    public int update(T t) {
        if (Objects.nonNull(validatorHelper)) {
            validatorHelper.validate(t);
        }

        cascadeInsertOrUpdate(t);
        Object[] objects = resolverParamsAndId(t);
        return updateById(t, tableMeta.getUpdateColumnNames(), (Object[]) objects[0], (Long) objects[1]);
    }

    /**
     * @param t
     * @param params   where语句后面的参数
     * @param conditionSQL
     * @return
     */
    @Override
    public int update(T t, Object[] params, String conditionSQL) {
        if (Objects.nonNull(validatorHelper)) {
            validatorHelper.validate(t);
        }

        cascadeInsertOrUpdate(t);

        List<String> updateColumnNames = convertToArray(tableMeta.getUpdateColumnNames());
        int size = updateColumnNames.size();

        params = ArrayUtils.isEmpty(params) ? new Object[] {} : params;

        Object[] mergedParams = new Object[size + params.length];
        for (int i = 0; i < size; i++) {
            mergedParams[i] = resolverValue(getPropertyValue(t, columnNameToPropertyNameMap.get(updateColumnNames.get(i))));
        }

        System.arraycopy(params, 0, mergedParams, size, params.length);
        return update(t, tableMeta.getUpdateColumnNames(), mergedParams, conditionSQL);
    }

    /**
     * 批量更新
     * @param collection
     * @return
     */
    @Override
    public int[] update(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return new int[] {};
        }

        List<Object[]> paramsList = Lists.newArrayListWithCapacity(collection.size());

        String conditionSQL = null;
        for (T t : collection) {
            Object[] resolverParamsAndIdObjects = resolverParamsAndId(t);
            Object[] mergeIdParamObjects = mergeIdParam((Object[]) resolverParamsAndIdObjects[0], (Long) resolverParamsAndIdObjects[1]);
            Object[] finalObjects = handleConditionAdvice(handleAutoFill(t, (Object[]) mergeIdParamObjects[0], updateColumnNameList, ColumnFillType.UPDATE), (String) mergeIdParamObjects[1], false);
            paramsList.add((Object[]) finalObjects[0]);
            conditionSQL = (String) finalObjects[1];
            cascadeInsertOrUpdate(t);
        }
        return SQLUtils.update(tableMeta.getTableName(), tableMeta.getUpdateColumnNames(), paramsList, conditionSQL);
    }

    /**
     * 指定更新字段
     *
     * @param updateColumnNames name, age, updated_at
     * @param params            update && where 语句后面的参数 {"Rick", 23, null}
     * @param id                1
     */
    @Override
    public int update(String updateColumnNames, Object[] params, Long id) {
        return updateById(null, updateColumnNames, params, id);
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
        return update(null, updateColumnNames, params, conditionSQL);
    }

    @Override
    public int[] update(String updateColumnNames, List<Object[]> srcParamsList, String conditionSQL) {
        if (CollectionUtils.isEmpty(srcParamsList)) {
            return new int[] {};
        }

        List<Object[]> paramsList = Lists.newArrayListWithCapacity(srcParamsList.size());
        for (Object[] params : srcParamsList) {
            Object[] updateAutoObjects = handleUpdateAutoFill(updateColumnNames, params);
            Object[] conditionAdviceObject = handleConditionAdvice(handleAutoFill(null, (Object[]) updateAutoObjects[0], convertToArray((String) updateAutoObjects[1]), ColumnFillType.UPDATE), conditionSQL, false);
            paramsList.add((Object[]) conditionAdviceObject[0]);
            conditionSQL = (String) conditionAdviceObject[1];
            updateColumnNames = (String) updateAutoObjects[1];
        }
        return SQLUtils.update(tableMeta.getTableName(), updateColumnNames, paramsList, conditionSQL);
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

    /**
     * 通过ID查找
     *
     * @param id
     * @return
     */
    @Override
    public Optional<T> selectById(Long id) {
        Assert.notNull(id, "id cannot be null");
        Map<String, Object> params = Params.builder(1)
                .pv(tableMeta.getIdColumnName(), id)
                .build();

        List<T> list = selectByParams(params, tableMeta.getIdColumnName() + " = :id");
        return expectedAsOptional(list);
    }

    @Override
    public Map<Long, T> selectByIdsAsMap(String ids) {
        return listToMap(selectByIds(ids));
    }

    @Override
    public Map<Long, T> selectByIdsAsMap(Long ...ids) {
        return listToMap(selectByIds(ids));
    }

    @Override
    public Map<Long, T> selectByIdsAsMap(Collection<?> ids) {
        List<T> list = selectByIds(ids);
        return listToMap(list);
    }

    /**
     * 通过多个ID查找//eg：ids -> “1,2,3,4”
     *
     * @param ids
     * @returndeleteById
     */
    @Override
    public List<T> selectByIds(String ids) {
        Assert.hasText(ids, "ids cannot be empty");
        return selectByIdsWithSpecifiedValue(ids);
    }

    @Override
    public List<T> selectByIds(Long ...ids) {
        Assert.notEmpty(ids, "ids cannot be empty");
        return selectByIdsWithSpecifiedValue(ids);
    }

    @Override
    public List<T> selectByIds(Collection<?> ids) {
        Assert.notEmpty(ids, "ids cannot be empty");
        return selectByIdsWithSpecifiedValue(ids);
    }

    /**
     * name=23&age=15,13 => name=:name AND age IN(:age)
     * 注意`=`两边的空格问题
     *
     * @param queryString
     * @return
     */
    @Override
    public List<T> selectByParams(String queryString) {
        return selectByParams(queryString, null);
    }

    @Override
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
    @Override
    public List<T> selectByParams(T t) {
        return selectByParams(t, null);
    }

    @Override
    public List<T> selectByParams(T t, String conditionSQL) {
        Map params;
        if (isMapClass()) {
            params = (Map) t;
        } else {
//            params = JsonUtils.objectToMap(t);
            params = entityToMap(t);
        }
        return selectByParams(params, conditionSQL);
    }

    /**
     * 根据条件查找
     *
     * @param
     * @return
     */
    @Override
    public List<T> selectByParams(Map<String, ?> params) {
        return selectByParams(params, null);
    }

    /**
     * 获取所有
     *
     * @return
     */
    @Override
    public List<T> selectAll() {
        return selectByParams(Collections.emptyMap(), null);
    }

    @Override
    public long countByParams(Map<String, ?> params, String conditionSQL) {
        return (Long) selectByParams(params, "count(*)", conditionSQL, srcSQL -> srcSQL, Long.class).get(0);
    }

    @Override
    public boolean existsByParams(Map<String, ?> params, String conditionSQL) {
        if (sharpDatabaseProperties.getType().equals(DB_MYSQL)) {
            return selectByParams(params, "1", conditionSQL, srcSQL -> srcSQL + " LIMIT 1", Long.class).size() > 0 ? true : false;
        } else {
            return countByParams(params, conditionSQL) > 1;
        }
    }

    @Override
    public List<T> selectByParams(Map<String, ?> params, String conditionSQL) {
        return (List<T>) selectByParams(params, fullColumnNames, conditionSQL, srcSQL -> srcSQL, entityClass);
    }


    /**
     * 根据条件获取id，如果有多条会抛出异常
     * @param t
     * @param conditionSQL
     * @return
     */
    @Override
    public Optional<Long> selectIdByParams(T t, String conditionSQL) {
        List<Long> ids = selectByParams(entityToMap(t), "id", conditionSQL, sql -> sql, Long.class);
        return expectedAsOptional(ids);
    }

    @Override
    public List<Long> selectIdsByParams(T t, String conditionSQL) {
        return selectIdsByParams(entityToMap(t), conditionSQL);
    }

    @Override
    public List<Long> selectIdsByParams(Map<String, ?> params, String conditionSQL) {
        return selectByParams(params, "id", conditionSQL, sql -> sql, Long.class);
    }

    @Override
    public void checkId(Long id) {
        checkId(id, Collections.emptyMap(), null);
    }

    @Override
    public void checkId(Long id, Map<String, Object> params, String condition) {
        com.rick.common.http.exception.Assert.notNull(id, "id cannot be null");
        if (!existsByParams(Params.builder(1 + params.size()).pv("id", id).pvAll(params).build(), "id = :id" + (StringUtils.isBlank(condition) ? "" : " AND " + condition))) {
            throw new BizException(ResultUtils.exception(404, "id=" + id + " +不存在", id));
        }
    }

    @Override
    public void checkIds(Collection<Long> ids) {
        checkIds(ids, Collections.emptyMap(), null);
    }

    @Override
    public void checkIds(Collection<Long> ids, Map<String, Object> params, String condition) {
        com.rick.common.http.exception.Assert.state(CollectionUtils.isNotEmpty(ids), "ids cannot be empty");
        List<Long> idsInDB = sharpService.query("SELECT id FROM " + getTableName() + " WHERE id in (:ids)" + (StringUtils.isBlank(condition) ? "" : " AND " + condition), Params.builder(1 + params.size()).pv("ids", ids).pvAll(params).build(), Long.class);
        SetUtils.SetView<Long> difference = SetUtils.difference(Sets.newHashSet(ids), Sets.newHashSet(idsInDB));
        if (CollectionUtils.isNotEmpty(difference)) {
            throw new BizException(ResultUtils.exception(404, "id=" + StringUtils.join(difference.toArray(), ",") + "不存在", difference.toArray()));
        }
    }

    @Override
    public <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, Class<E> clazz) {
        return selectByParams(params, columnNames, conditionSQL, src -> src, clazz);
    }


    @Override
    public List<T> selectByParamsWithoutCascade(T t) {
        return selectByParamsWithoutCascade(t, null);
    }

    @Override
    public List<T> selectByParamsWithoutCascade(T t, String conditionSQL) {
        return selectByParamsWithoutCascade(entityToMap(t), fullColumnNames, conditionSQL);
    }

    @Override
    public List<T> selectByParamsWithoutCascade(Map<String, ?> params) {
        return selectByParamsWithoutCascade(params, null);
    }

    @Override
    public List<T> selectByParamsWithoutCascade(Map<String, ?> params, String conditionSQL) {
        return selectByParamsWithoutCascade(params, fullColumnNames, conditionSQL);
    }

    @Override
    public List<T> selectByParamsWithoutCascade(Map<String, ?> params, String columnNames, String conditionSQL) {
        Object[] executeCondition = getExecuteCondition(params, columnNames, conditionSQL, sql -> sql);
        return sharpService.query((String) executeCondition[0], (Map)executeCondition[1], getEntity());
    }

    /**
     * @param params
     * @param columnNames 比如 id,name。id作key，name作value
     * @param conditionSQL
     * @return
     */
    @Override
    public <K, V> Map<K, V> selectByParamsAsMap(Map<String, ?> params, String columnNames, String conditionSQL) {
        Object[] executeCondition = getExecuteCondition(params, columnNames, conditionSQL, sql -> sql);
        return sharpService.queryForKeyValue((String) executeCondition[0], (Map)executeCondition[1]);
    }

    /**
     * 依赖sharpService，可以进行不定条件的查询
     *
     * @param params
     * @param conditionSQL
     * @return
     */
    private <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, SqlHandler sqlHandler, Class<E> clazz) {
        Object[] executeCondition = getExecuteCondition(params, columnNames, conditionSQL, sqlHandler);

        List<E> list = sharpService.query((String) executeCondition[0], (Map)executeCondition[1], clazz);
        if (clazz == entityClass) {
            cascadeSelect((List<T>) list);
            BaseDAOThreadLocalValue.removeByTableName(getTableName());
        }
        return list;
    }

    /**
     * 获取最终执行的参数，可以使用sharpService执行
     * @param params
     * @param columnNames
     * @param conditionSQL
     * @param sqlHandler
     * @return
     */
    private Object[] getExecuteCondition(Map<String, ?> params, String columnNames, String conditionSQL, SqlHandler sqlHandler) {
        if (MapUtils.isEmpty(params)) {
            params = Collections.emptyMap();
        }

        Map<String, Object> conditionParams;
        String finalConditionSQL = (Objects.isNull(conditionSQL) ? getConditionSQL(params) : conditionSQL);
        String additionCondition = "";
        if (Objects.nonNull(conditionAdvice)) {
            conditionParams = conditionAdvice.getCondition();
            if (MapUtils.isNotEmpty(conditionParams)) {
                additionCondition = getConditionSQL(conditionParams.keySet().stream().filter(key -> this.columnNameList.contains(key) && !isConditionSQLContainsColumnName(finalConditionSQL, key)).collect(Collectors.toList()), conditionParams);
                conditionParams.putAll(params);
            } else {
                conditionParams = (Map<String, Object>) params;
            }
        } else {
            conditionParams = (Map<String, Object>) params;
        }

        return new Object[] {sqlHandler.handlerSQl("SELECT " + columnNames + " FROM " + getTableName() +  " WHERE " + ((StringUtils.isBlank(additionCondition) ? "" : additionCondition + " AND "))  + finalConditionSQL), conditionParams};
    }

    @Override
    public void selectAsSubTable(List<Map<String, Object>> masterData, String property, String refColumnName) {
        Map<Long, List<T>> refColumnNameMap = groupByColumnName(refColumnName, masterData.stream().map(row -> row.get(EntityConstants.ID_COLUMN_NAME)).collect(Collectors.toSet()));

        for (Map<String, Object> row : masterData) {
            List<T> subTableList = refColumnNameMap.get(row.get(EntityConstants.ID_COLUMN_NAME));
            row.put(property, CollectionUtils.isEmpty(subTableList) ? Collections.emptyList() : subTableList);
        }
    }

    @Override
    public Map<Long, List<T>> groupByColumnName(String refColumnName, Collection<?> refValues) {
        Map<Long, List<T>> refColumnNameMap = selectByParams(Params.builder(1).pv("refColumnName", refValues).build(),
                refColumnName + " IN (:refColumnName)").stream().collect(Collectors.groupingBy(t-> {

            Object propertyValue = getPropertyValue(t, columnNameToPropertyNameMap.get(refColumnName));
            if (propertyValue instanceof Long) {
                return (Long)propertyValue;
            }

            return (Long)getPropertyValue(propertyValue, ReflectionUtils.findField(propertyValue.getClass(), EntityConstants.ID_COLUMN_NAME));

        }));
        return refColumnNameMap;
    }

    @Override
    public String getSelectSQL() {
        return "SELECT "+fullColumnNames+" FROM " + getTableName();
    }

    @Override
    public String getFullColumnNames() {
        return fullColumnNames;
    }

    @Override
    public String getTableName() {
        return tableMeta.getTableName();
    }

    @Override
    public Map<String, Object> entityToMap(T t) {
        Map<String, Object> params;
        params = Maps.newHashMapWithExpectedSize(columnNameList.size());
        for (String columnName : columnNameList) {
            String propertyName = columnNameToPropertyNameMap.get(columnName);
            Object propertyValue = getPropertyValue(t, propertyName);
            if (Objects.nonNull(propertyValue)) {
                if (propertyValue instanceof BaseEntity || propertyValue instanceof BaseEntityWithIdentity || propertyValue instanceof BaseEntityWithAssign) {
                    propertyValue = getIdValue(propertyValue);
                }

                params.put(propertyName, propertyValue);
                params.put(columnName, propertyValue);
            }
        }
        return params;
    }

    @Override
    public Class getEntity() {
        return entityClass;
    }

    @Override
    public <E> Optional<E> expectedAsOptional(List<E> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Optional.empty();
        }

        if (list.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, list.size());
        }

        return Optional.of(list.get(0));
    }

    private boolean isMapClass() {
        return this.entityClass == Map.class;
    }

    private boolean hasSubTables() {
        return !tableMeta.getSubTables().isEmpty();
    }

    private void init() {
        Class<?>[] actualTypeArgument = ClassUtils.getClassGenericsTypes(this.getClass());
        this.entityClass = Objects.nonNull(this.entityClass) ? this.entityClass
                : (Objects.nonNull(actualTypeArgument) ? actualTypeArgument[0] : null);

        if (Objects.nonNull(this.entityClass)) {
            if (Map.class.isAssignableFrom(this.entityClass)) {
                this.entityClass = Map.class;
            } else {
                TableMeta tableMeta = TableMetaResolver.resolve(this.entityClass);
                this.subTableRefColumnName = tableMeta.getName() + "_" + EntityConstants.ID_COLUMN_NAME;
                this.propertyList = convertToArray(tableMeta.getProperties());
                this.updatePropertyList = convertToArray(tableMeta.getUpdateProperties());
                this.entityFields = ReflectUtils.getAllFields(this.entityClass);

                propertyDescriptorMap = Maps.newHashMapWithExpectedSize(this.entityFields.length);
                for (Field entityField : this.entityFields) {
                    try {
                        propertyDescriptorMap.put(entityField.getName(), new PropertyDescriptor(entityField.getName(), this.entityClass));
                    } catch (IntrospectionException e) {
                        throw new BeanInitializationException(tableMeta.getTableName() + "初始化异常", e);
                    }
                }

                if (Objects.nonNull(this.tableMeta)) {
                    this.tableMeta = new TableMeta(tableMeta.getTable(), tableMeta.getId(), tableMeta.getName(), this.tableMeta.getTableName(), this.tableMeta.getColumnNames(), tableMeta.getProperties(), tableMeta.getUpdateColumnNames(), tableMeta.getUpdateProperties(), this.tableMeta.getIdColumnName(), tableMeta.getSubTables(), tableMeta.getOneToManyAnnotationList(), tableMeta.getManyToOneAnnotationList(), tableMeta.getManyToManyAnnotationList(), tableMeta.getColumnNameFieldMap(), tableMeta.getColumnNameMap());
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

        initFullColumnNames();

        log.debug("tableName: {}, tableMeta.getColumnNames(): {}", tableMeta.getTableName(), tableMeta.getColumnNames());
    }

    private String getConditionSQL(Collection<String> paramNameList, Map<String, ?> params) {
        StringBuilder sb = new StringBuilder();
        for (String columnName : paramNameList) {
            Object value = params.get(columnName);
            sb.append(columnName).append(decideParamHolder(columnName, value)).append(" AND ");
        }

        return StringUtils.isBlank(sb) ? "" : sb.substring(0, sb.length() - 5);
    }

    private String decideParamHolder(String columnName, Object value) {
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
            Id.GenerationType strategy;
            if (tableMeta.getId() == null) {
                strategy = SEQUENCE;
            } else {
                strategy = tableMeta.getId().strategy();
            }
            Long id = strategy == SEQUENCE ? IdGenerator.getSequenceId() : null;
            Map<String, Object> fill = (ColumnFillType.INSERT == fillType) ? columnAutoFill.insertFill(id) : columnAutoFill.updateFill();

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
                        setPropertyValue(t, columnNameToPropertyNameMap.get(fillColumnName), fillColumnValue);
                    }
                }
            }
        }

        return params;
    }

    private List<String> convertToArray(String values) {
        return Arrays.asList(values.split(EntityConstants.COLUMN_NAME_SEPARATOR_REGEX));
    }

    private void initFullColumnNames() {
        if (CollectionUtils.isEmpty(this.propertyList)) {
            this.fullColumnNames = tableMeta.getColumnNames();
            return;
        }

        int columnSize = this.columnNameList.size();
        StringBuilder selectSQLBuilder = new StringBuilder();
        for (int i = 0; i < columnSize; i++) {
            selectSQLBuilder.append(this.getTableName() + "." + this.columnNameList.get(i))
                    .append(" AS \"").append(this.propertyList.get(i)).append("\",");
        }
        selectSQLBuilder.deleteCharAt(selectSQLBuilder.length() - 1);

        this.fullColumnNames = selectSQLBuilder.toString();
    }

    private Object getPropertyValue(Object t, Field field) {
        return getPropertyValue(t, field.getName());
    }

    private Object getPropertyValue(Object t, String propertyName) {
        try {
            if (this.entityClass.isAssignableFrom(t.getClass())) {
                return propertyDescriptorMap.get(propertyName).getReadMethod().invoke(t);
            } else {
                return BaseDAOManager.entityPropertyDescriptorMap.get(t.getClass()).get(propertyName).getReadMethod().invoke(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setPropertyValue(Object t, Field field, Object propertyValue) {
        if (field.getType() == Long.class && propertyValue instanceof BaseEntity) {
            propertyValue = this.getIdValue(propertyValue);
        }

        setPropertyValue(t, field.getName(), propertyValue);
    }

    private void setPropertyValue(Object t, String propertyName, Object propertyValue) {
        try {
            if (this.entityClass.isAssignableFrom(t.getClass())) {
                propertyDescriptorMap.get(propertyName).getWriteMethod().invoke(t, propertyValue);
            } else {
                BaseDAOManager.entityPropertyDescriptorMap.get(t.getClass()).get(propertyName).getWriteMethod().invoke(t, propertyValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        for (int i = 0; i < params.length; i++) {
            Object param = resolverValue(getPropertyValue(t, columnNameToPropertyNameMap.get(columnNameList.get(i))));
            params[i] = param;
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
        } else if (BaseEntity.class.isAssignableFrom(value.getClass())
        || BaseEntityWithAssign.class.isAssignableFrom(value.getClass())
        || BaseEntityWithIdentity.class.isAssignableFrom(value.getClass())) {
            return getIdValue(value);
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

    private Map<Long, T> listToMap(List<T> list) {
        return list.stream().collect(Collectors.toMap(t -> (isMapClass()) ? (Long) ((Map) t).get(tableMeta.getIdColumnName()) : (Long) getPropertyValue(t, tableMeta.getIdColumnName()), v -> v));
    }

    private int updateById(T t, String updateColumnNames, Object[] params, Long id) {
        Assert.notNull(id, "id不能为空");
        Object[] objects = mergeIdParam(params, id);
        return update(t, updateColumnNames, (Object[]) objects[0], (String) objects[1]);
    }

    private int update(T t, String updateColumnNames, Object[] params, String conditionSQL) {
        return update(tableMeta.getTableName(), t, updateColumnNames, params, conditionSQL);
    }

    private int update(String tableName, T t, String updateColumnNames, Object[] params, String conditionSQL) {
        Object[] updateAutoObjects = handleUpdateAutoFill(updateColumnNames, params);
        Object[] conditionAdviceObjects = handleConditionAdvice(handleAutoFill(t, (Object[]) updateAutoObjects[0], convertToArray((String) updateAutoObjects[1]), ColumnFillType.UPDATE), conditionSQL, false);
        return SQLUtils.update(tableName, (String) updateAutoObjects[1], (Object[])conditionAdviceObjects[0], (String) conditionAdviceObjects[1]);
    }

    /**
     * 将id合并到参数中
     * @param params
     * @param id
     * @return
     */
    private Object[] mergeIdParam(Object[] params, Long id) {
        Object[] mergedParams;
        if (ArrayUtils.isEmpty(params)) {
            mergedParams = new Object[] {id};
        } else {
            mergedParams = new Object[params.length + 1];
            mergedParams[params.length] = id;
            System.arraycopy(params, 0, mergedParams, 0, params.length);
        }

        return new Object[] {mergedParams, "id = ?"};
    }

    private Object[] handleConditionAdvice() {
        return handleConditionAdvice(new Object[]{}, null, false);
    }

    /**
     * 处理条件自动注入
     * @param params
     * @param conditionSQL
     * @return
     */
    private Object[] handleConditionAdvice(Object[] params, String conditionSQL, boolean isParamHolder) {
        Object[] mergedParams = params;
        if (Objects.nonNull(this.conditionAdvice)) {
            Map<String, Object> conditionParams = conditionAdvice.getCondition();

            if (MapUtils.isNotEmpty(conditionParams)) {
                Collection<String> filteredConditionParams = conditionParams.keySet().stream().filter(key -> this.columnNameList.contains(key)).collect(Collectors.toList());

                String additionCondition = getConditionSQL(filteredConditionParams, conditionParams);
                if (StringUtils.isNotBlank(additionCondition)) {
                    if (!isParamHolder) {
                        additionCondition = SQLUtils.paramsHolderToQuestionHolder(additionCondition);
                    }

                    conditionSQL = StringUtils.isNotBlank(conditionSQL) ? (conditionSQL + " AND " + additionCondition) : additionCondition;

                    mergedParams = new Object[params.length + filteredConditionParams.size()];
                    System.arraycopy(params, 0, mergedParams, 0, params.length);

                    int i = 0;
                    for (String key : filteredConditionParams) {
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

    private Object[] resolverParamsAndId(T t) {
        Object[] params;
        Long id;
        if (isMapClass()) {
            Map map = (Map) t;
            params = mapToParamsArray(map, this.updateColumnNameList);
            id = (Long) map.get(tableMeta.getIdColumnName());
        } else {
            params = instanceToParamsArray(t, updatePropertyList);
            id = (Long) getPropertyValue(t, tableMeta.getIdColumnName());
        }

        return new Object[] {params, id};
    }

    private Object[] handleUpdateAutoFill(String updateColumnNames, Object[] params) {
        Object mergedParams = params;
        if (Objects.nonNull(columnAutoFill)) {
            Map<String, Object> fill = columnAutoFill.updateFill();
            List<String> updateColumnNameList = convertToArray(updateColumnNames);

            int size = 0;
            for (String fillColumnName : fill.keySet()) {
                if (!updateColumnNameList.contains(fillColumnName) && tableMeta.getUpdateColumnNames().contains(fillColumnName)) {
                    updateColumnNames = fillColumnName + "," + updateColumnNames;
                    size++;
                }
            }

            mergedParams = new Object[params.length + size];
            System.arraycopy(params, 0, mergedParams, size, params.length);

        }

        return new Object[] {mergedParams, updateColumnNames};
    }

    private void cascadeSelect(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        //region OneToMany
        for (TableMeta.OneToManyProperty oneToManyProperty : tableMeta.getOneToManyAnnotationList()) {
            String storeKey = oneToManyProperty.getOneToMany().subTable() + ":" + oneToManyProperty.getOneToMany().joinValue();
            if (BaseDAOThreadLocalValue.remove(storeKey)) {
                continue;
            }
            BaseDAOThreadLocalValue.add(storeKey);

            String targetTable = oneToManyProperty.getOneToMany().subTable();
            BaseDAO subTableBaseDAO =  BaseDAOManager.baseDAOMap.get(targetTable);


            Set<Long> refIds = list.stream().map(t -> getIdValue(t)).collect(Collectors.toSet());
            Map<Long, List<?>> subTableData = subTableBaseDAO.groupByColumnName(oneToManyProperty.getOneToMany().joinValue(), refIds);

            for (T t : list) {
                Object data = subTableData.get(getIdValue(t));
                if (oneToManyProperty.getOneToMany().oneToOne()) {
                    setPropertyValue(t, oneToManyProperty.getField(), Objects.isNull(data) ? null : ((Collection)data).iterator().next());
                } else {
                    setPropertyValue(t, oneToManyProperty.getField(), Objects.isNull(data) ? Collections.emptyList() : data);
                }

            }
        }
        //endregion

        //region ManyToOne
        for (TableMeta.ManyToOneProperty manyToOneProperty : tableMeta.getManyToOneAnnotationList()) {
            String refColumnName = manyToOneProperty.getManyToOne().value();
            String storeKey = getTableName() + ":" + refColumnName;

            if (BaseDAOThreadLocalValue.remove(storeKey)) {
                continue;
            }
            BaseDAOThreadLocalValue.add(storeKey);

            String targetTable = manyToOneProperty.getManyToOne().parentTable();
            BaseDAO parentTableDAO = BaseDAOManager.baseDAOMap.get(targetTable);

            Set<Long> refIds = list.stream().map(t -> getIdValue(getPropertyValue(t, columnNameToPropertyNameMap.get(refColumnName))))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(refIds)) {
                continue;
            }

            List<?> parentList = parentTableDAO.selectByIds(refIds);

            Map<Long, ?> parentIdMap = parentList.stream().collect(Collectors.toMap(this::getIdValue, v -> v));
            for (T t : list) {
                Object data = parentIdMap.get(getIdValue(getPropertyValue(t, columnNameToPropertyNameMap.get(refColumnName))));
                setPropertyValue(t, manyToOneProperty.getField(), Objects.isNull(data) ? null : data);
            }
        }
        //endregion

        //region ManyToMany
        for (TableMeta.ManyToManyProperty manyToManyProperty : tableMeta.getManyToManyAnnotationList()) {
            String referenceColumnName = manyToManyProperty.getManyToMany().referenceColumnName();
            String columnDefinition = manyToManyProperty.getManyToMany().columnDefinition();
            String storeKey1 = getTableName() + ":" + referenceColumnName;
            String storeKey2 = getTableName() + ":" + columnDefinition;

            if (BaseDAOThreadLocalValue.remove(storeKey1) || BaseDAOThreadLocalValue.remove(storeKey2)) {
                continue;
            }

            BaseDAOThreadLocalValue.add(storeKey1);
            BaseDAOThreadLocalValue.add(storeKey2);

            Set<Long> columnIds = list.stream().map(t -> getIdValue(t)).collect(Collectors.toSet());

            String thirdPartyTable = manyToManyProperty.getManyToMany().thirdPartyTable();
            String referenceTable = manyToManyProperty.getManyToMany().referenceTable();

            BaseDAO referenceTableDAO =  BaseDAOManager.baseDAOMap.get(referenceTable);

            List<Map<String, Object>> refMapData = sharpService.query(String.format("select %s, %s from %s  where %s in (:value)",
                    columnDefinition, referenceColumnName, thirdPartyTable, columnDefinition
            ), Params.builder(1).pv("value", columnIds).build());

            Collection<Long> refIds = Lists.newArrayList();
            for (Map<String, Object> row : refMapData) {
                refIds.add((Long) row.get(referenceColumnName));
            }

            Map<Long, ?> referenceMap = referenceTableDAO.selectByIdsAsMap(refIds);

            Map<Long, List<Object>> referenceListMap = Maps.newHashMap();

            for (Map<String, Object> row : refMapData) {
                List<Object> group = referenceListMap.get(row.get(columnDefinition));
                if (CollectionUtils.isEmpty(group)) {
                    group = Lists.newArrayList();
                    referenceListMap.put((Long) row.get(columnDefinition), group);
                }
                group.add(referenceMap.get(row.get(referenceColumnName)));
            }

            for (T t : list) {
                Object data = referenceListMap.get(getIdValue(t));
                setPropertyValue(t, manyToManyProperty.getField(), Objects.isNull(data) ? Collections.emptyList() : data);
            }
        }
        //endregion
    }

    private void cascadeInsertOrUpdate(List<T> instanceList) {
        for (T t : instanceList) {
            cascadeInsertOrUpdate(t, false);
        }
    }

    private void cascadeInsertOrUpdate(T t) {
        cascadeInsertOrUpdate(t, true);
    }

    private void cascadeInsertOrUpdate(T t, boolean sync) {
        // OneToMany
        for (TableMeta.OneToManyProperty oneToManyProperty : tableMeta.getOneToManyAnnotationList()) {
            if (!oneToManyProperty.getOneToMany().cascadeSaveOrUpdate()) {
                continue;
            }
            if (sync) {
                String storeKey = "InsertOrUpdate:" + oneToManyProperty.getOneToMany().subTable() + ":" + oneToManyProperty.getOneToMany().joinValue();
                if (BaseDAOThreadLocalValue.remove(storeKey)) {
                    continue;
                }
                BaseDAOThreadLocalValue.add(storeKey);
            }

            String targetTable = oneToManyProperty.getOneToMany().subTable();
            BaseDAO subTableBaseDAO =  BaseDAOManager.baseDAOMap.get(targetTable);

            List<?> subDataList;
            Class subClass;
            if (oneToManyProperty.getOneToMany().oneToOne()) {
                Object propertyValue = getPropertyValue(t, oneToManyProperty.getField());
                subDataList = propertyValue == null ? Collections.emptyList() : Arrays.asList(propertyValue);
                subClass = oneToManyProperty.getField().getType();
            } else {
                subDataList = (List<?>) getPropertyValue(t, oneToManyProperty.getField());
                subClass = ((Class) ((ParameterizedType) oneToManyProperty.getField().getGenericType()).getActualTypeArguments()[0]);
            }

            Object refId = getIdValue(t);
            String refColumnName = oneToManyProperty.getOneToMany().joinValue();

            Field reverseField = ReflectionUtils.findField(subClass, oneToManyProperty.getOneToMany().reversePropertyName());

            if (Objects.isNull(reverseField)) {
                throw new IllegalArgumentException("reversePropertyName must be set");
            }

            if (oneToManyProperty.getOneToMany().cascadeDelete()) { // 级联删除
                if (CollectionUtils.isEmpty(subDataList)) {
                    // 删除所有
                    SQLUtils.delete(subTableBaseDAO.getTableName(), refColumnName, Arrays.asList(refId));
                    return;
                }

                Set<Long> deletedIds = subDataList.stream().filter(d -> Objects.nonNull(getIdValue(d))).map(d -> getIdValue(d)).collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(deletedIds)) {
                    // 删除所有
                    SQLUtils.delete(subTableBaseDAO.getTableName(), refColumnName, Arrays.asList(refId));
                } else {
                    // 删除 除id之外的其他记录
                    SQLUtils.deleteNotIn(subTableBaseDAO.getTableName(), "id",
                            deletedIds, new Object[] {refId} , refColumnName + " = ?");
                }
            }

            for (Object subData : subDataList) {
                setPropertyValue(subData, reverseField, t);
            }

            // 再插入或更新
            subTableBaseDAO.insertOrUpdate(subDataList);
        }

        // ManyToOne
        for (TableMeta.ManyToOneProperty manyToOneProperty : tableMeta.getManyToOneAnnotationList()) {
            if (!manyToOneProperty.getManyToOne().cascadeSaveOrUpdate()) {
                continue;
            }
            if (sync) {
                String refColumnName = manyToOneProperty.getManyToOne().value();
                String storeKey = "InsertOrUpdate:" + getTableName() + ":" + refColumnName;

                if (BaseDAOThreadLocalValue.remove(storeKey)) {
                    continue;
                }
                BaseDAOThreadLocalValue.add(storeKey);
            }

            String targetTable = manyToOneProperty.getManyToOne().parentTable();
            BaseDAO parentTableBaseDAO =  BaseDAOManager.baseDAOMap.get(targetTable);
            Object targetObject = getPropertyValue(t, manyToOneProperty.getField());
            if (Objects.nonNull(targetObject)) {
                parentTableBaseDAO.insertOrUpdate(targetObject);
                update(manyToOneProperty.getManyToOne().value(), new Object[] {getIdValue(targetObject)}, getIdValue(t));
            }
        }

        // ManyToMany 更新中间表
        updateManyToManyReferenceTable(t);
    }

    private void updateManyToManyReferenceTable(T t) {
        // ManyToMany
        for (TableMeta.ManyToManyProperty manyToManyProperty : tableMeta.getManyToManyAnnotationList()) {
            String referenceTable = manyToManyProperty.getManyToMany().thirdPartyTable();

            List<?> refDataList = (List<?>) getPropertyValue(t, manyToManyProperty.getField());

            List<Long> refIdsValue = null;
            if (CollectionUtils.isNotEmpty(refDataList)) {
                refIdsValue = Lists.newArrayListWithExpectedSize(refDataList.size());
                for (Object o : refDataList) {
                    refIdsValue.add(getIdValue(o));
                }
            }

            // 更新中间表
            SQLUtils.updateRefTable(referenceTable,
                    manyToManyProperty.getManyToMany().columnDefinition(), manyToManyProperty.getManyToMany().referenceColumnName(),
                    getIdValue(t), refIdsValue);
        }
    }

    private Long getIdValue(Object o) {
        if (Objects.isNull(o)) {
            return null;
        }
        return (Long) getPropertyValue(o, EntityConstants.ID_COLUMN_NAME);
    }

    private String getParamsUpdateSQL(String updateColumnNames, Map<String, Object> paramsMap, String conditionSQL) {
        List<String> updateColumnList = convertToArray(updateColumnNames);
        Object[] params = new Object[updateColumnList.size()];
        Object[] updateAutoObjects = handleUpdateAutoFill(updateColumnNames, params);
        Object[] conditionAdviceObjects = handleConditionAdvice(handleAutoFill(null, (Object[]) updateAutoObjects[0], convertToArray((String) updateAutoObjects[1]), ColumnFillType.UPDATE), conditionSQL, true);

        List<String> newUpdateColumnList = convertToArray((String) updateAutoObjects[1]);
        Object[] newParams = (Object[]) conditionAdviceObjects[0];
        StringBuilder updateColumnNameBuilder = new StringBuilder();
        int i = 0;
        for (String updateColumnName : newUpdateColumnList) {
            updateColumnNameBuilder.append(updateColumnName).append(" = :").append(updateColumnName).append(",");
            Object newParam = newParams[i++];
            if (Objects.isNull(paramsMap.get(updateColumnName))) {
                paramsMap.put(updateColumnName, newParam);
            }
        }
        updateColumnNameBuilder.deleteCharAt(updateColumnNameBuilder.length() - 1);

        String newConditionSQL = (String)conditionAdviceObjects[1];

        String updateSQL = "UPDATE " + tableMeta.getTableName() + " SET " + updateColumnNameBuilder + " WHERE " + newConditionSQL;
        return updateSQL;
    }

    private List<T> selectByIdsWithSpecifiedValue(Object ids) {
        Map<String, Object> params = Params.builder(1).pv("ids", ids).build();
        return selectByParams(params, tableMeta.getIdColumnName() + " IN(:ids)");
    }

    private interface SqlHandler {
        String handlerSQl(String srcSQL);
    }

}
