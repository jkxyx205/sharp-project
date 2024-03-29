package com.rick.db.plugin.dao.core;

import com.rick.common.util.ClassUtils;
import com.rick.common.util.IdGenerator;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.plugin.DatabaseMetaData;
import com.rick.db.plugin.dao.support.ColumnAutoFill;
import com.rick.db.plugin.dao.support.ConditionAdvice;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;

/**
 * 无实体 Map DAO
 * @author Rick
 * @createdAt 2022-11-25 15:34:00
 */
public class MapDAOImpl<ID> extends AbstractCoreDAO<ID> implements MapDAO<ID> {

    public MapDAOImpl(String tableName) {
        this(tableName, DatabaseMetaData.tableColumnMap.get(tableName), DatabaseMetaData.tablePrimaryKeyMap.get(tableName));
    }

    /**
     * @see #of
     * @param tableName 表名
     * @param idClass 注解字段的class
     */
    public MapDAOImpl(String tableName, Class<ID> idClass) {
        this(tableName, StringUtils.join(DatabaseMetaData.tableColumnMap.get(tableName), ", "), DatabaseMetaData.tablePrimaryKeyMap.get(tableName), idClass);
    }

    public MapDAOImpl(String tableName, List<String> columnNameList, String idColumnName) {
        this(tableName, StringUtils.join(columnNameList, ", "), idColumnName);
    }

    public MapDAOImpl(String tableName, String columnNames, String idColumnName) {
        this(tableName, columnNames, idColumnName, null);
    }

    private MapDAOImpl(String tableName, String columnNames, String idColumnName, Class<ID> idClass) {
        super.init(tableName, columnNames, idColumnName, idClass == null ? (Class<ID>) ClassUtils.getClassGenericsTypes(this.getClass())[0] : idClass);
    }

    public static <ID> MapDAO<ID> of(ApplicationContext applicationContext, String tableName, Class<ID> idClass) {
        MapDAOImpl<ID> mapDAO = new MapDAOImpl<>(tableName, idClass);
        mapDAO.sharpService = applicationContext.getBean(SharpService.class);
        mapDAO.conditionAdvice = applicationContext.getBean(ConditionAdvice.class);
        mapDAO.columnAutoFill = applicationContext.getBean(ColumnAutoFill.class);
        mapDAO.validatorHelper = applicationContext.getBean(ValidatorHelper.class);
        mapDAO.sharpDatabaseProperties = applicationContext.getBean(SharpDatabaseProperties.class);
        return mapDAO;
    }

    @Override
    public Optional<Map<String, Object>> selectById(ID id) {
        Assert.notNull(id, "id cannot be null");
        Map<String, Object> params = Params.builder(1)
                .pv("id", id)
                .build();

        List<Map<String, Object>> list = selectByParams(params, getSelectColumnNames(), getIdColumnName() + " = :id");
        return expectedAsOptional(list);
    }

    @Override
    public Map<ID, Map<String, Object>> selectByIdsAsMap(String ids) {
        return null;
    }

    @Override
    public Map<ID, Map<String, Object>> selectByIdsAsMap(ID... ids) {
        return null;
    }

    @Override
    public Map<ID, Map<String, Object>> selectByIdsAsMap(Collection<?> ids) {
        return null;
    }

    @Override
    public Map<ID, Map<String, Object>> selectByIdsAsMap(Map<String, ?> params, String conditionSQL) {
        return null;
    }

    @Override
    public List<Map<String, Object>> selectByIds(String ids) {
        return null;
    }

    @Override
    public List<Map<String, Object>> selectByIds(ID... ids) {
        return null;
    }

    @Override
    public List<Map<String, Object>> selectByIds(Collection<?> ids) {
        return null;
    }

    @Override
    public Optional<ID> selectIdByParams(Map<String, Object> example) {
        return Optional.empty();
    }

    @Override
    public Optional<ID> selectIdByParams(Map<String, Object> example, String conditionSQL) {
        return Optional.empty();
    }

    @Override
    public <S> Optional<S> selectSingleValueById(ID id, String columnName, Class<S> clazz) {
        return Optional.empty();
    }

    @Override
    public List<Map<String, Object>> selectByParams(String queryString) {
        return null;
    }

    @Override
    public List<Map<String, Object>> selectByParams(String queryString, String conditionSQL) {
        return null;
    }

    @Override
    public List<Map<String, Object>> selectAll() {
        return null;
    }

    @Override
    public List<Map<String, Object>> selectByParams(Map<String, ?> params) {
        return null;
    }


    @Override
    public void selectAsSubMapTable(List<Map<String, Object>> data, String refColumnName, String valueKey, String property) {

    }

    @Override
    public Map<ID, List<Map<String, Object>>> groupByColumnName(String refColumnName, Collection<?> refValues) {
        return null;
    }

    @Override
    public <M> Map<ID, List<M>> groupByColumnName(String refColumnName, Collection<?> refValues, String columnNames, Function<Map<String, Object>, M> function) {
        return null;
    }

    @Override
    protected ID generatorId(Object t) {
        if (Objects.isNull(t)) {
            return (ID) IdGenerator.getSequenceId();
        }

        ID id = (ID) ((Map)t).get(getIdColumnName());
        return id == null ? (ID) IdGenerator.getSequenceId() :id;
    }

    @Override
    public List<Map<String, Object>> selectByParams(Map<String, ?> params, String columnNames) {
        return selectByParams(params, columnNames, null, null, null);
    }

    @Override
    public List<Map<String, Object>> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL) {
        return selectByParams(params, columnNames, conditionSQL, null, null);
    }


}
