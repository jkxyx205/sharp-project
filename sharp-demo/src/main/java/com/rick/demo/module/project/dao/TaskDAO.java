package com.rick.demo.module.project.dao;

import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.demo.module.project.domain.entity.group.Task;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2022-03-22 12:25:00
 */

@Repository
public class TaskDAO extends EntityDAOImpl<Task, Long> {

}