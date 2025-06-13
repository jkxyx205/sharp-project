package com.rick.excel.table.model;

import lombok.Getter;

import java.util.function.Function;

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
public class MapTableColumn extends TableColumn {

    private String name;

    private Function converter;

    public MapTableColumn(String name, String label) {
        super(label);
        this.name = name;
    }

    public MapTableColumn(String name, String label, Function converter) {
        super(label);
        this.name = name;
        this.converter = converter;
    }

    public MapTableColumn(String name, String label, int columnWidth) {
        super(label, columnWidth);
        this.name = name;
    }

    public MapTableColumn(String name, String label, int columnWidth, Function converter) {
        super(label, columnWidth);
        this.name = name;
        this.converter = converter;
    }

    public MapTableColumn setAlign(AlignEnum align) {
        super.setAlign(align);
        return this;
    }
}
