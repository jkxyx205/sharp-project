package com.rick.demo.module.school.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.rick.common.http.json.deserializer.EntityWithLongIdPropertyDeserializer;
import com.rick.db.repository.*;
import com.rick.db.repository.model.BaseEntity;
import com.rick.demo.module.project.domain.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rick
 * @createdAt 2022-05-01 13:52:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_school", comment = "学校")
public class School extends BaseEntity<Long> {

    @Column(comment = "学校名称")
    private String name;

    @Column(comment = "建校日期")
    private LocalDate buildDate;

    @Column(comment = "学校性质 PRIVATE：私立；PUBLIC：公立")
    private TypeEnum type;

    @Column(comment = "每年经费预算")
    private BigDecimal budget;

    @Column(comment = "专业数")
    private Integer score;

    @Column(comment = "学校地址")
    private Address address;

    @Column(comment = "其他信息")
    private Map<String, Object> additionalInfo;

    @Column(comment = "历届领导信息")
    private List<Map<String, Object>> leadershipInformationList;

    @Column(comment = "历届获奖信息")
    private Set<Map<String, Object>> awardsSet;

    @Column(comment = "历届学校评分")
    private List<Float> scoreList;

    @Column(comment = "学校评价")
    @ToStringValue
    private Evaluate evaluate;

    /**
     * 「学校」和「证书」1对1外键
     *  1 <==> 1
     */
    @JsonDeserialize(using = EntityWithLongIdPropertyDeserializer.class)
//    @JsonAlias("schoolLicenseId")
    @ManyToOne(value = "school_license_id", comment = "证书信息")
    private SchoolLicense schoolLicense;

    /**
     * 「学校」和「学生」1对多
     *  1 <==> N
     */
    @JsonDeserialize(using = EntityWithLongIdPropertyDeserializer.class)
    @JsonAlias("studentIds")
    @OneToMany(joinColumnId = "school_id", mappedBy = "school")
    private List<Student> studentList;

    /**
     * 「学校」和「老师」多对多
     *  N <==> N
     */
    @JsonDeserialize(using = EntityWithLongIdPropertyDeserializer.class)
    @JsonAlias("teacherIds")
    @ManyToMany(tableName = "t_school_teacher_related", joinColumnId = "school_id", inverseJoinColumnId = "teacher_id")
    private List<Teacher> teacherList;

    @AllArgsConstructor
    @Getter
    public enum TypeEnum {
        PRIVATE("私立"),
        PUBLIC("公立");

        @JsonValue
        public String getCode() {
            return this.name();
        }
        private final String label;
        public static TypeEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }
}