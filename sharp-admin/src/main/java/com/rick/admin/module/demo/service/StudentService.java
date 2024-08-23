package com.rick.admin.module.demo.service;

import com.rick.admin.module.demo.dao.StudentDAO;
import com.rick.admin.module.demo.entity.Student;
import com.rick.db.service.BaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Rick.Xu
 * @date 2024/8/23 16:49
 */
@Service
public class StudentService extends BaseServiceImpl<StudentDAO, Student> {

    public StudentService(StudentDAO baseDAO) {
        super(baseDAO);
    }
}
