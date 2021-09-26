package com.rick.db.service.table;

import com.google.common.collect.Maps;
import com.rick.common.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-26 10:21:00
 */
public class DefaultTableColumnAutoFill implements TableColumnAutoFill {

    @Override
    public Map<String, Object> fill() {
        Map<String, Object> fillMap = Maps.newHashMapWithExpectedSize(4);
        LocalDateTime now = LocalDateTime.now();
        fillMap.put("id", IdGenerator.getSequenceId());
        fillMap.put("created_at", now);
        fillMap.put("updated_at", now);
        fillMap.put("is_deleted", false);
        return fillMap;
    }
}
