package com.rick.demo.module.book.controller;

import com.rick.demo.module.book.dao.BookDAO;
import com.rick.demo.module.book.entity.Book;
import com.rick.demo.module.book.model.BookDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Rick
 * @createdAt 2022-10-27 14:03:00
 */
@RestController
@RequestMapping("books")
public class BookController {

    @Autowired
    private BookDAO bookDAO;

    @PostMapping
    public BookDTO postSave(@RequestBody BookDTO book) {
        bookDAO.insertOrUpdate(book);
        return book;
    }

    @PostMapping("/v2")
    public Book postSave2(@RequestBody Book book) {
        bookDAO.insertOrUpdate(book);
        return book;
    }

    @GetMapping
    public BookDTO getSave(BookDTO book, String testId) {
//        bookDAO.insertOrUpdate(book);
        return book;
    }

    @GetMapping("v2")
    public Book getSave2(Book book) {
//        bookDAO.insertOrUpdate(book);
        return book;
    }

    @GetMapping("{id}")
    public Book queryById(@PathVariable Long id) {
        return bookDAO.selectById(id).get();
    }

}
