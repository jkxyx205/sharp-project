package com.rick.report.core.service;

import com.rick.db.dto.Grid;
import com.rick.report.core.entity.Report;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/4 08:43
 */
public interface ReportAdvice {

    default void beforeQuery(Report report, Map<String, Object> requestMap) {};

    default void beforeSetRow(Report report, List<Map<String, Object>> rows) {};

    default void combineSummaryList(Report report, List<BigDecimal> summaryList, Map<String, Object> requestMap, String conditionSql){}

    default Grid<Map<String, Object>> fetchDataWithoutSql(Report report, Map<String, Object> requestMap, Map<String, BigDecimal> summaryMap) {return null;}
}
