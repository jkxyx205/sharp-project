package com.rick.db.plugin.dao.support;

import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-26 10:21:00
 */
public interface ColumnAutoFill<ID> {
    /**
     * 将填充的字段返回
     * @return
     */
    Map<String, Object> insertFill(String idPropertyName, ID id);

    /**
     * 将填充的字段返回
     * @return
     */
    Map<String, Object> updateFill();
}
