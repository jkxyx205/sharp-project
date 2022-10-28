package com.rick.demo.db;

import com.rick.db.plugin.SQLUtils;
import com.rick.demo.module.project.dao.PersonDAO;
import com.rick.demo.module.project.domain.entity.Person;
import com.rick.demo.module.project.domain.entity.Role;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-03-03 19:43:00
 */
@SpringBootTest
public class ManyToManyTest {

    @Autowired
    private PersonDAO personDAO;

    @AfterAll
    public static void init() {
        SQLUtils.deleteNotIn("t_person", "id", Arrays.asList(552098712424472576L, 552100575806939136L, 617327246029365249L));
        SQLUtils.deleteNotIn("t_person_role", "person_id", Arrays.asList(552098712424472576L, 552100575806939136L));
    }

    @Test
    public void testInsert() {
        Person person = Person.builder()
                .name("Rick")
                .roleList(Arrays.asList(Role.builder().id(530861443353075712L).build()))
                .build();

        int count = personDAO.insert(person);
        assertThat(count).isEqualTo(1);

        Optional<Person> personOptional = personDAO.selectById(person.getId());
        Person person2 = personOptional.get();
        assertThat(person2.getRoleList().get(0).getName()).isEqualTo("admin");
    }

    @Test
    public void testUpdate() {
        Person person = Person.builder()
                .id(552098712365752321L)
                .name("admin")
                .roleList(Arrays.asList(Role.builder().id(530861443353075712L).build()
//                        , Role.builder().id(530861443353075713L).build()
                        , Role.builder().id(530861443353075714L).build())
                )
                .build();

        personDAO.update(person);
    }

    @Test
    public void testSelect() {
        Optional<Person> personOptional = personDAO.selectById(552100575806939136L);
        Person person = personOptional.get();
        assertThat(person.getRoleList().get(0).getName()).isEqualTo("admin");
    }

    @Test
    public void testDelete() {
        personDAO.deleteById(552098712365752321L);
    }
}
