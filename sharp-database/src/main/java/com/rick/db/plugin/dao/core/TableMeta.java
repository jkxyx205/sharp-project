package com.rick.db.plugin.dao.core;

import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.OneToMany;
import lombok.ToString;
import lombok.Value;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:40:00
 */
@Value
@ToString
class TableMeta {

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
    private Set<String> subTables;

    private Map<String, OneToManyProperty> oneToManyAnnotationMap;

    private Map<String, ManyToOneProperty> manyToOneAnnotationMap;

    @Value
    public static class OneToManyProperty {

        private OneToMany oneToMany;

        private Field field;
    }

    @Value
    public static class ManyToOneProperty {

        private ManyToOne oneToMany;

        private Field field;
    }
}