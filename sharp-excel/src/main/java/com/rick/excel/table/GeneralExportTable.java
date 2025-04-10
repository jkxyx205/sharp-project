package com.rick.excel.table;

import com.rick.excel.table.model.TableColumn;

import java.util.List;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 9:18 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class GeneralExportTable extends AbstractExportTable<Object[]> {

    public GeneralExportTable(List<TableColumn> tableColumnList, List<Object[]> rows) {
        super(tableColumnList, rows);
    }

    @Override
    protected Object[] resolve(Object row) {
        return (Object[]) row;
    }
}
