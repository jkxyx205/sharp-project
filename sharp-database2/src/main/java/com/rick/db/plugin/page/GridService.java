package com.rick.db.plugin.page;

import com.rick.db.repository.JdbcTemplateCallback;
import com.rick.db.repository.TableDAO;
import com.rick.db.repository.support.dialect.AbstractDialect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rick.db.repository.support.Constants.ASC;
import static com.rick.db.repository.support.Constants.GROUP_DUMMY_TABLE_NAME;

/**
 *
 * @author rick
 * @date 2018/3/16
 */
@Slf4j
public class GridService {

    private static final int DEFAULT_PAGE_SIZE = 15;

    private static final int DEFAULT_PAGE_MAX_SIZE = 1000;

    private final TableDAO tableDAO;

    @Getter
    private final AbstractDialect dialect;

    public GridService(TableDAO tableDAO, AbstractDialect dialect) {
        this.tableDAO = tableDAO;
        this.dialect = dialect;
    }

    public Grid<Map<String, Object>> query(String sql, PageModel model, Map<String, ?> params) {
        return query(sql, model, params, "");
    }

    public Grid<Map<String, Object>> query(String sql, PageModel model, Map<String, ?> params, String countSQL) {

        return query(sql, model, params, (jdbcTemplate, sql2, args) -> tableDAO.select(sql2, args), countSQL);
    }

    public <T> Grid<T> query(String sql, PageModel model, Map<String, ?> params, Class<T> clazz) {
        return query(sql, model, params, clazz, null);
    }

    public <T> Grid<T> query(String sql, PageModel model, Map<String, ?> params, Class<T> clazz, String countSQL) {
        return query(sql, model, params, (jdbcTemplate, sql2, args) -> tableDAO.select(clazz, sql2, args), countSQL);
    }

    public <T> Grid<T> query(String sql, PageModel model, Map<String, ?> params, JdbcTemplateCallback<T> jdbcTemplateCallback) {
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
    public <T> Grid<T> query(String sql, PageModel model, Map<String, ?> params, JdbcTemplateCallback<T> jdbcTemplateCallback, String countSQL) {
        int records = 0;
        int totalPages = 0;

        if (model.isPageQueryModel()) {
            countSQL = StringUtils.isBlank(countSQL) ? dialect.formatSqlCount(sql) : countSQL;
            records = tableDAO.select(Integer.class, countSQL, params).get(0);

            if (records == 0) {
                return Grid.emptyInstance(model.getSize());
            }

            // validate PageModel
            totalPages = validatePageModelAndReturnTotalPages(model, records);
        }

        List<T> rows = tableDAO.select(sql, params, (jdbcTemplate, sql2, args) -> {
            //change add 20170223
            if(model.isPageQueryModel()) { // 分页排序
                sql2 = dialect.pageSql(sql2, model);
            } else { // 显示全部信息 仅仅排序
                sql2 = dialect.wrapSordString(sql2, model.getSidx(), model.getSord());
            }
            //end
            return jdbcTemplateCallback.select(jdbcTemplate, sql2, args);
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

    public void setOrderParams(PageModel pageModel, String[] sortableColumns) {
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

    public String getOrderBy(String tablePrefix, String column, Boolean asc, String[] sortableColumns) {
        if (!sortable(column, sortableColumns, true)) {
            return null;
        }

        tablePrefix = StringUtils.isBlank(tablePrefix) ? "" : tablePrefix + ".";
        asc = ObjectUtils.defaultIfNull(asc, false);

        return dialect.getOrderBy(tablePrefix, column, asc, sortableColumns);
    }

    public TableDAO getTableDAO() {
        return tableDAO;
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
