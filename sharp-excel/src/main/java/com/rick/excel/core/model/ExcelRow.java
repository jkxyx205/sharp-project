package com.rick.excel.core.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

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
public class ExcelRow extends BaseExcelCell<Object[]> {

    private XSSFCellStyle[] cellStyles;

    public ExcelRow(int x, int y) {
        super(x, y, null);
    }

    public ExcelRow(int x, int y, Object[] values) {
        super(x, y, values);
    }

    public ExcelRow(int x, int y, float heightInPoints, Object... values) {
        super(x, y, heightInPoints, values);
    }
}
