package com.rick.demo.module.project.domain.entity;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.TableName;
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
@TableName("t_project_detail1")
@NoArgsConstructor
public class ProjectDetail extends BasePureEntity {

    @NotBlank(message = "项目名称不能为空")
    private String title;

    private Long groupId;

//    private Long projectId;

    @ManyToOne(value = "project_id", parentTable = "t_project1")
    private Project project;

}
