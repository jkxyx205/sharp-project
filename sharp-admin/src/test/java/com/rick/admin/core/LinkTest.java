package com.rick.admin.core;

import com.rick.report.core.entity.Report;
import com.rick.report.core.model.HiddenReportColumn;
import com.rick.report.core.model.QueryField;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.SordEnum;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * @author Rick.Xu
 * @date 2023/12/15 13:52
 */
@SpringBootTest
public class LinkTest {

    @Autowired
    private ReportService reportService;

    @Value("${fileupload.local.server-url}")
    private String serverUrl;

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .code("sys_document")
                .tplName("modules/link/list")
                .name("图片管理")
                .reportAdviceName("linkReportAdvice")
                .querySql("select id, name, concat('"+serverUrl+"', group_name, '/', path) url, extension, content_type, ROUND(size / (1024 * 1024), 1) size, create_time, '' copy from sys_document where name like :name and group_name = 'link'")
                .queryFieldList(Arrays.asList(
                        new QueryField("name", "名称")
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new HiddenReportColumn("isImageType"),
                        new ReportColumn("url", "文件"),
                        new ReportColumn("name", "名称", true),
                        new ReportColumn("content_type", "类型", true),
                        new ReportColumn("size", "大小（M）", true),
                        new ReportColumn("create_time", "创建时间", false,null, Arrays.asList("localDateTimeConverter")),
                        new ReportColumn("copy", "复制")
                ))
                .pageable(true)
                .sidx("create_time")
                .sord(SordEnum.DESC)
                .build());
    }
}
