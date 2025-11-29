package com.rick.db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rick.Xu on 2016/03/22.
 */
@Value
@Builder
public class Grid<T> implements Serializable {

    /**
     * 当前页面索引
     */
    private int page;

    /**
     * 一页显示记录条数
     */
    @JsonProperty("pageSize")
    private int pageSize;

    /***
     * 总纪录数
     */
    private int records;

    /***
     * 总页数
     */
    @JsonProperty("totalPages")
    private int totalPages;

    /***
     * 数据项
     */
    private List<T> rows;

    private Map<String, Object> additionalInfo;

    public static <T> Grid<T> emptyInstance(int pageSize) {
        Grid<T> grid = Grid.builder().totalPages(0)
                .page(1)
                .rows(Collections.EMPTY_LIST)
                .pageSize(pageSize)
                .records(0)
                .additionalInfo(new HashMap())
                .build();
        return grid;
    }
}