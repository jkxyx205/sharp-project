package com.rick.db.plugin.dao.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rick.common.http.exception.ExceptionCode;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.config.Constants;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.constant.SharpDbConstants;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.plugin.dao.support.ColumnAutoFill;
import com.rick.db.plugin.dao.support.ConditionAdvice;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.rick.db.config.Constants.DB_MYSQL;

/**
 *
 * @author Rick
 * @createdAt 2022-11-25 15:16:00
 */
public abstract class AbstractCoreDAO<ID> implements CoreDAO<ID> {

    @Autowired
    protected SharpService sharpService;

    @Autowired(required = false)
    protected ConditionAdvice conditionAdvice;

    @Autowired(required = false)
    protected ColumnAutoFill columnAutoFill;

    @Autowired(required = false)
    protected ValidatorHelper validatorHelper;

    @Autowired
    protected SharpDatabaseProperties sharpDatabaseProperties;

    protected String idColumnName;

    /**
     * eg. name, user_name
     */
    protected String columnNames;

    protected String tableName;

    protected List<String> columnNameList;

    /**
     * eg. name AS name, user_name as userName
     */
    protected String selectColumnNames;

    protected Class<?> idClass;

    public AbstractCoreDAO() {}

    public AbstractCoreDAO(String tableName, String columnNames, String idColumnName, Class<ID> idClass) {
        this.init(tableName, columnNames, idColumnName, idClass);
    }

    @Override
    public int insertOrUpdate(Map<String, Object> params) {
        if (params.get(this.getIdColumnName()) == null) {
            return insert(params);
        }

        return update(params);
    }

    @Override
    public int insert(Map<String, Object> params) {
        Object[] arrayParams = SQLUtils.convertToArray(params, this.columnNameList);
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
        return SQLUtils.insert(this.tableName, this.columnNames, resolveParams(handleAutoFill(null, params, columnNameList, ColumnFillType.INSERT)));
    }

