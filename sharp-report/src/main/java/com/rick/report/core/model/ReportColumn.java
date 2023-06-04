package com.rick.report.core.model;

import com.fasterxml.jackson.annotation.JsonValue;
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

    private Boolean sortable;

    private Integer columnWidth;

    private Boolean hidden;

    private AlignEnum align;

    private Boolean tooltip;

    private TypeEnum type;

    public ReportColumn(String name, String label) {
        this(name, label, false);
    }

    public ReportColumn(String name, String label, Boolean sortable) {
        this(name, label, sortable, null, null);
    }

    public ReportColumn(String name, String label, Boolean sortable, List<String> valueConverterNameList) {
        this(name, label, sortable, null, valueConverterNameList);
    }

    public ReportColumn(String name, String label, Boolean sortable, String context, List<String> valueConverterNameList) {
        this(name, label, sortable, context, valueConverterNameList, null, AlignEnum.LEFT, false, false, TypeEnum.TEXT);
    }

    public ReportColumn(String name, String label, Boolean sortable, String context, List<String> valueConverterNameList, Integer columnWidth,
                        AlignEnum align,  Boolean hidden, Boolean tooltip, TypeEnum type) {
        this.name = name;
        this.label = label;
        this.context = context;
        this.valueConverterNameList = valueConverterNameList;
        this.sortable = sortable;
        this.align = align;
        this.columnWidth = columnWidth;
        this.hidden = hidden;
        this.tooltip = tooltip;
        this.type = type;
    }

    public ReportColumn setColumnWidth(Integer columnWidth) {
        this.columnWidth = columnWidth;
        return this;
    }

    public ReportColumn setAlign(AlignEnum align) {
        this.align = align;
        return this;
    }

    public ReportColumn setType(TypeEnum type) {
        this.type = type;
        return this;
    }

    public enum TypeEnum {
        TEXT, NUMERIC;

        @JsonValue
        public String jsonValue() {
            return this.name().toLowerCase();
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }
}
