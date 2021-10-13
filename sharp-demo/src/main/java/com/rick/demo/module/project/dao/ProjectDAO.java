package com.rick.demo.module.project.dao;


import com.rick.db.plugin.dao.BaseDAOImpl;
import com.rick.demo.module.project.domain.entity.Project;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:50:00
 */
@Repository
public class ProjectDAO extends BaseDAOImpl<Project> {

    public ProjectDAO() {
        /**
         * 手动指定
         */
        super("t_project",
                "title,description,cover_url,owner_id,sex,address,status,list,id,created_by,created_at,updated_by,updated_at,is_deleted",
                "id");
    }
}
