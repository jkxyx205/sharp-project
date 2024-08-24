package com.rick.admin.module.common.service;

import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/8/25 01:57
 */
@Component
@RequiredArgsConstructor
public class OperatorReportAdvice implements ReportAdvice {

    private final SharpService sharpService;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        ReportAdvice.super.beforeSetRow(report, rows);

        List<Long> ids = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            ids.add((Long) row.get("createBy"));
            ids.add((Long) row.get("updateBy"));
        }

        Map<Long, String> idValueMap = sharpService.queryForKeyValue("select id, name from sys_user where id IN (:ids)", Params.builder(1).pv("ids", ids).build());

        for (Map<String, Object> row : rows) {
            row.put("createBy", idValueMap.get(row.get("createBy")));
            row.put("updateBy", idValueMap.get(row.get("updateBy")));
        }
    }
}
