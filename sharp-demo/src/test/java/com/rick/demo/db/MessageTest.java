package com.rick.demo.db;

import com.rick.demo.module.project.dao.MessageDAO;
import com.rick.demo.module.project.domain.entity.Message;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageTest {

    @Autowired
    private MessageDAO messageDAO;


    @Order(0)
    @Test
    public void testInsert() {
        messageDAO.insert(Message.builder().text("hello world").build());
    }

    @Order(1)
    @Test
    public void testSelect() {
        Message message = messageDAO.selectByParams("text=hello world").get(0);
    }

}
