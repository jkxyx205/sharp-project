package com.rick.excel.table;

import com.rick.db.repository.JdbcTemplateCallback;
import com.rick.db.repository.TableDAO;
import com.rick.excel.core.model.ExcelRow;
import com.rick.excel.table.model.MapTableColumn;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 不带分页和前端排序，排序写到SQL中
 * @author: Rick.Xu
 * @date: 6/17/20 9:18 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class QueryRollingExportTable extends AbstractExportTable {

    private TableDAO tableDAO;

    private String sql;

    private Map<String, Object> params;

    private List<MapTableColumn>  tableColumnList;

    private int columnSize;

    public QueryRollingExportTable(TableDAO tableDAO,
                                   String sql,
                                   Map<String, Object> params,
                                   List<MapTableColumn> tableColumnList) {
        super(tableColumnList, null);
        this.columnSize = tableColumnList.size();
        this.tableColumnList = tableColumnList;
        this.tableDAO = tableDAO;
        this.sql = sql;
        this.params = params;
    }

    @Override
    public void writeRows() {
        int startY = CollectionUtils.isEmpty(tableColumnList) ? 1 : 2;

        tableDAO.select(sql, params, (JdbcTemplateCallback) (jdbcTemplate, sql, args) -> {
            jdbcTemplate.query(sql, args, rs -> {
                Object[] data = new Object[columnSize];

                for (int i = 0; i < columnSize; i++) {
                    MapTableColumn mapTableColumn = tableColumnList.get(i);
                    data[i] = rs.getObject(mapTableColumn.getName());
                }

                ExcelRow row = new ExcelRow(1, rs.getRow() - 1 + startY, data);
                row.setHeightInPoints(rowHeight);

                if (Objects.nonNull(rowStyle)) {
                    row.setStyle(rowStyle);
                } else {
                    row.setStyle(getDefaultRowStyle());
                }
                excelWriter.writeRow(row);

            });

            return null;
        });
    }

    @Override
    protected Object[] resolve(Object row) {
        return null;
    }
}
