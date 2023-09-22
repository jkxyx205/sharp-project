package com.rick.report.core.model;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 报表查询字段
 * @author: Rick.Xu
 * @date: 6/18/20 3:22 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Data
@NoArgsConstructor
public class QueryField implements Serializable, JsonStringToObjectConverterFactory.JsonValue {

    private String name;

    private String label;

    /**
     * type: text, mutile select, date_range
     */
    private Type type;

    /**
     * 处理不同类型的额外数据
     */
    private String extraData;

    private String value;

    private String placeholder;

    public QueryField(String name, String label) {
        this(name, label, Type.TEXT);
    }

    public QueryField(String name, String label, Type type) {
        this(name, label, type, null);
    }

    public QueryField(String name, String label, Type type, String extraData) {
        this.name = name;
        this.label = label;
        this.type= type;
        this.extraData = extraData;
    }

    public enum Type {
        TEXT, SELECT, GROUP_SELECT, MULTIPLE_SELECT, DATE_RANGE;
    }

    public QueryField setValue(String value) {
        this.value = value;
        return this;
    }

    public QueryField setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }
}
