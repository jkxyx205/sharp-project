package com.rick.admin.module.student.service;

import com.rick.admin.module.student.dao.StudentDAO;
import com.rick.admin.module.student.entity.Student;
import com.rick.db.service.BaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Rick.Xu
 * @date 2024-09-01 07:33:43
 */
@Service
public class StudentService extends BaseServiceImpl<StudentDAO, Student> {

    public StudentService(StudentDAO studentDAO) {
        super(studentDAO);
    }
}