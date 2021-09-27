package com.rick.db.plugin.dao;

import lombok.Value;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:40:00
 */
@Value
public class TableMeta {

    private String tableName;

    /**
     * 列表
     */
    private String columnNames;

    /**
     * 属性名
     */
    private String properties;
}
