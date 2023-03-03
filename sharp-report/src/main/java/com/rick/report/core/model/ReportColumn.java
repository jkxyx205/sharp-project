package com.rick.report.core.model;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/18/20 3:22 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportColumn implements Serializable, JsonStringToObjectConverterFactory.JsonValue {

    private String name;

    private String label;

    private Boolean sortable = false;

    private Integer columnWidth;

    private AlignEnum align = AlignEnum.LEFT;

    private Boolean tooltip = false;

    public ReportColumn(String name, String label) {
        this(name, label, false);
    }

    public ReportColumn(String name, String label, Boolean sortable) {
        this.name = name;
        this.label = label;
        this.sortable = sortable;
    }

}