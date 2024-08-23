package com.rick.admin.module.demo.controller;

import com.rick.admin.common.api.BaseFormController;
import com.rick.admin.module.demo.entity.Student;
import com.rick.admin.module.demo.service.StudentService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Rick.Xu
 * @date 2024/8/23 16:48
 */
@Controller
@RequestMapping("students")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StudentController extends BaseFormController<Student, StudentService> {

    public StudentController(StudentService studentService) {
        super(studentService, "demos/student/edit");
    }
}
