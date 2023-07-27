package com.rick.db.plugin.dao.core;

import com.rick.db.plugin.dao.annotation.*;
import lombok.ToString;
import lombok.Value;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:40:00
 */
@Value
@ToString
public class TableMeta {

    private Table table;

    private Id id;

    private Field idField;

    private VersionProperty versionProperty;

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
     * id字段名
     */
    private String idColumnName;

    /**
     * id属性名
     */
    private String idPropertyName;

    private List<EmbeddedProperty> embeddedPropertyList;

    private List<SelectProperty> selectAnnotationList;

    private List<SqlProperty> SqlAnnotationList;

    private List<OneToManyProperty> oneToManyAnnotationList;

    private List<ManyToOneProperty> manyToOneAnnotationList;

    private List<ManyToManyProperty> manyToManyAnnotationList;

    private Map<String, Field> columnNameFieldMap;

    private Map<String, Field> fieldMap;

    private Map<String, Column> columnNameMap;

    @Value
    public static class OneToManyProperty {

        private OneToMany oneToMany;

        private Field field;

        private Class subEntityClass;

    }

    @Value
    public static class EmbeddedProperty {

        private Embedded embedded;

        private Field field;
    }

    @Value
    public static class SelectProperty {

        private Select select;

        private Field field;

        private Class subEntityClass;
    }

    @Value
    public static class SqlProperty {

        private Sql sql;

        private Field field;

        private Class<?> targetClass;

    }

    @Value
    public static class ManyToOneProperty {

        private ManyToOne manyToOne;

        private Field field;
    }

    @Value
    public static class ManyToManyProperty {

        private ManyToMany manyToMany;

        private Field field;
    }

    @Value
    public static class VersionProperty {

        private String columnName;

        private String propertyName;
    }
}
