package com.rick.db.plugin;

import com.rick.db.dto.Grid;
import com.rick.db.dto.PageModel;
import com.rick.db.dto.QueryModel;
import com.rick.db.service.GridService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: Grid表格数据工具类
 * @author: Rick.Xu
 * @date: 11/6/20 9:38 AM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public final class GridUtils {

    private static GridService GRID_SERVICE;

    public void setGridService(GridService gridService) {
        GridUtils.GRID_SERVICE = gridService;
    }

    public static Grid<Map<String, Object>> list(String sql, Map<String, Object> params) {
        return list(sql, params, null, (String) params.get(PageModel.PARAM_SIDX));
    }

    public static Grid<Map<String, Object>> list(String sql, Map<String, Object> params, String countSQL) {
        return list(sql, params, countSQL, (String) params.get(PageModel.PARAM_SIDX));
    }
    /**
     *
     * @param sql
     * @param params
     * @param countSQL 可以单独指定count的SQL，可以为null
     * @param sortableColumns 可以允许排序的列，查询字段必须包含id列，不然mysql排序会有问题。如果为null。所有列允许排序
     * @return
     */
    public static Grid<Map<String, Object>> list(String sql, Map<String, Object> params, String countSQL, String... sortableColumns) {
        QueryModel queryModel = QueryModel.of(params);

        PageModel pageModel = queryModel.getPageModel();
        SQLUtils.setOrderParams(pageModel, sortableColumns);

        return GridUtils.GRID_SERVICE.query(sql, queryModel.getPageModel(), queryModel.getParams(), countSQL);
    }

    /**
     * 结果是数字的单行记录
     * @param sql
     *  合计summary:select sum(score), sum(money) from t_xx
     *  平均值avg:select avg(score), avg(money) from t_xx
     * @param params
     * @return
     */
    public static List<BigDecimal> numericObject(String sql, Map<String, Object> params) {
        return Arrays.asList(GridUtils.GRID_SERVICE.getSharpService().queryForObject(sql, params).get().values().toArray(new BigDecimal[] {}));
    }
}
