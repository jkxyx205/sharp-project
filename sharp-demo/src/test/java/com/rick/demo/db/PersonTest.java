package com.rick.demo.db;

import com.rick.demo.module.project.dao.IdCardDAO;
import com.rick.demo.module.project.dao.PersonDAO;
import com.rick.demo.module.project.domain.entity.IdCard;
import com.rick.demo.module.project.domain.entity.Person;
import com.rick.demo.module.project.domain.entity.Role;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-05-01 09:37:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonTest {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private IdCardDAO idCardDAO;

    @Test
    @Order(0)
    public void savePerson() {
        Person person = Person.builder()
                .name("Tomcat11")
                .roleList(Arrays.asList(Role.builder().id(530861443353075712L).build()))
                .idCard(IdCard.builder().idNum("32128787988762110x").address("江苏").build())
                .build();
        personDAO.insert(person);
    }

    @Test
    @Order(0)
    public void saveCardId() {
        Person person = Person.builder()
                .name("Docker")
                .roleList(Arrays.asList(Role.builder().id(530861443353075712L).build()))
                .build();

        IdCard idCard = IdCard.builder().idNum("32128787988762120").address("陕西")
                .person(person)
                .build();

        idCardDAO.insert(idCard);
    }

    @Order(1)
    @Test
    public void findPersonById() {
        Person person = personDAO.selectById(552098712424472576L).get();
        assertThat(person.getRoleList().get(0).getName()).isEqualTo("admin");
        assertThat(person.getName()).isEqualTo("Rick");
        assertThat(person.getIdCard().getIdNum()).isEqualTo("32128787988762");
    }

    @Order(2)
    @Test
    public void findIdCardById() {
        final IdCard idCard = idCardDAO.selectById(552098712365752320L).get();
        assertThat(idCard.getPerson().getRoleList().get(0).getName()).isEqualTo("admin");
        assertThat(idCard.getPerson().getName()).isEqualTo("Rick");
        assertThat(idCard.getIdNum()).isEqualTo("32128787988762");
    }

    @Order(3)
    @Test
    public void testUpdateAll() {
        Person person = Person.builder()
                .id(617762367291449344L)
                .name("Tomcat")
                .roleList(Arrays.asList(Role.builder().id(530861443353075712L).build()))
                .idCard(IdCard.builder().id(617762367413084160L).idNum("32128787988762110Y").address("四川").build())
                .build();
        personDAO.update(person);
    }

    @Order(4)
    @Test
    public void testUpdateAndInsert() {
        Person person = Person.builder()
                .id(617762367291449344L)
                .name("TomcatNew")
                .roleList(Arrays.asList(Role.builder().id(530861443353075712L).build()))
                .idCard(IdCard.builder().idNum("32128787988762110Y").address("广州").build())
                .build();
        personDAO.update(person);
    }

    @Order(4)
    @Test
    public void testUpdateAndInsert2() {
        Person person = Person.builder()
                .name("乾隆")
                .roleList(Arrays.asList(Role.builder().id(530861443353075712L).build()))
                .build();

        IdCard idCard = IdCard.builder().idNum("32128787988762120").address("广西")
                .id(617764728776876032L)
                .person(person)
                .build();

        idCardDAO.update(idCard);
    }

    @Order(7)
    @Test
    public void testDelete() {
        personDAO.deleteLogicallyById(667049700548964352L);
    }

    @Order(8)
    @Test
    public void testDelete2() {
        personDAO.deleteById(667049700548964352L);
    }

    @Order(1)
    @Test
    public void testUpdate() {
        Person person = personDAO.selectById(552098712424472576L).get();
        person.getIdCard().setAddress("陕西b");
        System.out.println("1. -------------------");
        personDAO.update(person, "name");
//        System.out.println("2. -------------------");
//        personDAO.update("name", new Object[]{person.getName(), person.getId()}, "id = ?");
    }

    @Test
    @Order(11)
    public void savePersonWithManyToOneNull() {
        Person person = Person.builder()
                .name("Tomcat11")
                .roleList(Arrays.asList(Role.builder().id(530861443353075712L).build()))
                .idCard(IdCard.builder().id(665551747804065792L).idNum("42128787988762110x").address("江苏").build())
                .build();
        personDAO.insert(person);

        person.setIdCard(null);
        person.setRoleList(null);
        personDAO.update(person);

        Person person1 = personDAO.selectById(person.getId()).get();
        assertThat(person1.getIdCard()).isNull();
        assertThat(person1.getRoleList().size()).isEqualTo(0);
    }
}
