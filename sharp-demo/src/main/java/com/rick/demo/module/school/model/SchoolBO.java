package com.rick.demo.module.school.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/5/15 15:08
 */
@Data
public class SchoolBO {

    private Long id;

    private String name;

    private LocalDate buildDate;

    private SchoolLicenseBO sl;

    private List<StudentBO> studentList;

    private List<TeacherBO> teacherList;

    /**
     * 1 <-> 1
     */
    @Data
    public static class SchoolLicenseBO {

        private Long id;

        private String number;

        private String remark;
    }

    /**
     * 1 <-> N
     */
    @Data
    public static class StudentBO {

        private Long id;

        private String name;

        private Integer grade;

        private String sex;

        private Long schoolId;
    }

    /**
     * N <-> N
     */
    @Data
    public static class TeacherBO {

        private Long id;

        private String name;

        private Integer age;

        private Long schoolId;
    }
}
