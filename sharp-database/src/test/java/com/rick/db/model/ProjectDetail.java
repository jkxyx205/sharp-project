package com.rick.db.model;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.TableName;
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
@TableName("t_project_detail")
@NoArgsConstructor
public class ProjectDetail extends BasePureEntity {

    private String title;

//    private Long projectId;

    @ManyToOne(value = "project_id", parentTable = "t_project")
    private Project project;

}
