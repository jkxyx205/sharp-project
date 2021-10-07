package com.rick.db.plugin.dao;

import lombok.ToString;
import lombok.Value;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:40:00
 */
@Value
@ToString
public class TableMeta {

    private String tableName;

    /**
     * 所有列
     */
    private String columnNames;

    /**
     * 所有属性名
     */
    private String properties;

    /**
     * 可更新列表
     */
    private String updateColumnNames;

    /**
     * 可更新属性名
     */
    private String updateProperties;

    /**
     * id属性名
     */
    private String idColumnName;
}
