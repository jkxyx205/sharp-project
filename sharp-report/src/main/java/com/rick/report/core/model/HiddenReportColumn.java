package com.rick.report.core.model;

import java.util.List;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/18/20 3:22 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class HiddenReportColumn extends ReportColumn {

    public HiddenReportColumn(String name) {
        super(name,null, false, null, null, null,
                AlignEnum.LEFT,  true, false, TypeEnum.TEXT);
    }

    public HiddenReportColumn(String name, String context, List<String> valueConverterNameList) {
        super(name,null, false, context, valueConverterNameList, null,
                AlignEnum.LEFT,  true, false, TypeEnum.TEXT);
    }

}
