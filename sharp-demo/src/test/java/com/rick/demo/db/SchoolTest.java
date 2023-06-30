package com.rick.demo.db;

import com.google.common.collect.Sets;
import com.rick.db.service.support.Params;
import com.rick.demo.module.project.domain.entity.Address;
import com.rick.demo.module.school.dao.SchoolDAO;
import com.rick.demo.module.school.dao.SchoolLicenseDAO;
import com.rick.demo.module.school.dao.StudentDAO;
import com.rick.demo.module.school.dao.TeacherDAO;
import com.rick.demo.module.school.entity.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SchoolTest {

    @Autowired
    private SchoolDAO schoolDAO;

    @Autowired
    private SchoolLicenseDAO schoolLicenseDAO;

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private TeacherDAO teacherDAO;

    @Order(0)
    @Test
    public void testSave() {
        SchoolLicense schoolLicense = createSchoolLicense();
        schoolLicenseDAO.insert(schoolLicense);

        School school = createSchool(schoolLicense);
        schoolDAO.insert(school);

        Student student = createStudent(school);
        studentDAO.insert(student);

        Teacher teacher = createTeacher(Arrays.asList(school));
        teacherDAO.insert(teacher);
    }

    @Order(1)
    @Test
    public void testSchoolFindById() {
        Optional<School> option = schoolDAO.selectById(552173736070144000L);
        School school = option.get();

        checkSchool(school);
        checkSchoolLicense(school.getSchoolLicense());
        checkStudent(school.getStudentList().get(0));
        checkTeacher(school.getTeacherList().get(0));
    }

    @Order(1)
    @Test
    public void testStudentFindById() {
        Student student = studentDAO.selectById(552173736246304768L).get();
        checkStudent(student);
        checkSchool(student.getSchool());
    }

    @Order(2)
    @Test
    public void testTeacherFindById() {
        Teacher teacher = teacherDAO.selectById(552173736518934528L).get();
        checkTeacher(teacher);
        checkSchool(teacher.getSchoolList().get(0));
    }

    @Order(3)
    @Test
    public void testSchoolLicenseFindById() {
        SchoolLicense schoolLicense = schoolLicenseDAO.selectById(552173735432609792L).get();
        checkSchoolLicense(schoolLicense);
        checkSchool(schoolLicense.getSchool());
    }

    @Order(4)
    @Test
    public void testUpdate() {
        Optional<School> option = schoolDAO.selectById(552173736070144000L);
        School school = option.get();
        System.out.println("1. -------------------");
        schoolDAO.update(school, "name");
        System.out.println("2. -------------------");
        schoolDAO.update("name", new Object[]{school.getName(), school.getId()}, "id = ?");
    }

    @Order(5)
    @Test
    public void testSelect() {
//        List<School> schoolList = schoolDAO.selectByParams(Params.builder()
////                        .pv("eq_score", 90) // 1
////                        .pv("lt_score", 91) // 1
//                        .pv("gt_score", 90) // 329
////                        .pv("op", "eq")
//                        .build(),
//                "score > :gt_score AND score = :eq_score AND score < :lt_score");


        List<School> schoolList = schoolDAO.selectByParams(Params.builder()
                        .pv("score", 90) // 1
                        .pv("op", ">")
                        .build(),
                "score ${op} :score");

        System.out.println(schoolList.size());



    }

    private School createSchool(SchoolLicense schoolLicense) {
        return School.builder()
                .name("清华大学")
                .buildDate(LocalDate.now())
                .type(School.TypeEnum.PUBLIC)
                .score(100)
                .budget(new BigDecimal("111323.23"))
                .address(Address.builder().code("100084").detail("中华人民共和国北京市海淀区清华园").build())
                .additionalInfo(Params.builder(1).pv("Teacher Count", 3641).build())
                .evaluate(new Evaluate(1, "GREAT!"))
                .leadershipInformationList(Arrays.asList(Params.builder(1).pv("Principal", "王希勤").build()))
                .awardsSet(Sets.newHashSet(Params.builder(1).pv("c", "C9联盟").build()))
                .scoreList(Arrays.asList(99.9f, 98f, 100f))
                .schoolLicense(schoolLicense)
                .build();
    }

    private Student createStudent(School school) {
        return Student.builder()
                .name("Rick.Xu")
                .sex(Student.SexEnum.MALE)
                .grade(1)
                .school(school)
                .build();
    }

    private Teacher createTeacher(List<School> schoolList) {
        return Teacher.builder()
                .name("姚期智")
                .age(50)
                .schoolList(schoolList)
                .build();
    }

    private SchoolLicense createSchoolLicense() {
        return SchoolLicense.builder()
                .number("124535354C34X")
                .remark("自强不息 厚德载物")
                .build();
    }

    private void checkSchool(School school) {
        assertThat(school.getName(),  equalTo("清华大学"));
        assertThat(school.getBuildDate(), notNullValue());
        assertThat(school.getType(), equalTo(School.TypeEnum.PUBLIC));
        assertThat(school.getScore(), equalTo(100));
        assertThat(school.getBudget(), equalTo(new BigDecimal("111323.2300")));
        assertThat(school.getAddress(), anyOf(
                hasProperty("code", equalTo("100084")),
                hasProperty("address", equalTo("中华人民共和国北京市海淀区清华园"))
        ));
        assertThat(school.getAdditionalInfo(), hasKey("Teacher Count"));
        assertThat(school.getEvaluate(), anyOf(
                hasProperty("grade", equalTo(1)),
                hasProperty("description", equalTo("GREAT!"))
        ));
        assertThat(school.getLeadershipInformationList().get(0).get("Principal"), equalTo("王希勤"));
        assertThat(school.getAwardsSet().iterator().next().get("c"), equalTo("C9联盟"));
        assertThat(school.getScoreList(), hasItem(99.9f));
    }

    private void checkStudent(Student student) {
        assertThat(student.getName(),  equalTo("Rick.Xu"));
        assertThat(student.getGrade(), equalTo(1));
        assertThat(student.getSex(), equalTo(Student.SexEnum.MALE));
    }

    private void checkTeacher(Teacher teacher) {
        assertThat(teacher.getName(),  equalTo("姚期智"));
        assertThat(teacher.getAge(), equalTo(50));
    }

    private void checkSchoolLicense(SchoolLicense schoolLicense) {
        assertThat(schoolLicense.getNumber(),  equalTo("124535354C34X"));
        assertThat(schoolLicense.getRemark(), equalTo("自强不息 厚德载物"));
    }
}
