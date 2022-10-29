package com.rick.demo.module.school.dao;

import com.rick.db.plugin.dao.core.BaseDAOImpl;
import com.rick.demo.module.school.entity.Student;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:32:00
 */
@Repository
public class StudentDAO extends BaseDAOImpl<Student, Long> {

}