    @Override
    public int update(Map<String, ?> params) {
        List<String> updateColumnNameList = this.columnNameList.stream().filter(columnName -> !columnName.equals(idColumnName)).collect(Collectors.toList());
        return updateById(params,
                updateColumnNameList.stream().collect(Collectors.joining(",")),
                SQLUtils.convertToArray(params, updateColumnNameList),
                (ID) params.get(idColumnName));
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
        if(Map.class.isAssignableFrom(paramClass)) {
            List<Object[]> params = Lists.newArrayListWithExpectedSize(paramsList.size());
            for (Object o : paramsList) {
                params.add(SQLUtils.convertToArray((Map)o, this.columnNameList));
            }
            return SQLUtils.insert(this.tableName, this.columnNames, resolveParams(handleAutoFill(params, params, columnNameList, ColumnFillType.INSERT)));
        } else if (paramClass == Object[].class) {
            return SQLUtils.insert(this.tableName, this.columnNames, resolveParams(handleAutoFill((List<?>) paramsList, (List<Object[]>) paramsList, columnNameList, ColumnFillType.INSERT)));
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
    public int deleteByIds(ID... ids) {
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
        Object[] objects = handleConditionAdvice(new Object[]{}, null, null, false);
        return SQLUtils.delete(this.tableName, deleteColumn, deleteValues, (Object[]) objects[0], (String) objects[1]);
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
        Object[] objects = handleConditionAdvice(params, null, conditionSQL, false);
        return SQLUtils.delete(this.tableName, (Object[]) objects[0], (String) objects[1]);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Map<String, Object> params, String conditionSQL) {
        Object[] objects = handleConditionAdvice(new Object[]{}, params, conditionSQL, true);
        String conditionSql = (String) objects[1];
        return sharpService.update("DELETE FROM " + getTableName() + " WHERE " + conditionSql, params);
    }

    /**
     * 通过主键逻辑id刪除
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogicallyById(ID id) {
        Assert.notNull(id, "id不能为空");
        return update(SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, new Object[]{1, id}, this.idColumnName + " = ?");
//        return deleteLogicallyByIds(Arrays.asList(id));
    }

    /**
     * 通过主键id批量逻辑刪除 eg：ids -> “1,2,3,4”
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogicallyByIds(String ids) {
        Assert.hasText(ids, "id不能为空");
        return deleteLogicallyByIds(Arrays.asList(ids.split(SharpDbConstants.COLUMN_NAME_SEPARATOR_REGEX)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogicallyByIds(ID... ids) {
        Assert.notNull(ids, "ids不能为空");
        return deleteLogicallyByIds(Arrays.asList(ids));
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
        return update(SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, mergedParams, this.idColumnName + " IN " + SQLUtils.formatInSQLPlaceHolder(ids.size()));
    }

    @Override
    public long deleteAll() {
        long count = countByParams(null, null);
        if (count > 0) {
            SQLUtils.execute("TRUNCATE TABLE " + tableName);
        }
        return count;
    }

    /**
     * 更新所有字段
     *
     * @param params
     * @param id
     */
    @Override
    public int update(Object[] params, ID id) {
        return updateById(null, this.columnNames, params, id);
    }

    /**
     * 指定更新字段，构造条件更新
     *
     * @param updateColumnNames name, age
     * @param params            {"Rick", 23, LocalDateTime.now, 1}
     * @param conditionSQL      create_time > ? AND create_id = ?
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

        List<Object[]> paramsList = Lists.newArrayListWithExpectedSize(srcParamsList.size());
        for (Object[] params : srcParamsList) {
            Object[] updateAutoObjects = handleUpdateAutoFill(updateColumnNames, params);
            Object[] conditionAdviceObject = handleConditionAdvice(handleAutoFill(null, (Object[]) updateAutoObjects[0], convertToList((String) updateAutoObjects[1]), ColumnFillType.UPDATE), null, conditionSQL, false);
            paramsList.add((Object[]) conditionAdviceObject[0]);
            conditionSQL = (String) conditionAdviceObject[1];
            updateColumnNames = (String) updateAutoObjects[1];
        }
        return SQLUtils.update(this.tableName, updateColumnNames, paramsList, conditionSQL);
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
        return selectIdsByParams(params, null);
    }

    @Override
    public List<ID> selectIdsByParams(Map<String, ?> params, String conditionSQL) {
        return (List<ID>) selectByParams(params, this.idColumnName, conditionSQL, this.idClass);
    }

    @Override
    public <E> List<E> selectByParams(Map<String, ?> params, String columnNames, Class<E> clazz) {
        return selectByParams(params, columnNames, null, clazz);
    }

    @Override
    public <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, Class<E> clazz) {
        return selectByParams(params, columnNames, conditionSQL, src -> src, clazz, null);
    }

    /**
     * 依赖sharpService，可以进行不定条件的查询
     * @param params
     * @param columnNames
     * @param conditionSQL
     * @param sqlHandler
     * @param clazz
     * @return
     */
    protected <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, SqlHandler sqlHandler, Class<E> clazz) {
        return selectByParams(params, columnNames, conditionSQL, sqlHandler, clazz, null);
    }

    /**
     *
     * @param params
     * @param columnNames
     * @param conditionSQL
     * @param sqlHandler
     * @param clazz 如何为null，则返回map
     * @param afterSelect
     * @param <E>
     * @return
     */
    protected <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, SqlHandler sqlHandler, Class<E> clazz, Consumer<List<E>> afterSelect) {
        sqlHandler = (sqlHandler == null) ? src -> src : sqlHandler;

        Object[] executeCondition = getSelectByParamsCondition(params, columnNames, conditionSQL, sqlHandler);

        List<E> list = Objects.isNull(clazz) ? sharpService.query((String) executeCondition[0], (Map)executeCondition[1]) :
                sharpService.query((String) executeCondition[0], (Map)executeCondition[1], clazz);

        if (Objects.nonNull(afterSelect)) {
            afterSelect.accept(list);
        }
        return list;
    }

    public interface SqlHandler {
        String handlerSQl(String srcSQL);
    }

