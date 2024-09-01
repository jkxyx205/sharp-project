package com.rick.admin.module.student.dao;

import com.rick.admin.module.student.entity.Student;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2024-09-01 10:48:56
 */
@Repository
public class StudentDAO extends EntityCodeDAOImpl<Student, Long> {

}