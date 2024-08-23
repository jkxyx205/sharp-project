package com.rick.admin.module.demo.dao;

import com.rick.admin.module.demo.entity.Student;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2024/8/23 16:50
 */
@Repository
public class StudentDAO extends EntityDAOImpl<Student, Long> {

}