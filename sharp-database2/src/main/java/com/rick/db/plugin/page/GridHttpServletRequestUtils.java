package com.rick.db.plugin.page;

import com.rick.common.http.HttpServletRequestUtils;
import lombok.experimental.UtilityClass;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-05 12:00:00
 */
@UtilityClass
public final class GridHttpServletRequestUtils {

    public static Grid<Map<String, Object>> list(String sql, HttpServletRequest request) {
        return list(sql, request, null, null);
    }

    public static Grid<Map<String, Object>> list(String sql, HttpServletRequest request, String countSQL) {
        return list(sql, request, null, countSQL);
    }

    public static Grid<Map<String, Object>> list(String sql, HttpServletRequest request, Map<String, Object> extendParams, String countSQL) {
        Map<String, Object> parameterMap = HttpServletRequestUtils.getParameterMap(request, extendParams);
        return GridUtils.list(sql, parameterMap, countSQL, (String) parameterMap.get(PageModel.PARAM_SIDX));
    }

    public static List<BigDecimal> numericObject(String sql, HttpServletRequest request) {
        return numericObject(sql, request, null);
    }

    public static List<BigDecimal> numericObject(String sql, HttpServletRequest request, Map<String, Object> extendParams) {
        return GridUtils.numericObject(sql, HttpServletRequestUtils.getParameterMap(request, extendParams));
    }
}
