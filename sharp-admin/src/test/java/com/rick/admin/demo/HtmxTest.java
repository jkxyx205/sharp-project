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
 * @date 2025/6/13 09:15
 */
@SpringBootTest
public class HtmxTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(964844123011960832L)
                .code("sys_dict-htmx")
                .tplName("demos/htmx/list") // 渲染模版需要使用 demos/htmx/layout.html
                .name("字典管理-htmx")
                .reportAdviceName("dictHtmxReportAdvice")
                .summaryColumnNames("sort")
                .additionalInfo(Params.builder(2).pv("formId", "695312747063197696")
                        // 1. link
//                        .pv("formAction", "link")
//                        .pv("formPage", "demos/htmx/form")
                        // 2. drawer
                        .pv("formAction", "drawer")
                        // .pv("formPage", "tpl/form/form-full") // drawer 默认使用 tpl/form/form-full，从 form tplName 继承
                        .pv("select", true).build())
                .querySql("select id, type, name, label, sort from sys_dict where type = :type and is_deleted = 0 order by type, sort asc")
                .queryFieldList(Arrays.asList(
                        new QueryField("type", "分类", QueryField.Type.SELECT, "sys_dict_type")
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("name", "编码"),
                        new ReportColumn("label", "显示值"),
                        new ReportColumn("type", "分类", true),
                        new ReportColumn("sort", "排序号").setColumnWidth(30)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }

    @Test
    public void testReportAdminLTE() {
        reportService.saveOrUpdate(Report.builder()
                .id(966383991991062528L)
                .code("sys_dict-adminlte")
                .tplName("adminlte/list") // 渲染模版需要使用 adminlte/layout.html
                .name("字典管理-adminlte")
                .reportAdviceName("dictHtmxReportAdvice")
                .summaryColumnNames("sort")
                .additionalInfo(Params.builder(2).pv("formId", "695312747063197696")
                        // 1. link
//                        .pv("formAction", "link")
//                        .pv("formPage", "demos/htmx/form")
                        // 2. drawer
                        .pv("formAction", "drawer")
                        // .pv("formPage", "tpl/form/form-full") // drawer 默认使用 tpl/form/form-full，从 form tplName 继承
                        .pv("select", true).build())
                .querySql("select id, type, name, label, sort from sys_dict where type = :type and is_deleted = 0 order by type, sort asc")
                .queryFieldList(Arrays.asList(
                        new QueryField("type", "分类", QueryField.Type.SELECT, "sys_dict_type")
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("name", "编码"),
                        new ReportColumn("label", "显示值"),
                        new ReportColumn("type", "分类", true),
                        new ReportColumn("sort", "排序号").setColumnWidth(30)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}
