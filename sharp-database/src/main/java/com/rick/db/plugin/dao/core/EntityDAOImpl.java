package com.rick.db.plugin.dao.core;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.util.ClassUtils;
import com.rick.common.util.IdGenerator;
import com.rick.db.constant.SharpDbConstants;
import com.rick.db.dto.SimpleEntity;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.plugin.dao.annotation.Id;
import com.rick.db.service.support.Params;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rick.db.plugin.dao.annotation.Id.GenerationType.ASSIGN;
import static com.rick.db.plugin.dao.annotation.Id.GenerationType.SEQUENCE;

/**
 * @author Rick
 * @createdAt 2021-09-23 16:41:00
 */
@Slf4j
public class EntityDAOImpl<T, ID> extends AbstractCoreDAO<ID> implements EntityDAO<T, ID> {

    @Autowired
    @Qualifier("dbConversionService")
    private ConversionService conversionService;

    private TableMeta tableMeta;

    private List<String> updateColumnNameList;

    private List<String> propertyList;

    private List<String> updatePropertyList;

    private Class<T> entityClass;

    private String subTableRefColumnName;

    private Map<String, String> columnNameToPropertyNameMap;

    private Map<String, String> propertyNameToColumnNameMap;

    private boolean hasCascadeDelete = false;

    private boolean hasVersionManagement = false;

    /**
     * id 生成策略
     */
    private Id.GenerationType strategy;

