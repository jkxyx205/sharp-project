package com.rick.demo.db;

import com.rick.common.util.IdGenerator;
import com.rick.common.util.Maps;
import com.rick.db.repository.TableDAO;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookTest {

    @Autowired
    private TableDAO tableDAO;

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

    @Order(4)
    @Test
    public void testEntityToMap() {
        final Book book = bookDAO.selectById(617321100761636864L).get();
        Map<String, Object> bookMap = bookDAO.entityToMap(book);

        assertThat(617327246029365249L).isEqualTo(((Person)bookMap.get("person")).getId());
        assertThat("威尼斯人").isEqualTo(bookMap.get("title"));
        assertThat("文学").isEqualTo(((List<Tag>)bookMap.get("tagList")).get(0).getTitle());

//        Book book2 = bookDAO.mapToEntity(bookMap);

//        assertThat(617327246029365249L).isEqualTo(book2.getPerson().getId());
//        assertThat("威尼斯人").isEqualTo(book2.getTitle());
//        assertThat("文学").isEqualTo(book2.getTagList().get(0).getTitle());

        bookMap.put("person", "617327246029365249");
//        bookMap.put("person", 617327246029365249L);
        bookMap.remove("person_id");

//        Book book3 = bookDAO.mapToEntity(bookMap);

//        assertThat(617327246029365249L).isEqualTo(book3.getPerson().getId());
//        assertThat("威尼斯人").isEqualTo(book3.getTitle());
//        assertThat("文学").isEqualTo(book3.getTagList().get(0).getTitle());

    }

    @Order(5)
    @Test
    public void testSqlUtilInsert() {
        tableDAO.insert("t_book","id, title, person_id", Maps.of("id", IdGenerator.getSimpleId(), "title", "Test1" + System.currentTimeMillis(), "person_id", "617327246029365249"));
    }

    @Order(5)
    @Test
    public void testSqlUtilUpdate() {
        tableDAO.update("t_book", "title, person_id", "id = ?", "Test2", 666643811488796672L, 1678682617744205L);
    }

    @Order(3)
    @Test
    public void testUpdateByColumnName() {
        final Book book = bookDAO.selectById(617321100761636864L).get();
        bookDAO.update("title", "id = ?", new Object[]{book.getTitle(), book.getId()});
    }

}
