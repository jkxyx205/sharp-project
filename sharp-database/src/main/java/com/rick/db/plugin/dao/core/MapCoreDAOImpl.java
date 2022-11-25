package com.rick.db.plugin.dao.core;

import com.rick.db.service.support.Params;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 无实体DAO
 * @author Rick
 * @createdAt 2022-11-25 15:34:00
 */
public class MapCoreDAOImpl<ID> extends AbstractCoreDAO<ID> {

    public MapCoreDAOImpl(String idColumnName, String columnNames, String tableName) {
        super(idColumnName, columnNames, tableName);
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
}
