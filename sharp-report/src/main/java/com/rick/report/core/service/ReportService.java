package com.rick.report.core.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.dto.Grid;
import com.rick.db.dto.PageModel;
import com.rick.db.dto.QueryModel;
import com.rick.db.plugin.GridUtils;
import com.rick.db.service.GridService;
import com.rick.excel.table.QueryResultExportTable;
import com.rick.excel.table.model.MapTableColumn;
import com.rick.meta.dict.convert.ValueConverter;
import com.rick.report.core.dao.ReportDAO;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.ReportDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 6/18/20 3:34 PM
 */
@Service
@RequiredArgsConstructor
@Validated
public class ReportService {

    private static final String EXCEL_EXTENSION = ".xlsx";

    private final ReportDAO reportDAO;

    private final GridService gridService;

    private final Map<String, ValueConverter> valueConverterMap;

    private final  Map<String, ReportAdvice> reportAdviceMap;

    /**
     * 创建报表
     */
    public int saveOrUpdate(@Valid Report report) {
        validateNonDeleteSql(report.getQuerySql());
        return reportDAO.insertOrUpdate(report);
    }

    /**
     * 删除报表
     * @param id 报表id
     */
    public int delete(Long id) {
        return reportDAO.deleteById(id);
    }

    public Optional<Report> findById(Long id) {
        return reportDAO.selectById(id);
    }

    public ReportDTO list(long id, Map<String, Object> requestMap) {
        Report report = getReport(id);

        String summarySQL = null;
        List<String> summaryColumnNameList = null;
        if (StringUtils.isNotEmpty(report.getSummaryColumnNames())) {
            summaryColumnNameList = Arrays.stream(report.getSummaryColumnNames().split("\\s*,\\s*")).collect(Collectors.toList());
            summarySQL = "SELECT " + summaryColumnNameList.stream().map(c -> "CONVERT(sum("+c+"), DECIMAL(10,3))").collect(Collectors.joining(", ")) +
                    report.getQuerySql().substring(report.getQuerySql().toUpperCase().indexOf("FROM"));
        }

        Grid<Map<String, Object>> grid = GridUtils.list(report.getQuerySql(), requestMap);

        Map<String, BigDecimal> summaryMap = null;
        if (StringUtils.isNotEmpty(report.getSummaryColumnNames())) {
            List<BigDecimal> summaryList = GridUtils.numericObject(summarySQL, requestMap);
            if (grid.getRecords() > 0) {
                summaryMap = Maps.newHashMapWithExpectedSize(summaryColumnNameList.size());
                for (int i = 0; i < summaryColumnNameList.size(); i++) {
                    summaryMap.put(summaryColumnNameList.get(i), summaryList.get(i));
                }
            }
        }

        return new ReportDTO(report, convert(grid, report), summaryMap);
    }

    public void export(HttpServletRequest request, HttpServletResponse response, long id) throws IOException {
        Report report = getReport(id);

        QueryModel queryModel = QueryModel.of(HttpServletRequestUtils.getParameterMap(request));
        PageModel pageModel = queryModel.getPageModel();
        pageModel.setSize(-1);

        LocalDateTime localDateTime = LocalDateTime.now();
        String timestamp = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmSSS"));
        String fileName = report.getName() + timestamp + EXCEL_EXTENSION;

        QueryResultExportTable exportTable = new QueryResultExportTable(gridService, report.getQuerySql(), pageModel, queryModel.getParams(), convert(report.getReportColumnList())) {
            @Override
            public void setRows(List<?> rows) {
                toObjectArrayListAndConvert((List<Map<String, Object>>) rows, report.getReportColumnList());
                super.setRows(rows);
            }
        };

        exportTable.write(HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, fileName));
    }

    private Report getReport(long id) {
        Optional<Report> optional = findById(id);
        if (!optional.isPresent()) {
            throw new BizException(ResultUtils.fail("Report not exists"));
        }

        Report report = optional.get();
        validateNonDeleteSql(report.getQuerySql());
        return report;
    }

    private Grid<Object[]> convert(Grid<Map<String, Object>> paramGrid, Report report) {
        ReportAdvice reportAdvice = reportAdviceMap.get(report.getReportAdviceName());
        if (reportAdvice != null) {
            reportAdvice.beforeSetRow(report, paramGrid.getRows());
        }

        List<ReportColumn> reportColumnList = report.getReportColumnList();

        return Grid.<Object[]>builder()
                .rows(toObjectArrayListAndConvert(paramGrid.getRows(), reportColumnList))
                .pageSize(paramGrid.getPageSize())
                .page(paramGrid.getPage())
                .totalPages(paramGrid.getTotalPages())
                .records(paramGrid.getRecords())
                .build();
    }

    private List<Object[]> toObjectArrayListAndConvert(List<Map<String, Object>> rows, List<ReportColumn> reportColumnList) {
        List<Object[]> objectArrayRows = Lists.newArrayListWithExpectedSize(rows.size());

        int columnSize = reportColumnList.size();
        for (Map<String, Object> rowMap : rows) {
            Object[] row = new Object[columnSize];
            for (int i = 0; i < columnSize; i++) {
                ReportColumn reportColumn = reportColumnList.get(i);

                row[i] = rowMap.get(reportColumn.getName());

                if (CollectionUtils.isNotEmpty(reportColumn.getValueConverterNameList())) {
                    for (String convertName : reportColumn.getValueConverterNameList()) {
                        row[i] = valueConverterMap.get(convertName).convert(reportColumn.getContext(), row[i]);
                    }

                    rowMap.put(reportColumn.getName(), row[i]);
                }

            }
            objectArrayRows.add(row);
        }

        return objectArrayRows;
    }

    private List<MapTableColumn> convert(List<ReportColumn> reportColumnList) {
        List<MapTableColumn> mapTableColumnList = Lists.newArrayListWithExpectedSize(reportColumnList.size());

        for (ReportColumn reportColumn : reportColumnList) {
            if (reportColumn.getHidden()) {
                continue;
            }

            MapTableColumn mapTableColumn = new MapTableColumn(reportColumn.getName(), reportColumn.getLabel());
            if (Objects.nonNull(reportColumn.getColumnWidth())) {
                mapTableColumn.setColumnWidth(reportColumn.getColumnWidth() * 50);
            }

            mapTableColumnList.add(mapTableColumn);
        }

        return mapTableColumnList;
    }

    private void validateNonDeleteSql(String sql) {
        boolean isNullOrDeleteSql = Objects.isNull(sql) || sql.matches("(?i).*delete\\s+from*.");
        if (isNullOrDeleteSql) {
            throw new BizException(ResultUtils.fail("Report sql error!"));
        }
    }
}
