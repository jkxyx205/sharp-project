package com.rick.report.core.service;

import com.google.common.collect.Lists;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.dto.Grid;
import com.rick.db.dto.PageModel;
import com.rick.db.dto.QueryModel;
import com.rick.db.service.GridService;
import com.rick.excel.table.QueryResultExportTable;
import com.rick.excel.table.model.MapTableColumn;
import com.rick.report.core.dao.ReportDAO;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.ReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
        QueryModel queryModel = QueryModel.of(requestMap);
        if (!report.getPageable()) {
            queryModel.getPageModel().setSize(-1);
        }
        Grid<Map<String, Object>> grid = gridService.query(report.getQuerySql(), queryModel.getPageModel(), queryModel.getParams());

        ReportDTO reportDTO = new ReportDTO(report, convert(grid, report));
        return reportDTO;
    }

    private Grid<Object[]> convert(Grid<Map<String, Object>> paramGrid, Report report) {
        List<Object[]> rows = Lists.newArrayListWithExpectedSize(paramGrid.getRows().size());
        List<ReportColumn>  reportColumnList = report.getReportColumnList();
        int columnSize = reportColumnList.size();

        for (Map m : paramGrid.getRows()) {
            Object[] row = new Object[columnSize];
            for (int i = 0; i < columnSize; i++) {
                row[i] = m.get(reportColumnList.get(i).getName());
            }
            rows.add(row);
        }

        Grid<Object[]> grid = Grid.<Object[]>builder()
                .rows(rows)
                .pageSize(paramGrid.getPageSize())
                .page(paramGrid.getPage())
                .totalPages(paramGrid.getTotalPages())
                .records(paramGrid.getRecords())
                .build();

        return grid;
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

        QueryResultExportTable exportTable = new QueryResultExportTable(gridService, report.getQuerySql(), pageModel, queryModel.getParams(), convert(report.getReportColumnList()));
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
