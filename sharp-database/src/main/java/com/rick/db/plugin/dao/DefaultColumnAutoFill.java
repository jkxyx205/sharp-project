package com.rick.db.plugin.dao;

import com.google.common.collect.Maps;
import com.rick.common.util.IdGenerator;
import com.rick.db.constant.EntityConstants;

import java.time.Instant;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-26 10:21:00
 */
public class DefaultColumnAutoFill implements ColumnAutoFill {

    @Override
    public Map<String, Object> insertFill() {
        Map<String, Object> fillMap = Maps.newHashMapWithExpectedSize(4);
        Instant now = Instant.now();
        fillMap.put(EntityConstants.ID_COLUMN_NAME, IdGenerator.getSequenceId());
        fillMap.put(EntityConstants.CREATED_AT_COLUMN_NAME, now);
        fillMap.put(EntityConstants.UPDATED_AT_COLUMN_NAME, now);
        fillMap.put(EntityConstants.LOGIC_DELETE_COLUMN_NAME, false);
        return fillMap;
    }

    @Override
    public Map<String, Object> updateFill() {
        Map<String, Object> fillMap = Maps.newHashMapWithExpectedSize(2);
        Instant now = Instant.now();
        fillMap.put(EntityConstants.UPDATED_AT_COLUMN_NAME, now);
        fillMap.put(EntityConstants.LOGIC_DELETE_COLUMN_NAME, false);
        return fillMap;
    }
}
