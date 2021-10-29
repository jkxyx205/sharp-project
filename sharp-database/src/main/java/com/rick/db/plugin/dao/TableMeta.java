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

    /**
     * 类名，约定优于配置，它的子表的外键用"name_id"表示
     */
    private String name;

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

    /**
     * 子表
     */
    private String[] subTables;
}