    /**
     * 获取最终执行的参数，可以使用sharpService执行
     * @param params
     * @param columnNames
     * @param conditionSQL
     * @param sqlHandler
     * @return
     */
    private Object[] getSelectByParamsCondition(Map<String, ?> params, String columnNames, String conditionSQL, SqlHandler sqlHandler) {
        if (MapUtils.isEmpty(params)) {
            params = Collections.emptyMap();
        }

        Map<String, Object> conditionParams;
        String finalConditionSQL = (StringUtils.isBlank(conditionSQL) ? getConditionSQL(params) : conditionSQL);
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

    private boolean isConditionSQLContainsColumnName(String conditionSQL, String columnName) {
        if (StringUtils.isBlank(conditionSQL)) {
            return false;
        }

        return conditionSQL.matches(".*((?i)(to_char|NVL)?\\s*([(][^([(]|[)])]*[)])|("+columnName+"))"+ "\\s*" + "(?i)(like|!=|>=|<=|<|>|=|\\s+in|\\s+not\\s+in|regexp).*");
    }


    @Override
    public long countByParams(Map<String, ?> params, String conditionSQL) {
        return selectByParams(params, "count(*)", conditionSQL, srcSQL -> srcSQL, Long.class).get(0);
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
    public <K, V> Map<K, V> selectByParamsAsMap(Map<String, ?> params, String columnNames) {
        return selectByParamsAsMap(params, columnNames, null);
    }

    /**
     * @param params
     * @param columnNames 比如 id,name。id作key，name作value
     * @param conditionSQL
     * @return
     */
    @Override
    public <K, V> Map<K, V> selectByParamsAsMap(Map<String, ?> params, String columnNames, String conditionSQL) {
        Object[] executeCondition = getSelectByParamsCondition(params, columnNames, conditionSQL, sql -> sql);
        return sharpService.queryForKeyValue((String) executeCondition[0], (Map)executeCondition[1]);
    }

    @Override
    public void checkId(ID id) {
        checkId(id, Collections.emptyMap(), null);
    }

    @Override
    public void checkId(ID id, Map<String, Object> params, String conditionSQL) {
        ExceptionCode.notNull(id, "id cannot be null");
        if (!existsByParams(Params.builder(1 + params.size()).pv("id", id).pvAll(params).build(), getIdColumnName() +" = :id" + (StringUtils.isBlank(conditionSQL) ? "" : " AND " + conditionSQL))) {
            ExceptionCode.notExists(tableName + " "+getIdColumnName()+" = " + id + " +不存在", id);
        }
    }

    @Override
    public void checkIds(Collection<ID> ids) {
        checkIds(ids, Collections.emptyMap(), null);
    }

    @Override
    public void checkIds(Collection<ID> ids, Map<String, Object> params, String conditionSQL) {
        ExceptionCode.state(CollectionUtils.isNotEmpty(ids), "ids cannot be empty");
        List<ID> idsInDB = (List<ID>) sharpService.query("SELECT "+getIdColumnName()+" FROM " + getTableName() + " WHERE "+getIdColumnName()+" IN (:ids)" + (StringUtils.isBlank(conditionSQL) ? "" : " AND " + conditionSQL), Params.builder(1 + params.size()).pv("ids", ids).pvAll(params).build(), this.idClass);
        SetUtils.SetView<ID> difference = SetUtils.difference(Sets.newHashSet(ids), Sets.newHashSet(idsInDB));
        if (CollectionUtils.isNotEmpty(difference)) {
            ExceptionCode.notExists(tableName + " "+getIdColumnName()+" = " + StringUtils.join(difference.toArray(), ",") + "不存在", difference.toArray());
        }
    }

    /**
     * 指定更新字段
     *
     * @param updateColumnNames name, age, update_time
     * @param params            update && where 语句后面的参数 {"Rick", 23, null}
     * @param id                1
     */
    @Override
    public int updateById(String updateColumnNames, Object[] params, ID id) {
        return updateById(null, updateColumnNames, params, id);
    }

    @Override
    public String getSelectSQL() {
        return "SELECT "+ selectColumnNames +" FROM " + getTableName();
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

    public String getSelectColumnNames() {
        return selectColumnNames;
    }

    protected Object resolveValue(Object value) {
       return SQLUtils.resolveValue(value);
    }

    protected void init(String tableName, String columnNames, String idColumnName, Class<ID> idClass) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.idColumnName = idColumnName;
        this.idClass = idClass;
        this.columnNameList = convertToList(this.columnNames);
        initSelectColumnNames();
    }

    private void initSelectColumnNames() {
        int columnSize = this.columnNameList.size();
        StringBuilder selectSQLBuilder = new StringBuilder();
        for (int i = 0; i < columnSize; i++) {
            selectSQLBuilder.append(this.getTableName() + "." + this.columnNameList.get(i))
                    .append(" AS \"").append(columnAliasName(this.columnNameList.get(i))).append("\",");
        }
        selectSQLBuilder.deleteCharAt(selectSQLBuilder.length() - 1);

        this.selectColumnNames = selectSQLBuilder.toString();
    }

    protected String columnAliasName(String columnName) {
        return com.rick.common.util.StringUtils.camelToSnake(columnName);
    }

    protected Object[] mergeParam(Object[] params, Object id, String columnName) {
        Object[] mergedParams;
        if (ArrayUtils.isEmpty(params)) {
            mergedParams = new Object[]{id};
        } else {
            mergedParams = new Object[params.length + 1];
            mergedParams[params.length] = id;
            System.arraycopy(params, 0, mergedParams, 0, params.length);
        }

        return new Object[]{mergedParams, "" + columnName + " = ?"};
    }

    protected Object[] resolveParams(Object[] params) {
        for (int i = 0; i < params.length; i++) {
            params[i] = resolveValue(params[i]);
        }
        return params;
    }

    protected List<Object[]> resolveParams(List<Object[]> paramList) {
        for (Object[] params : paramList) {
            for (int i = 0; i < params.length; i++) {
                params[i] = resolveValue(params[i]);
            }
        }

        return paramList;
    }

    protected List<String> convertToList(String values) {
        return Arrays.asList(values.split(SharpDbConstants.COLUMN_NAME_SEPARATOR_REGEX));
    }

    private String getParamsUpdateSQL(String updateColumnNames, Map<String, Object> paramsMap, String conditionSQL) {
        List<String> updateColumnList = convertToList(updateColumnNames);
        Object[] params = new Object[updateColumnList.size()];
        Object[] updateAutoObjects = handleUpdateAutoFill(updateColumnNames, params);

        Object[] conditionAdviceObjects = handleConditionAdvice(handleAutoFill(null, (Object[]) updateAutoObjects[0], convertToList((String) updateAutoObjects[1]), ColumnFillType.UPDATE), paramsMap, conditionSQL, true);

        List<String> newUpdateColumnList = convertToList((String) updateAutoObjects[1]);
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

        String updateSQL = "UPDATE " + this.tableName + " SET " + updateColumnNameBuilder + " WHERE " + newConditionSQL;
        return updateSQL;
    }

    protected Object[] handleAutoFill(Object t, Object[] params, List<String> columnNameList, ColumnFillType fillType) {
        // handle version
        if (t != null && t != params && EntityDAOManager.isEntityClass(t.getClass()) &&
         StringUtils.isNotBlank(EntityDAOManager.getTableMeta(t.getClass()).getVersionProperty().getPropertyName())) {
            int index = columnNameList.indexOf(EntityDAOManager.getTableMeta(t.getClass()).getVersionProperty().getColumnName());

            if (params[index] == null) {
                params[index] = 1;
            } else {
                params[index] = (Integer)params[index] + 1;
            }

            // 将auto值回写到实体中
            EntityDAOManager.setPropertyValue(t,
                    EntityDAOManager.getTableMeta(t.getClass()).getVersionProperty().getPropertyName(),
                    params[index]);

        }

        if (Objects.nonNull(columnAutoFill)) {
            Map<String, Object> fill = (ColumnFillType.INSERT == fillType) ? columnAutoFill.insertFill(this.idColumnName, generatorId(t)) : columnAutoFill.updateFill();

            for (Map.Entry<String, Object> en : fill.entrySet()) {
                String fillColumnName = en.getKey();
                Object fillColumnValue = en.getValue();

                int index = columnNameList.indexOf(fillColumnName);
                if (index > -1 && Objects.isNull(params[index])) {
                    params[index] = resolveValue(fillColumnValue);

                    if (t == null || t == params) {
                        continue;
                    }

                    if (Map.class.isAssignableFrom(t.getClass())) {
                        ((Map)t).put(fillColumnName, fillColumnValue);
                    } else if (EntityDAOManager.isEntityClass(t.getClass())) {
                        // 将auto值回写到实体中
                        EntityDAOManager.setPropertyValue(t,
                                EntityDAOManager.getTableMeta(t.getClass()).getColumnNameFieldMap().get(fillColumnName).getName(),
                                fillColumnValue);
                    }

                }
            }
        }

        return params;
    }

    protected List<Object[]> handleAutoFill(List<?> list, List<Object[]> paramsList, List<String> columnNameList, ColumnFillType fillType) {
        for (int i = 0; i < paramsList.size(); i++) {
            handleAutoFill(list.get(i), paramsList.get(i), columnNameList, fillType);
        }
        return paramsList;
    }

    protected abstract ID generatorId(Object t);

    private Object[] handleUpdateAutoFill(String updateColumnNames, Object[] params) {
        Object mergedParams = params;
        if (Objects.nonNull(columnAutoFill)) {
            Map<String, Object> fill = columnAutoFill.updateFill();
            List<String> updateColumnNameList = convertToList(updateColumnNames);

            int size = 0;
            for (String fillColumnName : fill.keySet()) {
                if (!updateColumnNameList.contains(fillColumnName) && isUpdateFillColumnName(fillColumnName)) {
                    updateColumnNames = fillColumnName + "," + updateColumnNames;
                    size++;
                }
            }

            mergedParams = new Object[params.length + size];
            System.arraycopy(params, 0, mergedParams, size, params.length);
        }

        return new Object[] {mergedParams, updateColumnNames};
    }

    protected boolean isUpdateFillColumnName(String fillColumnName) {
        return true;
    }

    protected int updateById(Object t, String updateColumnNames, Object[] params, ID id) {
        Assert.notNull(id, "id不能为空");
        Object[] objects = mergeParam(params, id, getIdColumnName());
        return update(t, updateColumnNames, (Object[]) objects[0], (String) objects[1]);
    }

    private int update(Object t, String updateColumnNames, Object[] params, String conditionSQL) {
        return update(this.tableName, t, updateColumnNames, params, conditionSQL);
    }

    protected int update(String tableName, Object t, String updateColumnNames, Object[] params, String conditionSQL) {
        Object[] updateAutoObjects = handleUpdateAutoFill(updateColumnNames, params);
        Object[] conditionAdviceObjects = handleConditionAdvice(handleAutoFill(t, (Object[]) updateAutoObjects[0], convertToList((String) updateAutoObjects[1]), ColumnFillType.UPDATE), null, conditionSQL, false);
        return SQLUtils.update(tableName, (String) updateAutoObjects[1], resolveParams((Object[])conditionAdviceObjects[0]), (String) conditionAdviceObjects[1]);
    }

    /**
     * 记录超过1个会抛出异常
     * @param list
     * @param <E>
     * @return
     */
    protected <E> Optional<E> expectedAsOptional(List<E> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Optional.empty();
        }

        if (list.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, list.size());
        }

        return Optional.of(list.get(0));
    }

