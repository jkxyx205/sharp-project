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

    private School createSchool(SchoolLicense schoolLicense) {
        return School.builder()
                .name("????????????")
                .buildDate(LocalDate.now())
                .type(School.TypeEnum.PUBLIC)
                .score(100)
                .budget(new BigDecimal("111323.23"))
                .address(Address.builder().code("100084").detail("????????????????????????????????????????????????").build())
                .additionalInfo(Params.builder(1).pv("Teacher Count", 3641).build())
                .evaluate(new Evaluate(1, "GREAT!"))
                .leadershipInformationList(Arrays.asList(Params.builder(1).pv("Principal", "?????????").build()))
                .awardsSet(Sets.newHashSet(Params.builder(1).pv("c", "C9??????").build()))
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
                .name("?????????")
                .age(50)
                .schoolList(schoolList)
                .build();
    }

    private SchoolLicense createSchoolLicense() {
        return SchoolLicense.builder()
                .number("124535354C34X")
                .remark("???????????? ????????????")
                .build();
    }

    private void checkSchool(School school) {
        assertThat(school.getName(),  equalTo("????????????"));
        assertThat(school.getBuildDate(), notNullValue());
        assertThat(school.getType(), equalTo(School.TypeEnum.PUBLIC));
        assertThat(school.getScore(), equalTo(100));
        assertThat(school.getBudget(), equalTo(new BigDecimal("111323.2300")));
        assertThat(school.getAddress(), anyOf(
                hasProperty("code", equalTo("100084")),
                hasProperty("address", equalTo("????????????????????????????????????????????????"))
        ));
        assertThat(school.getAdditionalInfo(), hasKey("Teacher Count"));
        assertThat(school.getEvaluate(), anyOf(
                hasProperty("grade", equalTo(1)),
                hasProperty("description", equalTo("GREAT!"))
        ));
        assertThat(school.getLeadershipInformationList().get(0).get("Principal"), equalTo("?????????"));
        assertThat(school.getAwardsSet().iterator().next().get("c"), equalTo("C9??????"));
        assertThat(school.getScoreList(), hasItem(99.9f));
    }

    private void checkStudent(Student student) {
        assertThat(student.getName(),  equalTo("Rick.Xu"));
        assertThat(student.getGrade(), equalTo(1));
        assertThat(student.getSex(), equalTo(Student.SexEnum.MALE));
    }

    private void checkTeacher(Teacher teacher) {
        assertThat(teacher.getName(),  equalTo("?????????"));
        assertThat(teacher.getAge(), equalTo(50));
    }

    private void checkSchoolLicense(SchoolLicense schoolLicense) {
        assertThat(schoolLicense.getNumber(),  equalTo("124535354C34X"));
        assertThat(schoolLicense.getRemark(), equalTo("???????????? ????????????"));
    }
}
