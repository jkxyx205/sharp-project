package com.rick.excel.table;

import com.rick.db.dto.PageModel;
import com.rick.db.service.GridService;
import com.rick.excel.table.model.MapTableColumn;

import java.util.List;
import java.util.Map;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 9:18 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class QueryResultExportTable extends MapExcelTable {

    private GridService gridService;

    private String sql;

    private Map<String, ?> params;

    private PageModel pageModel;

    public QueryResultExportTable(GridService gridService,
                                  String sql,
                                  PageModel pageModel,
                                  Map<String, ?> params,
                                  List<MapTableColumn> tableColumnList) {
        super(tableColumnList, null);
        this.gridService = gridService;
        this.sql = sql;
        this.pageModel = pageModel;
        this.params = params;
    }

    @Override
    protected void writeRows() {
        List<Map<String, Object>> list =  gridService.query(sql, pageModel, params).getRows();
        setRows(list);
        super.writeRows();
    }
}
