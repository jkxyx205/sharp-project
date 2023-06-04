package com.rick.report.core.service;

import com.rick.report.core.entity.Report;

import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/4 08:43
 */
public interface ReportAdvice {

    void beforeSetRow(Report report, List<Map<String, Object>> rows);
}
