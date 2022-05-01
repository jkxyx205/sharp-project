package com.rick.demo.module.school.entity;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Rick
 * @createdAt 2022-05-01 13:52:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_school_teacher", comment = "教师")
public class Teacher extends BasePureEntity {

    @Column(comment = "姓名")
    private String name;

    @Column(comment = "年龄")
    private Integer age;

    @ManyToMany(thirdPartyTable = "t_school_teacher_related", columnDefinition = "teacher_id", referenceTable = "t_school", referenceColumnName = "school_id")
    private List<School> schoolList;

}