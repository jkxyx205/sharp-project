package com.rick.demo.module.project.service;

import com.rick.db.service.BaseServiceImpl;
import com.rick.demo.module.project.dao.ProjectDAO;
import com.rick.demo.module.project.domain.entity.Project;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-10-22 17:14:00
 */
@Service
@Validated
public class ProjectService extends BaseServiceImpl<ProjectDAO, Project, Long> {

    public ProjectService(ProjectDAO projectDAO) {
        super(projectDAO);
    }

//    @Validated
//    public void save(@Valid Project project) {
//
//    }

    public void checkId(Long id) {
        baseDAO.checkId(id);
    }

    public void checkId(Long id, Map<String, Object> conditionParams, String condition) {
        baseDAO.checkId(id, conditionParams, condition);
    }

    public void checkIds(Collection<Long> ids) {
        baseDAO.checkIds(ids);
    }

    public void checkIds(Collection<Long> ids, Map<String, Object> conditionParams, String condition) {
        baseDAO.checkIds(ids, conditionParams, condition);
    }
}
