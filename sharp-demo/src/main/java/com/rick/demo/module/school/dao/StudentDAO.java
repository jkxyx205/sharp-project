package com.rick.demo.module.school.dao;

import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.demo.module.school.entity.Student;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:32:00
 */
@Repository
public class StudentDAO extends EntityDAOImpl<Student, Long> {

}