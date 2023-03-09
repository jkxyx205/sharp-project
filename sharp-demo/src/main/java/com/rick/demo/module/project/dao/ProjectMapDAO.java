package com.rick.demo.module.project.dao;

import com.rick.db.plugin.dao.core.MapCoreDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2022-11-25 15:42:00
 */
@Repository
public class ProjectMapDAO extends MapCoreDAOImpl<Long> {

    public ProjectMapDAO() {
        super("t_project3",
                "测试表",
                "title,description,cover_url,owner_id,sex,address,status,list,created_by,created_at,updated_by,updated_at,is_deleted,phone_number,id",
                "id");
    }
}
