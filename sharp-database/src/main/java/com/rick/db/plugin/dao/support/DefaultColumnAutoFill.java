package com.rick.db.plugin.dao.support;

import com.google.common.collect.Maps;
import com.rick.db.constant.SharpDbConstants;

import java.time.Instant;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-26 10:21:00
 */
public class DefaultColumnAutoFill implements ColumnAutoFill {

    @Override
    public Map<String, Object> insertFill(String idPropertyName, Object id) {
        Map<String, Object> fillMap = Maps.newHashMapWithExpectedSize(4);
        Instant now = Instant.now();
        fillMap.put(idPropertyName, id);
        fillMap.put(SharpDbConstants.CREATED_AT_COLUMN_NAME, now);
        fillMap.put(SharpDbConstants.UPDATED_AT_COLUMN_NAME, now);
        fillMap.put(SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, false);
        return fillMap;
    }

    @Override
    public Map<String, Object> updateFill() {
        Map<String, Object> fillMap = Maps.newHashMapWithExpectedSize(2);
        Instant now = Instant.now();
        fillMap.put(SharpDbConstants.UPDATED_AT_COLUMN_NAME, now);
        fillMap.put(SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, false);
        return fillMap;
    }
}
