package com.rick.report.core.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.JsonUtils;
import com.rick.db.dto.Grid;
import com.rick.db.dto.PageModel;
import com.rick.db.dto.QueryModel;
import com.rick.db.plugin.GridUtils;
import com.rick.excel.table.AbstractExportTable;
import com.rick.excel.table.MapExcelTable;
import com.rick.excel.table.model.AlignEnum;
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
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
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

    private final Map<String, ValueConverter> valueConverterMap;

    private final Map<String, ReportAdvice> reportAdviceMap;

    public static final Map<Long, Report> reportCacheMap = new HashMap<>();

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
        Report report = reportCacheMap.get(id);

        if (report == null) {
            Optional<Report> optional = reportDAO.selectById(id);
            if (!optional.isPresent()) {
                throw new BizException(ResultUtils.fail("Report not exists"));
            }
            report = optional.get();
            reportCacheMap.put(id, report);

            if (report.getAdditionalInfo() == null) {
                report.setAdditionalInfo(new HashMap<>());
            }
        }

        return Optional.of(JsonUtils.toObject(JsonUtils.toJson(report), Report.class));
    }

    public ReportDTO list(long id, Map<String, Object> requestMap) {
        Report report = findById(id).get();
        init(report);
        mergeParams(report, requestMap);
        if (StringUtils.isBlank(report.getQuerySql())) {
            return fetchDataWithoutSql(report, requestMap);
        }
        // format sql
        report.setQuerySql(report.getQuerySql()
                .replaceFirst("(?i)(select)\\s+", "SELECT ")
                .replaceFirst("(?i)\\s+(from)\\s+", " FROM ")
                .replaceFirst("(?i)\\s+(WHERE)\\s+", " WHERE "));

        ReportAdvice reportAdvice = reportAdviceMap.get(report.getReportAdviceName());
        if (reportAdvice != null) {
            reportAdvice.beforeQuery(report, requestMap);
        }

        String summarySQL = null;
        List<String> summaryColumnNameList = null;
        List<String> summaryQueryColumnNameList;
        if (StringUtils.isNotEmpty(report.getSummaryColumnNames())) {
            // query columns
            List<String> queryColumnNameList = Arrays.stream(StringUtils.substringBetween(report.getQuerySql(), "SELECT ", " FROM").split("\\s*,\\s*")).collect(Collectors.toList());

            summaryColumnNameList = Arrays.stream(report.getSummaryColumnNames().split("\\s*,\\s*")).collect(Collectors.toList());

            summaryQueryColumnNameList = Lists.newArrayListWithExpectedSize(summaryColumnNameList.size());

            for (String columnName : queryColumnNameList) {
                for (String summaryColumnName : summaryColumnNameList) {
                    if (columnName.equalsIgnoreCase(summaryColumnName)) {
                        summaryQueryColumnNameList.add(columnName);
                    } else if (columnName.endsWith(" " + summaryColumnName)) {
                        summaryQueryColumnNameList.add(StringUtils.substringBefore(columnName, " " + summaryColumnName));
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(summaryQueryColumnNameList)) {
                summarySQL = "SELECT " + summaryQueryColumnNameList.stream().map(c -> "CONVERT(sum("+c+"), DECIMAL(20,3))").collect(Collectors.joining(", ")) +
                        report.getQuerySql().substring(report.getQuerySql().indexOf("FROM"));
            }
        }

        Grid<Map<String, Object>> grid = GridUtils.list(report.getQuerySql(), requestMap);
        if (reportAdvice != null && CollectionUtils.isNotEmpty(grid.getRows())) {
            reportAdvice.beforeSetRow(report, grid.getRows(), requestMap);
        }

        Map<String, BigDecimal> summaryMap = null;
        if (StringUtils.isNotEmpty(report.getSummaryColumnNames())) {
            if (grid.getRecords() > 0) {
                List<BigDecimal> summaryList;

                if (StringUtils.isNotBlank(summarySQL)) {
                    summaryList = GridUtils.numericObject(summarySQL, requestMap);
                } else {
                    summaryList = new ArrayList<>();
                }

                if (reportAdvice != null) {
                    reportAdvice.combineSummaryList(report, summaryList, requestMap, StringUtils.substringAfter(report.getQuerySql(), "WHERE "));
                }

                summaryMap = Maps.newLinkedHashMapWithExpectedSize(summaryColumnNameList.size());
                for (int i = 0; i < summaryColumnNameList.size(); i++) {
                    summaryMap.put(summaryColumnNameList.get(i), summaryList.get(i));
                }
            }
        }

        return new ReportDTO(report, convert(grid, report), grid, summaryMap);
    }

    public void init(Report report) {
        ReportAdvice reportAdvice = reportAdviceMap.get(report.getReportAdviceName());
        if (reportAdvice != null) {
            reportAdvice.init(report);
        }
    }

    private ReportDTO fetchDataWithoutSql(Report report, Map<String, Object> requestMap) {
        Map<String, BigDecimal> summaryMap = null;
        if (StringUtils.isNotEmpty(report.getSummaryColumnNames())) {
            summaryMap = new HashMap<>();
        }

        ReportAdvice reportAdvice = reportAdviceMap.get(report.getReportAdviceName());
        if (reportAdvice != null) {
            Grid<Map<String, Object>> grid = reportAdvice.fetchDataWithoutSql(report, requestMap, summaryMap);
            return new ReportDTO(report, convert(grid, report), grid, summaryMap);
        }

        throw new RuntimeException("reportAdvice is needed!");
    }

    private void mergeParams(Report report, Map<String, Object> requestMap) {
        if (requestMap.get(PageModel.PARAM_SIDX) == null) {
            requestMap.put(PageModel.PARAM_SIDX,  report.getSidx());
        }

        if (requestMap.get("sord") == null && report.getSord() != null) {
            requestMap.put("sord", report.getSord().name());
        }

        if (!report.getPageable()) {
            requestMap.put(PageModel.PARAM_SIZE,  -1);
        }
    }

    public void export(HttpServletRequest request, HttpServletResponse response, long id) throws IOException {
        Map<String, Object> requestMap = HttpServletRequestUtils.getParameterMap(request);
        requestMap.put("export", true);
        export(requestMap, report -> {
            LocalDateTime localDateTime = LocalDateTime.now();
            String timestamp = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmSSS"));
            String fileName = report.getName() + timestamp + EXCEL_EXTENSION;
            try {
                return HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, id);
    }

    /**
     * 根据 id 导出到 Excel 中
     * @param requestMap
     * @param osSupplier
     * @param id
     * @throws IOException
     */
    public void export(Map<String, Object> requestMap, Function<Report, OutputStream> osSupplier, Long id) throws IOException {
        Report report = findById(id).get();
        ReportAdvice reportAdvice = reportAdviceMap.get(report.getReportAdviceName());
        MapExcelTable excelTable = null;

        if (reportAdvice != null) {
            QueryModel queryModel = QueryModel.of(requestMap);
            PageModel pageModel = queryModel.getPageModel();
            pageModel.setSize(-1);
            // 自己手动调用 QueryResultExportTable 查询获取结果
            excelTable = reportAdvice.getMapExcelTable(report, queryModel.getParams(), pageModel, null);
        }

        if (excelTable == null) {
            requestMap.put(PageModel.PARAM_SIZE, "-1");
            ReportDTO reportDTO = list(report.getId(), requestMap);
            excelTable = new MapExcelTable(convert(report.getReportColumnList()), reportDTO.getGridMap().getRows());
        }

        Consumer<AbstractExportTable> consumerExcelWriter = null;
        if (reportAdvice != null) {
            consumerExcelWriter = reportAdvice.beforeExportAndReturnBeforeToFileConsumer(report, excelTable, requestMap);
        }
        excelTable.getExcelWriter().getBook().setSheetName(0, report.getName());
        excelTable.write(osSupplier.apply(report), consumerExcelWriter);
    }

    private Grid<Object[]> convert(Grid<Map<String, Object>> grid, Report report) {
        List<ReportColumn> reportColumnList = report.getReportColumnList();

        return Grid.<Object[]>builder()
                .rows(toObjectArrayListAndConvert(grid.getRows(), reportColumnList))
                .pageSize(grid.getPageSize())
                .page(grid.getPage())
                .totalPages(grid.getTotalPages())
                .records(grid.getRecords())
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
                        ValueConverter valueConverter = valueConverterMap.get(convertName);
                        if (valueConverter == null) {
                            throw new BizException(400, "没有找到转换器：" + convertName);
                        }
                        row[i] = valueConverter.convert(reportColumn.getContext(), row[i]);
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
            } else {
                // 根据数据类型推断长度
                if (reportColumn.getType() == ReportColumn.TypeEnum.DATE) {
                    mapTableColumn.setColumnWidth(3000);
                } else if (reportColumn.getType() == ReportColumn.TypeEnum.DATETIME) {
                    mapTableColumn.setColumnWidth(5000);
                }
            }

            if (Objects.nonNull(reportColumn.getAlign())) {
                mapTableColumn.setAlign(AlignEnum.valueOf(reportColumn.getAlign().name()));
            }

            mapTableColumnList.add(mapTableColumn);
        }

        return mapTableColumnList;
    }

    private void validateNonDeleteSql(String sql) {
        if (StringUtils.isNotBlank(sql) && sql.matches("(?i).*delete\\s+from*.")) {
            throw new BizException(ResultUtils.fail("Report sql error!"));
        }
    }

//    private void formatValue(List<Map<String, Object>> rows) {
//        for (Map<String, Object> row : rows) {
//            Set<Map.Entry<String, Object>> kvs = row.entrySet();
//            for (Map.Entry<String, Object> kv : kvs) {
//                if (kv.getValue() == null) {
//                    continue;
//                }
//
//                if (Date.class.isAssignableFrom(kv.getValue().getClass())) {
//                    row.put(kv.getKey(), Time2StringUtils.format((java.sql.Date) kv.getValue()));
//                }
//                if (kv.getValue().getClass() == LocalDate.class) {
//                    row.put(kv.getKey(), Time2StringUtils.format((LocalDate) kv.getValue()));
//                } else if (kv.getValue().getClass() == LocalDateTime.class) {
//                    row.put(kv.getKey(), Time2StringUtils.format((LocalDateTime) kv.getValue()));
//                }
//            }
//        }
//    }

}
