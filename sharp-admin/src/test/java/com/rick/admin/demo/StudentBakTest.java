package com.rick.admin.demo;

import com.rick.db.service.support.Params;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.HiddenReportColumn;
import com.rick.report.core.model.QueryField;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.SordEnum;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * @author Rick.Xu
 * @date 2024/8/23 16:33
 */
@SpringBootTest
public class StudentBakTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        Report report = Report.builder()
                .code("t_student")// 　建议和数据库表名保持一致
                .tplName("demos/student/list") // 拷贝模版页面到指定目录
//                .tplName("tpl/list") // 没有特殊要求使用模版页面
//                .tplName("tpl/ajax_list") // 没有特殊要求使用模版页面
                .name("学生信息")
                .additionalInfo(Params.builder(1).pv("operator-bar", true) // 显示操作按钮
                        .pv("endpoint", "students")
                        .build()) // 显示操作按钮
                .querySql("select id, name, create_time from t_student where name like :name and is_deleted = 0")
                .queryFieldList(Arrays.asList(
                        new QueryField("name", "姓名")
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("name", "名称", true),
                        new ReportColumn("create_time", "创建时间", false, null, Arrays.asList("localDateTimeConverter"))
                ))
                .pageable(true)
                .sidx("create_time")
                .sord(SordEnum.DESC)
                .build();

        reportService.saveOrUpdate(report);
        System.out.println("=========reportId = " + report.getId());
    }
}
