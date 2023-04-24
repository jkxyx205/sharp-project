package com.rick.demo.module.project.dao;

import com.rick.db.plugin.dao.core.MapDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2022-11-25 15:42:00
 */
@Repository
public class ProjectMapDAO extends MapDAOImpl<Long> {

    public ProjectMapDAO() {
        super("t_project3",
                "title,description,cover_url,owner_id,sex,address,status,list,create_id,create_time,update_id,update_time,is_deleted,phone_number,id",
                "id");
    }
}
