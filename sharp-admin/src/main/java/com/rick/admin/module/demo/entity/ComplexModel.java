package com.rick.admin.module.demo.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.rick.admin.module.common.entity.CodeDescription;
import com.rick.admin.module.demo.model.EmbeddedValue;
import com.rick.common.http.json.deserializer.EntityWithCodePropertyDeserializer;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Embedded;
import com.rick.db.plugin.dao.annotation.Sql;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.meta.config.validator.DictValueCheck;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * DictUtils.fillDictLabel(this) 手动填充label
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
    /**
     * 使用注解 @JsonFormat(shape = JsonFormat.Shape.OBJECT) 可以不使用 @JsonValue
     */
    CodeDescription.CategoryEnum category;

    @Column(comment = "状态")
    WorkStatusEnum workStatus;

    @Embedded(columnPrefix="category_")
    @JsonAlias("categoryDict")
    @JsonDeserialize(using = EntityWithCodePropertyDeserializer.class)
    @DictType(type = "CategoryEnum")
    @DictValueCheck(type = "CategoryEnum")
    DictValue categoryDict;

    @Column(comment = "年龄", nullable = false)
    private Integer age;

    @Column(comment = "出生时间")
    private LocalDate birthday;

    /**
     * 必须使用 @JsonValue， 如果使用 @JsonFormat 数据写入会有问题
     */
    @Column(comment = "分类", value = "category_list", nullable = false)
    private List<CodeDescription.CategoryEnum> categoryList;

    @Column(comment = "字典分类", value = "category_dict_list", nullable = false)
    @DictType(type = "CategoryEnum")
    private List<DictValue> categoryDictList;

    @Column(comment = "婚否", nullable = false)
    private Boolean marriage;

    @Column(comment = "附件", columnDefinition = "text", value = "attachment", nullable = false)
    private List<Map<String, Object>> attachmentList;

    @Column(comment = "学习经历", columnDefinition = "text", value = "school_experience")
    private List<List<String>> schoolExperienceList;

    @Column(columnDefinition = "text", value = "map", nullable = false)
    private Map<String, Object> map;

    EmbeddedValue embeddedValue;

    @AllArgsConstructor
    @Getter
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum WorkStatusEnum {
        UNFINISHED(0, "未完成"),
        FINISHED(1, "已完成");
        private static final Map<Integer, WorkStatusEnum> codeMap = new HashMap<>();

        static {
            for (WorkStatusEnum e : values()) {
                codeMap.put(e.code, e);
            }
        }

        private final int code;
        private final String label;

        public int getCode() {
            return this.code;
        }

        /**
         * code枚举 必须重写toString()
         *
         * @return
         */
        @Override
        public String toString() {
            return String.valueOf(code);
        }

        public static WorkStatusEnum valueOfCode(int code) {
            return codeMap.get(code);
        }
    }
}