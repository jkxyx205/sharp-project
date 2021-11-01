package com.rick.db.model;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author rick
 */
@SuperBuilder
@Getter
@Setter
@TableName(value = "t_project", subTables = {"t_project_detail"})
@NoArgsConstructor
public class Project extends BasePureEntity {


    private String title;

    private String description;

    private String coverUrl;

    private Long ownerId;


    @OneToMany(subTable = "t_project_detail")
    private List<ProjectDetail> projectDetailList;

}