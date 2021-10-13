package com.rick.db.plugin.dao;

import com.google.common.collect.Maps;
import com.rick.common.util.IdGenerator;

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
        fillMap.put("id", IdGenerator.getSequenceId());
        fillMap.put("created_at", now);
        fillMap.put("updated_at", now);
        fillMap.put("is_deleted", false);
        return fillMap;
    }

    @Override
    public Map<String, Object> updateFill() {
        Map<String, Object> fillMap = Maps.newHashMapWithExpectedSize(2);
        Instant now = Instant.now();
        fillMap.put("updated_at", now);
        return fillMap;
    }
}
