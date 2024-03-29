package com.rick.db.service;


import com.rick.db.dto.Grid;
import com.rick.db.dto.PageModel;
import com.rick.db.formatter.AbstractSqlFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 *
 * @author rick
 * @date 2018/3/16
 */
@Slf4j
public class GridService {

    private static final int DEFAULT_PAGE_SIZE = 15;

    private static final int DEFAULT_PAGE_MAX_SIZE = 1000;

    private final SharpService sharpService;

    public GridService(SharpService sharpService) {
        this.sharpService = sharpService;
    }

    public Grid<Map<String, Object>> query(String sql, PageModel model, Map<String, ?> params) {
        return query(sql, model, params, "");
    }

    public Grid<Map<String, Object>> query(String sql, PageModel model, Map<String, ?> params, String countSQL) {
        return query(sql, model, params, (jdbcTemplate, sql2, args) -> sharpService.toMap(jdbcTemplate, sql2, args), countSQL);
    }

    public <T> Grid<T> query(String sql, PageModel model, Map<String, ?> params, Class<T> clazz) {
        return query(sql, model, params, clazz, null);
    }

    public <T> Grid<T> query(String sql, PageModel model, Map<String, ?> params, Class<T> clazz, String countSQL) {
        return query(sql, model, params, (jdbcTemplate, sql2, args) -> sharpService.toClass(jdbcTemplate, sql2, args, clazz), countSQL);
    }

    public <T> Grid<T> query(String sql, PageModel model, Map<String, ?> params, SharpService.JdbcTemplateCallback<T> jdbcTemplateCallback) {
        return query(sql, model, params, jdbcTemplateCallback, null);
    }

    /**
     *
     * @param sql 查询sql
     * @param countSQL 优化后的countSQL
     * @param model 分页模型
     * @param params 查询参数
     * @param jdbcTemplateCallback 回调函数
     * @param <T>
     * @return
     */
    public <T> Grid<T> query(String sql, PageModel model, Map<String, ?> params, SharpService.JdbcTemplateCallback<T> jdbcTemplateCallback, String countSQL) {
        int records = 0;
        int totalPages = 0;
        AbstractSqlFormatter sqlFormatter = sharpService.getSqlFormatter();

        if (model.isPageQueryModel()) {
            countSQL = StringUtils.isBlank(countSQL) ? sqlFormatter.formatSqlCount(sql) : countSQL;
            records = sharpService.query(countSQL, params, Integer.class).get(0);

            if (records == 0) {
                return Grid.emptyInstance(model.getSize());
            }

            // validate PageModel
            totalPages = validatePageModelAndReturnTotalPages(model, records);
        }

        List<T> rows = sharpService.query(sql, params, (jdbcTemplate, sql2, args) -> {
            //change add 20170223
            if(model.isPageQueryModel()) { // 分页排序
                sql2 = sqlFormatter.pageSql(sql2, model);
            } else { // 显示全部信息 仅仅排序
                sql2 = sqlFormatter.wrapSordString(sql2, model.getSidx(), model.getSord());
            }
            //end
            return jdbcTemplateCallback.query(jdbcTemplate, sql2, args);
        });

        if (model.isAllQueryModel()) {
            records = rows.size();
            totalPages = records > 0 ? 1 : 0;
        }

        Grid<T> grid = Grid.<T>builder()
                .page(model.getPage())
                .totalPages(totalPages)
                .rows(rows)
                .records(records)
                .pageSize(model.getSize())
                .build();
        return grid;
    }

    public SharpService getSharpService() {
        return sharpService;
    }

    /**
     * 调校查询参数，并返回分页数据
     * @param model
     * @param records
     * @return
     */
    private int validatePageModelAndReturnTotalPages(PageModel model, int records) {
        if (model.isPageQueryModel()) {
            if (model.getSize() < 1) {
                model.setSize(DEFAULT_PAGE_SIZE);
            } else if (model.getSize() > DEFAULT_PAGE_MAX_SIZE) {
                model.setSize(DEFAULT_PAGE_MAX_SIZE);
            }

            int totalPages = totalPages(records, model.getSize());

            if (model.getPage() > totalPages) {
                model.setPage(totalPages);
            } else if (model.getPage() < 1) {
                model.setPage(1);
            }

            return totalPages;
        }

        return records > 0 ? 1 : 0;
    }

    /**
     * 计算页数
     * @param records
     * @param pageSize
     * @return
     */
    private int totalPages(int records, int pageSize) {
        int totalPages;

        if (records % pageSize == 0) {
            totalPages = records / pageSize;
        } else {
            totalPages = records / pageSize + 1;
        }

        return totalPages;
    }
}
