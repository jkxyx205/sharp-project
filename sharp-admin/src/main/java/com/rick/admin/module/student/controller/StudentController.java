package com.rick.admin.module.student.controller;

import com.rick.admin.common.api.BaseFormController;
import com.rick.admin.module.student.entity.Student;
import com.rick.admin.module.student.service.StudentService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Rick.Xu
 * @date 2024-08-25 03:23:48
 */
@Controller
@RequestMapping("students")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StudentController extends BaseFormController<Student, StudentService> {

    public StudentController(StudentService studentService) {
        super(studentService, "demos/student/edit");
    }
}