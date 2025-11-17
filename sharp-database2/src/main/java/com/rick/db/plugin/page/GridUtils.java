package com.rick.db.plugin.page;

import com.rick.db.repository.support.SQLParamCleaner;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;

import static com.rick.db.repository.support.Constants.ASC;
import static com.rick.db.repository.support.Constants.GROUP_DUMMY_TABLE_NAME;

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
        setOrderParams(pageModel, sortableColumns);

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
        Map<String, Object> formatMap = new HashMap<>();
        sql = SQLParamCleaner.formatSql(sql, params, formatMap);

        return Arrays.asList(GridUtils.GRID_SERVICE.getTableDAO().selectForObject(sql, formatMap).get().values().toArray(new BigDecimal[] {}));
    }


    public static void setOrderParams(PageModel pageModel, String[] sortableColumns) {
        String groupBy = getOrderBy(GROUP_DUMMY_TABLE_NAME, pageModel.getSidx(), ASC.equalsIgnoreCase(pageModel.getSord()), sortableColumns);
        if (StringUtils.isNotBlank(groupBy)) {
            int blank = groupBy.indexOf(" ");
            pageModel.setSidx(groupBy.substring(0, blank));
            pageModel.setSord(groupBy.substring(blank + 1));
        } else {
            pageModel.setSidx(null);
            pageModel.setSord(null);
        }
    }

    public static String getOrderBy(String tablePrefix, String column, Boolean asc, String[] sortableColumns) {
        if (!sortable(column, sortableColumns, true)) {
            return null;
        }

        tablePrefix = StringUtils.isBlank(tablePrefix) ? "" : tablePrefix + ".";
        asc = ObjectUtils.defaultIfNull(asc, false);

        return GRID_SERVICE.getDialect().getOrderBy(tablePrefix, column, asc, sortableColumns);
    }

    /**
     * 是否支持排序
     * @param column
     * @param sortableColumns
     * @param ignoreCase
     * @return
     */
    private static boolean sortable(String column, String[] sortableColumns, boolean ignoreCase) {
        if (StringUtils.isBlank(column) || Objects.isNull(sortableColumns)) {
            return false;
        }

        boolean columnSortable = false;
        for (String sortableColumn : sortableColumns) {
            if (ignoreCase) {
                columnSortable = sortableColumn.equalsIgnoreCase(column);
            } else {
                columnSortable = sortableColumn.equals(column);
            }

            if (columnSortable) {
                break;
            }
        }

        if (!columnSortable) {
            return false;
        }

        return true;
    }
}
