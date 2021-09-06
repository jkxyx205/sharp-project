package com.rick.excel.table.model;

import lombok.Getter;
import lombok.Setter;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 9:17 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Getter
@Setter
public class TableColumn {

    private String label;

    private int columnWidth = 2400;

    public TableColumn(String label) {
        this.label = label;
    }

    public TableColumn(String label, int columnWidth) {
        this.label = label;
        this.columnWidth = columnWidth;
    }

}
