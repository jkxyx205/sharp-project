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
                .name("Rick")
                .roleList(Arrays.asList(Role.builder().id(530861443353075712L).build()))
                .idCard(IdCard.builder().idNum("32128787988762").address("江苏").build())
                .build();
        personDAO.insert(person);
    }

    @Test
    @Order(0)
    public void saveCardId() {
        Person person = Person.builder()
                .name("Rick")
                .roleList(Arrays.asList(Role.builder().id(530861443353075712L).build()))
                .build();

        IdCard idCard = IdCard.builder().idNum("32128787988762").address("陕西")
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
}
