package com.rick.excel.plugin;

import com.rick.db.plugin.page.GridService;
import com.rick.db.plugin.page.QueryModel;
import com.rick.excel.table.QueryResultExportTable;
import com.rick.excel.table.model.MapTableColumn;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-03 11:11:00
 */
public final class ExportUtils {

    private static GridService GRID_SERVICE;

    public void setGridService(GridService gridService) {
        ExportUtils.GRID_SERVICE = gridService;
    }

    /**
     * 报表导出
     * @param sql
     * @param params
     * @param outputStream
     * @param columnList 需要显示的列。MapTableColumn#name列的字段名 MapTableColumn#label显示名称
     * @throws IOException
     */
    public static void export(String sql, Map<String, Object> params, OutputStream outputStream, List<MapTableColumn> columnList) throws IOException {
        QueryModel queryModel = QueryModel.of(params);
        queryModel.getPageModel().setSize(-1);

        QueryResultExportTable exportTable = new QueryResultExportTable(ExportUtils.GRID_SERVICE, sql, queryModel.getPageModel(), queryModel.getParams(), columnList);
        exportTable.write(outputStream);
    }
}
