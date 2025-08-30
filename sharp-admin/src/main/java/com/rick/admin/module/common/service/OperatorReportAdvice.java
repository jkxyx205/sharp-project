package com.rick.admin.module.common.service;

import com.rick.common.util.Maps;
import com.rick.db.repository.TableDAO;
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

    private final TableDAO tableDAO;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows, Map<String, Object> requestMap) {
        ReportAdvice.super.beforeSetRow(report, rows, requestMap);

        List<Long> ids = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            ids.add((Long) row.get("createBy"));
            ids.add((Long) row.get("updateBy"));
        }

        Map<Long, String> idValueMap = tableDAO.selectForKeyValue("select id, name from sys_user where id IN (:ids)", Maps.of("ids", ids));

        for (Map<String, Object> row : rows) {
            row.put("createBy", idValueMap.get(row.get("createBy")));
            row.put("updateBy", idValueMap.get(row.get("updateBy")));
        }
    }
}
