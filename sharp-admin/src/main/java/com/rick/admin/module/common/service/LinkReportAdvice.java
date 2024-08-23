package com.rick.admin.module.common.service;

import com.rick.common.util.FileUtils;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/8/23 14:25
 */
@Component
public class LinkReportAdvice implements ReportAdvice {

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        ReportAdvice.super.beforeSetRow(report, rows);

        // 判断是否是图片
        for (Map<String, Object> row : rows) {
            row.put("isImageType", FileUtils.isImageType((String) row.get("extension")));
        }
    }
}
