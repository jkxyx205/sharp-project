package com.rick.demo.module.project.dao;


import com.rick.db.plugin.dao.core.BaseDAOImpl;
import com.rick.demo.module.project.domain.entity.group.Project;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:50:00
 */
@Repository
public class ProjectDAO2 extends BaseDAOImpl<Project, Long> {

    public ProjectDAO2() {
        /**
         * 手动指定
         */
        super("t_project2",
                "title,description,cover_url,owner_id,sex,address,status,list,phone_number,created_by,created_at,updated_by,updated_at,is_deleted, id",
                "id");
    }
}
