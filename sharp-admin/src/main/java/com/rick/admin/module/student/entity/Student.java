package com.rick.admin.module.student.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.rick.admin.module.common.entity.CodeDescription;
import com.rick.common.http.json.deserializer.BooleanPropertyDeserializer;
import com.rick.common.http.json.deserializer.NamePropertyDeserializer;
import com.rick.db.repository.Column;
import com.rick.db.repository.Embedded;
import com.rick.db.repository.Select;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseCodeEntity;
import com.rick.db.repository.model.IdCodeValue;
import com.rick.fileupload.client.support.Document;
import com.rick.meta.config.validator.DictValueCheck;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2024/8/23 16:28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "t_student", comment = "学生表")
public class Student extends BaseCodeEntity<Long> {


//    String name; 可以直接属性，其他默认
    // columnDefinition

    @NotBlank
//    @Column(nullable = false, comment = "姓名")
    // 使用了 columnDefinition ，数据库中 nullable、comment 失效，但可以指定 comment， 生成默认 StudentTest 的 report 作为name
    // 最佳实践： comment 两边都必须写
    @Column(columnDefinition = "varchar(16) not null comment '姓名'")
    String name;

    @NotNull(message = "性别必须选择")
    GenderEnum gender;

    /**
     * 前端 input 手动添加 pattern="^(.+)@(\S+)$"
     */
    @Pattern(regexp = "^(.+)@(\\S+)$", message = "邮箱格式错误")
    String email;

    @Column(value = "birthday", comment = "出生日期")
    LocalDate birthday;

    @Column(comment = "年龄")
    Integer age;

    @Column(value = "is_marriage", comment = "婚否")
    @JsonDeserialize(using = BooleanPropertyDeserializer.class)
    Boolean marriage = false; //默认值false

    @Embedded(columnPrefix="unit_")
    @JsonAlias("unitCode")
//    @JsonDeserialize(using = EntityWithCodePropertyDeserializer.class)
    //    看看更强大的 NamePropertyDeserializer，支持集合
    @JsonDeserialize(using = NamePropertyDeserializer.class)
    @DictType(type = "UNIT")
    @DictValueCheck(type = "UNIT")
    DictValue unit;

    List<Document> attachments;

    Document avatar;

    @NotEmpty(message = "兴趣爱好必须选择一个")
    @Valid
    List<HobbyEnum> hobbyList;

    @Column(comment = "物料类型", value = "material_type")
    @DictType(type = "MATERIAL")
    List<DictValue> materialTypeList;

    @Column(comment = "分类", value = "category")
    @DictType(type = "CategoryEnum")
    CodeDescription.CategoryEnum category;

    @Column(value = "is_available", comment = "是否可用")
    @JsonDeserialize(using = BooleanPropertyDeserializer.class)
    Boolean available = false; // 默认值false

    @Column(comment = "简介")
    String remark;


    // 数据库增加字段 user_id, 前端提交数据 operatorId = 1
//    @Embedded(columnPrefix="user_")
//    @JsonAlias("operatorId")
//    @JsonDeserialize(using = EntityWithLongIdPropertyDeserializer.class)
//    @Sql(value = "select id, code, name as description from sys_user where id = :id ", params="id@operator.id", nullWhenParamsIsNull="id")
//    IdValue operator;

    // 数据库增加字段 user_id user_code 前端提交数据 operator { id: "1", code: "ADMIN"}
    @Embedded(columnPrefix="user_")
    @Select(value = "select id, code, name as description from sys_user where id = :id ", params="id@operator.id", nullWhenParamsIsNull="id")
    IdCodeValue operator;

//    /**
//     * 可以前台获取 this.form.unitCode = data.unit ? data.unit.code : ''
//     * @return
//     */
//    public String getUnitCode() {
//        return Objects.nonNull(unit) ? unit.getCode() : "";
//    }

    @AllArgsConstructor
    @Getter
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum GenderEnum {
        M("男"),
        F("女");

        public String getCode() {
            return this.name();
        }

        public String getLabel() {
            return label;
        }

        private final String label;

        public static GenderEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }

    @AllArgsConstructor
    @Getter
    public enum HobbyEnum {
        BASKETBALL("篮球"),
        FOOTBALL("足球");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        public String getLabel() {
            return label;
        }

        private final String label;

        public static HobbyEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }
}