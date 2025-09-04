package com.rick.demo.module.school.entity;

import com.rick.db.repository.Column;
import com.rick.db.repository.OneToMany;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:12:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_school_license", comment = "学校证书")
public class SchoolLicense extends BaseEntity<Long> {

    @Column(comment = "证书编号")
    private String number;

    @Column(comment = "备注")
    private String remark;

    @OneToMany(joinColumnId = "school_license_id", oneToOne = true, cascadeSave = false)
    private School school;

}