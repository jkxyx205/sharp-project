package com.rick.db.plugin.dao;

import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-26 10:21:00
 */
public interface ColumnAutoFill {
    /**
     * 将填充的字段返回
     * @return
     */
    Map<String, Object> insertFill();

    /**
     * 将填充的字段返回
     * @return
     */
    Map<String, Object> updateFill();
}