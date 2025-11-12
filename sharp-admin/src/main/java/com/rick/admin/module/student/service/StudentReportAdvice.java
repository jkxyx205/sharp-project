package com.rick.admin.module.student.service;

import com.rick.admin.module.common.service.OperatorReportAdvice;
import com.rick.common.util.JsonUtils;
import com.rick.db.repository.TableDAO;
import com.rick.fileupload.client.support.Document;
import com.rick.report.core.entity.Report;
import com.rick.report.core.support.ReportConstants;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2024/9/2 12:09
 */
@Component
public class StudentReportAdvice extends OperatorReportAdvice {

    public StudentReportAdvice(TableDAO tableDAO) {
        super(tableDAO);
    }

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows, Map<String, Object> requestMap) {
        super.beforeSetRow(report, rows, requestMap);

        // 文件解析
        for (Map<String, Object> row : rows) {
            Object attachmentsObject = row.get("attachments");
            if (Objects.nonNull(attachmentsObject)) {
                List<Document> list = JsonUtils.toList(row.get("attachments").toString(), Document.class);
                row.put("attachments", list.stream().map(document -> document.getName()).collect(Collectors.joining(",")));
            }

            Object avatarObject = row.get("avatar");
            if (Objects.nonNull(avatarObject)) {
                Document avatar = JsonUtils.toObject(avatarObject.toString(), Document.class);
                row.put("avatar", avatar == null ? "/img/default_avatar.png" : avatar.getUrl());
            } else {
                row.put("avatar","/img/default_avatar.png");
            }

            Object hobbyListObject = row.get("hobbyList");
            if (Objects.nonNull(hobbyListObject)) {
                row.put("hobbyList", hobbyListObject.toString());
            }

            Object materialTypeListObject = row.get("materialTypeList");
            if (Objects.nonNull(materialTypeListObject)) {
                row.put("materialTypeList", materialTypeListObject.toString());
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
