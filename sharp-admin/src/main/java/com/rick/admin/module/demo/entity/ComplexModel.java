package com.rick.admin.module.demo.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.rick.admin.module.common.entity.CodeDescription;
import com.rick.admin.module.demo.model.EmbedValue;
import com.rick.common.http.json.deserializer.EntityWithCodePropertyDeserializer;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Embedded;
import com.rick.db.plugin.dao.annotation.Sql;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import com.rick.meta.config.validator.DictValueCheck;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * @author Rick.Xu
 * @date 2024/8/18 04:09
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "t_complex_model", comment = "测试")
public class ComplexModel extends BaseEntity {

    @NotBlank
    String name;

    /**
     * 查询字典表 sys_dict 中获取数据
     * 通过注解 @Sql 获取 label 值
     */
    @Embedded(columnPrefix="material_type_")
    @JsonAlias("materialType")
    @JsonDeserialize(using = EntityWithCodePropertyDeserializer.class)
    @Sql(value = "select name code, label from sys_dict WHERE type = 'MATERIAL_TYPE'  AND name = :name", params="id@materialType.code", nullWhenParamsIsNull="code")
    @DictType(type = "MATERIAL_TYPE") // 可以省略 从@Sql 获取label
    @DictValueCheck(type = "MATERIAL_TYPE")
    DictValue materialType;

    /**
     * 查询字典表 sys_dict 中获取数据
     * 通过注解 @Sql 获取 label 值
     */
    @Embedded(columnPrefix="unit_")
    @JsonAlias("unit")
    @JsonDeserialize(using = EntityWithCodePropertyDeserializer.class)
    @DictType(type = "UNIT")
    @DictValueCheck(type = "UNIT")
    DictValue unit;

    /**
     * 用于 BaseCodeEntity 的关联，拥有 id ， 区别于DictValue
     */
//    @Embedded(columnPrefix="plant_")
//    @JsonAlias("plantCode")
//    @JsonDeserialize(using = EntityWithCodePropertyDeserializer.class)
//    @Sql(value = "SELECT id, code, description from core_plant WHERE id = :id", params="id@plant.id", nullWhenParamsIsNull="id")
//    private IdCodeValue plant;

    CodeDescription.CategoryEnum category;

    @Embedded(columnPrefix="category_")
    @JsonAlias("categoryDict")
    @JsonDeserialize(using = EntityWithCodePropertyDeserializer.class)
    @DictType(type = "CategoryEnum")
    @DictValueCheck(type = "CategoryEnum")
    DictValue categoryDict;

    @Transient
    EmbedValue embedValue;
}