package com.rick.demo.report;


import com.rick.report.core.entity.Report;
import com.rick.report.core.model.QueryField;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.SordEnum;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * @author Rick
 * @createdAt 2021-10-13 15:27:00
 */
@SpringBootTest
public class ReportTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void test0() {
        reportService.saveOrUpdate(Report.builder()
                .id(619541501440958464L)
                .code("t_book")
                .name("图书报表")
                .querySql("SELECT t_book.id, t_book.title, t_person.name, sys_dict.label \"sexLabel\"\n" +
                        "FROM t_book,\n" +
                        "     t_person LEFT JOIN sys_dict on t_person.sex = sys_dict.name AND type = 'sex'\n" +
                        "WHERE t_book.person_id = t_person.id\n" +
                        "  AND t_book.title LIKE :title\n" +
                        "  AND t_person.name = :name\n" +
                        "  AND t_person.sex = :sex")
                .reportColumnList(Arrays.asList(
                        new ReportColumn("title", "书名", true),
                        new ReportColumn("name", "作者", true),
                        new ReportColumn("sexLabel", "性别")
                ))
                .queryFieldList(Arrays.asList(
                        new QueryField("title", "书名"),
                        new QueryField("name", "作者"),
                        new QueryField("sex", "性别", QueryField.Type.SELECT, "sex")
                ))
                .pageable(true)
                .sidx("title")
                .sord(SordEnum.ASC)
                .build());
    }

}
