package com.rick.excel.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 6:46 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Getter
@Setter
public class ExcelCell extends BaseExcelCell<Object> {

    private int rowSpan = 1;

    private int colSpan = 1;

    public ExcelCell(int x, int y, Object value) {
        super(x, y, value);
    }
}
