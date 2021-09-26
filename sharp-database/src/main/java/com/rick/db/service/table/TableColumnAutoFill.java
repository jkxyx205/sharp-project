package com.rick.db.service.table;

import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-26 10:21:00
 */
public interface TableColumnAutoFill {
    /**
     * 将填充的字段返回
     * @return
     */
    Map<String, Object> fill();
}
