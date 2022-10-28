package com.rick.demo.module.project.dao;


import com.rick.db.plugin.dao.core.BaseDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2021-09-27 11:50:00
 */
@Repository
public class ProjectMapDAO extends BaseDAOImpl {

    public ProjectMapDAO() {
        super("t_project3",
                "title,description,cover_url,owner_id,sex,address,status,list,created_by,created_at,updated_by,updated_at,is_deleted,id",
                "id");
    }
}
