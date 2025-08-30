package com.rick.admin.module.student.dao;

import com.rick.admin.module.student.entity.Student;
import com.rick.db.repository.EntityCodeDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2024-09-05 12:26:01
 */
@Repository
public class StudentDAO extends EntityCodeDAOImpl<Student, Long> {

}