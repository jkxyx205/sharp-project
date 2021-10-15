package com.rick.db.plugin.table;

import com.rick.common.http.HttpServletRequestUtils;
import com.rick.db.dto.Grid;
import com.rick.db.plugin.GridUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-10-15 09:37:00
 */
public abstract class AbstractTableGridService {

    public Grid<Map<String, Object>> list(Map<String, ?> params) {
        return GridUtils.list(getListSQL(), params, getCountSQL());
    }

    public Grid<Map<String, Object>> list(HttpServletRequest request) {
        return list(request, null);
    }

    public Grid<Map<String, Object>> list(HttpServletRequest request, Map<String, ?> extendParams) {
        return GridUtils.list(getListSQL(), HttpServletRequestUtils.getParameterMap(request, extendParams), getCountSQL());
    }

    public List<BigDecimal> summary(Map<String, ?> params) {
        return GridUtils.numericObject(getSummarySQL(), params);
    }

    public List<BigDecimal> summary(HttpServletRequest request) {
        return summary(request, null);
    }

    public List<BigDecimal> summary(HttpServletRequest request, Map<String, ?> extendParams) {
        return GridUtils.numericObject(getSummarySQL(), HttpServletRequestUtils.getParameterMap(request, extendParams));
    }

    /**
     * 列合计SQL
     * @return
     */
    public String getSummarySQL() {
        return null;
    }

    /**
     * 查询SQL
     * @return
     */
    public abstract String getListSQL();

    /**
     * 查询count SQL
     * @return
     */
    public String getCountSQL() {
        return null;
    }
}
