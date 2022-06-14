package com.rick.db.model;

import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
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
@Table(value = "t_project", subTables = {"t_project_detail"})
@NoArgsConstructor
public class Project extends BaseEntity {


    private String title;

    private String description;

    private String coverUrl;

    private Long ownerId;

    @OneToMany(subTable = "t_project_detail", reversePropertyName = "project", joinValue = "project_id")
    private List<ProjectDetail> projectDetailList;

}
