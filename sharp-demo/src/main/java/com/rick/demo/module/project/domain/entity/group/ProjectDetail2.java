package com.rick.demo.module.project.domain.entity.group;

import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

;

/**
 * @author rick
 */
@SuperBuilder
@Getter
@Setter
@Table("t_project_detail2")
@NoArgsConstructor
public class ProjectDetail2 extends BaseEntity<Long> {

    @NotBlank(message = "项目名称不能为空")
    private String title;

    private Long groupId;

    private Long projectId;

}
