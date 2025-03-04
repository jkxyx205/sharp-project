package com.rick.excel.table;

import com.rick.excel.table.model.MapTableColumn;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 9:18 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class MapExcelTable extends AbstractExportTable {

    public MapExcelTable(List<MapTableColumn> tableColumnList, List<Map<String, Object>> rows) {
        super(tableColumnList, rows);
    }

    @Override
    protected Object[] resolve(Object row) {
        Map<String, Object> map = (Map<String, Object>) row;
        return toObjectArray(map);
    }

    private Object[] toObjectArray(Map<String, Object> map) {
        int columnSize = tableColumnList.size();
        Object[] row = new Object[columnSize];

        for (int i = 0; i < columnSize; i++) {
            MapTableColumn mapTableColumn = (MapTableColumn) tableColumnList.get(i);
            Object value = map.get(mapTableColumn.getName());
            if (Objects.isNull(mapTableColumn.getConverter())) {
                row[i] = value;
            } else  {
                row[i] = mapTableColumn.getConverter().apply(value);
            }
        }
        return row;
    }
}
