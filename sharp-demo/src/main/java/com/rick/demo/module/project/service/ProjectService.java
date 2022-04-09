package com.rick.demo.module.project.service;

import com.rick.db.service.BaseServiceImpl;
import com.rick.demo.module.project.dao.ProjectDAO;
import com.rick.demo.module.project.domain.entity.Project;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author Rick
 * @createdAt 2021-10-22 17:14:00
 */
@Service
@Validated
public class ProjectService extends BaseServiceImpl<ProjectDAO, Project> {

    public ProjectService(ProjectDAO projectDAO) {
        super(projectDAO);
    }

//    @Validated
//    public void save(@Valid Project project) {
//
//    }
}
