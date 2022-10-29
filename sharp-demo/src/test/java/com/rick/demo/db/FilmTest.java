package com.rick.demo.db;

import com.rick.demo.module.project.dao.FilmDAO;
import com.rick.demo.module.project.domain.entity.Film;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilmTest {

    @Autowired
    private FilmDAO filmDAO;

    @Order(0)
    @Test
    public void testInsert() {
        filmDAO.insert(Film.builder().seqId(System.currentTimeMillis() + "xoo").title("hello world").build());
//        filmDAO.insert(Film.builder().title("hello world222").build());
    }

    @Order(0)
    @Test
    public void testSelect() {
        Optional<Film> optional = filmDAO.selectById("1667050984013");
        assertThat("hello world").isEqualTo(optional.get().getTitle());
    }

}