    public EntityDAOImpl() {
        this.init();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertOrUpdate(T entity) {
        if (Objects.isNull(getIdValue(entity))) {
            return insert(entity);
        } else {
            int count =  update(entity);
            if (strategy == ASSIGN && count == 0) {
                return insert(entity);
            }

            return count;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int[] insertOrUpdate(Collection<T> entities) {
        int[] updateCount = update(entities.stream().filter(t -> Objects.nonNull(getIdValue(t))).collect(Collectors.toList()));
        int[] insertCount = insert(entities.stream().filter(t -> Objects.isNull(getIdValue(t))).collect(Collectors.toList()));

        return ArrayUtils.addAll(insertCount, updateCount);
    }

    @Override
    public int insert(Map<String, Object> params) {
        T t = mapToEntity(params);
        int count = insert(t);
        if (count > 0) {
            params.put(tableMeta.getIdPropertyName(), getIdValue(t));
        }

        return count;
    }

    /**
     * 插入单条数据
     *
     * @param entity 参数对象
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(T entity) {
        if (Objects.nonNull(validatorHelper)) {
            validatorHelper.validate(entity);
        }

        Object[] params = handleAutoFill(entity, instanceToParamsArray(entity), columnNameList, ColumnFillType.INSERT);
        int count = SQLUtils.insert(this.tableName, this.columnNames, params);

        cascadeInsertOrUpdate(entity, true);
        EntityDAOThreadLocalValue.removeAll();
        return count;
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
        if (paramClass == this.entityClass) {
            List<T> instanceList = Lists.newArrayListWithExpectedSize(paramsList.size());
            List<Object[]> params = Lists.newArrayListWithExpectedSize(paramsList.size());

            for (Object o : paramsList) {
                instanceList.add((T) o);
                if (Objects.nonNull(validatorHelper)) {
                    validatorHelper.validate(o);
                }
                params.add(instanceToParamsArray((T) o));
            }
            int[] count = SQLUtils.insert(this.tableName, this.columnNames, handleAutoFill(instanceList, params, columnNameList, ColumnFillType.INSERT));
            cascadeInsertOrUpdate(instanceList, true);
            return count;
        } else {
            return super.insert(paramsList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(String deleteColumn, Collection<?> deleteValues) {
        return delete(Params.builder(1).pv(deleteColumn, deleteValues).build(), deleteColumn + " IN (:" + deleteColumn + ")");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Map<String, Object> params, String conditionSQL) {
        if (hasCascadeDelete) {
            CascadeSelectThreadLocalValue.add((oneToManyProperty, subTableEntityDAO, set) -> {
                TableMeta subTableMeta = subTableEntityDAO.getTableMeta();
                // 删除子表
                if (CollectionUtils.isNotEmpty(set)) {
                    SQLUtils.delete(subTableMeta.getTableName(), oneToManyProperty.getOneToMany().joinValue(), set);
                }
            }, (manyToManyProperty, set) -> {
                if (CollectionUtils.isNotEmpty(set)) {
                    SQLUtils.delete(manyToManyProperty.getManyToMany().thirdPartyTable(), manyToManyProperty.getManyToMany().columnDefinition(), set);
                }
            });

            selectByParams(params, idColumnName, conditionSQL, null, this.entityClass);
            CascadeSelectThreadLocalValue.removeAll();
        }

        return super.delete(params, conditionSQL);
    }

    /**
     * 通过主键逻辑id刪除
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogicallyById(ID id) {
        Assert.notNull(id, "id不能为空");
        return deleteLogicallyByIds(Arrays.asList(id));
    }

    /**
     * 通过主键id批量逻辑刪除 eg：ids -> [1, 2, 3, 4]
     *
     * @param
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogicallyByIds(Collection<?> ids) {
        if (hasCascadeDelete) {
            CascadeSelectThreadLocalValue.add((oneToManyProperty, subTableEntityDAO, set) -> {
                // 逻辑删除子表
                if (CollectionUtils.isNotEmpty(set)) {
                    subTableEntityDAO.update(SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, Params.builder(1).pv("refIds", set)
                                    .pv(SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, true)
                                    .build(),
                            oneToManyProperty.getOneToMany().joinValue() + " IN (:refIds) AND is_deleted = 0");
                }
            }, (manyToManyProperty, set) -> {
                if (set.size() > 0) {
                    Object[] mergedParams = new Object[set.size() + 1];
                    mergedParams[0] = 1;
                    System.arraycopy(set.toArray(), 0, mergedParams, 1, set.size());
                    SQLUtils.update(manyToManyProperty.getManyToMany().thirdPartyTable(), SharpDbConstants.LOGIC_DELETE_COLUMN_NAME,
                            mergedParams, manyToManyProperty.getManyToMany().columnDefinition() + " IN " + SQLUtils.formatInSQLPlaceHolder(set.size()));
                }
            });

            selectByParams(Params.builder(1).pv("ids", ids).build(), idColumnName, getIdColumnName() + " IN (:ids)", null, this.entityClass);
            CascadeSelectThreadLocalValue.removeAll();
        }

        return super.deleteLogicallyByIds(ids);
    }

    @Override
    public long deleteAll() {
        return this.delete(Collections.emptyMap(), null);
    }

    /**
     * 更新所有字段
     *
     * @param params
     * @param id
     */
    @Override
    public int update(Object[] params, ID id) {
        return updateById(null, tableMeta.getUpdateColumnNames(), params, id);
    }

    @Override
    public <E> List<E> selectByParams(Map<String, ?> params, String columnNames, Class<E> clazz) {
        return selectByParams(params, columnNames, null, clazz);
    }

    @Override
    public int update(Map<String, ?> params) {
        return update(mapToEntity(params));
    }

    /**
     * 更新所有字段
     * @param entity
     * @return 返回影响的行。返回0，有两种情况，id不存在；如果版本管理，版本滞后。
     */
    @Override
    public int update(T entity) {
        return update(entity, tableMeta.getUpdateColumnNames());
    }

    @Override
    public int update(T entity, String updateColumnNames) {
        if (Objects.nonNull(validatorHelper)) {
            validatorHelper.validate(entity);
        }

        Object[] objects = resolveUpdateParamsAndId(entity, convertToList(updateColumnNames));
        return updateById(entity, updateColumnNames, (Object[]) objects[0], (ID) objects[1]);
    }

    /**
     * 更新entity
     *
     * @param entity
     * @param params       where语句后面的参数
     * @param conditionSQL 这里的条件优先级高，可以覆盖condition
     * @return
     */
    @Override
    public int update(T entity, Object[] params, String conditionSQL) {
        if (Objects.nonNull(validatorHelper)) {
            validatorHelper.validate(entity);
        }

        int size = this.updateColumnNameList.size();

        params = ArrayUtils.isEmpty(params) ? new Object[]{} : params;

        Object[] mergedParams = new Object[size + params.length];
        for (int i = 0; i < size; i++) {
            mergedParams[i] = resolveValue(EntityDAOManager.getPropertyValue(entity, columnNameToPropertyNameMap.get(this.updateColumnNameList.get(i))));
        }

        System.arraycopy(params, 0, mergedParams, size, params.length);
        return update(entity, tableMeta.getUpdateColumnNames(), mergedParams, conditionSQL);
    }

    /**
     * 批量更新。
     * 批量更新忽略版本管理，如果想要版本管理，请单个更新！
     *
     * @param collection
     * @return
     */
    @Override
    public int[] update(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return new int[]{};
        }

        List<Object[]> paramsList = Lists.newArrayListWithExpectedSize(collection.size());

        String conditionSQL = null;
        for (T entity : collection) {
            Object[] resolverParamsAndIdObjects = resolveUpdateParamsAndId(entity, updateColumnNameList);
            Object[] mergeIdParamObjects = mergeParam((Object[]) resolverParamsAndIdObjects[0], resolverParamsAndIdObjects[1], getIdColumnName());
            Object[] finalObjects = handleConditionAdvice(handleAutoFill(entity, (Object[]) mergeIdParamObjects[0], updateColumnNameList, ColumnFillType.UPDATE), null, (String) mergeIdParamObjects[1], false);
            paramsList.add((Object[]) finalObjects[0]);
            conditionSQL = (String) finalObjects[1];
            cascadeInsertOrUpdate(entity, false);
        }

        EntityDAOThreadLocalValue.removeAll();
        return SQLUtils.update(this.tableName, tableMeta.getUpdateColumnNames(), paramsList, conditionSQL);
    }

    /**
     * 通过ID查找
     *
     * @param id
     * @return
     */
    @Override
    public Optional<T> selectById(ID id) {
        Assert.notNull(id, "id cannot be null");
        Map<String, Object> params = Params.builder(1)
                .pv("id", id)
                .build();

        List<T> list = selectByParams(params, this.idColumnName + " = :id");
        return expectedAsOptional(list);
    }

    @Override
    public Map<ID, T> selectByIdsAsMap(String ids) {
        return listToIdMap(selectByIds(ids));
    }

    @Override
    public Map<ID, T> selectByIdsAsMap(ID... ids) {
        return listToIdMap(selectByIds(ids));
    }

    @Override
    public Map<ID, T> selectByIdsAsMap(Collection<?> ids) {
        List<T> list = selectByIds(ids);
        return listToIdMap(list);
    }

    @Override
    public Map<ID, T> selectByIdsAsMap(Map<String, ?> params, String conditionSQL) {
        List<T> list = selectByParams(params, conditionSQL);
        return listToIdMap(list);
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
    public List<T> selectByIds(ID... ids) {
        Assert.notEmpty(ids, "ids cannot be empty");
        return selectByIdsWithSpecifiedValue(ids);
    }

    @Override
    public List<T> selectByIds(Collection<?> ids) {
        Assert.notEmpty(ids, "ids cannot be empty");
        return selectByIdsWithSpecifiedValue(ids);
    }

    /**
     * name=23&age=15,13&material_id => name=:name AND age IN(:age) AND material_id = :material_id
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
        final Map<String, String> map = StringUtils.isBlank(queryString) ? Collections.emptyMap() : Splitter.on('&').trimResults().withKeyValueSeparator('=').split(queryString);
        return selectByParams(map, conditionSQL);
    }

    /**
     * 根据条件查找
     *
     * @param
     * @return
     */
    @Override
    public List<T> selectByParams(T example) {
        return selectByParams(example, null);
    }

    @Override
    public List<T> selectByParams(T example, String conditionSQL) {
        return selectByParams(entityToMap(example), conditionSQL);
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

    @Override
    public List<T> selectByParams(Map<String, ?> params, String conditionSQL) {
        return selectByParams(params, selectColumnNames, conditionSQL, null, entityClass);
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
    public Optional<ID> selectIdByParams(T example) {
        return selectIdByParams(example, null);
    }

    @Override
    public <T> Optional<T> selectSingleValueById(ID id, String columnName, Class<T> clazz) {
        Assert.notNull(id, "id cannot be null");
        List<T> values = selectByParams(Params.builder(1).pv(tableMeta.getIdPropertyName(), id).build(), columnName, this.idColumnName + " = :" + tableMeta.getIdPropertyName(), clazz);
        return expectedAsOptional(values);
    }

    @Override
    public List<ID> selectIdsByParams(T example) {
        return selectIdsByParams(example, null);
    }

    @Override
    public List<ID> selectIdsByParams(T example, String conditionSQL) {
        return selectIdsByParams(entityToMap(example), conditionSQL);
    }

    /**
     * 根据条件获取id，如果有多条会抛出异常
     *
     * @param example
     * @param conditionSQL
     * @return
     */
    @Override
    public Optional<ID> selectIdByParams(T example, String conditionSQL) {
        List<ID> ids = (List<ID>) selectByParams(entityToMap(example), this.idColumnName, conditionSQL, sql -> sql, this.idClass);
        return expectedAsOptional(ids);
    }

    @Override
    public List<T> selectByParamsWithoutCascade(T example) {
        return selectByParamsWithoutCascade(example, null);
    }

    @Override
    public List<T> selectByParamsWithoutCascade(T example, String conditionSQL) {
        return selectByParamsWithoutCascade(entityToMap(example), selectColumnNames, conditionSQL);
    }

    @Override
    public List<T> selectByParamsWithoutCascade(Map<String, ?> params) {
        return selectByParamsWithoutCascade(params, null);
    }

    @Override
    public List<T> selectByParamsWithoutCascade(Map<String, ?> params, String conditionSQL) {
        return selectByParamsWithoutCascade(params, selectColumnNames, conditionSQL);
    }

    @Override
    public List<T> selectByParamsWithoutCascade(Map<String, ?> params, String columnNames, String conditionSQL) {
        return super.selectByParams(params, columnNames, conditionSQL, getEntityClass());
    }

    @Override
    public <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, Class<E> clazz) {
        return selectByParams(params, columnNames, conditionSQL, src -> src, clazz);
    }

    @Override
    protected <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, SqlHandler sqlHandler, Class<E> clazz) {
        return super.selectByParams(params, columnNames, conditionSQL, sqlHandler, clazz, list -> {
            if (EntityDAOManager.isEntityClass(clazz)) {
                cascadeSelect((List<T>) list);
                EntityDAOThreadLocalValue.removeByTableName(getTableName());
            }
        });
    }

    @Override
    public void selectAsSubTable(List<Map<String, Object>> data, String refColumnName, String valueKey, String property) {
        Map<ID, List<T>> refColumnNameMap = groupByColumnName(refColumnName, data.stream().map(row -> row.get(valueKey)).collect(Collectors.toSet()));

        for (Map<String, Object> row : data) {
            List<T> subTableList = refColumnNameMap.get(valueKey);
            row.put(property, CollectionUtils.isEmpty(subTableList) ? Collections.emptyList() : subTableList);
        }
    }

    @Override
    public Map<ID, List<T>> groupByColumnName(String refColumnName, Collection<?> refValue) {
        return groupByColumnName(refColumnName, refValue, this.selectColumnNames, Function.identity());
    }

    @Override
    public <M> Map<ID, List<M>> groupByColumnName(String refColumnName, Collection<?> refValue, String columnNames, Function<T, M> function) {
        List<T> list = selectByParams(Params.builder(1).pv("refColumnName", refValue).build(), columnNames, refColumnName + " IN (:refColumnName)", this.entityClass);

        return list.stream().collect(Collectors.groupingBy(t -> {
            Object propertyValue = EntityDAOManager.getPropertyValue(t, columnNameToPropertyNameMap.get(refColumnName));
            if (this.idClass.isAssignableFrom(propertyValue.getClass())) {
                return (ID) propertyValue;
            }

            return (ID) getPropertyValue(propertyValue, ReflectionUtils.findField(propertyValue.getClass(), tableMeta.getIdPropertyName()));

        }, Collectors.mapping(function, Collectors.toList())));
    }

    @Override
    public Map<String, Object> entityToMap(T entity) {
        if (entity == null) {
            return Collections.emptyMap();
        }

        Set<String> fieldNames = tableMeta.getFieldMap().keySet();

        Map<String, Object> params;
        params = Maps.newHashMapWithExpectedSize(fieldNames.size());
        for (String fieldName : fieldNames) {
            String columnName = propertyNameToColumnNameMap.get(fieldName);
            Object propertyValue = EntityDAOManager.getPropertyValue(entity, fieldName);
            if (Objects.nonNull(propertyValue)) {
                params.put(fieldName, propertyValue);

                if (columnName != null && !fieldName.equals(columnName)) {
                    params.put(columnName, resolveValue(propertyValue));
                }

            }
        }
        return params;
    }

    @Override
    public T mapToEntity(Map<String, ?> map) {
        T mappedObject = BeanUtils.instantiateClass(this.entityClass);

        BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
        bw.setAutoGrowNestedPaths(true);
        bw.setConversionService(conversionService);

        Set<String> fieldNames = tableMeta.getFieldMap().keySet();

        for (String fieldName : fieldNames) {
            String columnName = propertyNameToColumnNameMap.get(fieldName);
            Object propertyValue = map.get(fieldName) == null ? map.get(columnName) : map.get(fieldName);

            if (Objects.nonNull(propertyValue)) {
                bw.setPropertyValue(fieldName, propertyValue);
            }
        }

        return mappedObject;
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public TableMeta getTableMeta() {
        return this.tableMeta;
    }

    private void init() {
        Class<?>[] actualTypeArgument = ClassUtils.getClassGenericsTypes(this.getClass());

        this.entityClass = (Class<T>) actualTypeArgument[0];

        this.tableMeta = TableMetaResolver.resolve(this.entityClass);
        log.debug("properties: {}", tableMeta.getProperties());

        hasCascadeDelete = CollectionUtils.isNotEmpty(tableMeta.getManyToManyAnnotationList()) ||
                tableMeta.getOneToManyAnnotationList().stream().anyMatch(s -> s.getOneToMany().cascadeDelete());

        hasVersionManagement = StringUtils.isNotBlank(tableMeta.getVersionProperty().getPropertyName());

        this.subTableRefColumnName = tableMeta.getName() + "_" + tableMeta.getIdColumnName();

        this.propertyList = convertToList(tableMeta.getProperties());
        this.updatePropertyList = convertToList(tableMeta.getUpdateProperties());

        this.columnNameList = convertToList(this.tableMeta.getColumnNames());
        this.updateColumnNameList = convertToList(this.tableMeta.getUpdateColumnNames());

        if (CollectionUtils.isNotEmpty(propertyList)) {
            columnNameToPropertyNameMap = Maps.newHashMapWithExpectedSize(columnNameList.size());
            propertyNameToColumnNameMap = Maps.newHashMapWithExpectedSize(columnNameList.size());
            for (int i = 0; i < columnNameList.size(); i++) {
                columnNameToPropertyNameMap.put(columnNameList.get(i), propertyList.get(i));
                propertyNameToColumnNameMap.put(propertyList.get(i), columnNameList.get(i));
            }
        }

        strategy = tableMeta.getId() == null ? SEQUENCE : tableMeta.getId().strategy();

        super.init(tableMeta.getTableName(), tableMeta.getColumnNames(), tableMeta.getIdColumnName(), (Class<ID>) actualTypeArgument[1]);
        log.debug("tableName: {}, this.columnNames: {}", this.tableName, this.columnNames);
    }

    @Override
    protected Object resolveValue(Object value) {
        return SQLUtils.resolveValue(value, v -> {
            if (EntityDAOManager.isEntityClass((value.getClass()))) {
                // 实体对象
                return new Object[]{Boolean.TRUE, EntityDAOManager.getIdValue(value)};
            }

            return new Object[]{Boolean.FALSE};
        });
    }

    @Override
    protected boolean isUpdateFillColumnName(String fillColumnName) {
        return tableMeta.getUpdateColumnNames().contains(fillColumnName);
    }

    @Override
    protected ID generatorId(Object t) {
        ID id = strategy == SEQUENCE ? (ID) IdGenerator.getSequenceId() : getIdValue(t);
        if (strategy == ASSIGN && Objects.isNull(id)) {
            throw new RuntimeException("Strategy is assign, id cannot be null!");
        }

        return id;
    }

    protected ID getIdValue(Object o) {
        return (ID) EntityDAOManager.getPropertyValue(o, this.tableMeta.getIdPropertyName());
    }

    protected void setPropertyValue(Object t, String propertyName, Object propertyValue) {
        EntityDAOManager.setPropertyValue(t, propertyName, propertyValue);
    }

    private void setPropertyValue(Object t, Field field, Object propertyValue) {
        if (field.getType() == Long.class && propertyValue != null && EntityDAOManager.isEntityClass((propertyValue.getClass()))) {
            propertyValue = this.getIdValue(propertyValue);
        }

        setPropertyValue(t, field.getName(), propertyValue);
    }

    private Object[] instanceToParamsArray(T t, List<String> includePropertyList) {
        Object[] params = new Object[includePropertyList.size()];
        for (int i = 0; i < includePropertyList.size(); i++) {
            params[i] = resolveValue(EntityDAOManager.getPropertyValue(t, includePropertyList.get(i)));
        }
        return params;
    }

    protected Object[] instanceToParamsArray(T t) {
        Object[] params = new Object[this.columnNameList.size()];
        for (int i = 0; i < params.length; i++) {
            Object param = resolveValue(EntityDAOManager.getPropertyValue(t, columnNameToPropertyNameMap.get(columnNameList.get(i))));
            params[i] = param;
        }
        return params;
    }

    @Override
    protected int update(String tableName, Object t, String updateColumnNames, Object[] params, String conditionSQL) {
        if (hasVersionManagement) {
            Integer version = (Integer) getValue(t, tableMeta.getVersionProperty().getPropertyName());
            Assert.notNull(version, "version不能为空");

            String columnName = tableMeta.getVersionProperty().getColumnName();
            Object[] objects = mergeParam(params, version, columnName);
            params = (Object[]) objects[0];
            conditionSQL += " AND " + objects[1];
        }

        int count = super.update(tableName, t, updateColumnNames, params, conditionSQL);
        if (count > 0 && t != null && t.getClass() == getEntityClass()) {
            cascadeInsertOrUpdate((T) t, false);
            EntityDAOThreadLocalValue.removeAll();
        }

        return count;
    }

    private Map<ID, T> listToIdMap(List<T> list) {
        return list.stream().collect(Collectors.toMap(t -> (ID) EntityDAOManager.getPropertyValue(t, tableMeta.getIdPropertyName()), v -> v));
    }

    private int update(T t, String updateColumnNames, Object[] params, String conditionSQL) {
        return update(this.tableName, t, updateColumnNames, params, conditionSQL);
    }

    private Object[] resolveUpdateParamsAndId(T t, List<String> updateColumnNameList) {
        ID id = getIdValue(t);
        Assert.notNull(id, "id不能为空");

        Object[] params;
        params = instanceToParamsArray(t,
                updatePropertyList.stream().filter(property -> updateColumnNameList.contains(propertyNameToColumnNameMap.get(property))).collect(Collectors.toList()));

        return new Object[]{params, id};
    }

    private void cascadeSelect(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        // region Select
        for (TableMeta.SelectProperty selectProperty : tableMeta.getSelectAnnotationList()) {
            String joinValue = StringUtils.isBlank(selectProperty.getSelect().joinValue()) ? subTableRefColumnName : selectProperty.getSelect().joinValue();
            String referencePropertyName = StringUtils.isBlank(selectProperty.getSelect().referencePropertyName()) ? tableMeta.getIdPropertyName() : selectProperty.getSelect().referencePropertyName();

            String storeKey = selectProperty.getSelect().table() + ":" + joinValue;
            if (EntityDAOThreadLocalValue.remove(storeKey)) {
                continue;
            }
            EntityDAOThreadLocalValue.add(storeKey);

            String targetTable = selectProperty.getSelect().table();
            EntityDAO subTableEntityDAO = EntityDAOManager.baseDAOTableNameMap.get(targetTable);
            if (subTableEntityDAO == null) {
                throw new RuntimeException("Table [" + targetTable + "] lost DAOImpl");
            }

            Set<Object> refIds = list.stream().map(t -> getValue(t, referencePropertyName)).collect(Collectors.toSet());
            Map<Object, List<?>> subTableData = subTableEntityDAO.groupByColumnName(joinValue, refIds);

            for (T t : list) {
                Object data = subTableData.get(getValue(t, referencePropertyName));
                if (selectProperty.getSelect().oneToOne()) {
                    setPropertyValue(t, selectProperty.getField(), Objects.isNull(data) ? null : ((Collection) data).iterator().next());
                } else {
                    setPropertyValue(t, selectProperty.getField(), Objects.isNull(data) ? Collections.emptyList() : data);
                }

            }
        }
        // endregion

        //region OneToMany
        for (TableMeta.OneToManyProperty oneToManyProperty : tableMeta.getOneToManyAnnotationList()) {
            String storeKey = oneToManyProperty.getOneToMany().subTable() + ":" + oneToManyProperty.getOneToMany().joinValue();
            if (EntityDAOThreadLocalValue.remove(storeKey)) {
                continue;
            }
            EntityDAOThreadLocalValue.add(storeKey);

            String targetTable = oneToManyProperty.getOneToMany().subTable();
            EntityDAO subTableEntityDAO = EntityDAOManager.baseDAOTableNameMap.get(targetTable);
            if (subTableEntityDAO == null) {
                throw new RuntimeException("Table [" + targetTable + "] lost DAOImpl");
            }

            Set<ID> refIds = list.stream().map(t -> getIdValue(t)).collect(Collectors.toSet());

            // 级联删除
            if (CascadeSelectThreadLocalValue.getOneToManyConsumer() != null) {
                if (!oneToManyProperty.getOneToMany().cascadeDelete()) {
                    continue;
                }
            }

            Map<ID, List<? extends SimpleEntity>> subTableData = subTableEntityDAO.groupByColumnName(oneToManyProperty.getOneToMany().joinValue(), refIds);

            for (T t : list) {
                Object data = subTableData.get(getIdValue(t));
                if (oneToManyProperty.getOneToMany().oneToOne()) {
                    setPropertyValue(t, oneToManyProperty.getField(), Objects.isNull(data) ? null : ((Collection) data).iterator().next());
                } else {
                    setPropertyValue(t, oneToManyProperty.getField(), Objects.isNull(data) ? Collections.emptyList() : data);
                }
            }

            // 级别联删除
            if (CascadeSelectThreadLocalValue.getOneToManyConsumer() != null) {
                CascadeSelectThreadLocalValue.getOneToManyConsumer().accept(oneToManyProperty, subTableEntityDAO, refIds);
            }

        }
        //endregion

        //region ManyToOne
        for (TableMeta.ManyToOneProperty manyToOneProperty : tableMeta.getManyToOneAnnotationList()) {
            String refColumnName = StringUtils.isBlank(manyToOneProperty.getManyToOne().value()) ? propertyNameToColumnNameMap.get(manyToOneProperty.getField().getName()) : manyToOneProperty.getManyToOne().value();
            String storeKey = getTableName() + ":" + refColumnName;

            if (EntityDAOThreadLocalValue.remove(storeKey)) {
                continue;
            }
            EntityDAOThreadLocalValue.add(storeKey);

            String targetTable = manyToOneProperty.getManyToOne().parentTable();
            EntityDAO parentTableDAO = EntityDAOManager.baseDAOTableNameMap.get(targetTable);

            Set<ID> refIds = list.stream().map(t -> getIdValue(EntityDAOManager.getPropertyValue(t, columnNameToPropertyNameMap.get(refColumnName))))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(refIds)) {
                continue;
            }

            List<?> parentList = parentTableDAO.selectByIds(refIds);

            Map<ID, ?> parentIdMap = parentList.stream().collect(Collectors.toMap(this::getIdValue, v -> v));
            for (T t : list) {
                Object data = parentIdMap.get(getIdValue(EntityDAOManager.getPropertyValue(t, columnNameToPropertyNameMap.get(refColumnName))));
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

            if (EntityDAOThreadLocalValue.remove(storeKey1) || EntityDAOThreadLocalValue.remove(storeKey2)) {
                continue;
            }

            EntityDAOThreadLocalValue.add(storeKey1);
            EntityDAOThreadLocalValue.add(storeKey2);

            Set<ID> columnIds = list.stream().map(t -> getIdValue(t)).collect(Collectors.toSet());

            String thirdPartyTable = manyToManyProperty.getManyToMany().thirdPartyTable();
            String referenceTable = manyToManyProperty.getManyToMany().referenceTable();

            EntityDAO referenceTableDAO = EntityDAOManager.baseDAOTableNameMap.get(referenceTable);

            List<Map<String, Object>> refMapData = sharpService.query(String.format("SELECT %s, %s FROM %s WHERE %s IN (:value)",
                    columnDefinition, referenceColumnName, thirdPartyTable, columnDefinition
            ), Params.builder(1).pv("value", columnIds).build());

            Collection<Long> refIds = Lists.newArrayList();
            for (Map<String, Object> row : refMapData) {
                refIds.add((Long) row.get(referenceColumnName));
            }

            Map<Long, ?> referenceMap = refMapData.isEmpty() ? Collections.emptyMap() : referenceTableDAO.selectByIdsAsMap(refIds);

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

            if (CascadeSelectThreadLocalValue.getManyToManyConsumer() != null) {
                CascadeSelectThreadLocalValue.getManyToManyConsumer().accept(manyToManyProperty, columnIds);
            }
        }
        //endregion
    }

    private void cascadeInsertOrUpdate(List<T> instanceList, boolean insert) {
        for (T t : instanceList) {
            cascadeInsertOrUpdate(t, insert);
        }
        EntityDAOThreadLocalValue.removeAll();
    }

    /**
     * @param t
     * @param insert One的一段是否是新增
     */
    private void cascadeInsertOrUpdate(T t, boolean insert) {
        // OneToMany
        for (TableMeta.OneToManyProperty oneToManyProperty : tableMeta.getOneToManyAnnotationList()) {
            if (!oneToManyProperty.getOneToMany().cascadeInsertOrUpdate() && !oneToManyProperty.getOneToMany().cascadeInsert()) {
                continue;
            }

            String storeKey = oneToManyProperty.getOneToMany().subTable() + ":" + oneToManyProperty.getOneToMany().joinValue() + ":InsertOrUpdate";
            if (EntityDAOThreadLocalValue.remove(storeKey)) {
                continue;
            }
            EntityDAOThreadLocalValue.add(storeKey);

            String targetTable = oneToManyProperty.getOneToMany().subTable();
            EntityDAO subTableEntityDAO = EntityDAOManager.baseDAOTableNameMap.get(targetTable);

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

            if (!insert && oneToManyProperty.getOneToMany().cascadeDelete()) { // 级联删除
                if (CollectionUtils.isEmpty(subDataList)) {
                    // 删除所有
                    SQLUtils.delete(subTableEntityDAO.getTableName(), refColumnName, Arrays.asList(refId));
                    continue;
                }

                Set<ID> deletedIds = subDataList.stream().filter(d -> Objects.nonNull(getIdValue(d))).map(d -> getIdValue(d)).collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(deletedIds)) {
                    // 删除所有
                    SQLUtils.delete(subTableEntityDAO.getTableName(), refColumnName, Arrays.asList(refId));
                } else {
                    // 删除 除id之外的其他记录
                    SQLUtils.deleteNotIn(subTableEntityDAO.getTableName(), subTableEntityDAO.getIdColumnName(),
                            deletedIds, new Object[]{refId}, refColumnName + " = ?");
                }
            }

            if (CollectionUtils.isEmpty(subDataList)) {
                continue;
            }

            for (Object subData : subDataList) {
                setPropertyValue(subData, reverseField, t);
            }

            // 再插入或更新
            if (oneToManyProperty.getOneToMany().cascadeInsert()) {
                List<?> updateSubDataList = subDataList.stream().filter(e -> Objects.isNull(getIdValue(e))).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(updateSubDataList)) {
                    subTableEntityDAO.insert(updateSubDataList);
                }
            } else {
                subTableEntityDAO.insertOrUpdate(subDataList);
            }

        }

        // ManyToOne
        for (TableMeta.ManyToOneProperty manyToOneProperty : tableMeta.getManyToOneAnnotationList()) {
            if (!manyToOneProperty.getManyToOne().cascadeInsertOrUpdate() && !manyToOneProperty.getManyToOne().cascadeInsert()) {
                continue;
            }

            String refColumnName = manyToOneProperty.getManyToOne().value();
            String storeKey = getTableName() + ":" + refColumnName + ":InsertOrUpdate";

            if (EntityDAOThreadLocalValue.remove(storeKey)) {
                continue;
            }
            EntityDAOThreadLocalValue.add(storeKey);

            String targetTable = manyToOneProperty.getManyToOne().parentTable();
            EntityDAO parentTableEntityDAO = EntityDAOManager.baseDAOTableNameMap.get(targetTable);
            Object targetObject = getPropertyValue(t, manyToOneProperty.getField());
            if (Objects.nonNull(targetObject)) {
                ID refId = getIdValue(targetObject);
                if (manyToOneProperty.getManyToOne().cascadeInsert() && Objects.isNull(refId)) {
                    parentTableEntityDAO.insert(targetObject);
                } else {
                    parentTableEntityDAO.insertOrUpdate(targetObject);
                }

                if (refId == null) { // 添加外键
                    updateById(manyToOneProperty.getManyToOne().value(), new Object[]{getIdValue(targetObject)}, getIdValue(t));
                }
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

            List<ID> refIdsValue = null;
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

    private Object getValue(Object o, String propertyName) {
        return EntityDAOManager.getPropertyValue(o, propertyName);
    }

    private List<T> selectByIdsWithSpecifiedValue(Object ids) {
        Map<String, Object> params = Params.builder(1).pv("ids", ids).build();
        return selectByParams(params, this.idColumnName + " IN(:ids)");
    }

    @Override
    protected String columnAliasName(String columnName) {
        return columnNameToPropertyNameMap.get(columnName);
    }

    private Object getPropertyValue(Object t, Field field) {
        return EntityDAOManager.getPropertyValue(t, field.getName());
    }

    interface TriConsumer<K, V, S> {
        void accept(K k, V v, S s);
    }
}
