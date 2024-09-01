package com.rick.admin.module.student.controller;

import com.rick.admin.common.api.BaseFormController;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.student.entity.Student;
import com.rick.admin.module.student.service.StudentService;
import com.rick.db.plugin.dao.core.EntityDAOManager;
import com.rick.meta.dict.service.DictUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2024-08-30 21:23:20
 */
@Controller
@RequestMapping("students")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StudentController extends BaseFormController<Student, StudentService> {

    public StudentController(StudentService studentService) {
        super(studentService, "demos/student/edit-thymeleaf");
//        super(studentService, "demos/student/control-thymeleaf");

//        super(studentService, "demos/student/edit-vue");
//        super(studentService, "demos/student/control-vue");
    }

    @GetMapping("{id}")
    @ResponseBody
    public Student findById(@PathVariable Long id) {
        Optional<Student> byId = baseService.findById(id);
        return getEntityFromOptional(byId, id);
    }

    protected Student getEntityFromOptional(Optional<Student> optional, Object key) {
        Student student = optional.orElseThrow(() -> getResourceNotFoundException(key));
        DictUtils.fillDictLabel(student);
        return student;
    }

    protected ResourceNotFoundException getResourceNotFoundException(Object key) {
        return new ResourceNotFoundException(comment() + " id = " + key + "不存在");
    }

    protected String comment() {
        return EntityDAOManager.getTableMeta(baseService.getBaseDAO().getEntityClass()).getTable().comment();
    }

}