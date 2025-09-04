package com.rick.demo.module.book.dao;

import com.rick.db.repository.EntityDAOImpl;
import com.rick.demo.module.book.entity.Book;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2022-10-27 18:00:00
 */

@Repository
public class BookDAO extends EntityDAOImpl<Book, Long> {

}