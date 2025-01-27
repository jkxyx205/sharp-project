package com.rick.demo.module.school.entity;

import com.rick.db.dto.type.BaseEntityWithLongId;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
public class SchoolLicense extends BaseEntityWithLongId {

    @Column(comment = "证书编号")
    private String number;

    @Column(comment = "备注")
    private String remark;

    @OneToMany(subTable = "t_school", joinValue = "school_license_id", oneToOne = true, cascadeInsertOrUpdate = false)
    private School school;

}