    /**
     * 处理条件自动注入
     * @param params
     * @param conditionSQL
     * @return
     */
    protected Object[] handleConditionAdvice(Object[] params, Map<String, Object> paramsMap, String conditionSQL, boolean isParamHolder) {
        Object[] mergedParams = params;
        if (Objects.nonNull(this.conditionAdvice)) {
            Map<String, Object> conditionParams = conditionAdvice.getCondition();
            final String finalConditionSQL = conditionSQL;

            if (MapUtils.isNotEmpty(conditionParams)) {
                Collection<String> filteredConditionParams = conditionParams.keySet().stream().filter(key -> this.columnNameList.contains(key) && !isConditionSQLContainsColumnName(finalConditionSQL, key)).collect(Collectors.toList());
                String additionCondition = getConditionSQL(filteredConditionParams, conditionParams, true);
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
                        if (MapUtils.isNotEmpty(paramsMap)) {
                            paramsMap.put(key, conditionParams.get(key));
                        }
                    }
                }

            }
        }
        return new Object[]{mergedParams, conditionSQL};
    }

    protected String getConditionSQL(Map<String, ?> params) {
        return getConditionSQL(this.columnNameList, params, false);
    }

    private String getConditionSQL(Collection<String> paramNameList, Map<String, ?> params) {
        return getConditionSQL(paramNameList, params, false);
    }

    private String getConditionSQL(Collection<String> paramNameList, Map<String, ?> params, boolean onlyColumnNameHolder) {
        StringBuilder sb = new StringBuilder();
        for (String columnName : paramNameList) {
            Object value = params.get(columnName);
            if (onlyColumnNameHolder) {
                // 字段名做参数
                appendParamHolder(sb, columnName, value);
            } else {
                appendParamHolder(sb, columnName, value);
                appendParamHolder0(sb, columnName, value);
            }
        }

        return StringUtils.isBlank(sb) ? "" : sb.substring(0, sb.length() - 5);
    }

    private void appendParamHolder(StringBuilder sb, String columnName, Object value) {
        String paramHolder = decideParamHolder(columnName, value);
        sb.append(columnName).append(paramHolder).append(" AND ");
    }

    protected void appendParamHolder0(StringBuilder sb, String columnName, Object value){}

    protected String decideParamHolder(String paramHolderName, Object value) {
        if (Objects.isNull(value)) {
            return " = :" + paramHolderName;
        }

        if (value instanceof Iterable
                || value.getClass().isArray()
                || (value.getClass() == String.class && ((String) value).split(Constants.PARAM_IN_SEPARATOR).length > 1)) {
            return " IN (:" + paramHolderName + ")";
        } /*else if (((String) value).startsWith(Constants.PARAM_LIKE_SEPARATOR)) {
            params.put(paramHolderName, ((String) value).substring(1));
            return " LIKE :" + paramHolderName;
        }*/

        return " = :" + paramHolderName;
    }

}
