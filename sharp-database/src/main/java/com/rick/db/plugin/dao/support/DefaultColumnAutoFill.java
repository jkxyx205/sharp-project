package com.rick.db.plugin.dao.support;

import com.google.common.collect.Maps;
import com.rick.db.constant.SharpDbConstants;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-26 10:21:00
 */
public class DefaultColumnAutoFill implements ColumnAutoFill {

    @Override
    public Map<String, Object> insertFill(String idPropertyName, Object id) {
        Map<String, Object> fillMap = Maps.newHashMapWithExpectedSize(4);
        LocalDateTime now = LocalDateTime.now();
        fillMap.put(idPropertyName, id);
        fillMap.put(SharpDbConstants.CREATE_TIME_COLUMN_NAME, now);
        fillMap.put(SharpDbConstants.UPDATE_TIME_COLUMN_NAME, now);
        fillMap.put(SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, false);
        return fillMap;
    }

    @Override
    public Map<String, Object> updateFill() {
        Map<String, Object> fillMap = Maps.newHashMapWithExpectedSize(2);
        LocalDateTime now = LocalDateTime.now();
        fillMap.put(SharpDbConstants.UPDATE_TIME_COLUMN_NAME, now);
        fillMap.put(SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, false);
        return fillMap;
    }
}
