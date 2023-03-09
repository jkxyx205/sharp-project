package com.rick.db.plugin.dao.core;

import com.rick.common.util.ClassUtils;
import com.rick.common.util.IdGenerator;
import com.rick.db.service.support.Params;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 无实体DAO
 * @author Rick
 * @createdAt 2022-11-25 15:34:00
 */
public class MapCoreDAOImpl<ID> extends AbstractCoreDAO<ID> {

    public MapCoreDAOImpl(String tableName, String tableComment, String columnNames, String idColumnName) {
        super.init(tableName, tableComment, columnNames, idColumnName, (Class<ID>) ClassUtils.getClassGenericsTypes(this.getClass())[0]);
    }

    public Optional<Map> selectById(ID id) {
        Assert.notNull(id, "id cannot be null");
        Map<String, Object> params = Params.builder(1)
                .pv("id", id)
                .build();

        List<Map> list = selectByParams(params, getFullColumnNames(), getIdColumnName() + " = :id", Map.class);
        return expectedAsOptional(list);
    }

    public List<Map> selectByParams(Map<String, ?> params, String conditionSQL) {
        return selectByParams(params, getFullColumnNames(), conditionSQL);
    }

    public List<Map> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL) {
        return selectByParams(params, columnNames, conditionSQL, Map.class);
    }

    @Override
    protected ID getIdValue(Object o) {
        return (ID) ((Map)o).get(getIdColumnName());
    }

    @Override
    protected ID generatorId(Object t) {
        if (Objects.isNull(t)) {
            return (ID) IdGenerator.getSequenceId();
        }

        ID id = getIdValue(t);
        return id == null ? (ID) IdGenerator.getSequenceId() :id;
    }

    @Override
    protected void setColumnValue(Object o, String columnName, Object value) {
        ((Map)o).put(columnName, value);
    }

}
