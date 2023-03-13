package com.rick.demo.module.project.dao;

import com.rick.db.plugin.dao.core.MapDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2022-11-25 15:42:00
 */
@Repository
public class ProjectMapByTableNameDAO extends MapDAOImpl<Long> {

    public ProjectMapByTableNameDAO() {
        super("t_project3");
    }
}
