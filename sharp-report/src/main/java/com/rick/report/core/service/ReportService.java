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
import com.rick.db.plugin.table.DefaultTableGridService;
import com.rick.db.service.GridService;
import com.rick.excel.table.QueryResultExportTable;
import com.rick.excel.table.model.MapTableColumn;
import com.rick.meta.dict.convert.ValueConverter;
import com.rick.report.core.dao.ReportDAO;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.ReportDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/18/20 3:34 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Service
public class ReportService {

    private static final String EXCEL_EXTENSION = ".xlsx";

    @Autowired
    private ReportDAO reportDAO;

    @Autowired
    private GridService gridService;

    @Autowired
    private Map<String, ValueConverter> valueConverterMap;

    /**
     * 创建报表
     */
    public int saveOrUpdate(@Valid Report report) {
        validateNonDeleteSQL(report.getQuerySql());
        return reportDAO.insertOrUpdate(report);
    }

    /**
     * 删除报表
     * @param id
     */
    public int delete(Long id) {
        return reportDAO.deleteById(id);
    }

    public Optional<Report> findById(Long id) {
        return reportDAO.selectById(id);
    }

    public ReportDTO list(long id, Map<String, Object> requestMap) {
        Optional<Report> optional = findById(id);
        if (!optional.isPresent()) {
            throw new BizException(ResultUtils.fail("Report not exists"));
        }

        Report report = optional.get();
        validateNonDeleteSQL(report.getQuerySql());

        String summarySQL = null;
        List<String> summaryColumnNameList = null;
        if (StringUtils.isNotEmpty(report.getSummaryColumnNames())) {
            summaryColumnNameList = Arrays.stream(report.getSummaryColumnNames().split(",")).collect(Collectors.toList());
            summarySQL = "SELECT " + summaryColumnNameList.stream().map(c -> "CONVERT(sum("+c+"), DECIMAL(10,3))").collect(Collectors.joining(", ")) + " FROM "
                    + StringUtils.substringAfter(report.getQuerySql(), "FROM");
        }

        DefaultTableGridService defaultTableGridService = new DefaultTableGridService(report.getQuerySql(), null, summarySQL);
        Grid<Map<String, Object>> grid = defaultTableGridService.list(requestMap);

        Map<String, BigDecimal> summaryMap = null;
        if (StringUtils.isNotEmpty(report.getSummaryColumnNames())) {
            List<BigDecimal> summaryList = defaultTableGridService.summary(requestMap);
            if (grid.getRecords() > 0) {
                summaryMap = Maps.newHashMapWithExpectedSize(summaryColumnNameList.size());
                for (int i = 0; i < summaryColumnNameList.size(); i++) {
                    summaryMap.put(summaryColumnNameList.get(i), summaryList.get(i));
                }
            }
        }

        ReportDTO reportDTO = new ReportDTO(report, convert(grid, report), summaryMap);
        return reportDTO;
    }

    private Grid<Object[]> convert(Grid<Map<String, Object>> paramGrid, Report report) {
        List<ReportColumn> reportColumnList = report.getReportColumnList();

        Grid<Object[]> grid = Grid.<Object[]>builder()
                .rows(toObjectArrayRowsAndTranslate(paramGrid.getRows(), reportColumnList))
                .pageSize(paramGrid.getPageSize())
                .page(paramGrid.getPage())
                .totalPages(paramGrid.getTotalPages())
                .records(paramGrid.getRecords())
                .build();

        return grid;
    }

    private List<Object[]> toObjectArrayRowsAndTranslate(List<Map<String, Object>> rows, List<ReportColumn> reportColumnList) {
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

    public void export(HttpServletRequest request, HttpServletResponse response, long id) throws IOException {
        Optional<Report> optional = findById(id);
        if (!optional.isPresent()) {
            throw new BizException(ResultUtils.fail("Report not exists"));
        }

        Report report = optional.get();
        validateNonDeleteSQL(report.getQuerySql());

        QueryModel queryModel = QueryModel.of(HttpServletRequestUtils.getParameterMap(request));
        PageModel pageModel = queryModel.getPageModel();
        pageModel.setSize(-1);

        LocalDateTime localDateTime = LocalDateTime.now();
        String timestamp = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmSSS"));
        String fileName = report.getName() + timestamp + EXCEL_EXTENSION;

        QueryResultExportTable exportTable = new QueryResultExportTable(gridService, report.getQuerySql(), pageModel, queryModel.getParams(), convert(report.getReportColumnList())) {
            @Override
            public void setRows(List<?> rows) {
                toObjectArrayRowsAndTranslate((List<Map<String, Object>>) rows, report.getReportColumnList());
                super.setRows(rows);
            }
        };

        exportTable.write(HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, fileName));
    }

    private List<MapTableColumn> convert(List<ReportColumn> reportColumnList) {
        List<MapTableColumn> mapTableColumnList = Lists.newArrayListWithExpectedSize(reportColumnList.size());

        for (com.rick.report.core.model.ReportColumn ReportColumn : reportColumnList) {
            MapTableColumn mapTableColumn = new MapTableColumn(ReportColumn.getName(), ReportColumn.getLabel());
            if (Objects.nonNull(ReportColumn.getColumnWidth())) {
                mapTableColumn.setColumnWidth(ReportColumn.getColumnWidth() * 50);
            }

            mapTableColumnList.add(mapTableColumn);
        }

        return mapTableColumnList;
    }

    private void validateNonDeleteSQL(String sql) {
        boolean isNULLorDeleteSQL = Objects.isNull(sql) || sql.matches("(?i).*delete\\s+from*.");
        if (isNULLorDeleteSQL) {
            throw new BizException(ResultUtils.fail("Report sql error!"));
        }
    }
}
