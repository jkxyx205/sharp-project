package com.rick.report.core.model;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportColumn implements Serializable, JsonStringToObjectConverterFactory.JsonValue {

    private String name;

    private String label;

    private List<String> valueConverterNameList;

    private String context;

    private Boolean sortable = false;

    private Integer columnWidth;

    private AlignEnum align = AlignEnum.LEFT;

    private Boolean tooltip = false;

    public ReportColumn(String name, String label) {
        this(name, label, false);
    }

    public ReportColumn(String name, String label, Boolean sortable) {
        this(name, label, sortable, null, null);
    }

    public ReportColumn(String name, String label, Boolean sortable, List<String> valueConverterNameList) {
        this.name = name;
        this.label = label;
        this.valueConverterNameList = valueConverterNameList;
        this.sortable = sortable;
    }

    public ReportColumn(String name, String label, Boolean sortable, String context, List<String> valueConverterNameList) {
        this.name = name;
        this.label = label;
        this.context = context;
        this.valueConverterNameList = valueConverterNameList;
        this.sortable = sortable;

    }

}
