package com.rick.demo.module.project.dao;

import com.rick.db.repository.EntityDAOImpl;
import com.rick.demo.module.project.domain.entity.Worker;
import org.springframework.stereotype.Repository;



/**
 * @author Rick
 * @createdAt 2022-06-15 00:55:00
 */

@Repository
public class WorkerDAO extends EntityDAOImpl<Worker, Long> {

}