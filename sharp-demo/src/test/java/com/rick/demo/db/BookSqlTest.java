package com.rick.demo.db;

import com.rick.db.plugin.dao.core.EntityDAOSupport;
import com.rick.demo.module.book.dao.BookDAO;
import com.rick.demo.module.book.entity.Book;
import com.rick.demo.module.book.model.BookQuery;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookSqlTest {

    @Autowired
    private EntityDAOSupport entityDAOSupport;

    @Autowired
    private BookDAO bookDAO;

    @Test
    public void testSqlQuery() {
        List<BookQuery> query = entityDAOSupport.query("select * from t_book where id = 617342584087388160", null, BookQuery.class);
        System.out.println(query);

        Optional<BookQuery> optional = entityDAOSupport.queryForObject("select * from t_book where id = 617342584087388160", null, BookQuery.class);
        System.out.println(optional.get());

        final Book book = bookDAO.selectById(617321100761636864L).get();
        assertThat(book.getTitle()).isEqualTo("威尼斯人");
        assertThat("莎士比亚").isEqualTo(book.getPerson().getName());
        assertThat(2).isEqualTo(book.getTagList().size());


        BookQuery bookQuery = new BookQuery();
        bookQuery.setId(617342584087388160L);
        bookQuery.setPersonId("552100575806939136");

        entityDAOSupport.query(bookQuery);
        System.out.println(bookQuery);
    }

}
