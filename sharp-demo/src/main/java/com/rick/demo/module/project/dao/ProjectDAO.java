package com.rick.demo.module.project.dao;


import com.rick.db.repository.EntityDAOImpl;
import com.rick.demo.module.project.domain.entity.Project;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:50:00
 */
@Repository
public class ProjectDAO extends EntityDAOImpl<Project, Long> {

}
