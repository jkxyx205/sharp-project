package com.rick.demo.db;

import com.rick.demo.module.form.dao.NoticeDAO;
import com.rick.demo.module.form.entity.Notice;
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
public class VersionTest {

    @Autowired
    private NoticeDAO noticeDAO;

    @Order(0)
    @Test
    public void testNotice() {
        noticeDAO.insert(Notice.builder()
                        .name("hello new version")
                .build());
    }

    @Order(1)
    @Test
    public void testNoticeUpdate() {
        Notice notice = noticeDAO.selectById(688436409735868416L).get();
        notice.setName("new didi");
        noticeDAO.update(notice);
    }


}
