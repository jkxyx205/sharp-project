package com.rick.report.core.model;

import com.rick.db.plugin.page.Grid;
import com.rick.report.core.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/18/20 5:44 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Data
@AllArgsConstructor
public class ReportDTO {

    private Report report;

    private Grid<Object[]> gridArray;

    private Grid<Map<String, Object>> gridMap;

    private Map<String, BigDecimal> summaryMap;
}
