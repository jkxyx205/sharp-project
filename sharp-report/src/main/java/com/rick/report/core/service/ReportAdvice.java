package com.rick.report.core.service;

import com.rick.db.dto.Grid;
import com.rick.excel.table.AbstractExportTable;
import com.rick.excel.table.MapExcelTable;
import com.rick.report.core.entity.Report;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Rick.Xu
 * @date 2023/6/4 08:43
 */
public interface ReportAdvice {

    /**
     * list 页面加载的执行
     * @param report
     */
    default void init(Report report) {}

    default void beforeQuery(Report report, Map<String, Object> requestMap) {}

    default void beforeSetRow(Report report, List<Map<String, Object>> rows) {}

    default void combineSummaryList(Report report, List<BigDecimal> summaryList, Map<String, Object> requestMap, String conditionSql){}

    default Grid<Map<String, Object>> fetchDataWithoutSql(Report report, Map<String, Object> requestMap, Map<String, BigDecimal> summaryMap) {return null;}

    /**
     *
     * @param report
     * @param excelTable
     * @param requestMap
     * @return excel 写入列和数据后执行，比如对 excel 做样式处理，或插入其他数据， 如果 excel 不需要特殊处理，返回 null
     */
    default Consumer<AbstractExportTable> beforeExportAndReturnBeforeToFileConsumer(Report report, MapExcelTable excelTable, Map<String, Object> requestMap) {return null;}
}
