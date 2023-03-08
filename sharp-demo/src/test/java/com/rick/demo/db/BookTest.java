package com.rick.demo.db;

import com.rick.demo.module.book.dao.BookDAO;
import com.rick.demo.module.book.dao.TagDAO;
import com.rick.demo.module.book.entity.Book;
import com.rick.demo.module.book.entity.Tag;
import com.rick.demo.module.project.dao.PersonDAO;
import com.rick.demo.module.project.domain.entity.Person;
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
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookTest {

    @Autowired
    private TagDAO tagDAO;

    @Autowired
    private BookDAO bookDAO;

    @Autowired
    private PersonDAO personDAO;

    @Order(0)
    @Test
    public void testAddPerson() {
//        personDAO.insert(Arrays.asList(
//                Person.builder().name("列夫托尔斯泰").build(),
//                Person.builder().name("歌德").build(),
//                Person.builder().name("莎士比亚").build()
//        ));
    }


    @Order(1)
    @Test
    public void testSaveUpdateTag() {
        tagDAO.insertOrUpdate(Arrays.asList(
                Tag.builder().id(617320872469864448L).title("文学").build(),
                Tag.builder().id(617320872469864449L).title("科技").build(),
                Tag.builder().id(617320872469864450L).title("欧美").build()
        ));
    }

    @Order(2)
    @Test
    public void testSaveOrUpdateBook() {
        bookDAO.insertOrUpdate(Book.builder()
                .id(617321100761636864L)
                .title("威尼斯人")
                .person(Person.builder().id(617327246029365249L).build())
                .tagList(Arrays.asList(
                        Tag.builder().id(617320872469864448L).build(),
                        Tag.builder().id(617320872469864450L).build()
                ))
                .build());
    }

    @Order(3)
    @Test
    public void testSelectBook() {
        final Book book = bookDAO.selectById(617321100761636864L).get();
        assertThat(book.getTitle()).isEqualTo("威尼斯人");
        assertThat("莎士比亚").isEqualTo(book.getPerson().getName());
        assertThat(2).isEqualTo(book.getTagList().size());
    }

}
