package com.rick.db.model;

import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author rick
 */
@SuperBuilder
@Getter
@Setter
@Table("t_project_detail")
@NoArgsConstructor
public class ProjectDetail extends BaseEntity {

    private String title;

//    private Long projectId;

    @ManyToOne(value = "project_id", parentTable = "t_project")
    private Project project;

}
