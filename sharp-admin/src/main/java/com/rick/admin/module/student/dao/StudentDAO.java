package com.rick.admin.module.student.dao;

import com.rick.admin.module.student.entity.Student;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2024-08-25 03:23:48
 */
@Repository
public class StudentDAO extends EntityCodeDAOImpl<Student, Long> {

}