package com.rick.report.core.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/18/20 9:40 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public enum AlignEnum {
    LEFT, CENTER, RIGHT;

    @JsonValue
    public String jsonValue() {
        return this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}