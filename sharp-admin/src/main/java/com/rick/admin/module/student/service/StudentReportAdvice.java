package com.rick.admin.module.student.service;

import com.rick.admin.module.common.service.OperatorReportAdvice;
import com.rick.common.util.JsonUtils;
import com.rick.db.service.SharpService;
import com.rick.fileupload.client.support.Document;
import com.rick.report.core.entity.Report;
import com.rick.report.core.support.ReportConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2024/9/2 12:09
 */
@Component
public class StudentReportAdvice extends OperatorReportAdvice {

    public StudentReportAdvice(SharpService sharpService) {
        super(sharpService);
    }

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        super.beforeSetRow(report, rows);

        // 文件解析
        for (Map<String, Object> row : rows) {
            if (StringUtils.isNotBlank((String) row.get("attachments"))) {
                List<Document> list = JsonUtils.toList((String)row.get("attachments"), Document.class);
                row.put("attachments", list.stream().map(document -> document.getName()).collect(Collectors.joining(",")));
            }

            if (StringUtils.isNotBlank((String) row.get("avatar"))) {
                Document avatar = JsonUtils.toObject((String) row.get("avatar"), Document.class);
                row.put("avatar", avatar == null ? "/img/default_avatar.png" : avatar.getUrl());
            } else {
                row.put("avatar","/img/default_avatar.png");
            }

        }
    }

    @Override
    public void init(Report report) {
        super.init(report);

        report.getAdditionalInfo().put(ReportConstants.ADDITIONAL_JS, "$('table tr').find('td:eq(10)').each(function(){\n" +
                "    $(this).html('<img height=\"20\" src=\"'+$(this).text()+'\"/>')\n" +
                "})");
    }
}
