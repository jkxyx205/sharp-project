package com.rick.demo.module.project.domain.entity;

import com.rick.db.repository.ManyToOne;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * @author rick
 */
@SuperBuilder
@Getter
@Setter
@Table("t_project_detail1")
@NoArgsConstructor
public class ProjectDetail extends BaseEntity<Long> {

    @NotBlank(message = "项目名称不能为空")
    private String title;

    private Long groupId;

//    private Long projectId;

    @ManyToOne(value = "project_id")
    private Project project;

}
