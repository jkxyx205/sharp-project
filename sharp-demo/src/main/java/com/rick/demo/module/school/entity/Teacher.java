package com.rick.demo.module.school.entity;

import com.rick.db.repository.Column;
import com.rick.db.repository.ManyToMany;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

;

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
public class Teacher extends BaseEntity<Long> {

    @Column(comment = "姓名")
    private String name;

    @Column(comment = "年龄")
    private Integer age;

    @ManyToMany(tableName = "t_school_teacher_related", joinColumnId = "teacher_id", inverseJoinColumnId = "school_id")
    private List<School> schoolList;

}